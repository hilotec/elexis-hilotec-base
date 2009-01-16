set perl=d:\dev\xampp\perl\bin\perl
set upload=d:\source\elexis_trunk\BuildElexis\rsc\serverside\upload.pl
set source=d:\software\elexis\deploy_200\output\updater
set dest=ftp.rgw.ch/web/elexis/v2.0/update:rgw.ch:%1

%PERL% %UPLOAD% %source% %dest% size

