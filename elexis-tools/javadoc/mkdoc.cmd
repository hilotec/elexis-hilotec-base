@echo off
rem $Id: mkdoc.cmd 4777 2008-12-08 17:01:08Z rgw_ch $
rem =========================
set TOP=d:\source\elexis_trunk
rem ==========================

set j=%TOP%\elexis-tools\javadoc\jdoc.cmd

call %j% elexis ch
call %j% elexis-agenda ch
call %j% elexis-artikel-schweiz ch
call %j% elexis-arzttarife-schweiz ch
call %j% elexis-be-connector ch
call %j% elexis-befunde ch
call %j% elexis-bildanzeige ch
call %j% elexis-diagnosecodes-schweiz ch
call %j% elexis-ebanking-schweiz ch
call %j% elexis-eigendiagnosen ch
call %j% elexis-icpc ch
call %j% elexis-importer ch
call %j% elexis-nachrichten ch
call %j% elexis-notes ch
call %j% elexis-omnivore ch
call %j% elexis-privatnotizen ch
call %j% elexis-utilities ch
call %j% SGAM-xChange ch