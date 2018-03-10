﻿; Java Launcher with automatic JRE installation
; Use javaw.exe to avoid DOS box or java.exe to keep stdout/stderr
;------------------------------------------------------------------

Unicode "true"
SetCompressor lzma
RequestExecutionLevel user
SilentInstall silent
XPStyle on
AutoCloseWindow true
ShowInstDetails nevershow

; Include the project header file generated by the nsis-maven-plugin

!include "..\..\..\..\target\project.nsh"
!include "${PROJECT_BUILD_DIR}\extra.nsh"
!include "FileFunc.nsh"
!include "LogicLib.nsh"
!include "SearchJava.nsh"
!include "WinVer.nsh"
!include "WordFunc.nsh"
!include "x64.nsh"

Name "${PROJECT_NAME_SHORT}"
Caption "${PROJECT_NAME}"
Icon "${PROJECT_BASEDIR}\src\main\resources\images\logo.ico"

VIAddVersionKey "ProductName" "${PROJECT_NAME}"
VIAddVersionKey "Comments" "Media server"
VIAddVersionKey "CompanyName" "${PROJECT_ORGANIZATION_NAME}"
VIAddVersionKey "LegalTrademarks" ""
VIAddVersionKey "LegalCopyright" ""
VIAddVersionKey "FileDescription" "${PROJECT_NAME}"
VIAddVersionKey "FileVersion" "${PROJECT_VERSION}"
VIProductVersion "${PROJECT_VERSION_SHORT}.0"

Section
	javaLocation:
		${SearchJava}
		StrCmp $DownloadJava "1" 0 no
	${If} ${AtLeastWinVista}
		inetc::get /NOCANCEL /CONNECTTIMEOUT 30 /SILENT /WEAKSECURITY /NOCOOKIES /TOSTACK "https://lv.binarybabel.org/catalog-api/java/jdk8.txt?p=downloads.exe" "" /END
		Pop $1
		Pop $0
		${WordReplaceS} "$0" "download" "edelivery" "+1" $0
		${WordReplaceS} "$0" "jdk-" "jre-" "+1" $0
		${IfNot} ${RunningX64}
			${WordReplaceS} "$0" "-x64" "-i586" "+1" $0
		${EndIf}
		${WordFind} $0 "/" "-1}" $1
	${EndIf}
	${If} ${IsWinXP}
	${AndIfNot} ${RunningX64}
		; jre-7u80-windows-i586.exe
		; http://javadl.sun.com/webapps/download/AutoDL?BundleId=106307
		StrCpy $0 "javadl.oracle.com/webapps/download/AutoDL?BundleId=227550_e758a0de34e24606bca991d704f6dcbf"
		StrCpy $1 "jre-8u151-windows-i586.exe"
	${EndIf}
	${If} ${IsWinXP}
	${AndIf} ${RunningX64}
		; jre-7u80-windows-x64.exe
		; http://javadl.sun.com/webapps/download/AutoDL?BundleId=106309
		StrCpy $0 "javadl.oracle.com/webapps/download/AutoDL?BundleId=227552_e758a0de34e24606bca991d704f6dcbf"
		StrCpy $1 "jre-8u151-windows-x64.exe"
	${EndIf}
	System::Call 'ole32::CoCreateGuid(g .s)'
	Pop $2
	StrCpy $2 "$TEMP\$2"
	CreateDirectory "$2"
	inetc::get /WEAKSECURITY /RESUME "" /CONNECTTIMEOUT 30 /POPUP "$1" /CAPTION "Official Oracle Java 8" /QUESTION "" /USERAGENT "Mozilla/5.0 (Windows NT 6.3; rv:48.0) Gecko/20100101 Firefox/48.0" /HEADER "Cookie: oraclelicense=accept-securebackup-cookie" /NOCOOKIES "$0" "$2\$1" /END
	; /TRANSLATE $(downloading) $(downloadconnecting) $(downloadsecond) $(downloadminute) $(downloadhour) $(downloadplural) "%dkB (%d%%) of %dkB @ %d.%01dkB/s" " (%d %s%s $(downloadremaining))"
	Pop $0
	StrCmpS $0 "OK" +3
	MessageBox MB_ICONSTOP "HTTP download error ($0).$\r$\n$\r$\nVerify your firewall configuration and your Internet connection, or download Java manually.$\r$\n$\r$\n"
	Quit

	ExecWait "$2\$1" ; '"$2\$1 /s /v$\"/qn ADDLOCAL=ALL REBOOT=Suppress /L C:\setup.log$\""'
	RMDir /REBOOTOK $2\$1

	StrCmp $DownloadJava "1" javaLocation

	no:
		ReadRegStr $R3 HKCU "SOFTWARE\${PROJECT_NAME}" "HeapMem"
		StrCmp $R3 "" 0 +2
		StrCpy $R3 "768M"
		StrCpy $R3 "-Xmx$R3"

		; Get the command line parameters
		${GetParameters} $1

		SetOutPath $EXEDIR
		Exec '"$JavaLocation" -classpath update.jar;${PROJECT_ARTIFACT_ID}.jar $R3 -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 net.pms.PMS $1'
SectionEnd

Function .onInit
	${If} ${RunningX64}
		SetRegView 64
	${EndIf}

	${IfNot} ${AtLeastWinXP}
		MessageBox MB_ICONSTOP "Windows XP and above is required"
		Quit
	${EndIf}
FunctionEnd
