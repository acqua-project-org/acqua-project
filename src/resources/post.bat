@echo off
echo RUNNING POST-JAR SCRIPT

rem mkdir ..\..\..\test\lib
rem copy ..\..\dist\lib\*.* ..\..\..\test\lib
copy ..\..\dist\IFE.jar ..\..\..\test\
copy ..\..\conf*.xml ..\..\..\test\
copy ..\..\dist\lib\*.* ..\..\..\test\lib\

