rem $Id: clean.cmd 3962 2008-05-23 17:05:08Z rgw_ch $
rem if not *%JAVA_HOME% == * goto varsok
set JAVA_HOME=%dev%\jdk150

:varsok
cd rsc\build
call ant clean
cd ..\..
