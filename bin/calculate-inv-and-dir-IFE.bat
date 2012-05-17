@echo off
set DIREC=%1
echo Working for %DIREC%

set ALLFOLDER=D:\PFE\remote\code\gui_experiments\experiments\inplanetlab2\results

java -jar dist\IFE.jar -mf %ALLFOLDER%\%DIREC%-output-headed.txt > moutput-dir.logger
mkdir %DIREC%-dir
move *.logger %DIREC%-dir

rem java -jar dist\IFE.jar -i %ALLFOLDER% %DIREC% > moutput-inv.logger
rem mkdir %DIREC%-inv
rem move *.logger %DIREC%-inv

echo Done for %DIREC%
