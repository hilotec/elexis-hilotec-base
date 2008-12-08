@echo off
rem $Id: jdoc.cmd 4777 2008-12-08 17:01:08Z rgw_ch $

rem usage jdoc destdir sourcepath packages

mkdir %1
javadoc -d %1 -charset "utf-8" -docencoding "utf-8" -sourcepath %TOP%\%1\src -subpackages %2

