@echo off
set "GRADLE_USER_HOME=%~dp0..\.gradle-user-home"
set "TEMP=%~dp0..\.tmp"
set "TMP=%TEMP%"

if not exist "%GRADLE_USER_HOME%" mkdir "%GRADLE_USER_HOME%"
if not exist "%TEMP%" mkdir "%TEMP%"

call "%~dp0..\gradlew.bat" %*
exit /b %ERRORLEVEL%
