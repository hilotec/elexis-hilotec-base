rem $Id: build.cmd 675 2006-08-06 10:30:18Z rgw_ch $
rem if not *%JAVA_HOME% == * goto varsok
set JAVA_HOME=%dev%\jdk150

:varsok
cd rsc\build
if not *%1==*plugged goto unplugged
call ant pro
goto finish

:unplugged
call ant pro -Dunplugged=true

:finish
cd ..\..
