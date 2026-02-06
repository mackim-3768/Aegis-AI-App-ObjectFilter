@echo off
set APP_HOME=%~dp0
set JAVA_EXEC=java
if not "%JAVA_HOME%"=="" (
  if exist "%JAVA_HOME%\bin\java.exe" set JAVA_EXEC="%JAVA_HOME%\bin\java.exe"
)
%JAVA_EXEC% -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
