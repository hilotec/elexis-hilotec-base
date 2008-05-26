Versionen
=========

TODO: Datum angeben, wann welches Release veröffentlicht wurde. Evtl. mit SVN-Tag.

DATUM 1.0
DATUM 1.1
DATUM 1.1.1
DATUM 1.1.2

Änderungen
==========

TODO: Alle relevanten Änderungen angeben, insbesondere Verhaltensänderungen.

07.11.2007, danlutz
 * Ausdruck TarmedRechnung:
   Wenn der Druckerschacht in den Druckereinstellungen konfiguriert ist, wird
   dieser verwendet. Sonst wird die Standard-Einstellung bzw. der in der
   Vorlage eingestellte Druckerschacht verwendet.
   Bisher wurde der in den Druckereinstellungen konfigurierte Schacht gar nicht
   berücksichtigt. Dies führte bei bestimmten Druckermodellen dazu, dass der
   Standard-Schacht, nicht aber der in der Vorlage eingestellte Schacht
   verwendet wurde.
   Betroffene Drucker: "Drucker mit A4-Papier mit ESR", "Drucker mit weissem A4-Papier"

29.3.2008, gweirich
 *  Tarmed-Rechnung: Positionen werden sortiert, Hierarchie:
     1 - Datum
     2 - Tarmedlkeistungen nach Ziffer
     3 - andere Leistungen alphabetisch 
     
 * Omnivore: Spalten können sortiert werden durch Klick auf Spaltenkopf
 
 26.5.08 gweirich
 * Standardschriftarten für alle Views können eungestellt werden, als User-Settimg.
 Eine View, die die vom Anwender gewünschte Standardschriftart einstellen will, kann diese 
 Schriftart mit Desk.getFont(PreferenceConstants.USR_DEFAULTFONT) einlesen. Ausserdem sollte
 eine solche View einen UserChangeListener bei GlobalEvents einhängen, um bei einem Userwechsel jeweils 
 wieder die richtige Schriftart einzustellen.
  
 * Agenda merkt sich den zuletzt eingestellten Bereich
 
 * KonsDetailView und CodeSelektor merken sich die zuletzt eingestellten Grössenverhältnisse der einzelnen 
 Teilfenster.
  
 * Anwendereinstellungen zeigen die vorhandenen EInstellungsseiten an