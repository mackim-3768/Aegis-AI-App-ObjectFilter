#!/usr/bin/env sh

APP_HOME=$(cd "$(dirname "$0")" && pwd -P)

JAVA_EXEC="java"
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_EXEC="$JAVA_HOME/bin/java"
fi

exec "$JAVA_EXEC" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
