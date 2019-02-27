; Java Launcher
; Use javaw.exe to avoid DOS box or java.exe to keep stdout/stderr
;------------------------------------------------------------------

Unicode "true"
RequestExecutionLevel user
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
CRCCheck force

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

!define LastIsDigit "!insertmacro LastIsDigit"
!macro LastIsDigit ResultVar String
	Push "${String}"
	Call _LastIsDigit
	Pop "${ResultVar}"
!macroend

Section
	${SearchJava}

	${If} $DownloadJava == 1
		MessageBox MB_ICONSTOP "No suitable Java installation found. Please make sure that either Java 7 or 8 is installed."
		Quit
	${EndIf}

	ReadRegStr $R3 HKCU "SOFTWARE\${PROJECT_NAME}" "HeapMem"
	StrCmp $R3 "" default 0
	${LastIsDigit} $3 $R3
	${If} $3 > 0
		StrCpy $R3 "$R3M"
	${EndIf}
	goto memdone

	default:
	StrCpy $R3 "768M"

	memdone:
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
		MessageBox MB_ICONSTOP "Windows XP or above is required"
		Quit
	${EndIf}
FunctionEnd

Function _LastIsDigit
	Exch $0
	Push $1
	Push $2
	Push $3
	Push $4

	StrCpy $0 $0 1 -1
	StrCpy $1 "0123456789"
	StrLen $2 $1
	StrCpy $3 0
	loop:
		StrCpy $4 $1 1 $3
		StrCmp $0 $4 is_number
		IntOp $3 $3 + 1
		IntCmp $3 $2 loop loop isnt_number
	is_number:
		StrCpy $0 1
		goto done
	isnt_number:
		StrCpy $0 0
	done:

	Pop $4
	Pop $3
	Pop $2
	Pop $1
	Exch $0
FunctionEnd
