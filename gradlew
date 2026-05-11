#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
# Gradle Wrapper Script

##############################################################################
# Configuration
##############################################################################
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

##############################################################################
# Functions
##############################################################################
die() {
    echo "$*"
    exit 1
}

##############################################################################
# Setup
##############################################################################
CDPATH=""
DIRNAME=$(dirname "$0")
APP_HOME=$(cd "$DIRNAME" > /dev/null 2>&1 && pwd -P) || die "APP_HOME not found"

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
    [ -f "$JAVACMD" ] || die "JAVA_HOME ($JAVA_HOME) doesn't contain java executable."
else
    JAVACMD=$(which java 2>/dev/null) || die "JAVA_HOME not set and java not found in PATH."
fi

##############################################################################
# Main
##############################################################################
exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
