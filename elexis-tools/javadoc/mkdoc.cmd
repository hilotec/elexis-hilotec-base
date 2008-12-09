@echo off
rem $Id: mkdoc.cmd 4781 2008-12-09 18:10:32Z rgw_ch $
rem =========================
set TOP=d:\source\elexis_trunk
rem ==========================

set j=%TOP%\elexis-tools\javadoc\jdoc.cmd
if *%1==* goto stay
set DEST=%1\
:stay
call %j% elexis-utilities ch
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
call %j% elexis-externe-dokumente ch
call %j% elexis-h-net ch
call %j% elexis-icpc ch
call %j% elexis-importer ch
call %j% elexis-importer-aerztekasse ch
call %j% elexis-mail ch
call %j% elexis-medikamente-bag ch
call %j% elexis-nachrichten ch
call %j% elexis-notes ch
call %j% elexis-omnivore ch
call %j% elexis-privatnotizen ch
call %j% iatrix-help-wiki org
call %j% Laborimport-Viollier ch
call %j% OOWrapper ch
call %j% SGAM-xChange ch