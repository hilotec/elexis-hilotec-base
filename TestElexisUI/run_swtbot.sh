#!/bin/bash -v
# (c) 2010 Niklaus Giger
# Helper script to run SWTbot on our elexis application
#
# I would be happy if it would run at least under linux
# TODO: Have to add a few classes to the elexis hudson build
#
ECLIPSE_HOME=/home/src/galileo/eclipse
TEST_CLASS=ch.elexis.uitests.AllTests
# see http://github.com/ketan/swtbot/blob/master/org.eclipse.swtbot.eclipse.finder.test/src/org/eclipse/swtbot/eclipse/finder/AllTests.java for an example
/usr/bin/java \
-Xms128M -Xmx368M -XX:MaxPermSize=256M \
-DPLUGIN_PATH= \
-classpath $ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher_1.0.201.R35x_v20090715.jar \
org.eclipse.core.launcher.Main \
-application org.eclipse.swtbot.eclipse.junit4.headless.swtbottestapplication \
-testProduct ch.elexis.ElexisProduct \
-data workspace \
formatter=org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter,$ECLIPSE_HOME/$TEST_CLASS.xml \
formatter=org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter \
-testPluginName org.eclipse.swtbot.eclipse.finder.test \
-className $TEST_CLASS \
-os linux -ws linux -arch x86 \
-consoleLog -debug
