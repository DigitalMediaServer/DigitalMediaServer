﻿Unicode true
ManifestDPIAware true
ShowUninstDetails show

!pragma warning disable 6010

!include "MUI2.nsh"
!include "FileFunc.nsh"
!include "LogicLib.nsh"
!include "LocateJava.nsh"
!include "Sections.nsh"
!include "serviceLib.nsh"
!include "WinVer.nsh"
!include "WordFunc.nsh"
!include "x64.nsh"

!define UninstallEXE "uninstall.exe"

!define INSTALLERMUTEXNAME "$(^Name)"
!define PRODUCT_NAME "${PROJECT_NAME}"
!define PRODUCT_VERSION "v${PROJECT_VERSION_SHORT}"
!define PRODUCT_PUBLISHER "${PROJECT_NAME} Team"
!define PRODUCT_WEB_SITE "${PROJECT_ORGANIZATION_URL}"
!define REG_KEY_UNINSTALL "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${PROJECT_NAME}"
!define REG_KEY_SOFTWARE "SOFTWARE\${PROJECT_NAME}"
!define /utcdate BUILD_YEAR "%Y"

Name "${PROJECT_NAME}"
BrandingText "$CopyLeft"

XPStyle on
InstProgressFlags Smooth colored
SetDatablockOptimize on
SetDateSave on
CRCCheck force
RequestExecutionLevel admin
AllowSkipFiles off
ManifestSupportedOS all ; Left here to remember to add GUI ID in case Windows 11 or above appear before NSIS add their support by default

!define MUI_ABORTWARNING
!define MUI_CUSTOMFUNCTION_GUIINIT onGUIInit
!define MUI_UI "${PROJECT_BASEDIR}\src\main\external-resources\third-party\nsis\Contrib\UIs\modern.exe" ; UltraModern.exe
!define MUI_ICON "${PROJECT_BASEDIR}\src\main\resources\images\logo.ico"
!define MUI_UNICON "${PROJECT_BASEDIR}\src\main\resources\images\logo.ico"
!define MUI_WELCOMEFINISHPAGE_BITMAP_STRETCH AspectFitHeight
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_RIGHT
!define MUI_HEADERIMAGE_BITMAP_STRETCH AspectFitHeight
!define MUI_HEADER_TRANSPARENT_TEXT
!define MUI_BGCOLOR FFFFFF
!define MUI_LANGDLL_ALWAYSSHOW
!define MUI_LANGDLL_ALLLANGUAGES
; Remember the installer language (Language selection in dialog settings)
!define MUI_LANGDLL_REGISTRY_ROOT "HKLM"
!define MUI_LANGDLL_REGISTRY_KEY "${REG_KEY_SOFTWARE}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "Installer Language"
!define MUI_WELCOMEPAGE_TITLE_3LINES
!define MUI_PAGE_CUSTOMFUNCTION_SHOW showHiDPI
!insertmacro MUI_PAGE_WELCOME
!define MUI_LICENSEPAGE_TEXT_TOP " "
!insertmacro MUI_PAGE_LICENSE "${PROJECT_BASEDIR}\EULA.rtf"
!define MUI_COMPONENTSPAGE
!define MUI_COMPONENTSPAGE_SMALLDESC
!define MUI_COMPONENTSPAGE_TEXT_TOP " "
!define MUI_PAGE_CUSTOMFUNCTION_SHOW windowsResizing
!define MUI_CUSTOMFUNCTION_ONMOUSEOVERSECTION hideRequiredSize
!insertmacro MUI_COMPONENTSPAGE_INTERFACE
!insertmacro MUI_PAGEDECLARATION_COMPONENTS
!define MUI_PAGE_CUSTOMFUNCTION_LEAVE DirectoryLeave
!insertmacro MUI_PAGE_DIRECTORY
Page Custom LockedListShow LockedListLeave
!insertmacro MUI_PAGE_INSTFILES
!define MUI_PAGE_CUSTOMFUNCTION_SHOW showHiDPI
!define MUI_FINISHPAGE_TITLE_3LINES
!define MUI_FINISHPAGE_LINK_COLOR 1E90FF
!define MUI_FINISHPAGE_LINK $(OpenWebSite)
!define MUI_FINISHPAGE_LINK_LOCATION "${PROJECT_ORGANIZATION_URL}"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION RunDMS
!define MUI_FINISHPAGE_SHOWREADME ""
!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
!define MUI_FINISHPAGE_SHOWREADME_TEXT $(DesktopShortcut)
!define MUI_FINISHPAGE_SHOWREADME_FUNCTION CreateDesktopShortcut
!insertmacro MUI_PAGE_FINISH

!define MUI_UNABORTWARNING
!define MUI_WELCOMEPAGE_TITLE_3LINES
!define MUI_UNWELCOMEFINISHPAGE_BITMAP_STRETCH AspectFitHeight
!define MUI_PAGE_CUSTOMFUNCTION_SHOW "un.showHiDPI"
!insertmacro MUI_UNPAGE_WELCOME
!define MUI_CUSTOMFUNCTION_UNONMOUSEOVERSECTION un.hideRequiredSize
!define MUI_UNCOMPONENTSPAGE
!define MUI_UNCOMPONENTSPAGE_SMALLDESC
!define MUI_UNCOMPONENTSPAGE_TEXT_TOP " "
!insertmacro MUI_UNPAGE_COMPONENTS
UninstPage Custom "un.LockedListShow"
!insertmacro MUI_UNPAGE_INSTFILES
!define MUI_PAGE_CUSTOMFUNCTION_SHOW "un.showHiDPI"
!define MUI_FINISHPAGE_TITLE_3LINES
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!insertmacro MUI_UNPAGE_FINISH

!include setupLanguages.nsh

; Reserve Files

  ;If you are using solid compression, files that are required before
  ;the actual installation should be stored first in the data block,
  ;because this will make your installer start faster.

!insertmacro MUI_RESERVEFILE_LANGDLL

Var Clean
Var CopyLeft
Var RAM
Var X64
Var XP

;https://msdn.microsoft.com/en-us/library/ms645502(v=vs.85).aspx
!macro WindowSize x y cx cy
	IntOp $0 ${x} * $4
	IntOp $0 $0 / 4
	IntOp $1 ${y} * $5
	IntOp $1 $1 / 8
	IntOp $2 ${cx} * $4
	IntOp $2 $2 / 4
	IntOp $3 ${cy} * $5
	IntOp $3 $3 / 8
!macroend

; ComponentText "Select the components you want to install."

Section /o "-CleanDelegate" secCleanDelegate
	SetDetailsPrint textonly
	ReadEnvStr $R1 "ALLUSERSPROFILE"
	RMDir /r "$R1\${PROJECT_NAME_CAMEL}"
	RMDir /r "$TEMP\fontconfig"
	RMDir /r "$LOCALAPPDATA\fontconfig"
	RMDir /r "$INSTDIR"
SectionEnd

Section "!$(SectionServer)" sectionServer
	SetDetailsPrint both

	SetOutPath "$INSTDIR"
	LogSet on
	SetOverwrite on

	File /r /x "Thumbs.db" "${PROJECT_BASEDIR}\src\main\external-resources\documentation"
	File /r /x "Thumbs.db" "${PROJECT_BASEDIR}\src\main\external-resources\renderers"
	File /r /x "Thumbs.db" /x "ffmpe*.*" /x "avisynth" /x "MediaIn*.dll" "${PROJECT_BASEDIR}\target\bin\win32"
	File "${PROJECT_BUILD_DIR}\${PROJECT_NAME_SHORT}.exe"
	File "${PROJECT_BASEDIR}\src\main\external-resources\${PROJECT_NAME_SHORT}.bat"
	File /r /x "Thumbs.db" "${PROJECT_BASEDIR}\src\main\external-resources\web"
	File "${PROJECT_BUILD_DIR}\${PROJECT_ARTIFACT_ID}.jar"
	File /nonfatal "${PROJECT_BASEDIR}\CHANGELOG.txt"
	File "${PROJECT_BASEDIR}\EULA.rtf"
	File "${PROJECT_BASEDIR}\README.*"
	File "${PROJECT_BASEDIR}\LICENSE.txt"
	File "${PROJECT_BASEDIR}\src\main\external-resources\logba*.xml"
	File /oname=${PROJECT_ARTIFACT_ID}.ico "${PROJECT_BASEDIR}\src\main\resources\images\logo.ico"
	File "${PROJECT_BASEDIR}\src\main\external-resources\DummyInput.*"

	SetOutPath "$INSTDIR\win32"
	File "${PROJECT_BASEDIR}\src\main\external-resources\lib\ctrlsender\ctrlsender.exe"

	SetOutPath "$INSTDIR\win32\service"
	File /x "Thumbs.db" "${PROJECT_BASEDIR}\src\main\external-resources\third-party\wrapper\*.*"

	; The user may have set the installation folder as the profile folder, so we can't clobber this
	SetOutPath "$INSTDIR"
	File "${PROJECT_BASEDIR}\src\main\external-resources\*.conf"

	; Store install folder
	WriteRegStr HKLM "${REG_KEY_SOFTWARE}" "" "$INSTDIR"

	; Create uninstaller
	WriteRegStr HKLM "${REG_KEY_UNINSTALL}" "DisplayName" "${PROJECT_NAME}"
	WriteRegStr HKLM "${REG_KEY_UNINSTALL}" "DisplayIcon" "$INSTDIR\${PROJECT_ARTIFACT_ID}.ico"
	WriteRegStr HKLM "${REG_KEY_UNINSTALL}" "DisplayVersion" "${PROJECT_VERSION}"
	WriteRegStr HKLM "${REG_KEY_UNINSTALL}" "Publisher" "${PROJECT_ORGANIZATION_NAME}"
	WriteRegStr HKLM "${REG_KEY_UNINSTALL}" "URLInfoAbout" "${PROJECT_ORGANIZATION_URL}"
	WriteRegStr HKLM "${REG_KEY_UNINSTALL}" "UninstallString" "$INSTDIR\${UninstallEXE}"
	WriteRegDWORD HKLM "${REG_KEY_UNINSTALL}" "NoModify" 0x00000001
	WriteRegDWORD HKLM "${REG_KEY_UNINSTALL}" "NoRepair" 0x00000001

	SetOutPath "$INSTDIR"
	WriteUninstaller "$INSTDIR\${UninstallEXE}"

	ReadEnvStr $R0 "ALLUSERSPROFILE"
	CreateDirectory "$R0\${PROJECT_NAME_CAMEL}\data"
	AccessControl::GrantOnFile "$R0\${PROJECT_NAME_CAMEL}" "(BU)" "GenericRead + GenericExecute + GenericWrite + Delete + FullAccess"
	Pop $0
	SetOutPath "$R0\${PROJECT_NAME_CAMEL}"
	SetOverwrite off
	File "${PROJECT_BASEDIR}\src\main\external-resources\*.conf"
	File "${PROJECT_BASEDIR}\src\main\external-resources\logba*.xml"
	SetOverwrite on
SectionEnd

Section $(SectionShortcuts) sectionShortcuts
	SetShellVarContext all
	CreateDirectory "$SMPROGRAMS\${PROJECT_NAME}"
	CreateShortCut "$SMPROGRAMS\${PROJECT_NAME}\${PROJECT_NAME_SHORT} (Select Profile).lnk" "$INSTDIR\${PROJECT_NAME_SHORT}.exe" "profiles" "" "" SW_SHOWNORMAL
	CreateShortCut "$SMPROGRAMS\${PROJECT_NAME}\${PROJECT_NAME}.lnk" "$INSTDIR\${PROJECT_NAME_SHORT}.exe" "" "" "" SW_SHOWNORMAL
	CreateShortCut "$SMPROGRAMS\${PROJECT_NAME}\Uninstall.lnk" "$INSTDIR\${UninstallEXE}" "" "" "" SW_SHOWNORMAL
SectionEnd

Section /o "-32-bit" sec32Bit
	SetOverwrite on
	SetOutPath "$INSTDIR\win32"
	File "${PROJECT_BASEDIR}\target\bin\win32\ffmpeg.exe"
	File "${PROJECT_BASEDIR}\target\bin\win32\MediaInfo.dll"
SectionEnd

Section /o "-64-bit" sec64Bit
	SetOverwrite on
	SetOutPath "$INSTDIR\win32"
	File "${PROJECT_BASEDIR}\target\bin\win32\ffmpeg64.exe"
	File "${PROJECT_BASEDIR}\target\bin\win32\MediaInfo.dll"
	File "${PROJECT_BASEDIR}\target\bin\win32\MediaInfo64.dll"
SectionEnd

Section /o "-XP" secXP
	SetOverwrite on
	SetOutPath "$INSTDIR\win32"
	File /r /x "Thumbs.db" "${PROJECT_BASEDIR}\src\main\external-resources\lib\winxp"
SectionEnd

Section /o $(SectionCleanInstall) sectionCleanInstall
SectionEnd

Section /o $(SectionWindowsFirewall) sectionFirewall
	WriteRegStr HKLM "${REG_KEY_UNINSTALL}" "FirewallRules" "1"

	${If} ${IsWinXP}
	${OrIf} ${IsWin2003}
		nsExec::Exec 'netsh firewall set multicastbroadcastresponse mode=enable profile=standard'
		nsExec::Exec 'netsh firewall set multicastbroadcastresponse mode=enable profile=domain'
		nsExec::Exec 'netsh firewall add portopening protocol=tcp port=5252 name="Digital Media Server - TCP 5252" mode=enable profile=standard'
		nsExec::Exec 'netsh firewall add portopening protocol=tcp port=6363 name="Digital Media Server - TCP 6363" mode=enable profile=standard'
		nsExec::Exec 'netsh firewall add portopening protocol=tcp port=5252 name="Digital Media Server - TCP 5252" mode=enable profile=domain'
		nsExec::Exec 'netsh firewall add portopening protocol=tcp port=6363 name="Digital Media Server - TCP 6363" mode=enable profile=domain'
		nsExec::Exec 'netsh firewall add portopening protocol=all port=1900 name="Digital Media Server - TCP 1900" mode=enable profile=standard'
		nsExec::Exec 'netsh firewall add portopening protocol=all port=1900 name="Digital Media Server - TCP 1900" mode=enable profile=domain'
	${ElseIf} ${AtLeastWinVista}
		nsExec::Exec 'netsh advfirewall set privateprofile settings unicastresponsetomulticast enable'
		nsExec::Exec 'netsh advfirewall set domainprofile settings unicastresponsetomulticast enable'
		; Delete the rules before creating them, as it's possible to create multiple identical rules.
		nsExec::Exec 'netsh advfirewall firewall delete rule name="Digital Media Server - Incoming TCP port 1900/5252/6363"'
		nsExec::Exec 'netsh advfirewall firewall delete rule name="Digital Media Server - Incoming UDP port 1900"'
		nsExec::Exec 'netsh advfirewall firewall add rule name= "Digital Media Server - Incoming TCP port 1900/5252/6363" dir=in action=allow description="Incoming on TCP ports 1900, 5252 and 6363" profile=private,domain protocol=TCP localport=1900,5252,6363 enable=yes'
		nsExec::Exec 'netsh advfirewall firewall add rule name= "Digital Media Server - Incoming UDP port 1900" dir=in action=allow description="Incoming on UDP port 1900" profile=private,domain protocol=UDP localport=1900 enable=yes'
	${EndIf}
	; Future Windows 10 or later versions should not accept anymore "netsh" use for the firewall configuration, so a powershell script or plugin or code should be used
	; To check if other firewalls are blocking ports: netstat -ano | findstr -i "5252" or portqry.exe -n x.x.x.x -e 5252
SectionEnd

Section /o $(SectionDownloadJava) sectionInstallJava ; http://www.oracle.com/technetwork/java/javase/windows-diskspace-140460.html

	${If} ${AtMostWin2008}
		${If} ${RunningX64}
			StrCpy $0 "http://web.archive.org/web/20231111192459/http://download.ithb.ac.id/downloads/Softwares/Developers/java/oracle/v7/jre-7u80-windows-x64.exe"
			StrCpy $1 "jre-7u80-windows-x64.exe"
		${Else}
			StrCpy $0 "http://web.archive.org/web/20231111192435/http://download.ithb.ac.id/downloads/Softwares/Developers/java/oracle/v7/jre-7u80-windows-i586.exe"
			StrCpy $1 "jre-7u80-windows-i586.exe"
		${EndIf}
		StrCpy $3 "Java 7u80"
	${Else}
		${If} ${RunningX64}
			StrCpy $0 "https://api.adoptium.net/v3/installer/latest/8/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"
			StrCpy $1 "OpenJDK8U-jre_x64_windows_hotspot.msi"
		${Else}
			StrCpy $0 "https://api.adoptium.net/v3/installer/latest/8/ga/windows/x86/jre/hotspot/normal/eclipse?project=jdk"
			StrCpy $1 "OpenJDK8U-jre_x86-32_windows_hotspot.msi"
		${EndIf}
		StrCpy $3 "Java 8"
	${EndIf}
	
	${WordReplaceS} $(Downloading) "%s" "$3" "+1" $2 ;
	NScurl::http GET "$0" "$PLUGINSDIR\$1" /INSIST /CANCEL /RESUME /POPUP /STRING TITLE_NOSIZE "$2" /STRING TITLE "$2 [@PERCENT@%]" /STRING_NOSIZE TEXT "$(DownloadingFile) @OUTFILE@, @XFERSIZE@ @ @SPEED@ @ANIMDOTS@" /STRING TEXT "$(DownloadingFile) @OUTFILE@, @XFERSIZE@ / @FILESIZE@ @ @SPEED@ @ANIMDOTS@" /END
	Pop $0
	StrCmpS $0 "OK" JavaDownloadOK
	${WordReplaceS} $(DownloadError) "%s" $0 "+1" $0
	MessageBox MB_ICONEXCLAMATION $0
	Goto End

	JavaDownloadOK:
		${If} ${AtMostWin2008}
			ExecWait "$PLUGINSDIR\$1 SPONSORS=0"
		${Else}
			ExecWait "msiexec /i $PLUGINSDIR\$1 /norestart"
		${EndIf}

	End:
SectionEnd

SectionGroup $(SectionHeapSize) sectionHeapSize ; http://www.oracle.com/technetwork/java/hotspotfaq-138619.html#gc_heap_32bit
	Section /o "512 MB" sectionHeapSize_1
		WriteRegStr HKLM "${REG_KEY_SOFTWARE}" "HeapMem" "512M"
	SectionEnd

	Section /o "768 MB" sectionHeapSize_2
		WriteRegStr HKLM "${REG_KEY_SOFTWARE}" "HeapMem" "768M"
	SectionEnd

	Section /o "1280 MB" sectionHeapSize_3
		WriteRegStr HKLM "${REG_KEY_SOFTWARE}" "HeapMem" "1280M"
	SectionEnd

	Section /o "1536 MB" sectionHeapSize_4
		WriteRegStr HKLM "${REG_KEY_SOFTWARE}" "HeapMem" "1536M"
	SectionEnd

	Section /o "4096 MB" sectionHeapSize_5
		WriteRegStr HKLM "${REG_KEY_SOFTWARE}" "HeapMem" "4096M"
	SectionEnd

	Section /o "6144 MB" sectionHeapSize_6
		WriteRegStr HKLM "${REG_KEY_SOFTWARE}" "HeapMem" "6144M"
	SectionEnd
SectionGroupEnd

Section /o "AviSynth" sectionInstallAviSynth
	; https://forum.doom9.org/showthread.php?t=148782
	; https://nightly.mpc-hc.org/mpc-hc_apps/vsfilter/
	; A more up to date sofware with a 64-bit version and multithreading support like AviSynth+ or VapourSynth could replace AviSynth and be directly downloaded from their website:
	; https://github.com/pinterf/AviSynthPlus/releases
	; https://github.com/vapoursynth/vapoursynth/releases
	SetOverwrite on
	SetOutPath "$INSTDIR\win32\avisynth"
	File "${PROJECT_BASEDIR}\target\bin\win32\avisynth\avisynth.exe"
	ExecWait "$INSTDIR\win32\avisynth\avisynth.exe"
SectionEnd

Section "-EstimatedSize" sectionEstimatedSize
	${GetSize} "$INSTDIR" "/S=0B" $0 $1 $2
	IntFmt $0 "0x%08x" $0 ; https://msdn.microsoft.com/en-us/library/windows/desktop/ms647550(v=vs.85).aspx
	WriteRegDWORD HKLM "${REG_KEY_UNINSTALL}" "EstimatedSize" "$0" ; Used by Windows
SectionEnd

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionServer} $(SectionDescriptionServer)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionShortcuts} $(SectionDescriptionShortcuts)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionCleanInstall} $(SectionDescriptionCleanInstall)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionFirewall} $(SectionDescriptionWindowsFirewall)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionInstallJava} $(SectionDescriptionInstallJava)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionHeapSize} $(SectionDescriptionHeapSize)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionHeapSize_1} $(SectionDescriptionHeapSize)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionHeapSize_2} $(SectionDescriptionHeapSize)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionHeapSize_3} $(SectionDescriptionHeapSize)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionHeapSize_4} $(SectionDescriptionHeapSize)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionHeapSize_5} $(SectionDescriptionHeapSize)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionHeapSize_6} $(SectionDescriptionHeapSize)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionInstallAviSynth} $(SectionDescriptionAviSynth)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

Function .onSelChange
	SectionGetFlags ${sectionInstallJava} $1
	${If} $1 != 0
		FindWindow $1 "#32770" "" $HWNDPARENT
		GetDlgItem $1 $1 1023 ; Required disk space control
		ShowWindow $1 ${SW_HIDE}
	${Else}
		FindWindow $1 "#32770" "" $HWNDPARENT
		GetDlgItem $1 $1 1023
		ShowWindow $1 ${SW_SHOW}
	${EndIf}

	SectionGetFlags ${sectionServer} $1
	${If} $1 == 8
		SectionSetFlags ${sec32Bit} 0
		SectionSetFlags ${sec64Bit} 0
		SectionSetFlags ${secXP} 0
	${Else}
		${Select} $X64
			${Case} ""
				SectionSetFlags ${sec32Bit} ${SF_SELECTED}
			${CaseElse}
				SectionSetFlags ${sec64Bit} ${SF_SELECTED}
		${EndSelect}
		${Select} $XP
			${Case} "1"
				SectionSetFlags ${secXP} ${SF_SELECTED}
		${EndSelect}
	${EndIf}

	SectionGetFlags ${sectionCleanInstall} $1
	${If} $1 != $Clean
		${If} $1 != 0
			MessageBox MB_ICONEXCLAMATION|MB_YESNO|MB_DEFBUTTON2 $(CleanInstallWarning) IDYES +3
			SectionSetFlags ${sectionCleanInstall} 0
			StrCpy $1 0
		${EndIf}
		SectionSetFlags ${secCleanDelegate} $1
		StrCpy $Clean $1
	${EndIf}

	; Heap memory size section group radio buttons
	StrCpy $2 0

	OnlyOneRadioButtonSelected:
		Push $R2
		StrCpy $R2 ${SF_SELECTED}
		SectionGetFlags ${sectionHeapSize_1} $0
		IntOp $R2 $R2 & $0
		SectionGetFlags ${sectionHeapSize_2} $0
		IntOp $R2 $R2 & $0
		SectionGetFlags ${sectionHeapSize_3} $0
		IntOp $R2 $R2 & $0
		SectionGetFlags ${sectionHeapSize_4} $0
		IntOp $R2 $R2 & $0
		SectionGetFlags ${sectionHeapSize_5} $0
		IntOp $R2 $R2 & $0
		SectionGetFlags ${sectionHeapSize_6} $0
		IntOp $R2 $R2 & $0

		StrCmp $R2 0 NotAllSelected
			SectionSetFlags ${sectionHeapSize_1} 0
			SectionSetFlags ${sectionHeapSize_2} 0
			SectionSetFlags ${sectionHeapSize_3} 0
			SectionSetFlags ${sectionHeapSize_4} 0
			SectionSetFlags ${sectionHeapSize_5} 0
			SectionSetFlags ${sectionHeapSize_6} 0

	NotAllSelected:
		Pop $R2
		!insertmacro StartRadioButtons $R4
			!insertmacro RadioButton ${sectionHeapSize_1}
			!insertmacro RadioButton ${sectionHeapSize_2}
			!insertmacro RadioButton ${sectionHeapSize_3}
			!insertmacro RadioButton ${sectionHeapSize_4}
			!insertmacro RadioButton ${sectionHeapSize_5}
			!insertmacro RadioButton ${sectionHeapSize_6}
		!insertmacro EndRadioButtons

	StrCmp $2 0 0 +3
	StrCpy $2 1
	Goto OnlyOneRadioButtonSelected
FunctionEnd

Function RunDMS ; http://mdb-blog.blogspot.ru/2013/01/nsis-lunch-program-as-user-from-uac.html
	; Run program through explorer.exe to de-evaluate user from admin level to regular one.
	Exec '"$WINDIR\explorer.exe" "$INSTDIR\${PROJECT_NAME_SHORT}.exe"'
FunctionEnd

Function CreateDesktopShortcut ; Done here to avoid having a shortcut with administrator rights
	CreateShortCut "$DESKTOP\${PROJECT_NAME}.lnk" "$INSTDIR\${PROJECT_NAME_SHORT}.exe" "" "" "" SW_SHOWNORMAL
FunctionEnd

Function .onInit
	StrCpy $Clean 0 ; Initialize "clean install" status

	${If} ${RunningX64}
		StrCpy $X64 "1"
		SetRegView 64
		StrCpy "$INSTDIR" "$PROGRAMFILES64\${PROJECT_NAME}"
	${Else}
		StrCpy "$INSTDIR" "$PROGRAMFILES\${PROJECT_NAME}"
	${EndIf}

	; Get install folder from registry for updates
	ReadRegStr $1 HKLM "${REG_KEY_SOFTWARE}" ""
	IfErrors readRegInstLocDone
	${If} $1 != ""
		StrCpy $INSTDIR $1
	${EndIf}

	readRegInstLocDone:
	ClearErrors

	InitPluginsDir

	StrCpy $CopyLeft "(${U+2184}) ${BUILD_YEAR} ${PRODUCT_NAME} ${PRODUCT_VERSION}                                               Nullsoft Install System ${NSIS_VERSION}"
	${If} ${AtLeastWinVista}
	${AndIf} ${AtMostWin7}
		StrCpy $CopyLeft "(${U+2184}) ${BUILD_YEAR} ${PRODUCT_NAME} ${PRODUCT_VERSION}"
	${EndIf}

	BringToFront ; http://nsis.sourceforge.net/Allow_only_one_installer_instance
	!ifndef NSIS_PTR_SIZE & SYSTYPE_PTR
		!define SYSTYPE_PTR i ; NSIS v2.4x
	!else
		!define /ifndef SYSTYPE_PTR p ; NSIS v3.0+
	!endif
	System::Call 'kernel32::CreateMutex(${SYSTYPE_PTR}0, i1, t"${INSTALLERMUTEXNAME}")?e'
	Pop $0
	IntCmpU $0 183 0 launch launch ; ERROR_ALREADY_EXISTS
		StrLen $0 "$(^SetupCaption)"
		IntOp $0 $0 + 1 ; GetWindowText count includes \0
		StrCpy $1 "" ; Start FindWindow with NULL
		loop:
			FindWindow $1 "#32770" "" "" $1
			StrCmp 0 $1 notfound
			System::Call 'user32::GetWindowText(${SYSTYPE_PTR}r1, t.r2, ir0)'
			StrCmp $2 "$(^SetupCaption)" 0 loop
			SendMessage $1 0x112 0xF120 0 /TIMEOUT=2000 ; WM_SYSCOMMAND:SC_RESTORE to restore the window if it is minimized
			System::Call "user32::SetForegroundWindow(${SYSTYPE_PTR}r1)"
		notfound:
			Abort
	launch:

	SectionSetFlags ${sectionServer} 17 ; ${SF_SELECTED} | ${SF_RO}
	${If} ${RunningX64}
		SectionSetFlags ${sec32Bit} 0
		SectionSetFlags ${sec64Bit} ${SF_SELECTED}
	${Else}
		SectionSetFlags ${sec32Bit} ${SF_SELECTED}
		SectionSetFlags ${sec64Bit} 0
	${EndIf}

	${IfNot} ${AtLeastWinXP}
		MessageBox MB_OK|MB_ICONEXCLAMATION $(TooLowVersion)
		Quit
	${EndIf}
	${If} ${IsWinXP}
		${If} ${RunningX64}
			${IfNot} ${AtLeastServicePack} 2
				MessageBox MB_OK|MB_ICONEXCLAMATION $(TooLowSP64)
				Quit
			${EndIf}
		${Else}
			${IfNot} ${AtLeastServicePack} 3
				MessageBox MB_OK|MB_ICONEXCLAMATION $(TooLowSP)
				Quit
			${EndIf}
		${EndIf}
	${EndIf}

	${If} ${IsWinXP}
		StrCpy $XP "1"
		SectionSetFlags ${secXP} ${SF_SELECTED}
	${Else}
		SectionSetFlags ${secXP} 0
	${EndIf}

	!insertmacro MUI_LANGDLL_DISPLAY

	; Get the amount of total physical memory
	; https://nsis-dev.github.io/NSIS-Forums/html/t-242501.html
	; https://msdn.microsoft.com/fr-fr/library/windows/desktop/aa366589(v=vs.85).aspx
	System::Alloc 64
	Pop $1
	System::Call "*$1(i64)"
	System::Call "Kernel32::GlobalMemoryStatusEx(i r1)"
	System::Call "*$1(i.r2, i.r3, l.r4, l.r5, l.r6, l.r7, l.r8, l.r9, l.r10)"
	System::Free $1
	System::Int64Op $4 / 1048576 ; convert from bytes to Mbytes
	Pop $4

	; Choose the maximum Java memory heap size
	${If} $4 > 4000
		SectionSetFlags ${sectionHeapSize_3} ${SF_SELECTED}
		StrCpy $R4 ${sectionHeapSize_3}
	${Else}
		SectionSetFlags ${sectionHeapSize_2} ${SF_SELECTED}
		StrCpy $R4 ${sectionHeapSize_2}
	${EndIf}
	StrCpy $RAM $4

	${LocateJava}

	; If no installation is found, Java is to be downloaded. Assume that Java bitness = OS bitness
	${If} $JavaLocation == ""
		${If} ${RunningX64}
			StrCpy $JavaBitness "64"
		${Else}
			StrCpy $JavaBitness "32"
		${EndIf}
	${EndIf}

	${If} $JavaBitness == "64"
		IntCmpU $RAM 6000 +2 0 +2
		SectionSetText ${sectionHeapSize_5} ""
		IntCmpU $RAM 8000 +2 0 +2
		SectionSetText ${sectionHeapSize_6} ""
	${Else}
		SectionSetText ${sectionHeapSize_6} ""
		SectionSetText ${sectionHeapSize_5} ""
	${EndIf}

	${If} ${RunningX64}
		ReadRegStr $0 HKLM "SOFTWARE\Wow6432Node\Microsoft\Windows\CurrentVersion\Uninstall\AviSynth" "DisplayVersion"
	${Else}
		ReadRegStr $0 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\AviSynth" "DisplayVersion"
	${EndIf}
	${If} $0 S== "2.6.0 MT"
		SectionSetText ${sectionInstallAviSynth} ""
	${EndIf}

	SectionSetFlags ${sectionFirewall} 1

	${If} $JavaLocation == ""
		SectionSetFlags ${sectionInstallJava} 1
	${EndIf}

	SetOutPath "$PLUGINSDIR\Header"
	SetOverwrite on
	File /nonfatal "Images\Header@192.bmp"
	File /nonfatal "Images\HeaderRTL@192.bmp"
	File /nonfatal "Images\Header@144.bmp"
	File /nonfatal "Images\HeaderRTL@144.bmp"
	File /nonfatal "Images\Header@120.bmp"
	File /nonfatal "Images\HeaderRTL@120.bmp"
	File /nonfatal "Images\Header@96.bmp"
	File /nonfatal "Images\HeaderRTL@96.bmp"
	SetOutPath "$PLUGINSDIR\Wizard"
	File /nonfatal "Images\Installer@192.bmp"
	File /nonfatal "Images\Installer@144.bmp"
	File /nonfatal "Images\Installer@120.bmp"
	File /nonfatal "Images\Installer@96.bmp"

	SectionSetFlags ${sectionEstimatedSize} ${SF_SELECTED}
FunctionEnd

Function onGUIInit
	Aero::Apply ; Apply Aero if available
FunctionEnd

Function LockedListShow
	StrCmp $R9 0 +2 ; Skip the page if clicking Back from the next page.
		Abort
	!insertmacro MUI_HEADER_TEXT $(LockedTitle) $(LockedSubtitle)

	LockedList::AddModule "$INSTDIR\${PROJECT_NAME_SHORT}.exe"
	LockedList::AddModule "$INSTDIR\win32\MediaInfo.dll"
	${If} ${RunningX64}
		File /oname=$PLUGINSDIR\LockedList64.dll `${NSISDIR}\Plugins\x64-unicode\LockedList64.dll`
		LockedList::AddModule "$INSTDIR\win32\MediaInfo64.dll"
	${EndIf}

	LockedList::Dialog /autonext /heading $(LockedHeading) /colheadings $(LockedApplication) $(LockedProcess) /noprograms $(LockedNoProcesses) /searching $(LockedSearching) /endsearch $(LockedEndSearch) /endmonitor $(LockedEndMonitor) /menuitems $(LockedClose) $(LockedCopy) /autoclose $(LockedAutoClose) $(LockedAutoTerminate) $(LockedTerminateFailed) $(LockedProceed)
	Pop $R0
FunctionEnd

Function DirectoryLeave
	StrCpy $R9 0
FunctionEnd

Function LockedListLeave
	StrCpy $R9 1
FunctionEnd

Function un.LockedListShow
	!insertmacro MUI_HEADER_TEXT $(LockedTitle) $(LockedSubtitle)

	LockedList::AddModule "$INSTDIR\${PROJECT_NAME_SHORT}.exe"
	LockedList::AddModule "$INSTDIR\win32\MediaInfo.dll"
	${If} ${RunningX64}
		File /oname=$PLUGINSDIR\LockedList64.dll `${NSISDIR}\Plugins\x64-unicode\LockedList64.dll`
		LockedList::AddModule "$INSTDIR\win32\MediaInfo64.dll"
	${EndIf}

	LockedList::Dialog /autonext /heading $(LockedHeading) /colheadings $(LockedApplication) $(LockedProcess) /noprograms $(LockedNoProcesses) /searching $(LockedSearching) /endsearch $(LockedEndSearch) /endmonitor $(LockedEndMonitor) /menuitems $(LockedClose) $(LockedCopy) /autoclose $(LockedAutoClose) $(LockedAutoTerminate) $(LockedTerminateFailed) $(LockedProceed)
	Pop $R0
FunctionEnd

Function hideRequiredSize
	SectionGetFlags ${sectionInstallJava} $1
	${If} $JavaLocation == ""
	${AndIf} $1 != 0
		FindWindow $1 "#32770" "" $HWNDPARENT
		GetDlgItem $1 $1 1023
		ShowWindow $1 ${SW_HIDE}
	${EndIf}
FunctionEnd

Function .onGUIEnd
	LogSet off
	RMDir /r /REBOOTOK $PLUGINSDIR
FunctionEnd

Function windowsResizing
	FindWindow $mui.ComponentsPage "#32770" "" $HWNDPARENT
	System::Call "*(i 0, i 0, i 4, i 8) i .r1"
	System::Call "User32::MapDialogRect(i $mui.ComponentsPage, i r1) i .r2"
	System::Call "*$1(i .r2, i.r3, i.r4, i.r5)"
	System::Free $1
	GetDlgItem $mui.ComponentsPage.Text $mui.ComponentsPage 1006
	ShowWindow $mui.ComponentsPage.Text ${SW_HIDE}
	GetDlgItem $mui.ComponentsPage.InstTypesText $mui.ComponentsPage 1021
	ShowWindow $mui.ComponentsPage.InstTypesText ${SW_HIDE}
	GetDlgItem $mui.ComponentsPage.InstTypes $mui.ComponentsPage 1017
	ShowWindow $mui.ComponentsPage.InstTypes ${SW_HIDE}
	GetDlgItem $mui.ComponentsPage.ComponentsText $mui.ComponentsPage 1022
	GetDlgItem $mui.ComponentsPage.SpaceRequired $mui.ComponentsPage 1023
	GetDlgItem $mui.ComponentsPage.Components $mui.ComponentsPage 1032
	GetDlgItem $mui.ComponentsPage.DescriptionTitle $mui.ComponentsPage 1042
	GetDlgItem $mui.ComponentsPage.DescriptionText $mui.ComponentsPage 1043
	!insertmacro WindowSize 0 4 95 65
	System::Call "User32::SetWindowPos(i $mui.ComponentsPage.ComponentsText, i 0, i $0, i $1, i $2, i $3, i 0x0040)" ; 1022
	!insertmacro WindowSize 0 90 95 28
	System::Call "User32::SetWindowPos(i $mui.ComponentsPage.SpaceRequired, i 0, i $0, i $1, i $2, i $3, i 0x0040)" ; 1023
	!insertmacro WindowSize 102 0 195 85
	System::Call "User32::SetWindowPos(i $mui.ComponentsPage.Components, i 0, i $0, i $1, i $2, i $3, i 0x0040)" ; 1032
	!insertmacro WindowSize 102 85 195 50
	System::Call "User32::SetWindowPos(i $mui.ComponentsPage.DescriptionTitle, i 0, i $0, i $1, i $2, i $3, i 0x0040)" ; 1042
	!insertmacro WindowSize 108 97 183 33
	System::Call "User32::SetWindowPos(i $mui.ComponentsPage.DescriptionText, i 0, i $0, i $1, i $2, i $3, i 0x0040)" ; 1043
FunctionEnd

Function showHiDPI
	SysCompImg::GetSysDpi ; http://forums.winamp.com/showthread.php?t=443754
	${If} $0 > 144
	StrCpy $R6 "Header@192.bmp"
	StrCpy $R7 "Installer@192.bmp"
	${ElseIf} $0 > 120
	StrCpy $R6 "Header@144.bmp"
	StrCpy $R7 "Installer@144.bmp"
	${ElseIf} $0 > 96
	StrCpy $R6 "Header@120.bmp"
	StrCpy $R7 "Installer@120.bmp"
	${Else}
	StrCpy $R6 "Header@96.bmp"
	StrCpy $R7 "Installer@96.bmp"
	${EndIf}
	StrCmp "$(^RTL)" "1" 0 header
	${WordReplace} "$R6" "@" "RTL@" "+1" $R6
	header: SysCompImg::SetCustom "$PLUGINSDIR\Header\$R6" ; SetClassic, SetFlat, SetThemed
	SysCompImg::SetCustom "$PLUGINSDIR\Wizard\$R7"
	${NSD_SetStretchedImage} $mui.WelcomePage.Image "$PLUGINSDIR\Wizard\$R7" $mui.WelcomePage.Image.Bitmap
	${NSD_SetStretchedImage} $mui.FinishPage.Image "$PLUGINSDIR\Wizard\$R7" $mui.FinishPage.Image.Bitmap
	SetBrandingImage /IMGID=1046 /RESIZETOFIT "$PLUGINSDIR\Header\$R6"
FunctionEnd

Section /o "-un.RemoveDataAndSettings" sectionRemoveDataAndSettings
SectionEnd

Section "un.${PROJECT_NAME}" sectionUnStandard
	SectionIn RO
	SetShellVarContext all
	ReadEnvStr $R0 "ALLUSERSPROFILE"
	SectionGetFlags ${sectionRemoveDataAndSettings} $R1
	SetOutPath $TEMP ; Make sure $InstDir is not the current folder so we can remove it

	Delete /REBOOTOK "$INSTDIR\uninst.exe"
	RMDir /r /REBOOTOK "$INSTDIR\documentation"
	RMDir /r /REBOOTOK "$INSTDIR\web"
	RMDir /r /REBOOTOK "$INSTDIR\win32"
	RMDir /r /REBOOTOK "$INSTDIR\renderers"

	Delete /REBOOTOK "$INSTDIR\${PROJECT_NAME_SHORT}.exe"
	Delete /REBOOTOK "$INSTDIR\${PROJECT_NAME_SHORT}.bat"
	Delete /REBOOTOK "$INSTDIR\${PROJECT_ARTIFACT_ID}.jar"
	Delete /REBOOTOK "$INSTDIR\${PROJECT_ARTIFACT_ID}.conf"
	Delete /REBOOTOK "$INSTDIR\${PROJECT_ARTIFACT_ID}.ico"
	Delete /REBOOTOK "$INSTDIR\CHANGELOG.txt"
	Delete /REBOOTOK "$INSTDIR\WEB.conf"
	Delete /REBOOTOK "$INSTDIR\EULA.rtf"
	Delete /REBOOTOK "$INSTDIR\README.md"
	Delete /REBOOTOK "$INSTDIR\README.txt"
	Delete /REBOOTOK "$INSTDIR\LICENSE.txt"
	Delete /REBOOTOK "$INSTDIR\debug.log"
	Delete /REBOOTOK "$INSTDIR\debug.log.prev"
	Delete /REBOOTOK "$INSTDIR\logback.xml"
	Delete /REBOOTOK "$INSTDIR\logback.headless.xml"
	Delete /REBOOTOK "$INSTDIR\icon.ico"
	Delete /REBOOTOK "$INSTDIR\DummyInput.ass"
	Delete /REBOOTOK "$INSTDIR\DummyInput.jpg"
	Delete /REBOOTOK "$INSTDIR\VirtualFolders.conf"
	Delete /REBOOTOK "$INSTDIR\install.log"
	Delete /REBOOTOK "$INSTDIR\${UninstallEXE}"
	RMDir /REBOOTOK "$INSTDIR"

	Delete /REBOOTOK "$DESKTOP\${PROJECT_NAME}.lnk"
	RMDir /r /REBOOTOK "$SMPROGRAMS\${PROJECT_NAME}"

	StrCmp "$R1" "1" removeDataAndSettings

	goto serviceRunningTest ; Data and settings must not be deleted "by accident", so it's skipped unless explicitly called

	removeDataAndSettings:
		RMDir /r /REBOOTOK "$R0\${PROJECT_NAME_CAMEL}"
		RMDir /r /REBOOTOK "$TEMP\fontconfig"
		RMDir /r /REBOOTOK "$LOCALAPPDATA\fontconfig"
		RMDir /r /REBOOTOK "$INSTDIR"
		DeleteRegKey HKLM "${REG_KEY_SOFTWARE}"

	serviceRunningTest:
		!insertmacro SERVICE "running" "${PROJECT_NAME}" ""
		Pop $0
		StrCmpS $0 "false" Done

	ServiceStop:
		!insertmacro SERVICE "stop" "${PROJECT_NAME}" ""
		Pop $0
		StrCmpS $0 "false" 0 ServiceDelete
		MessageBox MB_ABORTRETRYIGNORE|MB_ICONEXCLAMATION $(ServiceStopError) IDIGNORE ServiceDelete IDRETRY ServiceStop
		Abort

	ServiceDelete:
		!insertmacro SERVICE "delete" "${PROJECT_NAME}" ""
		Pop $0
		StrCmpS $0 "false" 0 Done
		MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP $(ServiceUninstallError) IDIGNORE Done IDRETRY ServiceDelete
		Abort

	Done:

	ClearErrors
	ReadRegStr $0 HKLM "${REG_KEY_UNINSTALL}" "FirewallRules"
	IfErrors skipFirewall
	StrCmp $0 "1" 0 skipFirewall
	${If} ${IsWinXP}
	${OrIf} ${IsWin2003}
		nsExec::Exec 'netsh firewall delete portopening protocol=tcp port=5252 profile=standard'
		nsExec::Exec 'netsh firewall delete portopening protocol=tcp port=6363 profile=standard'
		nsExec::Exec 'netsh firewall delete portopening protocol=tcp port=5252 profile=domain'
		nsExec::Exec 'netsh firewall delete portopening protocol=tcp port=6363 profile=domain'
		nsExec::Exec 'netsh firewall delete portopening protocol=all port=1900 profile=standard'
		nsExec::Exec 'netsh firewall delete portopening protocol=all port=1900 profile=domain'
	${ElseIf} ${AtLeastWinVista}
		nsExec::Exec 'netsh advfirewall firewall delete rule name="Digital Media Server - Incoming TCP port 1900/5252/6363"'
		nsExec::Exec 'netsh advfirewall firewall delete rule name="Digital Media Server - Incoming UDP port 1900"'
	${EndIf}

	skipFirewall:
	ClearErrors
	DeleteRegKey HKLM "${REG_KEY_UNINSTALL}"

SectionEnd

Section /o "un.$(SectionUninstallComplete)" sectionUnComplete
SectionEnd

Function un.onInit
	StrCpy $Clean 0 ; Initialize "complete uninstall" status
	!insertmacro MUI_UNGETLANGUAGE

	${If} ${RunningX64}
		SetRegView 64
	${EndIf}
FunctionEnd

!insertmacro MUI_UNFUNCTION_DESCRIPTION_BEGIN
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionUnStandard} $(SectionDescriptionStandardUninstall)
	!insertmacro MUI_DESCRIPTION_TEXT ${sectionUnComplete} $(SectionDescriptionCompleteUninstall)
!insertmacro MUI_UNFUNCTION_DESCRIPTION_END

Function un.showHiDPI
	SetOutPath "$PLUGINSDIR\Header"
	SetOverwrite on
	File /nonfatal "Images\Header@192.bmp"
	File /nonfatal "Images\HeaderRTL@192.bmp"
	File /nonfatal "Images\Header@144.bmp"
	File /nonfatal "Images\HeaderRTL@144.bmp"
	File /nonfatal "Images\Header@120.bmp"
	File /nonfatal "Images\HeaderRTL@120.bmp"
	File /nonfatal "Images\Header@96.bmp"
	File /nonfatal "Images\HeaderRTL@96.bmp"
	SetOutPath "$PLUGINSDIR\Wizard"
	File /nonfatal "Images\Uninstaller@192.bmp"
	File /nonfatal "Images\Uninstaller@144.bmp"
	File /nonfatal "Images\Uninstaller@120.bmp"
	File /nonfatal "Images\Uninstaller@96.bmp"

	SysCompImg::GetSysDpi ; http://forums.winamp.com/showthread.php?t=443754
	${If} $0 > 144
		StrCpy $R6 "Header@192.bmp"
		StrCpy $R7 "Uninstaller@192.bmp"
	${ElseIf} $0 > 120
		StrCpy $R6 "Header@144.bmp"
		StrCpy $R7 "Uninstaller@144.bmp"
	${ElseIf} $0 > 96
		StrCpy $R6 "Header@120.bmp"
		StrCpy $R7 "Uninstaller@120.bmp"
	${Else}
		StrCpy $R6 "Header@96.bmp"
		StrCpy $R7 "Uninstaller@96.bmp"
	${EndIf}
	StrCmp "$(^RTL)" "1" 0 header
	${WordReplace} "$R6" "@" "RTL@" "+1" $R6
	header: SysCompImg::SetCustom "$PLUGINSDIR\Header\$R6" ; SetClassic, SetFlat, SetThemed
	SysCompImg::SetCustom "$PLUGINSDIR\Wizard\$R7"
	${NSD_SetStretchedImage} $mui.WelcomePage.Image "$PLUGINSDIR\Wizard\$R7" $mui.WelcomePage.Image.Bitmap
	${NSD_SetStretchedImage} $mui.FinishPage.Image "$PLUGINSDIR\Wizard\$R7" $mui.FinishPage.Image.Bitmap
	SetBrandingImage /IMGID=1046 /RESIZETOFIT "$PLUGINSDIR\Header\$R6"
	SetOutPath $TEMP
FunctionEnd

Function un.onSelChange
	SectionGetFlags ${sectionUnComplete} $1
	${If} $1 != $Clean
		${If} $1 != 0
			MessageBox MB_ICONEXCLAMATION|MB_YESNO|MB_DEFBUTTON2 $(CleanInstallWarning) IDYES +3
			SectionSetFlags ${sectionUnComplete} 0
			StrCpy $1 0
		${EndIf}
		SectionSetFlags ${sectionRemoveDataAndSettings} $1
		StrCpy $Clean $1
	${EndIf}
FunctionEnd

Function un.hideRequiredSize
	FindWindow $1 "#32770" "" $HWNDPARENT
	GetDlgItem $1 $1 1023
	ShowWindow $1 ${SW_HIDE}
FunctionEnd
