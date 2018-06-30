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
!insertmacro LANGFILE_INCLUDE_EX "English" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "French"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "French" "I18N\setup_fr.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "German"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "German" "I18N\setup_de.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Spanish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Spanish" "I18N\setup_es.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "SpanishInternational"
!insertmacro MUI_LANGUAGE "SimpChinese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "SimpChinese" "I18N\setup_zh-Hans.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "TradChinese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "TradChinese" "I18N\setup_zh-Hant.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Japanese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Japanese" "I18N\setup_ja.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Korean"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Korean" "I18N\setup_ko.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Italian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Italian" "I18N\setup_it.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Dutch"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Dutch" "I18N\setup_nl.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Danish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Danish" "I18N\setup_da.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Swedish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Swedish" "I18N\setup_sv.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "Tatar"
!insertmacro MUI_LANGUAGE "Norwegian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Norwegian" "I18N\setup_no.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Finnish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Finnish" "I18N\setup_fi.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Greek"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Greek" "I18N\setup_el.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Russian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Russian" "I18N\setup_ru.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Portuguese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Portuguese" "I18N\setup_pt.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "PortugueseBR"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "PortugueseBR" "I18N\setup_pt_BR.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "ScotsGaelic"
!insertmacro MUI_LANGUAGE "Polish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Polish" "I18N\setup_pl.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Ukrainian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Ukrainian" "I18N\setup_uk.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Czech"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Czech" "I18N\setup_cs.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Slovak"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Slovak" "I18N\setup_sk.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "Croatian"
!insertmacro MUI_LANGUAGE "Bulgarian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Bulgarian" "I18N\setup_bg.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Hungarian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Hungarian" "I18N\setup_hu.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Thai"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Thai" "I18N\setup_th.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Romanian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Romanian" "I18N\setup_ro.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "Latvian"
;!insertmacro MUI_LANGUAGE "Macedonian"
!insertmacro MUI_LANGUAGE "Estonian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Estonian" "I18N\setup_et.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Turkish"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Turkish" "I18N\setup_tr.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "Lithuanian"
!insertmacro MUI_LANGUAGE "Slovenian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Slovenian" "I18N\setup_sl.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Serbian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Serbian" "I18N\setup_sr.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "SerbianLatin"
!insertmacro MUI_LANGUAGE "Arabic"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Arabic" "I18N\setup_ar.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Farsi"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Farsi" "I18N\setup_fa.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Hebrew"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Hebrew" "I18N\setup_he.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Indonesian"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Indonesian" "I18N\setup_id.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "Mongolian"
;!insertmacro MUI_LANGUAGE "Luxembourgish"
;!insertmacro MUI_LANGUAGE "Albanian"
;!insertmacro MUI_LANGUAGE "Breton"
;!insertmacro MUI_LANGUAGE "Belarusian"
!insertmacro MUI_LANGUAGE "Icelandic"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Icelandic" "I18N\setup_is.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "Malay"
;!insertmacro MUI_LANGUAGE "Bosnian"
;!insertmacro MUI_LANGUAGE "Kurdish"
;!insertmacro MUI_LANGUAGE "Irish"
;!insertmacro MUI_LANGUAGE "Uzbek"
;!insertmacro MUI_LANGUAGE "Galician"
!insertmacro MUI_LANGUAGE "Afrikaans"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Afrikaans" "I18N\setup_af.nsh" "I18N\setup_en.nsh"
!insertmacro MUI_LANGUAGE "Catalan"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Catalan" "I18N\setup_ca.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "Esperanto"
;!insertmacro MUI_LANGUAGE "Asturian"
;!insertmacro MUI_LANGUAGE "Basque"
;!insertmacro MUI_LANGUAGE "Pashto"
;!insertmacro MUI_LANGUAGE "Georgian"
!insertmacro MUI_LANGUAGE "Vietnamese"
!insertmacro LANGFILE_INCLUDE_WITHDEFAULT_EX "Vietnamese" "I18N\setup_vi.nsh" "I18N\setup_en.nsh"
;!insertmacro MUI_LANGUAGE "Welsh"
;!insertmacro MUI_LANGUAGE "Armenian"
;!insertmacro MUI_LANGUAGE "Corsican"
