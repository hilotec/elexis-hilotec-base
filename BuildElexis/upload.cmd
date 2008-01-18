set perl=d:\dev\xampp\perl\bin\perl
set upload=d:\source\elexis_trunk\BuildElexis\rsc\serverside\upload.pl
set source=d:\software\elexis\deploy\output\updater
set dest=ftp.rgw.ch/web/elexis/v1.2/update:rgw.ch:%1

%PERL% %UPLOAD% %source% %dest% size

