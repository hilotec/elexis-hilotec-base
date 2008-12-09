
rem $Id: jdoc.cmd 4781 2008-12-09 18:10:32Z rgw_ch $

rem usage jdoc destdir sourcepath packages

mkdir %DEST%%1
%JAVA_HOME%\bin\javadoc -d %DEST%%1 -link file://localhost/%DEST%/elexis -link file://localhost/%DEST%/elexis-utilities -charset "utf-8" -docencoding "utf-8" -sourcepath %TOP%\%1\src -public -encoding "utf-8" -subpackages %2

