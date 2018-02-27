; Languages

!macro LANGFILE_INCLUDE_EX IDNAME FILENAME

  ; Parameters: LanguageName, FileName

  !ifdef LangFileString
    !undef LangFileString
  !endif

  !define LangFileString "!insertmacro LANGFILE_SETSTRING"

  !define LANGFILE_SETNAMES
  !ifdef LANGFILE_IDNAME
    !undef LANGFILE_IDNAME
  !endif

  !define LANGFILE_IDNAME "${IDNAME}"
  !include "${FILENAME}"
  !undef LANGFILE_SETNAMES

  ;Create language strings
  !define /redef LangFileString "!insertmacro LANGFILE_LANGSTRING"
  !include "${FILENAME}"

!macroend

!macro LANGFILE_INCLUDE_WITHDEFAULT_EX IDNAME FILENAME FILENAME_DEFAULT

  ; Parameters: LanguageName, FileName, DefaultFileName

  !ifdef LangFileString
    !undef LangFileString
  !endif

  !define LangFileString "!insertmacro LANGFILE_SETSTRING_EX"

  !define LANGFILE_SETNAMES
  !ifdef LANGFILE_IDNAME
    !undef LANGFILE_IDNAME
  !endif

  !define LANGFILE_IDNAME "${IDNAME}"
  !include "${FILENAME}"
  !undef LANGFILE_SETNAMES

  ;Include default language for missing strings
  !define LANGFILE_PRIV_INCLUDEISFALLBACK "${FILENAME_DEFAULT}"
  !include "${FILENAME_DEFAULT}"
  !undef LANGFILE_PRIV_INCLUDEISFALLBACK

  ;Create language strings
  !define /redef LangFileString "!insertmacro LANGFILE_LANGSTRING"
  !include "${FILENAME_DEFAULT}"

  ;Log if missing
  !ifdef LANGSTRING_MISSING
    !verbose push
    !verbose 4
    !echo 'One or more translations are missing in ${LANGFILE_IDNAME}'
    !verbose pop
    !undef LANGSTRING_MISSING
  !endif

!macroend

!macro LANGFILE_SETSTRING_EX NAME VALUE

  ;Set define with translated string

  !ifndef ${NAME}
    !if "${VALUE}" != ""
      !define "${NAME}" "${VALUE}"
      !ifdef LANGFILE_PRIV_INCLUDEISFALLBACK
        !ifndef LANGSTRING_MISSING
          !define LANGSTRING_MISSING
        !endif
      !endif
    !endif
  !endif

!macroend

!insertmacro MUI_LANGUAGE "English" ;first language is the default language
!insertmacro LANGFILE_INCLUDE_EX "English" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "French"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "French" "I18N\setupFrench.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "German"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "German" "I18N\setupGerman.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Spanish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Spanish" "I18N\setupSpanish.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "SpanishInternational"
!insertmacro MUI_LANGUAGE "SimpChinese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "SimpChinese" "I18N\setupChinese Simplified.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "TradChinese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "TradChinese" "I18N\setupChinese Traditional.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Japanese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Japanese" "I18N\setupJapanese.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Korean"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Korean" "I18N\setupKorean.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Italian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Italian" "I18N\setupItalian.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Dutch"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Dutch" "I18N\setupDutch.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Danish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Danish" "I18N\setupDanish.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Swedish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Swedish" "I18N\setupSwedish.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "Tatar"
!insertmacro MUI_LANGUAGE "Norwegian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Norwegian" "I18N\setupNorwegian.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Finnish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Finnish" "I18N\setupFinnish.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Greek"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Greek" "I18N\setupGreek.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Russian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Russian" "I18N\setupRussian.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Portuguese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Portuguese" "I18N\setupPortuguese.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "PortugueseBR"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "PortugueseBR" "I18N\setupPortuguese, Brazilian.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "ScotsGaelic"
!insertmacro MUI_LANGUAGE "Polish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Polish" "I18N\setupPolish.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Ukrainian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Ukrainian" "I18N\setupUkrainian.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Czech"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Czech" "I18N\setupCzech.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Slovak"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Slovak" "I18N\setupSlovak.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "Croatian"
!insertmacro MUI_LANGUAGE "Bulgarian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Bulgarian" "I18N\setupBulgarian.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Hungarian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Hungarian" "I18N\setupHungarian.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Thai"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Thai" "I18N\setupThai.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Romanian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Romanian" "I18N\setupRomanian.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "Latvian"
;!insertmacro MUI_LANGUAGE "Macedonian"
!insertmacro MUI_LANGUAGE "Estonian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Estonian" "I18N\setupEstonian.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Turkish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Turkish" "I18N\setupTurkish.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "Lithuanian"
!insertmacro MUI_LANGUAGE "Slovenian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Slovenian" "I18N\setupSlovenian.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Serbian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Serbian" "I18N\setupSerbian (Cyrillic).nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "SerbianLatin"
!insertmacro MUI_LANGUAGE "Arabic"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Arabic" "I18N\setupArabic.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Farsi"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Farsi" "I18N\setupPersian.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Hebrew"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Hebrew" "I18N\setupHebrew.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Indonesian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Indonesian" "I18N\setupIndonesian.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "Mongolian"
;!insertmacro MUI_LANGUAGE "Luxembourgish"
;!insertmacro MUI_LANGUAGE "Albanian"
;!insertmacro MUI_LANGUAGE "Breton"
;!insertmacro MUI_LANGUAGE "Belarusian"
!insertmacro MUI_LANGUAGE "Icelandic"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Icelandic" "I18N\setupIcelandic.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "Malay"
;!insertmacro MUI_LANGUAGE "Bosnian"
;!insertmacro MUI_LANGUAGE "Kurdish"
;!insertmacro MUI_LANGUAGE "Irish"
;!insertmacro MUI_LANGUAGE "Uzbek"
;!insertmacro MUI_LANGUAGE "Galician"
!insertmacro MUI_LANGUAGE "Afrikaans"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Afrikaans" "I18N\setupAfrikaans.nsh" "I18N\setupEnglish.nsh"
!insertmacro MUI_LANGUAGE "Catalan"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Catalan" "I18N\setupCatalan.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "Esperanto"
;!insertmacro MUI_LANGUAGE "Asturian"
;!insertmacro MUI_LANGUAGE "Basque"
;!insertmacro MUI_LANGUAGE "Pashto"
;!insertmacro MUI_LANGUAGE "Georgian"
!insertmacro MUI_LANGUAGE "Vietnamese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Vietnamese" "I18N\setupVietnamese.nsh" "I18N\setupEnglish.nsh"
;!insertmacro MUI_LANGUAGE "Welsh"
;!insertmacro MUI_LANGUAGE "Armenian"
;!insertmacro MUI_LANGUAGE "Corsican"
