rem $Id: clean.cmd 5000 2009-01-22 14:26:47Z rgw_ch $
rem if not *%JAVA_HOME% == * goto varsok
rem set JAVA_HOME=%dev%\jdk150

:varsok
cd rsc\build
call ant clean
cd ..\..
