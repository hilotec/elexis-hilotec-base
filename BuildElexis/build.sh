#! /bin/sh
# $Id: build.sh 2548 2007-06-21 13:16:43Z rgw_ch $
cd rsc/build
#$ANT_HOME/bin/ant "Linux" -Dunplugged=1 
$ANT_HOME/bin/ant "Linux"
cd ../..
