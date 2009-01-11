rem $Id: build.cmd 4929 2009-01-11 14:46:50Z rgw_ch $
rem if not *%JAVA_HOME% == * goto varsok
rem set JAVA_HOME=%DEV%\jdk150


:varsok
cd rsc\build
if not *%1==*plugged goto unplugged
call ant windows
goto finish

:unplugged
call ant windows -Dunplugged=true

:finish
cd ..\..
