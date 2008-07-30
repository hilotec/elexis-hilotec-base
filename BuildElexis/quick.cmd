rem $Id: quick.cmd 4206 2008-07-30 15:37:11Z rgw_ch $
rem if not *%JAVA_HOME% == * goto varsok
set JAVA_HOME=%dev%\jdk150

:varsok
cd rsc\build
if not *%1==*plugged goto unplugged
call ant pro -Dskip_texify
goto finish

:unplugged
call ant pro -Dunplugged=true -Dskip_texify=1

:finish
cd ..\..
