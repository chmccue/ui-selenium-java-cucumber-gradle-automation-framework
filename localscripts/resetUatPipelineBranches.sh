#!/bin/bash

##########################################################################################
# This script updates gitlab pipeline branches that run custom jobs.
# It allows us to easily delete/rebuild them with latest updates from a base branch.
# The base branch is usually "master", or "dev/<dev-name>", but can be any branch.
# Prerequisites:
# * Make sure local working directory is clean/free of updates, as script checks out base branch
# * In local git config, make sure "origin" is set to this remote repository.
##########################################################################################

if [ -z "${baseBranch}" ]; then
  baseBranch="master";
fi;
branchStart="uat-"

echo "Using $baseBranch as the base branch."
git checkout $baseBranch
for branchEnd in 01 02 03 04 05
  do
    branchUpdate="$branchStart$branchEnd"
    echo "Updating $branchUpdate..."
    git branch -D $branchUpdate
    git push origin --delete $branchUpdate
    wait
    git branch $branchUpdate &
    wait
    echo "Pushing $branchUpdate and skipping pipeline auto-run." &
    git push origin $branchUpdate -o ci.skip
    wait
    git branch -D $branchUpdate
  done
exit 0
