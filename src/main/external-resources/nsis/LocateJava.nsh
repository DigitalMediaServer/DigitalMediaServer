;Var DownloadJava ; used in both (not really needed..?)
;Var FileVersion
;Var Java64bit ; used in setup
;Var JavaLocation ; used in dms.exe

Var JavaLocation
Var JavaVersion
Var JavaBitness

!macro _LocateJava
	Call doLocate
!macroend

!define LocateJava "!insertmacro _LocateJava"

Function doLocate
	StrCpy $JavaLocation ""
	StrCpy $JavaVersion ""
	StrCpy $JavaBitness ""
	NsJavaLocator::Locate /RETVERSION /RETARCHBITS /MAXVER "<9" /OPTVER "8" /END
	Pop $JavaLocation
	Pop $JavaVersion
	Pop $JavaBitness
FunctionEnd
