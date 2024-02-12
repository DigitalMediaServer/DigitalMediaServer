;Var DownloadJava ; used in both (not really needed..?)
;Var FileVersion
;Var Java64bit ; used in setup
;Var JavaLocation ; used in dms.exe

Var JavaLocation
Var JavaBitness

!macro _LocateJava
	Call doLocate
!macroend

!define LocateJava "!insertmacro _LocateJava"

Function doLocate
	StrCpy $JavaLocation ""
	StrCpy $JavaBitness ""
	NsJavaLocator::Locate /RETARCHBITS /MAXVER "<9" /OPTVER "8" /END
	Pop $JavaLocation
	Pop $JavaBitness
FunctionEnd
