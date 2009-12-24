How to setup test environment

- Install SWTBot in Eclipse
- Create a new Run Configuration of Type "SWTBot Test"
in Tab "Test" set ch.elexis.uitests.core.TestElexisCore" as Test class and set
JUnit4 as Test runner. Toggle "Run in UI thread" selected.

In Tab "Main" set "Run an application" to ch.elexis.ElexisApp
In Tab "Plug-Ins" select all Plugins that are required to run elexis (select the elexis plugins and click "add required plugins"

