; $Id: Elexis.nsi 2734 2007-07-06 13:59:47Z rgw_ch $

;******************************************************************************************
; Dieses NSIS-Script erstellt den Windows-Installer für Elexis
; Wird normalerweise nicht direkt aufgerufen, sondern von Ant intern verwendet
; (im ANT-Script build.xml)
;
; Voraussetzungen: Läuft nur, wenn zuvor das Compile-Target von build.xml erfolgreich
; erstellt worden ist.
; NSIS.EXE muss installiert und erreichbar sein
; Die Environment-Variable SOFTWARE muss korrekt gesetzt sein und der ant-output muss in
; %SOFTWARE%\elexis\deploy
;
; (c) 2005-2006, G. Weirich
;******************************************************************************************

!define APPNAME "Elexis"
!define VERSION "1.0.1"
!define APPNAMEANDVERSION "${APPNAME} ${VERSION}"

; Haupteinstellungen
Name "${APPNAMEANDVERSION}"
InstallDir "$PROGRAMFILES\${APPNAME}"
InstallDirRegKey HKLM "Software\${APPNAME}" ""
;OutFile "../../output\elexis-win32-${VERSION}.exe"
OutFile "$%SOFTWARE%\elexis\deploy\output\elexis-win32-${VERSION}.exe"
!define src "$%SOFTWARE%\elexis\deploy\${APPNAME}-${VERSION}"
;!define src "../../${APPNAME}-${VERSION}"

; Maximale Kompression
SetCompressor lzma

; Modern interface settings
!include "MUI.nsh"

!define MUI_ABORTWARNING
!define MUI_WELCOMEPAGE_TITLE "Willkommen bei der Installation von Elexis"
!define MUI_WELCOMEPAGE_TITLE_3LINES
!define MUI_FINISHPAGE_TITLE_3LINES
!define MUI_WELCOMEPAGE_TEXT "Dieser Installer führt Sie durch die Einrichtung eines Clients für das Elexis-System."

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE ..\lizenz.txt
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

; Set languages (first is default language)
!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_RESERVEFILE_LANGDLL

; Das eigentliche Programm (wie von ant bereitgestellt)
Section "Elexis Basisprogramm" Section1

     SetOutPath $INSTDIR
	SetOverwrite ifnewer

	File "..\lizenz.txt"
	File "${src}\.eclipseproduct"
    File "${src}\Elexis.exe"
    File "${src}\Elexis.ini"
    File "${src}\startup.jar"
    file "${src}\elexis.ico"
    File "${src}\epl*.html"
    File /r "${src}\configuration"

    File /r /x ch.* "${src}\plugins"
    File /r "${src}\features"
    
    SetOutPath "$INSTDIR\plugins"
    File /r "${src}\plugins\ch.elexis_*.*"
    File /r "${src}\plugins\ch.elexis.jdomwrapper_*.*"
    File /r "${src}\plugins\ch.elexis.importer.div_*.*"
    File /r "${src}\plugins\ch.elexis.update_tool_*.*"
    
    ReadEnvStr $0 USERPROFILE
    CreateShortCut "$DESKTOP\Elexis.lnk" "$INSTDIR\Elexis.exe" '-data "$0\elexis"' "$INSTDIR\elexis.ico"
    CreateDirectory "$SMPROGRAMS\${APPNAME}"
    CreateShortCut "$SMPROGRAMS\${APPNAME}\Elexis.lnk" "$INSTDIR\Elexis.exe" '-data "$0\elexis"' "$INSTDIR\elexis.ico"
    CreateShortCut "$SMPROGRAMS\${APPNAME}\Deinstalliere Elexis.lnk" "$INSTDIR\uninstall.exe"
SectionEnd

Section "OpenOffice-Plugin" Section2
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.rgw.oowrapper*.*"
SectionEnd

Section "OpenOffice-Plugin (NOA)" Section2b
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.noatext*.*"
SectionEnd


Section "Arzttarife Schweiz" Section3
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.ebanking_ch*.*"
    File /r "${src}\plugins\ch.elexis.arzttarife_ch*.*"
    File /r "${src}\plugins\ch.elexis.artikel_ch*.*"

SectionEnd

Section "Diagnosecodes Schweiz" Section4
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.diagnosecodes_ch*.*"

SectionEnd

Section "Import PraxisDesktop" Section6
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.importer.praxisdesktop_*.*"
SectionEnd

Section "Import Aerztekasse" Section7
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.AerztekasseImport_*.*"
SectionEnd

Section "Agenda" Section8
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.agenda_*.*"
SectionEnd

Section "Messwerte" Section9
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.befunde_*.*"
SectionEnd

Section "Externe Dokumente" Section10
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.externe_dokumente_*.*"
SectionEnd

Section "Iatrix" Section11
     SetOutPath "$INSTDIR\plugins"
     SetOverwrite ifnewer

    File /r "${src}\plugins\org.iatrix_*.*"
SectionEnd

Section "Privatnotizen" Section12
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.privatnotizen_*.*"
SectionEnd

Section "Bilder" Section13
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer

    File /r "${src}\plugins\ch.elexis.bildanzeige_*.*"
SectionEnd

Section "Mail" Section14
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer
    File /r "${src}\plugins\ch.elexis.mail_*.*"
SectionEnd

Section "Omnivore" Section17
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer
    File /r "${src}\plugins\ch.elexis.omnivore_*.*"
SectionEnd
    
Section -FinishSection

	WriteRegStr HKLM "Software\${APPNAME}" "" "$INSTDIR"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName" "${APPNAME}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "$INSTDIR\uninstall.exe"
	WriteUninstaller "$INSTDIR\uninstall.exe"
	;ExecShell open "$INSTDIR\Client-Liesmich.html"

SectionEnd

Section "Simpletext" Section19
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer
    File /r "${src}\plugins\ch.elexis.Textplugin_*.*"
SectionEnd

Section "Notizen" Section20
    SetOutPath "$INSTDIR\plugins"
    SetOverwrite ifnewer
    File /r "${src}\plugins\ch.elexis.notes_*.*"
SectionEnd


!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${Section1} "Das eigentliche Programm."
    !insertmacro MUI_DESCRIPTION_TEXT ${Section2} "Plugin zur Verbindung mit OpenOffice bis 2.03"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section2b} "Plugin zur Verbindung mit OpenOffice 2.1ff"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section3} "Tarmed, Labortarif, Artikel, ESR-Management"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section4} "ICD-10, Tessiner Diagnosecode"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section6} "Import von Patientendaten aus dem Programm PraxisDesktop"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section7} "Import von Patientendaten aus dem Aerztekasse-Programm"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section8} "Verwaltung von Terminen"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section9} "Einfaches Messwert-Erfassungs Plugin"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section10} "Einbindung externer Dokumente aus dem Dateisystem"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section11} "Alternative Oberfläche, Problemorientierte KG"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section12} "Nicht-öffentliche Notizen in Konsultationstexte einbinden"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section13} "Bilder mit Konsultationstexten verknüpfen"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section14} "Mail Plugin (automatische Fehlerrückmeldungen)"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section17} "Plugin für Import und Zuordnung beliebiger files"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section19} "Einfaches Textplugin (statt OpenOffice)"
    !insertmacro MUI_DESCRIPTION_TEXT ${Section20} "Patientenunabhängige Dokumente erstellen und einbinden"
!insertmacro MUI_FUNCTION_DESCRIPTION_END

;m Deinstallation
Section Uninstall

	;Registry-EintrÃ¤ge
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
	DeleteRegKey HKLM "SOFTWARE\${APPNAME}"

	; Uninstaller ebenfalls lÃ¶schen
	Delete "$INSTDIR\uninstall.exe"

	; VerknÃ¼pfungen entfernen
	Delete "$DESKTOP\Elexis.lnk"
	Delete "$SMPROGRAMS\${APPNAME}\Elexis.lnk"
	Delete "$SMPROGRAMS\${APPNAME}\Deinstalliere Elexis.lnk"

	; Programmverzeichnis lÃ¶schen
	RMDir /r "$INSTDIR"
	RMDir "$SMPROGRAMS\${APPNAME}"


SectionEnd


BrandingText "Copyright (c) by Elexis.ch 2005-2007"

; eof
