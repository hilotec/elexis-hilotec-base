PLAF in Elexis 1.4.  -  G. Weirich 12/09
========================================


"plaf" (pluggable look and feel), aka "personality" is a technique to allow the user to
chose his/her favourite look for the personal elexis instance.

Later this will be extended do define role based action sets and ACL's. At this time, only 
the icons-feature ist implemented.

It works as follows:
The user can launch elexis with the parameter --plaf=<dir> where <dir> ist a elexis-root based
path. The selected plaf will be remembered for the current user and used in future launches without --plaf  parameter. The only way to change a plaf is to run elexis once with a different --plaf setting.


Default Images (ch.elexis.Desk.IMG_xxx - images)
================================================
This will autmatically be loaded from the plaf selected. If no Image with the given name
is found in the plaf, a default Icon from rsc/ will be used.


View Icons
==========
A View can define the following code: