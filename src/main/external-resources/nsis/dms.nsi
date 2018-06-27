; Java Launcher
; Use javaw.exe to avoid DOS box or java.exe to keep stdout/stderr
;------------------------------------------------------------------

Unicode "true"
RequestExecutionLevel user
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow

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
	${SearchJava}

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
