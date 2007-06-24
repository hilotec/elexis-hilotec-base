rem $Id: clean.cmd 33 2006-03-24 16:53:54Z rgw_ch $
if not *%JAVA_HOME% == * goto varsok
set JAVA_HOME=e:\dev\jdk150

:varsok
cd rsc\build
call ant clean
cd ..\..
