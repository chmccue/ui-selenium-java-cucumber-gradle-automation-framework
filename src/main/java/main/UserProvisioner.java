package main;

import com.bettercloud.vault.VaultException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.google.gson.Gson;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class UserProvisioner {

  private static ConsulClient consul;
  private static String environment;
  private final static String UP_USERS_BASE_PATH = "user-provisioning/";

  private static LinkedList<UserItem> users;
  private static boolean retryAcquireLock = false;
  private static final String PENDING_RELEASE_USERS_FILE = "./build/pending_release_users.json";
  private static Map<String, User> pendingReleaseUsers = new HashMap<>();

  // Mapping of UP parameters to its string value
  private static final Map<String, UP> mapStringUP = new HashMap<>();

  // user object returned to grab a user from the vault
  public static class User {
    public String username;
    public String email;
    public String password;
    public String tfa_method;
    public String tfa_code;

    // Hashicorp data
    public String id;
    String userLock;
    String consulSessionID;
  }

  private static class UserItem {
    public User user;
    HashMap<String, String> config;

    UserItem(User user, HashMap<String, String> config) {
      this.user = user;
      this.config = config;
    }
  }

  // User Parameters Enumerator
  public enum UP {
    COUNTRY,
    TIER,
    TFA,
    FUNDS,
    DOC_ID,
    DOC_RES,
    DOC_FUNDS,
    TFA_METHOD,
    EMAIL,
    OTHER;

    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }

    public static UP getEnumFromString(String value) {
      if (mapStringUP.size() == 0) {
        for (UP up : UP.values()) {
          mapStringUP.put(up.toString(), up);
        }
      }

      if (mapStringUP.containsKey(value)) {
        return mapStringUP.get(value);
      } else {
        throw new RuntimeException("Cannot get the UP Enum instance for: " + value);
      }
    }

    public String getDescription() {
      if (this == COUNTRY) {
        return "Country";
      } else if (this == TIER) {
        return "Tier";
      } else if (this == TFA) {
        return "Two Factor Authentication";
      } else if (this == TFA_METHOD) {
        return "Two Factor Authentication method type (password, app)";
      } else if (this == FUNDS) {
        return "Funds";
      } else if (this == DOC_ID) {
        return "Document ID";
      } else if (this == DOC_RES) {
        return "Document Residence";
      } else if (this == DOC_FUNDS) {
        return "Document Source of Funds";
      } else if (this == EMAIL) {
        return "Email used by the account. Can be null if email is not known or needed.";
      } else if (this == OTHER) {
        return "Other can be used to identify accounts that do not fall under a predefined "
            + "parameter. For example: other='<thisIsSomeUniqueString>'";
      } else {
        throw new RuntimeException("Cannot get the description of USER_PARAM: " + super.toString());
      }
    }
  }

  UserProvisioner(Map<String, String> meta) throws VaultException{
    // save the current environment to use as default
    UserProvisioner.environment = meta.get("user_provisioning.env");
    if (UserProvisioner.environment == null || UserProvisioner.environment.equals("")) {
      throw new VaultException("The environment variable USER_PROVISIONING_ENVIRONMENT is not valid: " + UserProvisioner.environment);
    }

    String shouldRetryLock = meta.get("user_provisioning.retrylock");
    UserProvisioner.retryAcquireLock =
        shouldRetryLock != null && shouldRetryLock.equalsIgnoreCase("true");

    // retrieve the users for the given environment from consul
    initConsul(meta);
    users = getUsersFromConsul();

    // initialize the pending users file
    savePendingReleaseUsers();
  }

  /**
   * This static method releases a user once it is no longer needed for a test.
   * In case you forgot to release a given User or a fails occurs while you are developing,
   * every non-released User will be persisted in the file located at PENDING_RELEASE_USERS_FILENAME.
   * Them the command 'gradlew releasePendingConsulUsers' can be run to release any pending Users.
   * Locked Users auto-release after a set time (can be adjusted in this class) so no action is usually required.
   */
  public static void releaseUser(User user) {
    if (user == null || user.consulSessionID == null) {
      return;
    }
    releaseConsulLock(user.consulSessionID, user.userLock);
    destroyConsulSession(user.consulSessionID);
    user.consulSessionID = null;

    // remove from the pending list of users to release
    pendingReleaseUsers.remove(user.id);
    savePendingReleaseUsers();
  }

  /**
   * This static method allows you to request a given user with specific parameter values.
   * The users are queried and if there is at least one user matching all the requested parameter values
   * and it is not locked, that user will be returned.
   *
   * @return UserProvisioner.User
   */
  public static User getUser(UP[] params, String[] values) {
    return grabUser(params, values);
  }

  /**
   * This is just an overloaded getUser method that allow you to request the User
   * using string properties instead of having to pass the enumerator instances.
   *
   * @return UserProvisioner.User
   */
  public static User getUser(Map<String, String> enteredParams) {
    HashMap<String, String> params = new HashMap<>(enteredParams);
    // if "other" is not entered, we force it to null, so we don't pull an account with a set value
    params.putIfAbsent("other", "null");
    UP[] upParams = new UP[params.size()];
    int index = 0;
    List<String> keys = new ArrayList<>();
    String[] values = params.values().toArray(new String[0]);
    for (Map.Entry<String, String> param: params.entrySet()) {
      keys.add(param.getKey().replace("-", "_"));
      upParams[index] = UP.getEnumFromString(keys.get(index));
      index += 1;
    }
    return grabUser(upParams, values);
  }

  private static String createConsulSession(User user) {
    NewSession newSession = new NewSession();
    // the name should reference to the UAT tests
    newSession.setName(user.username + " | " + environment);
    // session expiration time
    newSession.setTtl("30m");
    // seconds that the consul server will wait, after a session is invalidated, to release the session's locks
    newSession.setLockDelay(10);
    // do not use any checks on the session besides expiration by TTL
    newSession.setChecks(new LinkedList<>());

    Log.debug("Creating a new consul session");
    String consulSessionID = consul.sessionCreate(newSession, null).getValue();
    Log.debug("Created consul session " + consulSessionID);

    return consulSessionID;
  }

  private static void destroyConsulSession(String consulSessionID) {
    if (consulSessionID == null)
      return;

    Log.debug("Destroying consul session " + consulSessionID);
    consul.sessionDestroy(consulSessionID, null);
    Log.debug("Destroyed consul session " + consulSessionID);
  }

  private static boolean acquireConsulLock(String consulSessionID, String consulLockFile) {
    PutParams lockAcquireParams = new PutParams();
    lockAcquireParams.setAcquireSession(consulSessionID);

    Log.debug("Acquiring consul lock " + consulLockFile + " with session " + consulSessionID);
    Boolean lockAcquired = consul.setKVValue(consulLockFile, "", lockAcquireParams).getValue();

    if (!lockAcquired) {
      // check if we should retry to acquire the lock again after 30 seconds
      if (UserProvisioner.retryAcquireLock) {
        Log.info("Retrying in 30 seconds to acquire the consul lock: " + consulLockFile);
        try {Thread.sleep(30000);} catch (InterruptedException ie) {ie.printStackTrace();}
        lockAcquired = consul.setKVValue(consulLockFile, "", lockAcquireParams).getValue();
      }
    }

    if (!lockAcquired) {
      Log.error("Unable to acquire the consul lock: " + consulLockFile);
      return false;
    }

    Log.info("Acquired consul lock: " + consulLockFile);
    return true;
  }

  private static void releaseConsulLock(String consulSessionID, String consuLockFile) {
    PutParams lockReleaseParams = new PutParams();
    lockReleaseParams.setReleaseSession(consulSessionID);

    Log.info("Releasing consul lock " + consuLockFile + " with session " + consulSessionID);
    Boolean lockReleased = consul.setKVValue(consuLockFile, "", lockReleaseParams).getValue();

    if (!lockReleased) {
      Log.error("Unable to release the consul lock: " + consuLockFile);
      throw new RuntimeException("Unable to release the consul lock: " + consuLockFile);
    }

    Log.info("Released consul lock: " + consuLockFile);
  }

  private static LinkedList<UserItem> getUsersFromConsul() {
    // compute the users file path
    String usersPath = UP_USERS_BASE_PATH + UserProvisioner.environment + ".json";

    // grab the user data from Consul
    Log.info("Fetching users from consul: " + usersPath);
    Response<GetValue> response = consul.getKVValue(usersPath);

    if (response.getValue() == null) {
      Log.error("Unable to fetch users from consul: " + usersPath);
      throw new RuntimeException("Unable to fetch users from consul: " + usersPath);
    }

    // deserialize the response into the user object
    Type listType = new TypeToken<LinkedList<UserItem>>() {}.getType();
    LinkedList<UserItem> users = new Gson().fromJson(response.getValue().getDecodedValue(), listType);

    Log.info("Fetched " + users.size() + " users from consul.");
    Collections.shuffle(users);
    return users;
  }

  private static User grabUser(UP[] params, String[] values) {
    User user = null;

    if (params.length != values.length)
      throw new RuntimeException("The params and values of the user must have the same length.");

    // session that will be used to grab the consul lock for a given user
    String consulSessionID;

    // iterate over all the users that match the request and return the first one we can grab the lock
    for (UserItem ui : UserProvisioner.users) {
      boolean matched = true;

      for (int i=0; i<params.length; i++) {
        if (ui.config.containsKey(params[i].toString())) {
          String uiValue = ui.config.get(params[i].toString());

          // reject null value requests and only compare non-null values
          if (uiValue == null || (!uiValue.equalsIgnoreCase(values[i]))) {
            matched = false;
            break;
          }
        } else {
          matched = false;
          break;
        }
      }

      if (matched) {
        user = ui.user;

        // only grab the user lock if we do not have the lock already
        if (pendingReleaseUsers.containsKey(user.id)) {
          Log.info("Returning the already acquired and locked user: " + user.id);

          // stop looking for an available user
          break;
        } else {
          // make sure the user has an ID to grab the lock
          if (user.id == null)
            throw new RuntimeException("The user ID is not valid to grab the lock.");

          // compute the path for the user lock file
          String userLockFilePath = UserProvisioner.UP_USERS_BASE_PATH
              + UserProvisioner.environment + "/" + user.id + ".lock";

          // create the consul session only the first time
          consulSessionID = createConsulSession(user);

          // try to acquire the lock for the user
          boolean lockAcquired = acquireConsulLock(consulSessionID, userLockFilePath);

          if (lockAcquired) {
            // save the consul lock and vault path for future reference
            user.consulSessionID = consulSessionID;
            user.userLock = userLockFilePath;

            // save the list of pending release users to the file
            pendingReleaseUsers.put(user.id, user);
            savePendingReleaseUsers();

            // stop looking for an available user
            break;
          } else {
            // destroy the consul session so it does not show up as a Lock Session
            destroyConsulSession(consulSessionID);

            // no user was grabbed
            user = null;
          }
        }
      }
    }

    if (user == null) {
      StringBuilder paramValues = new StringBuilder("/");
      for(int i=0; i<params.length; i++) {
        paramValues.append(params[i].toString());
        paramValues.append("=");
        paramValues.append(values[i]);
        paramValues.append(" ");
      }

      Log.error("Unable to find an available user with the parameters requested: " + paramValues.toString());
      throw new RuntimeException("Unable to find an available user with the parameters requested: " + paramValues.toString());
    }

    // return the user only if the consul lock was acquired
    return user;
  }

  private static void initConsul(Map<String, String> meta) throws RuntimeException{
    // validate that the consul server url is valid and initialize the consul client
    String consulUrl = meta.get("consul.url");
    try {
      new URL(consulUrl);
      consul = new ConsulClient(consulUrl, 443);
      Log.info("Consul Server URL: " + consulUrl);
    } catch (MalformedURLException mue) {
      throw new RuntimeException("The environment variable CONSUL_SERVER_URL is not a valid Consul Server URL: " + consulUrl);
    }
  }

  private static void savePendingReleaseUsers() {
    try (Writer writer = new FileWriter(PENDING_RELEASE_USERS_FILE)) {
      Gson gson = new GsonBuilder().create();
      gson.toJson(pendingReleaseUsers, writer);
    } catch (IOException ioex) {
      ioex.printStackTrace();
      throw new RuntimeException("Unable to create the " + PENDING_RELEASE_USERS_FILE + " file.");
    }
  }

  /**
   * This static method will read the PENDING_RELEASE_USERS_FILENAME file, iterate over each of them
   * and try to release the consul lock of each User using the stored consul session and lock path properties.
   * Environment variables required:
   *  - CONSUL_SERVER_URL
   *  - USER_PROVISIONING_ENVIRONMENT
   */
  private static void releasePendingConsulUsers(String consulUrl, String environment) {
    Log.info("Starting the release of pending users task.");

    if (!new File(PENDING_RELEASE_USERS_FILE).isFile()) {
      Log.warn("The file " + PENDING_RELEASE_USERS_FILE + " does not exist.");
      return;
    }

    // parse the file with the users
    HashMap<String, User> userMap;
    try {
      Gson gson = new Gson();
      JsonReader reader = new JsonReader(new FileReader(PENDING_RELEASE_USERS_FILE));
      Type listType = new TypeToken<HashMap<String, User>>() {}.getType();
      userMap = gson.fromJson(reader, listType);
    } catch (IOException ioex) {
      ioex.printStackTrace();
      throw new RuntimeException("Unable to read and parse the " + PENDING_RELEASE_USERS_FILE + " file.");
    }

    UserProvisioner.environment = environment;
    if (environment == null || environment.equals("")) {
      throw new RuntimeException("The environment variable USER_PROVISIONING_ENVIRONMENT is not valid: " + environment);
    }

    if (userMap.size() == 0) {
      Log.info("There are no user pending to be released.");
      return;
    }

    // initialize consul
    Map<String, String> meta = new HashMap<>();
    meta.put("consul.url", consulUrl);
    initConsul(meta);

    // save here the ones that failed to be released
    Map<String, User> failedReleaseUsers = new HashMap<>();

    // iterate over all the pending to release users and release them
    User user;
    for(Map.Entry<String, User> entry : userMap.entrySet()) {
      user = entry.getValue();
      try {
        UserProvisioner.releaseUser(user);
      } catch (RuntimeException re) {
        failedReleaseUsers.put(user.id, user);
      }
    }

    // save to the file the failed ones
    pendingReleaseUsers = failedReleaseUsers;
    savePendingReleaseUsers();

    Log.info("The task releasePendingConsulUsers has finished.");
  }

  /**
   * This static method allows you to parse a CSV file containing the list of users for a given environment and then
   * upload them to the user provisioning server in a file located at: CONSUL_UP_USERS_BASEPATH/environment.json
   * The uploaded file contains the JSON format expected by the UserProvisioner class.
   * Environment variables required:
   *  - USER_PROVISIONING_SERVER_URL
   *  - USER_PROVISIONING_ENVIRONMENT
   *
   */
  private static void uploadUsersToConsul(String consulUrl, String environment, String sourceFilePath) {
    Log.info("Starting the upload of users to consul task.");

    if (sourceFilePath == null || (!new File(sourceFilePath).isFile())) {
      Log.error("The file " + sourceFilePath + " does not exist. Please check.");
      return;
    }

    for (UP up: UP.values()) {
      System.out.println(up.toString());
    }

    int usersCount = 0;

    // store all the UserItems to upload as a file later
    LinkedList<UserItem> usersToUpload = new LinkedList<>();

    try (
        Reader reader = Files.newBufferedReader(Paths.get(sourceFilePath));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim())
    ) {
      for (CSVRecord csvRecord : csvParser) {
        usersCount++;

        // set the user details
        User user = new UserProvisioner.User();
        user.id = Integer.toString(usersCount);

        Field userField;
        for (String headerName : new String[] {
            "username",
            "email",
            "password",
            "tfa_method",
            "tfa_code",
        }) {
          // use the value of the CSV or null if missing
          String value;
          try {
            value = csvRecord.get(headerName);

            // handle a specific case for constant 'null' string
            if (value.equalsIgnoreCase("null")) {
              value = null;
            }
          } catch (IllegalArgumentException iargex) {
            value = null;
          }

          // set the value to the current user instance
          try {
            userField = user.getClass().getDeclaredField(headerName);
            userField.setAccessible(true);
            userField.set(user, value);
          } catch (NoSuchFieldException nsfex) {
            throw new RuntimeException("The UserProvisioner.User class does not have the member: " + headerName);
          } catch (IllegalAccessException iaccessex) {
            throw new RuntimeException("Error when modifiyng the UserProvisioner.User class member: " + headerName);
          }
        }

        // set the configuration for the User
        HashMap<String, String> userConfig = new HashMap<>();
        for (UP up : UP.values()) {
          String headerName = up.toString();
          try {
            String value = csvRecord.get(headerName);
            userConfig.put(headerName, value);
          } catch (IllegalArgumentException iaex) {
            // skipping this configuration as it is not available on the CSV
          }
        }

        // create the UserItem that will be added to the file
        usersToUpload.add(new UserItem(user, userConfig));
      }
    } catch (IOException ioex) {
      ioex.printStackTrace();
      throw new RuntimeException("IO Error when parsing the CSV users file: " + sourceFilePath);
    }

    UserProvisioner.environment = environment;
    if (environment == null || environment.equals("")) {
      throw new RuntimeException("The environment variable USER_PROVISIONING_ENVIRONMENT is not valid: " + environment);
    }

    if (usersToUpload.size() == 0) {
      Log.info("There are no user to be uploaded.");
      return;
    }

    // initialize consul
    Map<String, String> meta = new HashMap<>();
    meta.put("consul.url", consulUrl);
    initConsul(meta);

    // serialize the users to a json string
    Type listType = new TypeToken<LinkedList<UserItem>>() {}.getType();
    GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
    Gson gson = gsonBuilder.create();
    String jsonSerializedUsers = gson.toJson(usersToUpload, listType);

    // upload the users to consul
    String consulUsersPath = UP_USERS_BASE_PATH + UserProvisioner.environment + ".json";
    consul.setKVValue(consulUsersPath, jsonSerializedUsers);
    Log.info("Successfully uploaded " + usersCount + " users to Consul: " + consulUsersPath);

    Log.info("The task uploadUsersToConsul has finished.");
  }

  public static void main(String[] args) {
    Map<String, String> env = System.getenv();
    if (args.length > 0 && args[0].equalsIgnoreCase("release_pending_consul_users")) {
      releasePendingConsulUsers(env.get("CONSUL_SERVER_URL"), env.get("USER_PROVISIONING_ENVIRONMENT"));
    } else if (args.length > 0 && args[0].equalsIgnoreCase("upload_users_to_consul")) {
      uploadUsersToConsul(env.get("CONSUL_SERVER_URL"), env.get("USER_PROVISIONING_ENVIRONMENT"), env.get("UPLOAD_USERS_FILEPATH"));
    } else {
      Log.error("No UserProvisioner task executed.");
    }
  }
}
