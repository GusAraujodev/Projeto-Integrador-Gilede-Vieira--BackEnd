@echo off
@REM Maven Wrapper script for Windows
@REM Generated for this project to run the local Maven distribution
setlocal
set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR%"=="" set MAVEN_PROJECTBASEDIR=.
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%
set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties
if not exist "%WRAPPER_JAR%" (
  echo Cannot find %WRAPPER_JAR%
  exit /b 1
)
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto execute
set JAVA_EXE=java.exe
%JAVA_EXE% -version >nul 2>&1
if errorlevel 1 (
  echo JAVA_HOME is not set and no 'java' command could be found in your PATH.
  exit /b 1
)
:execute
"%JAVA_EXE%" %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
