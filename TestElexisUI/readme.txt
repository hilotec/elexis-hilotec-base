How to setup test environment

- Install SWTBot in Eclipse
- Create a new Run Configuration of Type "SWTBot Test"
in Tab "Test" set ch.elexis.uitests.core.TestElexisCore" as Test class and set
JUnit4 as Test runner. Toggle "Run in UI thread" selected.

In Tab "Main" set "Run an application" to ch.elexis.ElexisApp
In Tab "Arguments" add " -Dch.elexis.username=yourName  -Dch.elexis.password=topsecret" to "VM args" as username/password to access the mySql database.
In Tab "Plug-Ins" select all Plugins that are required to run elexis (select the elexis plugins and click "add required plugins"

Under Linux you may adapt run_swtbot.sh to make it run the SWTbot-test from
the command line.