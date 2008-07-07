# Auto-generated by EclipseNSIS Script Wizard
# 07.07.2008 10:27:55

Name "Elexis Updater"

# Defines
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION 1.0.0
!define COMPANY elexis.ch
!define URL http://www.elexis.ch
!define src "$%SOFTWARE%\elexis\deploy"

# Included files
!include Sections.nsh

# Reserved Files
ReserveFile "${NSISDIR}\Plugins\StartMenu.dll"

# Variables
Var StartMenuGroup

# Installer pages
Page directory
Page custom StartMenuGroupSelect "" ": $(StartMenuPageTitle)"
Page instfiles

# Installer languages
LoadLanguageFile "${NSISDIR}\Contrib\Language files\German.nlf"

# Installer attributes
; OutFile elexisupdate-setup.exe
OutFile "$%SOFTWARE%\elexis\deploy\output\elexisupdate-setup.exe"
InstallDir $PROGRAMFILES\Elexis
CRCCheck on
XPStyle on
Icon "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
ShowInstDetails show
AutoCloseWindow false
VIProductVersion 1.0.0.0
VIAddVersionKey /LANG=${LANG_GERMAN} ProductName "Elexis Updater"
VIAddVersionKey /LANG=${LANG_GERMAN} ProductVersion "${VERSION}"
VIAddVersionKey /LANG=${LANG_GERMAN} CompanyName "${COMPANY}"
VIAddVersionKey /LANG=${LANG_GERMAN} CompanyWebsite "${URL}"
VIAddVersionKey /LANG=${LANG_GERMAN} FileVersion "${VERSION}"
VIAddVersionKey /LANG=${LANG_GERMAN} FileDescription ""
VIAddVersionKey /LANG=${LANG_GERMAN} LegalCopyright ""

# Installer sections
Section -Main SEC0000
    SetOutPath $INSTDIR
    SetOverwrite on
    File D:\software\elexis\rcp33\elexisupdate.exe
    File D:\software\elexis\deploy\Elexis-1.3.2\elexis-mmc_1.0.0.20080626.jar
    WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd

# Installer functions
Function StartMenuGroupSelect
    Push $R1
    StartMenu::Select /checknoshortcuts "$(DisableStartMenuShortcutsText)" /autoadd /text "$(StartMenuPageText)" /lastused $StartMenuGroup Elexis
    Pop $R1
    StrCmp $R1 success success
    StrCmp $R1 cancel done
    MessageBox MB_OK $R1
    Goto done
success:
    Pop $StartMenuGroup
done:
    Pop $R1
FunctionEnd

Function .onInit
    InitPluginsDir
FunctionEnd

# Installer Language Strings
# TODO Update the Language Strings with the appropriate translations.

LangString StartMenuPageTitle ${LANG_GERMAN} "Start Menu Folder"

LangString StartMenuPageText ${LANG_GERMAN} "Select the Start Menu folder in which to create the program's shortcuts:"

LangString DisableStartMenuShortcutsText ${LANG_GERMAN} "Do not create shortcuts"
