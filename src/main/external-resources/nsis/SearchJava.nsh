; ======================================================================================
;
; Find the Java version 7 and above installed on a system
; https://docs.oracle.com/javase/9/install/installation-jdk-and-jre-microsoft-windows-platforms.htm#JSJIG-GUID-C11500A9-252C-46FE-BB17-FC5A9528EAEB
; Created by Sami32
; ======================================================================================

!include "FileFunc.nsh"
!include "WordFunc.nsh"
!include "x64.nsh"

Var DownloadJava
Var FileVersion
Var Java64bit
Var JavaLocation

!macro _SearchJava
	Push $R9
	Push $R8
	Push $R7
	Push $R6
	Push $R5
	Push $R4
	Push $R3
	Push $R2
	Push $R1
	Push $R0
	Push $9
	Push $8
	Push $7
	Push $6
	Push $5
	Push $4
	Push $3
	Push $2
	Push $1
	Push $0
	Call findJava
	Pop $0
	Pop $1
	Pop $2
	Pop $3
	Pop $4
	Pop $5
	Pop $6
	Pop $7
	Pop $8
	Pop $9
	Pop $R0
	Pop $R1
	Pop $R2
	Pop $R3
	Pop $R4
	Pop $R5
	Pop $R6
	Pop $R7
	Pop $R8
	Pop $R9
!macroend

!define SearchJava "!insertmacro _SearchJava"

Function findJava
	StrCpy $2 "SOFTWARE\JavaSoft\Java Runtime Environment"
	StrCpy $5 0
	StrCpy $R7 ""
	StrCpy $R8 ""
	StrCpy $R9 ""
	StrCpy $DownloadJava ""

	keyLoop:
		StrCpy $0 0
		StrCpy $7 0
		StrCpy $8 0
		StrCpy $9 0
		StrCpy $R1 0
		StrCpy $R2 0
		StrCpy $R3 0
		${Do}
			EnumRegKey $1 HKLM "$2" $0
			StrCmp $1 "" 0 +2
			${Break}
			IntOp $0 $0 + 1

			${WordFind} "$1" "1.8.0_" "*" $3
			StrCmp $3 $1 seven
			${WordFind} "$1" "_" "+1}" $8
			IntCmpU $8 $R1 seven seven
			ReadRegStr $R8 HKLM "$2\$1" "JavaHome"
			StrCpy $R1 $8

			seven:
				${WordFind} "$1" "1.7.0_" "*" $3
				StrCmp $3 $1 nine
				${WordFind} "$1" "_" "+1}" $7
				IntCmpU $7 $R2 nine nine
				ReadRegStr $R7 HKLM "$2\$1" "JavaHome"
				StrCpy $R2 $7

			nine:
				${For} $4 9 15
					${WordFind} "$1" "$4${U+002E}0." "*" $3
					StrCmp $3 $1 nextLoop
					${WordFind} "$1" ".0." "+1}" $R3
					IntCmpU $R3 $9 nextLoop nextLoop
					ReadRegStr $R9 HKLM "$2\$4" "JavaHome"
					StrCpy $9 $R3
					nextLoop:
				${Next}
		${LoopWhile} $1 != ""
		${If} $R8 != ""
		${OrIf} $R7 != ""
			Goto end
		${EndIf}
		IntOp $5 $5 + 1
		${IfNot} ${RunningX64}
			IntCmpU $5 4 pass2 0 pass2
		${EndIf}
		${If} $5 == 1
			StrCpy $2 "SOFTWARE\JavaSoft\JRE"
			Goto keyLoop
		${EndIf}
		${If} $5 == 2
			StrCpy $2 "SOFTWARE\JavaSoft\Java Development Kit"
			Goto keyLoop
		${EndIf}
		${If} $5 == 3
			StrCpy $2 "SOFTWARE\JavaSoft\JDK"
			Goto keyLoop
		${EndIf}
		${If} ${RunningX64}
		${AndIf} $R8 == ""
			StrCmp $R7 "" 0 end
			StrCmp $5 4 0 +2
			StrCpy $2 "SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment"
			StrCmp $5 5 0 +2
			StrCpy $2 "SOFTWARE\Wow6432Node\JavaSoft\JRE"
			StrCmp $5 6 0 +2
			StrCpy $2 "SOFTWARE\Wow6432Node\JavaSoft\Java Development Kit"
			StrCmp $5 7 0 +2
			StrCpy $2 "SOFTWARE\Wow6432Node\JavaSoft\JDK"
			IntCmpU $5 4 0 +2 0
			StrCpy $Java64bit "32"; 32-bit JVM on a 64-bit OS
			IntCmpU $5 8 0 keyLoop 0
		${EndIf}

	pass2: ; Check the environment variables
		StrCmp $R8 "" 0 end
		StrCmp $R7 "" 0 end
		StrCmp $R9 "" 0 end

		ExpandEnvStrings $4 "%JAVA_HOME%"
		StrCmpS $4 "%JAVA_HOME%" searchInPath
		${Locate} "$4" "/L=F /M=javaw.exe" "JavaHomeParsing"
		IfErrors searchInPath
		StrCmp $JavaLocation "" searchInPath
		Goto done

	searchInPath:
		ClearErrors
		StrCpy $7 ""
		StrCpy $8 ""
		StrCpy $9 ""
		ExpandEnvStrings $4 "%PATH%"
		StrCmpS $4 "%PATH%" nothingFound
		${Do}
			StrCpy $2 $4
			${WordFind} "$2" ";" "+1{" $1
			${WordFind} "$2" ";" "+1}" $4
			${WordFind} "$1" "ProgramData\Oracle\Java\javapath" "*" $5
			StrCmp $1 $5 next
			${WordFind} "$1" "\system32" "*" $5
			StrCmp $1 $5 next
			${Locate} "$1" "/L=F /G=0 /M=javaw.exe" "JavaInPath"

	next:
		${LoopWhile} $4 != $2
		${If} $R4 != ""
			StrCmp $Java64bit "32" +3
			StrCpy $JavaLocation $R4
			Goto done
		${ElseIf} $R3 != ""
			StrCmp $Java64bit "32" +3
			StrCpy $JavaLocation $R3
			Goto done
		${ElseIf} $R5 != ""
			StrCmp $Java64bit "32" +3
			StrCpy $JavaLocation $R5
			Goto done
		${ElseIf} $R4 != ""
			StrCpy $JavaLocation $R4
			Goto done
		${ElseIf} $R3 != ""
			StrCpy $JavaLocation $R3
			Goto done
		${ElseIf} $R5 != ""
			StrCpy $JavaLocation $R5
			Goto done
		${Else}
			StrCpy $DownloadJava "1"
			Goto done
		${EndIf}

	end:
		; Final Java selection that will fit best
		StrCmp $JavaLocation "" 0 done
		IntCmpU $5 7 0 jre 0
		${If} $R8 != ""
			StrCpy $R8 "$R8\jre"
		${ElseIf} $R7 != ""
			StrCpy $R7 "$R7\jre"
		${EndIf}

		jre:
			StrCpy $5 "1"
			${If} $R8 != ""
				StrCpy $JavaLocation "$R8\bin\javaw.exe"
				IfFileExists $JavaLocation done
				StrCpy $5 "0"
			${EndIf}
			${If} $5 == "0"
			${OrIf} $R8 == ""
				StrCmp $R7 "" +3
				StrCpy $JavaLocation "$R7\bin\javaw.exe"
				IfFileExists $JavaLocation done
			${EndIf}
			IntCmpU $4 16 nothingFound 0 nothingFound
			StrCpy $JavaLocation "$R9\bin\javaw.exe"
			IfFileExists $JavaLocation done

	nothingFound:
		StrCpy $DownloadJava "1"

	done:
		${If} ${RunningX64}
			StrCmp $Java64bit "32" +2
			StrCpy $Java64bit "64"
		${EndIf}
FunctionEnd

Function JavaInPath
	StrCmp $R9 "" end
	ClearErrors
	${GetFileVersion} $R9 $FileVersion

	${WordFind} "$FileVersion" "8.0." "E+1}" $R0
	IfErrors 0 +3
	StrCpy $R0 ""
	Goto seven
	StrCpy $R4 $R9 ; For Java 8
	Goto end

	seven:
		${WordFind} "$FileVersion" "7.0." "E+1}" $R0
		IfErrors 0 +3
		StrCpy $R0 ""
		Goto nine
		StrCpy $R3 $R9 ; For Java 7
		Goto end

	nine:
		${For} $R1 9 15
			${WordFind} "$FileVersion" "$R1${U+002E}0." "E+1}" $R0
			IfErrors +3
			StrCpy $R5 $R9 ; For Java 9 and above
			${Break}
			StrCpy $R0 ""
		${Next}

	end:
		${If} ${RunningX64}
		${AndIf} $R9 != ""
			StrCmp $R3 "" 0 +5
			StrCmp $R4 "" 0 +4
			StrCmp $R5 "" 0 +3
			StrCpy $Java64bit ""
			Goto none
			${WordFindS} "$R9" "x86" "*" $R0
			StrCmpS $R9 $R0 +2
			StrCpy $Java64bit "32"

			none:
		${EndIf}

		StrCpy $0 StopLocate
		Push $0
FunctionEnd

Function JavaHomeParsing
	StrCmp $R9 "" end
	${GetFileVersion} $R9 $FileVersion

	${If} ${RunningX64}
		${WordFindS} "$R9" "x86" "*" $Java64bit
		StrCmp $R9 $Java64bit +2
		StrCpy $Java64bit "32" ; 32-bit JVM on a 64-bit OS
	${EndIf}

	${WordFind} "$FileVersion" "8.0." "E+1}" $R0
	IfErrors 0 +3
	StrCpy $R0 ""
	Goto seven
	StrCpy $JavaLocation $R9 ; For Java 8
	Goto end

	seven:
		${WordFind} "$FileVersion" "7.0." "E+1}" $R0
		IfErrors 0 +3
		StrCpy $R0 ""
		Goto nine
		StrCpy $JavaLocation $R9 ; For Java 7
		Goto end

	nine:
		${For} $R1 9 15
			${WordFind} "$FileVersion" "$R1${U+002E}0." "E+1}" $R0
			IfErrors +3
			StrCpy $JavaLocation $R9 ; For Java 9 and above
			${Break}
			StrCpy $R0 ""
		${Next}

	end:
		StrCpy $0 StopLocate
		Push $0
FunctionEnd