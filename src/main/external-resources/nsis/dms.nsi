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
!include "WinVer.nsh"
!include "WordFunc.nsh"
!include "x64.nsh"
!include "StrStrip.nsh"

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

Var JavaLocation
Var JavaVersion

Section	
	StrCpy $JavaLocation ""
	StrCpy $JavaVersion ""

	; Get the command line parameters
	${GetParameters} $R1

	${GetOptions} $R1 "/?" $1
	IfErrors 0 usage
	${GetOptions} $R1 "/help" $1
	IfErrors 0 usage
	${GetOptions} $R1 "--help" $1
	IfErrors usagedone usage

	usage:
	; --help and --version is omitted because stdout is "trapped" by NSIS's Exec and never reaches the command line 
	MessageBox MB_ICONINFORMATION "Usage: ${PROJECT_NAME_SHORT}.exe [-P | -v | -c | -C | -s] [-db <db option>] [-p=profile path] [/help] [/javadebug] $\r$\n$\r$\n\
		Options:\
		$\r$\n  /help, --help - Display this help and exit.\
		$\r$\n  /javadebug, --javadebug - Display debug messages during the Java installation selection process.\
		$\r$\n  -p, --profile=PROFILE_PATH - Use the configuration in PROFILE_PATH.\
		$\r$\n  -P, --profiles - Show the profile selection dialog during startup, ignored if running headless or if \
		                         a profile is specified.\
		$\r$\n  -v, --trace - Force logging level to TRACE.\
		$\r$\n  -c, --headless - Run without GUI (Must be terminated from task manager).\
		$\r$\n  -C, --noconsole - Fail if a GUI can't be created.\
		$\r$\n  -s, --scrollbars - Force horizontal and vertical GUI scroll bars.\
		$\r$\n  -db, --database - Combine with one of the options below\
		$\r$\n    log, trace - Enable database logging.\
		$\r$\n    downgrade - Delete and recreate any database tables of a newer version. The data in the \
		                      incompatible tables will be lost.\
		$\r$\n    backup[=NAME] - Copy the database before downgrading it if any database tables are of a newer \
		                          version. If a name for the backup isn't provided, one will be generated.\
		$\r$\n    rename[=NAME] - Rename the database and create a new, empty database if there is a problem with \
		                          the current database. If a name isn't provided, one will be generated."
	Quit

	usagedone:
	ClearErrors

	; Push plugin parameters: /RETVERSION /MAXVER "<9" /OPTVER "8" /END
	Push "/END"
	Push "8"
	Push "/OPTVER"
	Push "<9"
	Push "/MAXVER"
	Push "/RETVERSION"

	StrCpy $R2 ""
	${GetOptions} $R1 "/javadebug" $1
	IfErrors 0 dialogdebug
	${GetOptions} $R1 "--javadebug" $1
	IfErrors dialogdebugdone dialogdebug

	dialogdebug:
	StrCpy $R2 "1"
	Push "DEBUG"
	Push "/LOGLEVEL"
	Push "/DIALOGDEBUG"
	${StrStrip} " /javadebug" $R1 $R1
	${StrStrip} "/javadebug " $R1 $R1
	${StrStrip} "/javadebug" $R1 $R1
	${StrStrip} " --javadebug" $R1 $R1
	${StrStrip} "--javadebug " $R1 $R1
	${StrStrip} "--javadebug" $R1 $R1

	dialogdebugdone:
	ClearErrors

	NsJavaLocator::Locate
	Pop $JavaLocation
	Pop $JavaVersion

	${If} $JavaLocation == ""
		MessageBox MB_ICONSTOP "No suitable Java installation found. Please make sure that either Java 7 or 8 is installed."
		Quit
	${EndIf}
	${If} $R2 == "1"
		MessageBox MB_ICONINFORMATION "Using Java installation: $JavaLocation"
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

	SetOutPath $EXEDIR
	${If} $JavaVersion == "7"
		Exec '"$JavaLocation" -classpath update.jar;${PROJECT_ARTIFACT_ID}.jar $R3 -Dhttps.protocols=TLSv1.2 -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 net.pms.PMS $R1'
	${Else}
		Exec '"$JavaLocation" -classpath update.jar;${PROJECT_ARTIFACT_ID}.jar $R3 -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 net.pms.PMS $R1'
	${EndIf}
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
