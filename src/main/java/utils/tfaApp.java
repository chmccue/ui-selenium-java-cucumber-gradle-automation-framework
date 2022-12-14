package utils;

import org.jboss.aerogear.security.otp.Totp;

public class tfaApp {

  // gets sha-1 6 digit only
  public static String getTfaAppCode(String otpKey) {
    return new Totp(otpKey).now();
  }
}
