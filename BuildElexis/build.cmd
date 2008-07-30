rem $Id: build.cmd 4206 2008-07-30 15:37:11Z rgw_ch $
rem if not *%JAVA_HOME% == * goto varsok
set JAVA_HOME=%DEV%\jdk150


:varsok
cd rsc\build
if not *%1==*plugged goto unplugged
call ant windows
goto finish

:unplugged
call ant windows -Dunplugged=true

:finish
cd ..\..
