rem $Id: build.cmd 675 2006-08-06 10:30:18Z rgw_ch $
if not *%JAVA_HOME% == * goto varsok
set JAVA_HOME=e:\dev\jdk150

:varsok
cd rsc\build
if not *%1==*plugged goto unplugged
call ant options
goto finish

:unplugged
call ant options -Dunplugged=true

:finish
cd ..\..
