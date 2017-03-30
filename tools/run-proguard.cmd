@echo off
TITLE proguard
SetLocal
set APP_HOME=%~dp0
cd %APP_HOME%
proguard\bin\proguard.bat @proguard.txt
EndLocal

pause