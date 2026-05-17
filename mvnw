#!/usr/bin/env sh
# Maven Wrapper script for Unix-like shells
# Generated for this project to run the local Maven distribution
BASEDIR=$(cd "$(dirname "$0")" && pwd -P)
JAVA_CMD="${JAVA_HOME:+$JAVA_HOME/bin/java}"
if [ ! -x "$JAVA_CMD" ]; then
  JAVA_CMD=java
fi
exec "$JAVA_CMD" $MAVEN_OPTS $MAVEN_DEBUG_OPTS -classpath "$BASEDIR/.mvn/wrapper/maven-wrapper.jar" "-Dmaven.multiModuleProjectDirectory=$BASEDIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
