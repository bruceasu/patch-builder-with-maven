@echo off
TITLE install system scope lib to local repo
SetLocal
set APP_HOME=%~dp0..
set LIB_DIR=%APP_HOME%\lib
::: set PRG=%APP_HOME%\tools\install-to-local-repo.py
::: python %PRG% -a %LIB_DIR%\antlr-2.7.4.jar  -c  org.antlr:antlr:2.7.4:system
::: python %PRG% -a %LIB_DIR%\chardet-1.0.jar  -c  chardet:chardet:1.0:system
::: python %PRG% -a %LIB_DIR%\cpdetector_1.0.10.jar  -c  cpdetector:cpdetector:1.0.10:system

set PRG=%APP_HOME%\tools\install-to-local-repo\install-to-local-repo.exe
%PRG% -a %LIB_DIR%\antlr-2.7.4.jar  -c  org.antlr:antlr:2.7.4:system
%PRG% -a %LIB_DIR%\chardet-1.0.jar  -c  chardet:chardet:1.0:system
%PRG% -a %LIB_DIR%\cpdetector_1.0.10.jar  -c  cpdetector:cpdetector:1.0.10:system
EndLocal

pause