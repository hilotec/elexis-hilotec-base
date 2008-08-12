rem $Id: buildall.cmd 4263 2008-08-12 20:46:29Z rgw_ch $
rem if not *%JAVA_HOME% == * goto varsok
set JAVA_HOME=%DEV%\jdk150


:varsok
cd rsc\build
if not *%1==*plugged goto unplugged
call ant windows
call ant linux
goto finish

:unplugged
call ant windows -Dunplugged=true
call ant linux -Dunplugged=true
:finish
cd ..\..
