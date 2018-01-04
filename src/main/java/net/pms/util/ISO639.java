/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 */
package net.pms.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.Messages;
import net.pms.PMS;


/**
 * This {@code enum} represents the {@code ISO 639-1} and {@code ISO 639-2}
 * languages.
 * <p>
 * {@code ISO 639 codes} updated <b>2018-02-01</b>.
 *
 * @author Nadahar
 */
public enum ISO639 implements Language {

	/** Abkhazian */
	ABKHAZIAN("Abkhazian", null, LanguageType.NORMAL, "ab", "abk"),

	/** Achinese */
	ACHINESE("Achinese", null, LanguageType.NORMAL, null, "ace"),

	/** Acoli */
	ACOLI("Acoli", null, LanguageType.NORMAL, null, "ach"),

	/** Adangme */
	ADANGME("Adangme", null, LanguageType.NORMAL, null, "ada"),

	/** Adyghe/Adygei */
	ADYGHE("Adyghe;Adygei", null, LanguageType.NORMAL, null, "ady"),

	/** Afar */
	AFAR("Afar", null, LanguageType.NORMAL, "aa", "aar"),

	/** Afrihili */
	AFRIHILI("Afrihili", null, LanguageType.NORMAL, null, "afh"),

	/** Afrikaans */
	AFRIKAANS("Afrikaans", Messages.getString("Language.af"), LanguageType.NORMAL, "af", "afr"),

	/** Afro-Asiatic languages */
	AFRO_ASIATIC_LANGUAGES("Afro-Asiatic languages", null, LanguageType.GROUP, null, "afa"),

	/** Ainu/Ainu (Japan) */
	AINU("Ainu;Ainu (Japan)", null, LanguageType.NORMAL, null, "ain"),

	/** Akan */
	AKAN("Akan", null, LanguageType.NORMAL, "ak", "aka"),

	/** Akkadian */
	AKKADIAN("Akkadian", null, LanguageType.NORMAL, null, "akk"),

	/** Albanian */
	ALBANIAN("Albanian", null, LanguageType.NORMAL, "sq", "alb", "sqi"),

	/** Alemannic/Alsatian/Swiss German */
	ALEMANNIC("Alemannic;Alsatian;Swiss German", null, LanguageType.NORMAL, null, "gsw"),

	/** Aleut */
	ALEUT("Aleut", null, LanguageType.NORMAL, null, "ale"),

	/** Algonquian/Algonquian languages */
	ALGONQUIAN("Algonquian;Algonquian languages", null, LanguageType.GROUP, null, "alg"),

	/** Altaic/Altaic languages */
	ALTAIC("Altaic;Altaic languages", null, LanguageType.GROUP, null, "tut"),

	/** Amharic */
	AMHARIC("Amharic", null, LanguageType.NORMAL, "am", "amh"),

	/** Ancient Greek (to 1453) */
	ANCIENT_GREEK("Ancient Greek (to 1453)", null, LanguageType.HISTORICAL, null, "grc"),

	/** Angika */
	ANGIKA("Angika", null, LanguageType.NORMAL, null, "anp"),

	/** Apache/Apache languages */
	APACHE("Apache;Apache languages", null, LanguageType.GROUP, null, "apa"),

	/** Arabic */
	ARABIC("Arabic", Messages.getString("Language.ar"), LanguageType.NORMAL, "ar", "ara"),

	/** Aragonese */
	ARAGONESE("Aragonese", null, LanguageType.NORMAL, "an", "arg"),

	/** Arapaho */
	ARAPAHO("Arapaho", null, LanguageType.NORMAL, null, "arp"),

	/** Arawak */
	ARAWAK("Arawak", null, LanguageType.NORMAL, null, "arw"),

	/** Armenian */
	ARMENIAN("Armenian", null, LanguageType.NORMAL, "hy", "arm", "hye"),

	/** Aromanian/Arumanian/Macedo-Romanian */
	AROMANIAN("Aromanian;Arumanian;Macedo-Romanian", null, LanguageType.NORMAL, null, "rup"),

	/** Artificial languages */
	ARTIFICIAL("Artificial languages", null, LanguageType.GROUP, null, "art"),

	/** Assamese */
	ASSAMESE("Assamese", null, LanguageType.NORMAL, "as", "asm"),

	/** Asturian/Asturleonese/Bable/Leonese */
	ASTURIAN("Asturian;Asturleonese;Bable;Leonese", null, LanguageType.NORMAL, null, "ast"),

	/** Athapascan/Athapascan languages */
	ATHAPASCAN("Athapascan;Athapascan languages", null, LanguageType.GROUP, null, "ath"),

	/** Australian/Australian languages */
	AUSTRALIAN("Australian;Australian languages", null, LanguageType.GROUP, null, "aus"),

	/** Austronesian/Austronesian languages */
	AUSTRONESIAN("Austronesian;Austronesian languages", null, LanguageType.GROUP, null, "map"),

	/** Avaric */
	AVARIC("Avaric", null, LanguageType.NORMAL, "av", "ava"),

	/** Avestan */
	AVESTAN("Avestan", null, LanguageType.NORMAL, "ae", "ave"),

	/** Awadhi */
	AWADHI("Awadhi", null, LanguageType.NORMAL, null, "awa"),

	/** Aymara */
	AYMARA("Aymara", null, LanguageType.NORMAL, "ay", "aym"),

	/** Azerbaijani */
	AZERBAIJANI("Azerbaijani", null, LanguageType.NORMAL, "az", "aze"),

	/** Balinese */
	BALINESE("Balinese", null, LanguageType.NORMAL, null, "ban"),

	/** Baltic/Baltic languages */
	BALTIC("Baltic;Baltic languages", null, LanguageType.GROUP, null, "bat"),

	/** Baluchi */
	BALUCHI("Baluchi", null, LanguageType.NORMAL, null, "bal"),

	/** Bambara */
	BAMBARA("Bambara", null, LanguageType.NORMAL, "bm", "bam"),

	/** Bamileke/Bamileke languages */
	BAMILEKE("Bamileke;Bamileke languages", null, LanguageType.GROUP, null, "bai"),

	/** Banda/Banda languages */
	BANDA("Banda;Banda languages", null, LanguageType.GROUP, null, "bad"),

	/** Bantu/Bantu languages */
	BANTU("Bantu;Bantu languages", null, LanguageType.GROUP, null, "bnt"),

	/** Basa (Cameroon) */
	BASA("Basa (Cameroon)", null, LanguageType.NORMAL, null, "bas"),

	/** Bashkir */
	BASHKIR("Bashkir", null, LanguageType.NORMAL, "ba", "bak"),

	/** Basque */
	BASQUE("Basque", null, LanguageType.NORMAL, "eu", "baq", "eus"),

	/** Batak/Batak languages */
	BATAK("Batak;Batak languages", null, LanguageType.GROUP, null, "btk"),

	/** Bedawiyet/Beja */
	BEJA("Bedawiyet;Beja", null, LanguageType.NORMAL, null, "bej"),

	/** Belarusian */
	BELARUSIAN("Belarusian", null, LanguageType.NORMAL, "be", "bel"),

	/** Bemba (Zambia) */
	BEMBA("Bemba (Zambia)", null, LanguageType.NORMAL, null, "bem"),

	/** Bengali */
	BENGALI("Bengali", Messages.getString("Language.bn"), LanguageType.NORMAL, "bn", "ben"),

	/** Berber/Berber languages */
	BERBER("Berber;Berber languages", null, LanguageType.GROUP, null, "ber"),

	/** Bhojpuri */
	BHOJPURI("Bhojpuri", null, LanguageType.NORMAL, null, "bho"),

	/** Bihari/Bihari languages */
	BIHARI("Bihari;Bihari languages", null, LanguageType.GROUP, "bh", "bih"),

	/** Bikol */
	BIKOL("Bikol", null, LanguageType.NORMAL, null, "bik"),

	/** Bilen/Bilin/Blin */
	BILEN("Bilen;Bilin;Blin", null, LanguageType.NORMAL, null, "byn"),

	/** Bini/Edo */
	EDO("Bini;Edo", null, LanguageType.NORMAL, null, "bin"),

	/** Bislama */
	BISLAMA("Bislama", null, LanguageType.NORMAL, "bi", "bis"),

	/** Bliss/Blissymbolics/Blissymbols */
	BLISS("Bliss;Blissymbolics;Blissymbols", null, LanguageType.NORMAL, null, "zbl"),

	/** Bosnian */
	BOSNIAN("Bosnian", null, LanguageType.NORMAL, "bs", "bos"),

	/** Braj */
	BRAJ("Braj", null, LanguageType.NORMAL, null, "bra"),

	/** Breton */
	BRETON("Breton", null, LanguageType.NORMAL, "br", "bre"),

	/** Buginese */
	BUGINESE("Buginese", null, LanguageType.NORMAL, null, "bug"),

	/** Bulgarian */
	BULGARIAN("Bulgarian", Messages.getString("Language.bg"), LanguageType.NORMAL, "bg", "bul"),

	/** Buriat */
	BURIAT("Buriat", null, LanguageType.NORMAL, null, "bua"),

	/** Burmese */
	BURMESE("Burmese", null, LanguageType.NORMAL, "my", "bur", "mya"),

	/** Caddo */
	CADDO("Caddo", null, LanguageType.NORMAL, null, "cad"),

	/** Castilian/Spanish */
	SPANISH("Castilian;Spanish", Messages.getString("Language.es"), LanguageType.NORMAL, "es", "spa"),

	/** Catalan/Valencian */
	CATALAN("Catalan;Valencian", Messages.getString("Language.ca"), LanguageType.NORMAL, "ca", "cat"),

	/** Caucasian/Caucasian languages */
	CAUCASIAN("Caucasian;Caucasian languages", null, LanguageType.GROUP, null, "cau"),

	/** Cebuano */
	CEBUANO("Cebuano", null, LanguageType.NORMAL, null, "ceb"),

	/** Celtic/Celtic languages */
	CELTIC("Celtic;Celtic languages", null, LanguageType.GROUP, null, "cel"),

	/** Central American Indian languages */
	CENTRAL_AMERICAN_INDIAN_LANGUAGES("Central American Indian languages", null, LanguageType.GROUP, null, "cai"),

	/** Central Khmer/Khmer */
	KHMER("Central Khmer;Khmer", null, LanguageType.NORMAL, "km", "khm"),

	/** Chagatai */
	CHAGATAI("Chagatai", null, LanguageType.NORMAL, null, "chg"),

	/** Chamic/Chamic languages */
	CHAMIC("Chamic;Chamic languages", null, LanguageType.GROUP, null, "cmc"),

	/** Chamorro */
	CHAMORRO("Chamorro", null, LanguageType.NORMAL, "ch", "cha"),

	/** Chechen */
	CHECHEN("Chechen", null, LanguageType.NORMAL, "ce", "che"),

	/** Cherokee */
	CHEROKEE("Cherokee", null, LanguageType.NORMAL, null, "chr"),

	/** Chewa/Chichewa/Nyanja */
	CHEWA("Chewa;Chichewa;Nyanja", null, LanguageType.NORMAL, "ny", "nya"),

	/** Cheyenne */
	CHEYENNE("Cheyenne", null, LanguageType.NORMAL, null, "chy"),

	/** Chibcha */
	CHIBCHA("Chibcha", null, LanguageType.NORMAL, null, "chb"),

	/** Chinese */
	CHINESE("Chinese", Messages.getString("Language.zh"), LanguageType.NORMAL, "zh", "chi", "zho"),

	/** Chinook jargon */
	CHINOOK("Chinook jargon", null, LanguageType.NORMAL, null, "chn"),

	/** Chipewyan/Dene Suline */
	CHIPEWYAN("Chipewyan;Dene Suline", null, LanguageType.NORMAL, null, "chp"),

	/** Choctaw */
	CHOCTAW("Choctaw", null, LanguageType.NORMAL, null, "cho"),

	/** Chuang/Zhuang */
	ZHUANG("Chuang;Zhuang", null, LanguageType.NORMAL, "za", "zha"),

	/** Church Slavic/Church Slavonic/Old Bulgarian/Old Church Slavonic/Old Slavonic */
	CHURCH_SLAVIC(
		"Church Slavic;Church Slavonic;Old Bulgarian;Old Church Slavonic;Old Slavonic",
		null,
		LanguageType.HISTORICAL,
		"cu",
		"chu"
	),

	/** Chuukese */
	CHUUKESE("Chuukese", null, LanguageType.NORMAL, null, "chk"),

	/** Chuvash */
	CHUVASH("Chuvash", null, LanguageType.NORMAL, "cv", "chv"),

	/** Classical Nepal Bhasa/Classical Newari/Old Newari */
	CLASSICAL_NEWAR("Classical Nepal Bhasa;Classical Newari;Classical Newar;Old Newari", null, LanguageType.HISTORICAL, null, "nwc"),

	/** Classical Syriac */
	CLASSICAL_SYRIAC("Classical Syriac", null, LanguageType.HISTORICAL, null, "syc"),

	/** Cook Islands Maori/Rarotongan */
	COOK_ISLANDS_MAORI("Cook Islands Maori;Rarotongan", null, LanguageType.NORMAL, null, "rar"),

	/** Coptic */
	COPTIC("Coptic", null, LanguageType.NORMAL, null, "cop"),

	/** Cornish */
	CORNISH("Cornish", null, LanguageType.NORMAL, "kw", "cor"),

	/** Corsican */
	CORSICAN("Corsican", null, LanguageType.NORMAL, "co", "cos"),

	/** Cree */
	CREE("Cree", null, LanguageType.NORMAL, "cr", "cre"),

	/** Creek */
	CREEK("Creek", null, LanguageType.NORMAL, null, "mus"),

	/** Creoles and pidgins */
	CREOLES_AND_PIDGINS("Creoles and pidgins", null, LanguageType.GROUP, null, "crp"),

	/** Creoles and pidgins, English based */
	ENGLISH_BASED_CREOLES_AND_PIDGINS("English based Creoles and pidgins", null, LanguageType.GROUP, null, "cpe"),

	/** Creoles and pidgins, French-based */
	FRENCH_BASED_CREOLES_AND_PIDGINS("French-based Creoles and pidgins", null, LanguageType.GROUP, null, "cpf"),

	/** Creoles and pidgins, Portuguese-based */
	PORTUGUESE_BASED_CREOLES_AND_PIDGINS("Portuguese-based Creoles and pidgins", null, LanguageType.GROUP, null, "cpp"),

	/** Crimean Tatar/Crimean Turkish */
	CRIMEAN_TATAR("Crimean Tatar;Crimean Turkish", null, LanguageType.NORMAL, null, "crh"),

	/** Croatian */
	CROATIAN("Croatian", null, LanguageType.NORMAL, "hr", "hrv"),

	/** Cushitic/Cushitic languages */
	CUSHITIC("Cushitic;Cushitic languages", null, LanguageType.GROUP, null, "cus"),

	/** Czech */
	CZECH("Czech", Messages.getString("Language.cs"), LanguageType.NORMAL, "cs", "cze", "ces"),

	/** Dakota */
	DAKOTA("Dakota", null, LanguageType.NORMAL, null, "dak"),

	/** Danish */
	DANISH("Danish", Messages.getString("Language.da"), LanguageType.NORMAL, "da", "dan"),

	/** Dargwa */
	DARGWA("Dargwa", null, LanguageType.NORMAL, null, "dar"),

	/** Delaware */
	DELAWARE("Delaware", null, LanguageType.NORMAL, null, "del"),

	/** Dhivehi/Divehi/Maldivian */
	MALDIVIAN("Dhivehi;Divehi;Maldivian", null, LanguageType.NORMAL, "dv", "div"),

	/** Dholuo/Luo (Kenya and Tanzania) */
	DHOLUO("Dholuo;Luo (Kenya and Tanzania)", null, LanguageType.NORMAL, null, "luo"),

	/** Dimili/Dimli (macrolanguage)/Kirdki/Kirmanjki (macrolanguage)/Zaza/Zazaki */
	ZAZA("Dimili;Dimli (macrolanguage);Kirdki;Kirmanjki (macrolanguage);Zaza;Zazaki", null, LanguageType.NORMAL, null, "zza"),

	/** Dinka */
	DINKA("Dinka", null, LanguageType.NORMAL, null, "din"),

	/** Dogri (macrolanguage) */
	DOGRI("Dogri (macrolanguage)", null, LanguageType.NORMAL, null, "doi"),

	/** Dogrib */
	DOGRIB("Dogrib", null, LanguageType.NORMAL, null, "dgr"),

	/** Dravidian/Dravidian languages */
	DRAVIDIAN("Dravidian;Dravidian languages", null, LanguageType.GROUP, null, "dra"),

	/** Duala */
	DUALA("Duala", null, LanguageType.NORMAL, null, "dua"),

	/** Dutch/Flemish */
	DUTCH("Dutch;Flemish", Messages.getString("Language.nl"), LanguageType.NORMAL, "nl", "dut", "nld"),

	/** Dyula */
	DYULA("Dyula", null, LanguageType.NORMAL, null, "dyu"),

	/** Dzongkha */
	DZONGKHA("Dzongkha", null, LanguageType.NORMAL, "dz", "dzo"),

	/** Eastern Frisian */
	EASTERN_FRISIAN("Eastern Frisian", null, LanguageType.NORMAL, null, "frs"),

	/** Efik */
	EFIK("Efik", null, LanguageType.NORMAL, null, "efi"),

	/** Egyptian (Ancient) */
	EGYPTIAN("Egyptian (Ancient)", null, LanguageType.HISTORICAL, null, "egy"),

	/** Ekajuk */
	EKAJUK("Ekajuk", null, LanguageType.NORMAL, null, "eka"),

	/** Elamite */
	ELAMITE("Elamite", null, LanguageType.NORMAL, null, "elx"),

	/** English */
	ENGLISH("English", Messages.getString("Language.en"), LanguageType.NORMAL, "en", "eng"),

	/** Erzya */
	ERZYA("Erzya", null, LanguageType.NORMAL, null, "myv"),

	/** Esperanto */
	ESPERANTO("Esperanto", null, LanguageType.NORMAL, "eo", "epo"),

	/** Estonian */
	ESTONIAN("Estonian", null, LanguageType.NORMAL, "et", "est"),

	/** Ewe */
	EWE("Ewe", null, LanguageType.NORMAL, "ee", "ewe"),

	/** Ewondo */
	EWONDO("Ewondo", null, LanguageType.NORMAL, null, "ewo"),

	/** Fang (Equatorial Guinea) */
	FANG("Fang (Equatorial Guinea)", null, LanguageType.NORMAL, null, "fan"),

	/** Fanti */
	FANTI("Fanti", null, LanguageType.NORMAL, null, "fat"),

	/** Faroese */
	FAROESE("Faroese", null, LanguageType.NORMAL, "fo", "fao"),

	/** Fijian */
	FIJIAN("Fijian", null, LanguageType.NORMAL, "fj", "fij"),

	/** Filipino/Pilipino */
	FILIPINO("Filipino;Pilipino", null, LanguageType.NORMAL, null, "fil"),

	/** Finnish */
	FINNISH("Finnish", Messages.getString("Language.fi"), LanguageType.NORMAL, "fi", "fin"),

	/** Finno-Ugrian languages */
	FINNO_UGRIAN_LANGUAGES("Finno-Ugrian languages", null, LanguageType.GROUP, null, "fiu"),

	/** Fon */
	FON("Fon", null, LanguageType.NORMAL, null, "fon"),

	/** French */
	FRENCH("French", Messages.getString("Language.fr"), LanguageType.NORMAL, "fr", "fre", "fra"),

	/** Friulian */
	FRIULIAN("Friulian", null, LanguageType.NORMAL, null, "fur"),

	/** Fulah */
	FULAH("Fulah", null, LanguageType.NORMAL, "ff", "ful"),

	/** Ga */
	GA("Ga", null, LanguageType.NORMAL, null, "gaa"),

	/** Gaelic/Scottish Gaelic */
	GAELIC("Gaelic;Scottish Gaelic", null, LanguageType.NORMAL, "gd", "gla"),

	/** Galibi Carib */
	CARIB("Galibi Carib", null, LanguageType.NORMAL, null, "car"),

	/** Galician */
	GALICIAN("Galician", null, LanguageType.NORMAL, "gl", "glg"),

	/** Ganda */
	GANDA("Ganda", null, LanguageType.NORMAL, "lg", "lug"),

	/** Gayo */
	GAYO("Gayo", null, LanguageType.NORMAL, null, "gay"),

	/** Gbaya (Central African Republic) */
	GBAYA("Gbaya (Central African Republic)", null, LanguageType.NORMAL, null, "gba"),

	/** Geez */
	GEEZ("Geez", null, LanguageType.NORMAL, null, "gez"),

	/** Georgian */
	GEORGIAN("Georgian", null, LanguageType.NORMAL, "ka", "geo", "kat"),

	/** German */
	GERMAN("German", Messages.getString("Language.de"), LanguageType.NORMAL, "de", "ger", "deu"),

	/** Germanic/Germanic languages */
	GERMANIC("Germanic;Germanic languages", null, LanguageType.GROUP, null, "gem"),

	/** Gikuyu/Kikuyu */
	KIKUYU("Gikuyu;Kikuyu", null, LanguageType.NORMAL, "ki", "kik"),

	/** Gilbertese */
	GILBERTESE("Gilbertese", null, LanguageType.NORMAL, null, "gil"),

	/** Gondi */
	GONDI("Gondi", null, LanguageType.NORMAL, null, "gon"),

	/** Gorontalo */
	GORONTALO("Gorontalo", null, LanguageType.NORMAL, null, "gor"),

	/** Gothic */
	GOTHIC("Gothic", null, LanguageType.NORMAL, null, "got"),

	/** Grebo */
	GREBO("Grebo", null, LanguageType.NORMAL, null, "grb"),

	/** Greek/Modern Greek (1453-) */
	GREEK("Greek;Modern Greek (1453-)", Messages.getString("Language.el"), LanguageType.NORMAL, "el", "gre", "ell"),

	/** Greenlandic/Kalaallisut */
	GREENLANDIC("Greenlandic;Kalaallisut", null, LanguageType.NORMAL, "kl", "kal"),

	/** Guarani */
	GUARANI("Guarani", null, LanguageType.NORMAL, "gn", "grn"),

	/** Gujarati */
	GUJARATI("Gujarati", null, LanguageType.NORMAL, "gu", "guj"),

	/** Gwichʼin */
	GWICH_IN("Gwichʼin", null, LanguageType.NORMAL, null, "gwi"),

	/** Haida */
	HAIDA("Haida", null, LanguageType.NORMAL, null, "hai"),

	/** Haitian/Haitian Creole */
	HAITIAN("Haitian;Haitian Creole", null, LanguageType.NORMAL, "ht", "hat"),

	/** Hausa */
	HAUSA("Hausa", null, LanguageType.NORMAL, "ha", "hau"),

	/** Hawaiian */
	HAWAIIAN("Hawaiian", null, LanguageType.NORMAL, null, "haw"),

	/** Hebrew */
	HEBREW("Hebrew", Messages.getString("Language.iw"), LanguageType.HISTORICAL, "he", "heb"),

	/** Herero */
	HERERO("Herero", null, LanguageType.NORMAL, "hz", "her"),

	/** Hiligaynon */
	HILIGAYNON("Hiligaynon", null, LanguageType.NORMAL, null, "hil"),

	/** Himachali languages/Western Pahari languages */
	WESTERN_PAHARI("Himachali languages;Western Pahari languages", null, LanguageType.GROUP, null, "him"),

	/** Hindi */
	HINDI("Hindi", null, LanguageType.NORMAL, "hi", "hin"),

	/** Hiri Motu */
	HIRI_MOTU("Hiri Motu", null, LanguageType.NORMAL, "ho", "hmo"),

	/** Hittite */
	HITTITE("Hittite", null, LanguageType.NORMAL, null, "hit"),

	/** Hmong/Mong */
	HMONG("Hmong;Mong", null, LanguageType.NORMAL, null, "hmn"),

	/** Hungarian */
	HUNGARIAN("Hungarian", Messages.getString("Language.hu"), LanguageType.NORMAL, "hu", "hun"),

	/** Hupa */
	HUPA("Hupa", null, LanguageType.NORMAL, null, "hup"),

	/** Iban */
	IBAN("Iban", null, LanguageType.NORMAL, null, "iba"),

	/** Icelandic */
	ICELANDIC("Icelandic", Messages.getString("Language.is"), LanguageType.NORMAL, "is", "ice", "isl"),

	/** Ido */
	IDO("Ido", null, LanguageType.NORMAL, "io", "ido"),

	/** Igbo */
	IGBO("Igbo", null, LanguageType.NORMAL, "ig", "ibo"),

	/** Ijo/Ijo languages */
	IJO("Ijo;Ijo languages", null, LanguageType.GROUP, null, "ijo"),

	/** Iloko */
	ILOKO("Iloko", null, LanguageType.NORMAL, null, "ilo"),

	/** Imperial Aramaic (700-300 BCE)/Official Aramaic (700-300 BCE) */
	IMPERIAL_ARAMAIC("Imperial Aramaic (700-300 BCE);Official Aramaic (700-300 BCE)", null, LanguageType.HISTORICAL, null, "arc"),

	/** Inari Sami */
	INARI_SAMI("Inari Sami", null, LanguageType.NORMAL, null, "smn"),

	/** Indic/Indic languages */
	INDIC("Indic;Indic languages", null, LanguageType.GROUP, null, "inc"),

	/** Indo-European languages */
	INDO_EUROPEAN_LANGUAGES("Indo-European languages", null, LanguageType.GROUP, null, "ine"),

	/** Indonesian */
	INDONESIAN("Indonesian", null, LanguageType.NORMAL, "id", "ind"),

	/** Ingush */
	INGUSH("Ingush", null, LanguageType.NORMAL, null, "inh"),

	/** Interlingua (International Auxiliary Language Association) */
	INTERLINGUA("Interlingua (International Auxiliary Language Association)", null, LanguageType.NORMAL, "ia", "ina"),

	/** Interlingue/Occidental */
	INTERLINGUE("Interlingue;Occidental", null, LanguageType.NORMAL, "ie", "ile"),

	/** Inuktitut */
	INUKTITUT("Inuktitut", null, LanguageType.NORMAL, "iu", "iku"),

	/** Inupiaq */
	INUPIAQ("Inupiaq", null, LanguageType.NORMAL, "ik", "ipk"),

	/** Iranian/Iranian languages */
	IRANIAN("Iranian;Iranian languages", null, LanguageType.GROUP, null, "ira"),

	/** Irish */
	IRISH("Irish", null, LanguageType.NORMAL, "ga", "gle"),

	/** Iroquoian/Iroquoian languages */
	IROQUOIAN("Iroquoian;Iroquoian languages", null, LanguageType.GROUP, null, "iro"),

	/** Italian */
	ITALIAN("Italian", Messages.getString("Language.it"), LanguageType.NORMAL, "it", "ita"),

	/** Japanese */
	JAPANESE("Japanese", Messages.getString("Language.ja"), LanguageType.NORMAL, "ja", "jpn"),

	/** Javanese */
	JAVANESE("Javanese", null, LanguageType.NORMAL, "jv", "jav"),

	/** Jingpho/Kachin */
	JINGPHO("Jingpho;Kachin", null, LanguageType.NORMAL, null, "kac"),

	/** Judeo-Arabic */
	JUDEO_ARABIC("Judeo-Arabic", null, LanguageType.NORMAL, null, "jrb"),

	/** Judeo-Persian */
	JUDEO_PERSIAN("Judeo-Persian", null, LanguageType.NORMAL, null, "jpr"),

	/** Kabardian */
	KABARDIAN("Kabardian", null, LanguageType.NORMAL, null, "kbd"),

	/** Kabyle */
	KABYLE("Kabyle", null, LanguageType.NORMAL, null, "kab"),

	/** Kalmyk/Oirat */
	KALMYK_OIRAT("Kalmyk;Oirat;Kalmyk Oirat", null, LanguageType.NORMAL, null, "xal"),

	/** Kamba (Kenya) */
	KAMBA("Kamba (Kenya)", null, LanguageType.NORMAL, null, "kam"),

	/** Kannada */
	KANNADA("Kannada", null, LanguageType.NORMAL, "kn", "kan"),

	/** Kanuri */
	KANURI("Kanuri", null, LanguageType.NORMAL, "kr", "kau"),

	/** Kapampangan/Pampanga */
	KAPAMPANGAN("Kapampangan;Pampanga", null, LanguageType.NORMAL, null, "pam"),

	/** Karachay-Balkar */
	KARACHAY_BALKAR("Karachay-Balkar", null, LanguageType.NORMAL, null, "krc"),

	/** Kara-Kalpak */
	KARA_KALPAK("Kara-Kalpak", null, LanguageType.NORMAL, null, "kaa"),

	/** Karelian */
	KARELIAN("Karelian", null, LanguageType.NORMAL, null, "krl"),

	/** Karen/Karen languages */
	KAREN("Karen;Karen languages", null, LanguageType.GROUP, null, "kar"),

	/** Kashmiri */
	KASHMIRI("Kashmiri", null, LanguageType.NORMAL, "ks", "kas"),

	/** Kashubian */
	KASHUBIAN("Kashubian", null, LanguageType.NORMAL, null, "csb"),

	/** Kawi */
	KAWI("Kawi", null, LanguageType.NORMAL, null, "kaw"),

	/** Kazakh */
	KAZAKH("Kazakh", null, LanguageType.NORMAL, "kk", "kaz"),

	/** Khasi */
	KHASI("Khasi", null, LanguageType.NORMAL, null, "kha"),

	/** Khoisan/Khoisan languages */
	KHOISAN("Khoisan;Khoisan languages", null, LanguageType.GROUP, null, "khi"),

	/** Saka/Khotanese/Sakan */
	SAKA("Saka;Khotanese;Sakan", null, LanguageType.NORMAL, null, "kho"),

	/** Kimbundu */
	KIMBUNDU("Kimbundu", null, LanguageType.NORMAL, null, "kmb"),

	/** Kinyarwanda */
	KINYARWANDA("Kinyarwanda", null, LanguageType.NORMAL, "rw", "kin"),

	/** Kirghiz/Kyrgyz */
	KYRGYZ("Kirghiz;Kyrgyz", null, LanguageType.NORMAL, "ky", "kir"),

	/** Klingon/tlhIngan Hol */
	KLINGON("Klingon;tlhIngan Hol", null, LanguageType.NORMAL, null, "tlh"),

	/** Komi */
	KOMI("Komi", null, LanguageType.NORMAL, "kv", "kom"),

	/** Kongo */
	KONGO("Kongo", null, LanguageType.NORMAL, "kg", "kon"),

	/** Konkani (macrolanguage) */
	KONKANI("Konkani (macrolanguage)", null, LanguageType.NORMAL, null, "kok"),

	/** Korean */
	KOREAN("Korean", Messages.getString("Language.ko"), LanguageType.NORMAL, "ko", "kor"),

	/** Kosraean */
	KOSRAEAN("Kosraean", null, LanguageType.NORMAL, null, "kos"),

	/** Kpelle */
	KPELLE("Kpelle", null, LanguageType.NORMAL, null, "kpe"),

	/** Kru/Kru languages */
	KRU("Kru;Kru languages", null, LanguageType.GROUP, null, "kro"),

	/** Kuanyama/Kwanyama */
	KWANYAMA("Kuanyama;Kwanyama", null, LanguageType.NORMAL, "kj", "kua"),

	/** Kumyk */
	KUMYK("Kumyk", null, LanguageType.NORMAL, null, "kum"),

	/** Kurdish */
	KURDISH("Kurdish", null, LanguageType.NORMAL, "ku", "kur"),

	/** Kurukh */
	KURUKH("Kurukh", null, LanguageType.NORMAL, null, "kru"),

	/** Kutenai */
	KUTENAI("Kutenai", null, LanguageType.NORMAL, null, "kut"),

	/** Ladino */
	LADINO("Ladino", null, LanguageType.NORMAL, null, "lad"),

	/** Lahnda */
	LAHNDA("Lahnda", null, LanguageType.NORMAL, null, "lah"),

	/** Lamba */
	LAMBA("Lamba", null, LanguageType.NORMAL, null, "lam"),

	/** Land Dayak languages */
	LAND_DAYAK("Land Dayak languages", null, LanguageType.GROUP, null, "day"),

	/** Lao */
	LAO("Lao", null, LanguageType.NORMAL, "lo", "lao"),

	/** Latin */
	LATIN("Latin", null, LanguageType.NORMAL, "la", "lat"),

	/** Latvian */
	LATVIAN("Latvian", null, LanguageType.NORMAL, "lv", "lav"),

	/** Letzeburgesch/Luxembourgish */
	LUXEMBOURGISH("Letzeburgesch;Luxembourgish", null, LanguageType.NORMAL, "lb", "ltz"),

	/** Lezghian */
	LEZGHIAN("Lezghian", null, LanguageType.NORMAL, null, "lez"),

	/** Limburgan/Limburger/Limburgish */
	LIMBURGISH("Limburgan;Limburger;Limburgish", null, LanguageType.NORMAL, "li", "lim"),

	/** Lingala */
	LINGALA("Lingala", null, LanguageType.NORMAL, "ln", "lin"),

	/** Lithuanian */
	LITHUANIAN("Lithuanian", null, LanguageType.NORMAL, "lt", "lit"),

	/** Lojban */
	LOJBAN("Lojban", null, LanguageType.NORMAL, null, "jbo"),

	/** Low German/Low Saxon */
	LOW_GERMAN("Low German;Low Saxon", null, LanguageType.NORMAL, null, "nds"),

	/** Lower Sorbian */
	LOWER_SORBIAN("Lower Sorbian", null, LanguageType.NORMAL, null, "dsb"),

	/** Lozi */
	LOZI("Lozi", null, LanguageType.NORMAL, null, "loz"),

	/** Luba-Katanga */
	LUBA_KATANGA("Luba-Katanga", null, LanguageType.NORMAL, "lu", "lub"),

	/** Luba-Lulua */
	LUBA_LULUA("Luba-Lulua", null, LanguageType.NORMAL, null, "lua"),

	/** Luiseno */
	LUISENO("Luiseno", null, LanguageType.NORMAL, null, "lui"),

	/** Lule Sami */
	LULE_SAMI("Lule Sami", null, LanguageType.NORMAL, null, "smj"),

	/** Lunda */
	LUNDA("Lunda", null, LanguageType.NORMAL, null, "lun"),

	/** Lushai */
	LUSHAI("Lushai", null, LanguageType.NORMAL, null, "lus"),

	/** Macedonian */
	MACEDONIAN("Macedonian", null, LanguageType.NORMAL, "mk", "mac", "mkd"),

	/** Madurese */
	MADURESE("Madurese", null, LanguageType.NORMAL, null, "mad"),

	/** Magahi */
	MAGAHI("Magahi", null, LanguageType.NORMAL, null, "mag"),

	/** Maithili */
	MAITHILI("Maithili", null, LanguageType.NORMAL, null, "mai"),

	/** Makasar */
	MAKASAR("Makasar", null, LanguageType.NORMAL, null, "mak"),

	/** Malagasy */
	MALAGASY("Malagasy", null, LanguageType.NORMAL, "mg", "mlg"),

	/** Malay (macrolanguage) */
	MALAY("Malay (macrolanguage)", null, LanguageType.NORMAL, "ms", "may", "msa"),

	/** Malayalam */
	MALAYALAM("Malayalam", null, LanguageType.NORMAL, "ml", "mal"),

	/** Maltese */
	MALTESE("Maltese", null, LanguageType.NORMAL, "mt", "mlt"),

	/** Manchu */
	MANCHU("Manchu", null, LanguageType.NORMAL, null, "mnc"),

	/** Mandar */
	MANDAR("Mandar", null, LanguageType.NORMAL, null, "mdr"),

	/** Manding/Mandingo */
	MANDING("Manding;Mandingo", null, LanguageType.NORMAL, null, "man"),

	/** Manipuri */
	MANIPURI("Manipuri", null, LanguageType.NORMAL, null, "mni"),

	/** Manobo/Manobo languages */
	MANOBO("Manobo;Manobo languages", null, LanguageType.GROUP, null, "mno"),

	/** Manx */
	MANX("Manx", null, LanguageType.NORMAL, "gv", "glv"),

	/** Maori */
	MAORI("Maori", null, LanguageType.NORMAL, "mi", "mao", "mri"),

	/** Mapuche/Mapudungun */
	MAPUCHE("Mapuche;Mapudungun", null, LanguageType.NORMAL, null, "arn"),

	/** Marathi */
	MARATHI("Marathi", null, LanguageType.NORMAL, "mr", "mar"),

	/** Mari (Russia) */
	MARI("Mari (Russia)", null, LanguageType.NORMAL, null, "chm"),

	/** Marshallese */
	MARSHALLESE("Marshallese", null, LanguageType.NORMAL, "mh", "mah"),

	/** Marwari */
	MARWARI("Marwari", null, LanguageType.NORMAL, null, "mwr"),

	/** Masai */
	MASAI("Masai", null, LanguageType.NORMAL, null, "mas"),

	/** Mayan/Mayan languages */
	MAYAN("Mayan;Mayan languages", null, LanguageType.GROUP, null, "myn"),

	/** Mende (Sierra Leone) */
	MENDE("Mende (Sierra Leone)", null, LanguageType.NORMAL, null, "men"),

	/** Migmaw/Mikmaw/Micmac/Mi'kmaq */
	MIGMAW("Micmac;Mi'kmaq", null, LanguageType.NORMAL, null, "mic"),

	/** Middle Dutch (ca. 1050-1350) */
	MIDDLE_DUTCH("Middle Dutch (ca. 1050-1350)", null, LanguageType.HISTORICAL, null, "dum"),

	/** Middle English (1100-1500) */
	MIDDLE_ENGLISH("Middle English (1100-1500)", null, LanguageType.HISTORICAL, null, "enm"),

	/** Middle French (ca. 1400-1600) */
	MIDDLE_FRENCH("Middle French (ca. 1400-1600)", null, LanguageType.HISTORICAL, null, "frm"),

	/** Middle High German (ca. 1050-1500) */
	MIDDLE_HIGH_GERMAN("Middle High German (ca. 1050-1500)", null, LanguageType.HISTORICAL, null, "gmh"),

	/** Middle Irish (900-1200) */
	MIDDLE_IRISH("Middle Irish (900-1200)", null, LanguageType.HISTORICAL, null, "mga"),

	/** Minangkabau */
	MINANGKABAU("Minangkabau", null, LanguageType.NORMAL, null, "min"),

	/** Mirandese */
	MIRANDESE("Mirandese", null, LanguageType.NORMAL, null, "mwl"),

	/** Mohawk */
	MOHAWK("Mohawk", null, LanguageType.NORMAL, null, "moh"),

	/** Moksha */
	MOKSHA("Moksha", null, LanguageType.NORMAL, null, "mdf"),

	/** Romanian/Moldavian/Moldovan */
	MOLDOVAN("Romanian;Moldavian;Moldovan", Messages.getString("Language.ro"), LanguageType.NORMAL, "ro", "rum", "ron"),

	/** Mongo */
	MONGO("Mongo", null, LanguageType.NORMAL, null, "lol"),

	/** Mongolian */
	MONGOLIAN("Mongolian", null, LanguageType.NORMAL, "mn", "mon"),

	/** Mon-Khmer languages */
	MON_KHMER("Mon-Khmer languages", null, LanguageType.GROUP, null, "mkh"),

	/** Montenegrin */
	MONTENEGRIN("Montenegrin", null, LanguageType.NORMAL, null, "cnr"),

	/** Mossi */
	MOSSI("Mossi", null, LanguageType.NORMAL, null, "mos"),

	/** Multiple languages */
	MULTIPLE("Multiple languages", null, LanguageType.NON_LANGUAGE, null, "mul"),

	/** Munda/Munda languages */
	MUNDA("Munda;Munda languages", null, LanguageType.GROUP, null, "mun"),

	/** Nahuatl/Nahuatl languages */
	NAHUATL("Nahuatl;Nahuatl languages", null, LanguageType.GROUP, null, "nah"),

	/** Nauru */
	NAURU("Nauru", null, LanguageType.NORMAL, "na", "nau"),

	/** Navaho/Navajo */
	NAVAJO("Navaho;Navajo", null, LanguageType.NORMAL, "nv", "nav"),

	/** Ndonga */
	NDONGA("Ndonga", null, LanguageType.NORMAL, "ng", "ndo"),

	/** Neapolitan */
	NEAPOLITAN("Neapolitan", null, LanguageType.NORMAL, null, "nap"),

	/** Nepal Bhasa/Newari/Newar */
	NEWAR("Nepal Bhasa;Newari;Newar", null, LanguageType.NORMAL, null, "new"),

	/** Nepali (macrolanguage) */
	NEPALI("Nepali (macrolanguage)", null, LanguageType.NORMAL, "ne", "nep"),

	/** Nias */
	NIAS("Nias", null, LanguageType.NORMAL, null, "nia"),

	/** Niger-Kordofanian languages */
	NIGER_KORDOFANIAN("Niger-Kordofanian languages", null, LanguageType.GROUP, null, "nic"),

	/** Nilo-Saharan languages */
	NILO_SAHARAN("Nilo-Saharan languages", null, LanguageType.GROUP, null, "ssa"),

	/** Niuean */
	NIUEAN("Niuean", null, LanguageType.NORMAL, null, "niu"),

	/** N'Ko */
	NKO("N'Ko", null, LanguageType.NORMAL, null, "nqo"),

	/** No linguistic content/Not applicable */
	NA("No linguistic content;Not applicable;N/A", null, LanguageType.NORMAL, null, "zxx"),

	/** Nogai */
	NOGAI("Nogai", null, LanguageType.NORMAL, null, "nog"),

	/** North American Indian languages */
	NORTH_AMERICAN_INDIAN("North American Indian languages", null, LanguageType.GROUP, null, "nai"),

	/** North Ndebele */
	NORTH_NDEBELE("North Ndebele", null, LanguageType.NORMAL, "nd", "nde"),

	/** Northern Frisian */
	NORTHERN_FRISIAN("Northern Frisian", null, LanguageType.NORMAL, null, "frr"),

	/** Northern Sami */
	NORTHERN_SAMI("Northern Sami", null, LanguageType.NORMAL, "se", "sme"),

	/** Northern Sotho/Pedi/Sepedi */
	NORTHERN_SOTHO("Northern Sotho;Pedi;Sepedi", null, LanguageType.NORMAL, null, "nso"),

	/** Norwegian Bokmål */
	NORWEGIAN_BOKMAAL("Norwegian Bokmål", null, LanguageType.NON_LANGUAGE, "nb", "nob"),

	/** Norwegian Nynorsk */
	NORWEGIAN_NYNORSK("Norwegian Nynorsk", null, LanguageType.NON_LANGUAGE, "nn", "nno"),

	/** Norwegian */
	NORWEGIAN("Norwegian", Messages.getString("Language.no"), LanguageType.NORMAL, "no", "nor"),

	/** Nubian/Nubian languages */
	NUBIAN("Nubian;Nubian languages", null, LanguageType.GROUP, null, "nub"),

	/** Nuosu/Sichuan Yi */
	NUOSU("Nuosu;Sichuan Yi", null, LanguageType.NORMAL, "ii", "iii"),

	/** Nyamwezi */
	NYAMWEZI("Nyamwezi", null, LanguageType.NORMAL, null, "nym"),

	/** Nyankole */
	NYANKOLE("Nyankole", null, LanguageType.NORMAL, null, "nyn"),

	/** Nyoro */
	NYORO("Nyoro", null, LanguageType.NORMAL, null, "nyo"),

	/** Nzima */
	NZIMA("Nzima", null, LanguageType.NORMAL, null, "nzi"),

	/** Occitan (post 1500) */
	OCCITAN("Occitan (post 1500)", null, LanguageType.NORMAL, "oc", "oci"),

	/** Ojibwa */
	OJIBWA("Ojibwa", null, LanguageType.NORMAL, "oj", "oji"),

	/** Old English (ca. 450-1100) */
	OLD_ENGLISH("Old English (ca. 450-1100)", null, LanguageType.HISTORICAL, null, "ang"),

	/** Old French (842-ca. 1400) */
	OLD_FRENCH("Old French (842-ca. 1400)", null, LanguageType.HISTORICAL, null, "fro"),

	/** Old High German (ca. 750-1050) */
	OLD_HIGH_GERMAN("Old High German (ca. 750-1050)", null, LanguageType.HISTORICAL, null, "goh"),

	/** Old Irish (to 900) */
	OLD_IRISH("Old Irish (to 900)", null, LanguageType.HISTORICAL, null, "sga"),

	/** Old Norse */
	OLD_NORSE("Old Norse", null, LanguageType.HISTORICAL, null, "non"),

	/** Old Occitan (to 1500)/Old Provençal (to 1500) */
	OLD_OCCITAN("Old Occitan (to 1500);Old Provençal (to 1500)", null, LanguageType.HISTORICAL, null, "pro"),

	/** Old Persian (ca. 600-400 B.C.) */
	OLD_PERSIAN("Old Persian (ca. 600-400 B.C.)", null, LanguageType.HISTORICAL, null, "peo"),

	/** Oriya (macrolanguage) */
	ORIYA("Oriya (macrolanguage)", null, LanguageType.NORMAL, "or", "ori"),

	/** Oromo */
	OROMO("Oromo", null, LanguageType.NORMAL, "om", "orm"),

	/** Osage */
	OSAGE("Osage", null, LanguageType.NORMAL, null, "osa"),

	/** Ossetian/ Ossetic */
	OSSETIAN("Ossetian; Ossetic", null, LanguageType.NORMAL, "os", "oss"),

	/** Otomian/Otomian languages */
	OTOMIAN("Otomian;Otomian languages", null, LanguageType.GROUP, null, "oto"),

	/** Ottoman Turkish (1500-1928) */
	OTTOMAN_TURKISH("Ottoman Turkish (1500-1928)", null, LanguageType.HISTORICAL, null, "ota"),

	/** Pahlavi */
	PAHLAVI("Pahlavi", null, LanguageType.NORMAL, null, "pal"),

	/** Palauan */
	PALAUAN("Palauan", null, LanguageType.NORMAL, null, "pau"),

	/** Pali */
	PALI("Pali", null, LanguageType.NORMAL, "pi", "pli"),

	/** Pangasinan */
	PANGASINAN("Pangasinan", null, LanguageType.NORMAL, null, "pag"),

	/** Panjabi/Punjabi */
	PUNJABI("Panjabi;Punjabi", null, LanguageType.NORMAL, "pa", "pan"),

	/** Papiamento */
	PAPIAMENTO("Papiamento", null, LanguageType.NORMAL, null, "pap"),

	/** Papuan/Papuan languages */
	PAPUAN("Papuan;Papuan languages", null, LanguageType.GROUP, null, "paa"),

	/** Pashto/Pushto */
	PASHTO("Pashto;Pushto", null, LanguageType.NORMAL, "ps", "pus"),

	/** Persian */
	PERSIAN("Persian", Messages.getString("Language.fa"), LanguageType.NORMAL, "fa", "per", "fas"),

	/** Philippine/Philippine languages */
	PHILIPPINE("Philippine;Philippine languages", null, LanguageType.GROUP, null, "phi"),

	/** Phoenician */
	PHOENICIAN("Phoenician", null, LanguageType.NORMAL, null, "phn"),

	/** Pohnpeian */
	POHNPEIAN("Pohnpeian", null, LanguageType.NORMAL, null, "pon"),

	/** Polish */
	POLISH("Polish", Messages.getString("Language.pl"), LanguageType.NORMAL, "pl", "pol"),

	/** Portuguese */
	PORTUGUESE("Portuguese", Messages.getString("Language.pt"), LanguageType.NORMAL, "pt", "por"),

	/** Prakrit/Prakrit languages */
	PRAKRIT("Prakrit;Prakrit languages", null, LanguageType.GROUP, null, "pra"),

	/** Quechua */
	QUECHUA("Quechua", null, LanguageType.NORMAL, "qu", "que"),

	/** Rajasthani */
	RAJASTHANI("Rajasthani", null, LanguageType.NORMAL, null, "raj"),

	/** Rapanui */
	RAPANUI("Rapanui", null, LanguageType.NORMAL, null, "rap"),

	/** Reserved for local use */
	RESERVED_LOCAL("Reserved for local use", null, LanguageType.NON_LANGUAGE, null, "qaa-qtz"),

	/** Romance/Romance languages */
	ROMANCE("Romance;Romance languages", null, LanguageType.GROUP, null, "roa"),

	/** Romansh */
	ROMANSH("Romansh", null, LanguageType.NORMAL, "rm", "roh"),

	/** Romany */
	ROMANY("Romany", null, LanguageType.NORMAL, null, "rom"),

	/** Rundi */
	RUNDI("Rundi", null, LanguageType.NORMAL, "rn", "run"),

	/** Russian */
	RUSSIAN("Russian", Messages.getString("Language.ru"), LanguageType.NORMAL, "ru", "rus"),

	/** Salishan/Salishan languages */
	SALISHAN("Salishan;Salishan languages", null, LanguageType.GROUP, null, "sal"),

	/** Samaritan Aramaic */
	SAMARITAN_ARAMAIC("Samaritan Aramaic", null, LanguageType.NORMAL, null, "sam"),

	/** Sami/Sami languages */
	SAMI("Sami;Sami languages", null, LanguageType.GROUP, null, "smi"),

	/** Samoan */
	SAMOAN("Samoan", null, LanguageType.NORMAL, "sm", "smo"),

	/** Sandawe */
	SANDAWE("Sandawe", null, LanguageType.NORMAL, null, "sad"),

	/** Sango */
	SANGO("Sango", null, LanguageType.NORMAL, "sg", "sag"),

	/** Sanskrit */
	SANSKRIT("Sanskrit", null, LanguageType.NORMAL, "sa", "san"),

	/** Santali */
	SANTALI("Santali", null, LanguageType.NORMAL, null, "sat"),

	/** Sardinian */
	SARDINIAN("Sardinian", null, LanguageType.NORMAL, "sc", "srd"),

	/** Sasak */
	SASAK("Sasak", null, LanguageType.NORMAL, null, "sas"),

	/** Scots */
	SCOTS("Scots", null, LanguageType.NORMAL, null, "sco"),

	/** Selkup */
	SELKUP("Selkup", null, LanguageType.NORMAL, null, "sel"),

	/** Semitic languages */
	SEMITIC("Semitic languages", null, LanguageType.GROUP, null, "sem"),

	/** Serbian */
	SERBIAN("Serbian", Messages.getString("Language.sr"), LanguageType.NORMAL, "sr", "srp"),

	/** Serer */
	SERER("Serer", null, LanguageType.NORMAL, null, "srr"),

	/** Shan */
	SHAN("Shan", null, LanguageType.NORMAL, null, "shn"),

	/** Shona */
	SHONA("Shona", null, LanguageType.NORMAL, "sn", "sna"),

	/** Sicilian */
	SICILIAN("Sicilian", null, LanguageType.NORMAL, null, "scn"),

	/** Sidamo */
	SIDAMO("Sidamo", null, LanguageType.NORMAL, null, "sid"),

	/** Sign/Sign Languages */
	SIGN("Sign;Sign Languages", null, LanguageType.GROUP, null, "sgn"),

	/** Siksika */
	SIKSIKA("Siksika", null, LanguageType.NORMAL, null, "bla"),

	/** Sindhi */
	SINDHI("Sindhi", null, LanguageType.NORMAL, "sd", "snd"),

	/** Sinhala/Sinhalese */
	SINHALESE("Sinhala;Sinhalese", null, LanguageType.NORMAL, "si", "sin"),

	/** Sino-Tibetan languages */
	SINO_TIBETAN("Sino-Tibetan languages", null, LanguageType.GROUP, null, "sit"),

	/** Siouan/Siouan languages */
	SIOUAN("Siouan;Siouan languages", null, LanguageType.GROUP, null, "sio"),

	/** Skolt Sami */
	SKOLT_SAMI("Skolt Sami", null, LanguageType.NORMAL, null, "sms"),

	/** Slave (Athapascan) */
	SLAVE("Slave (Athapascan)", null, LanguageType.NORMAL, null, "den"),

	/** Slavic languages */
	SLAVIC("Slavic languages", null, LanguageType.GROUP, null, "sla"),

	/** Slovak */
	SLOVAK("Slovak", Messages.getString("Language.sk"), LanguageType.NORMAL, "sk", "slo", "slk"),

	/** Slovenian */
	SLOVENIAN("Slovenian", Messages.getString("Language.sl"), LanguageType.NORMAL, "sl", "slv"),

	/** Sogdian */
	SOGDIAN("Sogdian", null, LanguageType.NORMAL, null, "sog"),

	/** Somali */
	SOMALI("Somali", null, LanguageType.NORMAL, "so", "som"),

	/** Songhai languages */
	SONGHAI("Songhai languages", null, LanguageType.GROUP, null, "son"),

	/** Soninke */
	SONINKE("Soninke", null, LanguageType.NORMAL, null, "snk"),

	/** Sorbian languages */
	SORBIAN("Sorbian languages", null, LanguageType.GROUP, null, "wen"),

	/** South American Indian languages */
	SOUTH_AMERICAN_INDIAN("South American Indian languages", null, LanguageType.GROUP, null, "sai"),

	/** South Ndebele */
	SOUTH_NDEBELE("South Ndebele", null, LanguageType.NORMAL, "nr", "nbl"),

	/** Southern Altai */
	SOUTHERN_ALTAI("Southern Altai", null, LanguageType.NORMAL, null, "alt"),

	/** Southern Sami */
	SOUTHERN_SAMI("Southern Sami", null, LanguageType.NORMAL, null, "sma"),

	/** Southern Sotho */
	SOUTHERN_SOTHO("Southern Sotho", null, LanguageType.NORMAL, "st", "sot"),

	/** Sranan Tongo */
	SRANAN_TONGO("Sranan Tongo", null, LanguageType.NORMAL, null, "srn"),

	/** Standard Moroccan Tamazight */
	TAMAZIGHT("Standard Moroccan Tamazight", null, LanguageType.NORMAL, null, "zgh"),

	/** Sukuma */
	SUKUMA("Sukuma", null, LanguageType.NORMAL, null, "suk"),

	/** Sumerian */
	SUMERIAN("Sumerian", null, LanguageType.NORMAL, null, "sux"),

	/** Sundanese */
	SUNDANESE("Sundanese", null, LanguageType.NORMAL, "su", "sun"),

	/** Susu */
	SUSU("Susu", null, LanguageType.NORMAL, null, "sus"),

	/** Swahili (macrolanguage) */
	SWAHILI("Swahili (macrolanguage)", null, LanguageType.NORMAL, "sw", "swa"),

	/** Swati */
	SWATI("Swati", null, LanguageType.NORMAL, "ss", "ssw"),

	/** Swedish */
	SWEDISH("Swedish", Messages.getString("Language.sv"), LanguageType.NORMAL, "sv", "swe"),

	/** Syriac */
	SYRIAC("Syriac", null, LanguageType.NORMAL, null, "syr"),

	/** Tagalog */
	TAGALOG("Tagalog", null, LanguageType.NORMAL, "tl", "tgl"),

	/** Tahitian */
	TAHITIAN("Tahitian", null, LanguageType.NORMAL, "ty", "tah"),

	/** Tai/Tai languages */
	TAI("Tai;Tai languages", null, LanguageType.GROUP, null, "tai"),

	/** Tajik */
	TAJIK("Tajik", null, LanguageType.NORMAL, "tg", "tgk"),

	/** Tamashek */
	TAMASHEK("Tamashek", null, LanguageType.NORMAL, null, "tmh"),

	/** Tamil */
	TAMIL("Tamil", null, LanguageType.NORMAL, "ta", "tam"),

	/** Tatar */
	TATAR("Tatar", null, LanguageType.NORMAL, "tt", "tat"),

	/** Telugu */
	TELUGU("Telugu", null, LanguageType.NORMAL, "te", "tel"),

	/** Tereno */
	TERENO("Tereno", null, LanguageType.NORMAL, null, "ter"),

	/** Tetum */
	TETUM("Tetum", null, LanguageType.NORMAL, null, "tet"),

	/** Thai */
	THAI("Thai", Messages.getString("Language.th"), LanguageType.NORMAL, "th", "tha"),

	/** Tibetan */
	TIBETAN("Tibetan", null, LanguageType.NORMAL, "bo", "tib", "bod"),

	/** Tigre */
	TIGRE("Tigre", null, LanguageType.NORMAL, null, "tig"),

	/** Tigrinya */
	TIGRINYA("Tigrinya", null, LanguageType.NORMAL, "ti", "tir"),

	/** Timne */
	TIMNE("Timne", null, LanguageType.NORMAL, null, "tem"),

	/** Tiv */
	TIV("Tiv", null, LanguageType.NORMAL, null, "tiv"),

	/** Tlingit */
	TLINGIT("Tlingit", null, LanguageType.NORMAL, null, "tli"),

	/** Tok Pisin */
	TOK_PISIN("Tok Pisin", null, LanguageType.NORMAL, null, "tpi"),

	/** Tokelau */
	TOKELAU("Tokelau", null, LanguageType.NORMAL, null, "tkl"),

	/** Tonga (Nyasa) */
	TONGA_NYASA("Tonga (Nyasa)", null, LanguageType.NORMAL, null, "tog"),

	/** Tonga (Tonga Islands) */
	TONGA_TONGA_ISLANDS("Tonga (Tonga Islands)", null, LanguageType.NORMAL, "to", "ton"),

	/** Tsimshian */
	TSIMSHIAN("Tsimshian", null, LanguageType.NORMAL, null, "tsi"),

	/** Tsonga */
	TSONGA("Tsonga", null, LanguageType.NORMAL, "ts", "tso"),

	/** Tswana */
	TSWANA("Tswana", null, LanguageType.NORMAL, "tn", "tsn"),

	/** Tumbuka */
	TUMBUKA("Tumbuka", null, LanguageType.NORMAL, null, "tum"),

	/** Tupi languages */
	TUPI("Tupi languages", null, LanguageType.GROUP, null, "tup"),

	/** Turkish */
	TURKISH("Turkish", Messages.getString("Language.tr"), LanguageType.NORMAL, "tr", "tur"),

	/** Turkmen */
	TURKMEN("Turkmen", null, LanguageType.NORMAL, "tk", "tuk"),

	/** Tuvalu */
	TUVALU("Tuvalu", null, LanguageType.NORMAL, null, "tvl"),

	/** Tuvinian */
	TUVINIAN("Tuvinian", null, LanguageType.NORMAL, null, "tyv"),

	/** Twi */
	TWI("Twi", null, LanguageType.NORMAL, "tw", "twi"),

	/** Udmurt */
	UDMURT("Udmurt", null, LanguageType.NORMAL, null, "udm"),

	/** Ugaritic */
	UGARITIC("Ugaritic", null, LanguageType.NORMAL, null, "uga"),

	/** Uighur/Uyghur */
	UYGHUR("Uighur;Uyghur", null, LanguageType.NORMAL, "ug", "uig"),

	/** Ukrainian */
	UKRAINIAN("Ukrainian", Messages.getString("Language.uk"), LanguageType.NORMAL, "uk", "ukr"),

	/** Umbundu */
	UMBUNDU("Umbundu", null, LanguageType.NORMAL, null, "umb"),

	/** Uncoded languages */
	UNCODED("Uncoded languages", null, LanguageType.NON_LANGUAGE, null, "mis"),

	/** Undetermined */
	UND("Undetermined", null, LanguageType.UNDEFINED, null, "und"),

	/** Upper Sorbian */
	UPPER_SORBIAN("Upper Sorbian", null, LanguageType.NORMAL, null, "hsb"),

	/** Urdu */
	URDU("Urdu", null, LanguageType.NORMAL, "ur", "urd"),

	/** Uzbek */
	UZBEK("Uzbek", null, LanguageType.NORMAL, "uz", "uzb"),

	/** Vai */
	VAI("Vai", null, LanguageType.NORMAL, null, "vai"),

	/** Venda */
	VENDA("Venda", null, LanguageType.NORMAL, "ve", "ven"),

	/** Vietnamese */
	VIETNAMESE("Vietnamese", Messages.getString("Language.vi"), LanguageType.NORMAL, "vi", "vie"),

	/** Volapük */
	VOLAPUK("Volapük", null, LanguageType.NORMAL, "vo", "vol"),

	/** Votic */
	VOTIC("Votic", null, LanguageType.NORMAL, null, "vot"),

	/** Wakashan languages */
	WAKASHAN("Wakashan languages", null, LanguageType.GROUP, null, "wak"),

	/** Walloon */
	WALLOON("Walloon", null, LanguageType.NORMAL, "wa", "wln"),

	/** Waray (Philippines) */
	WARAY("Waray (Philippines)", null, LanguageType.NORMAL, null, "war"),

	/** Washo */
	WASHO("Washo", null, LanguageType.NORMAL, null, "was"),

	/** Welsh */
	WELSH("Welsh", null, LanguageType.NORMAL, "cy", "wel", "cym"),

	/** Western Frisian */
	WESTERN_FRISIAN("Western Frisian", null, LanguageType.NORMAL, "fy", "fry"),

	/** Wolaitta/Wolaytta */
	WOLAYTTA("Wolaitta;Wolaytta", null, LanguageType.NORMAL, null, "wal"),

	/** Wolof */
	WOLOF("Wolof", null, LanguageType.NORMAL, "wo", "wol"),

	/** Xhosa */
	XHOSA("Xhosa", null, LanguageType.NORMAL, "xh", "xho"),

	/** Yakut */
	YAKUT("Yakut", null, LanguageType.NORMAL, null, "sah"),

	/** Yao */
	YAO("Yao", null, LanguageType.NORMAL, null, "yao"),

	/** Yapese */
	YAPESE("Yapese", null, LanguageType.NORMAL, null, "yap"),

	/** Yiddish */
	YIDDISH("Yiddish", null, LanguageType.NORMAL, "yi", "yid"),

	/** Yoruba */
	YORUBA("Yoruba", null, LanguageType.NORMAL, "yo", "yor"),

	/** Yupik/Yupik languages */
	YUPIK("Yupik;Yupik languages", null, LanguageType.GROUP, null, "ypk"),

	/** Zande/Zande languages */
	ZANDE("Zande;Zande languages", null, LanguageType.GROUP, null, "znd"),

	/** Zapotec */
	ZAPOTEC("Zapotec", null, LanguageType.NORMAL, null, "zap"),

	/** Zenaga */
	ZENAGA("Zenaga", null, LanguageType.NORMAL, null, "zen"),

	/** Zulu */
	ZULU("Zulu", null, LanguageType.NORMAL, "zu", "zul"),

	/** Zuni */
	ZUNI("Zuni", null, LanguageType.NORMAL, null, "zun");

	/**
	 * A {@link Map} of common language name misspellings and their correct
	 * counterparts
	 */
	public static final Map<String, String> COMMON_MISSPELLINGS;

	/**
	 * A {@link Map} of {@code ISO 639-1} and {@code ISO 639-2} codes mapped to
	 * the corresponding {@link ISO639} instances for fast lookups.
	 */
	public static final Map<String, ISO639> LOOKUP_CODES;

	/**
	 * A {@link Map} of {@code ISO 639} language names mapped to the
	 * corresponding {@link ISO639} instances for fast lookups.
	 */
	public static final Map<String, ISO639> LOOKUP_NAMES;

	/** ISO code alias for the language set in the preferences */
	public static final String LOCAL_ALIAS = "loc";

	static {
		// Populate misspellings
		HashMap<String, String> misspellings = new HashMap<>();
		misspellings.put("ameircan", "american");
		misspellings.put("artifical", "artificial");
		misspellings.put("brasillian", "brazilian");
		misspellings.put("carrib", "carib");
		misspellings.put("centeral", "central");
		misspellings.put("chineese", "chinese");
		misspellings.put("curch", "church");
		misspellings.put("dravadian", "dravidian");
		misspellings.put("enlish", "english");
		misspellings.put("euorpean", "european");
		misspellings.put("farsi", "persian");
		misspellings.put("hawaian", "hawaiian");
		misspellings.put("hebrwe", "hebrew");
		misspellings.put("japaneese", "japanese");
		misspellings.put("javaneese", "javanese");
		misspellings.put("laguage", "language");
		misspellings.put("madureese", "madurese");
		misspellings.put("malteese", "maltese");
		misspellings.put("maltesian", "maltese");
		misspellings.put("miscelaneous", "miscellaneous");
		misspellings.put("miscellanious", "miscellaneous");
		misspellings.put("miscellanous", "miscellaneous");
		misspellings.put("northen", "northern");
		misspellings.put("norweigan", "norwegian");
		misspellings.put("ottaman", "ottoman");
		misspellings.put("philipine", "philippine");
		misspellings.put("phonecian", "phoenician");
		misspellings.put("portugese", "portuguese");
		misspellings.put("rusian", "russian");
		misspellings.put("sinhaleese", "sinhalese");
		misspellings.put("sourth", "south");
		misspellings.put("spainish", "spanish");
		misspellings.put("sweedish", "swedish");
		misspellings.put("ukranian", "ukrainian");
		misspellings.put("vietnameese", "vietnamese");
		COMMON_MISSPELLINGS = Collections.unmodifiableMap(misspellings);

		// Populate lookup maps
		Map<String, ISO639> codes = new HashMap<>();
		Map<String, ISO639> names = new HashMap<>();
		for (ISO639 entry : values()) {
			for (String name : entry.names) {
				if (isNotBlank(name)) {
					names.put(name.replaceAll("\\s*\\([^\\)]*\\)\\s*", "").toLowerCase(Locale.ROOT), entry);
				}
			}
			if (isNotBlank(entry.localizedName)) {
				names.put(entry.localizedName.trim().toLowerCase(Locale.ROOT), entry);
			}
			if (isNotBlank(entry.iso639Part1)) {
				codes.put(entry.iso639Part1, entry);
			}
			if (isNotBlank(entry.iso639Part2B)) {
				codes.put(entry.iso639Part2B, entry);
			}
			codes.put(entry.iso639Part2T, entry);
		}
		LOOKUP_CODES = Collections.unmodifiableMap(codes);
		LOOKUP_NAMES = Collections.unmodifiableMap(names);
	}

	@Nonnull
	private final LanguageType type;

	@Nonnull
	private final List<String> names;

	@Nullable
	final String localizedName;

	@Nullable
	private final String iso639Part1;

	@Nullable
	private final String iso639Part2B;

	@Nonnull
	private final String iso639Part2T;

	private ISO639(
		@Nonnull String names,
		@Nullable String localizedName,
		@Nonnull LanguageType type,
		@Nullable String part1,
		@Nonnull String part2T
	) {
		this.type = type;
		this.names = Collections.unmodifiableList(Arrays.asList(StringUtil.SEMICOLON.split(names)));
		this.iso639Part1 = part1;
		this.iso639Part2B = null;
		this.iso639Part2T = part2T;
		this.localizedName = localizedName;
	}

	private ISO639(
		@Nonnull String names,
		@Nullable String localizedName,
		@Nonnull LanguageType type,
		@Nullable String part1,
		@Nonnull String part2B,
		@Nonnull String part2T
	) {
		this.type = type;
		this.names = Arrays.asList(StringUtil.SEMICOLON.split(names));
		this.iso639Part1 = part1;
		this.iso639Part2B = part2B;
		this.iso639Part2T = part2T;
		this.localizedName = localizedName;
	}


	@Override
	public String getName() {
		return getFirstName();
	}

	/**
	 * @return The first registered English language name.
	 */
	@Nonnull
	public String getFirstName() {
		return names.get(0);
	}

	/**
	 * @return The English language names.
	 */
	@Nonnull
	public List<String> getNames() {
		return names;
	}

	@Override
	public String getLocalizedName() {
		return localizedName;
	}

	@Override
	public String getLocalizedNameFallback() {
		return isNotBlank(localizedName) ? localizedName : names.get(0);
	}

	@Override
	public String get2LetterCode() {
		return getPart1();
	}

	/**
	 * @return The {@code ISO 639-1} (2-letter) code.
	 */
	@Nullable
	public String getPart1() {
		return iso639Part1;
	}

	@Override
	public String getCode() {
		return getPart2B();
	}

	/**
	 * @return The bibliographic {@code ISO 639-2} (3-letter) code.
	 */
	@Nonnull
	public String getPart2B() {
		return iso639Part2B == null ? iso639Part2T : iso639Part2B;
	}

	/**
	 * @return The terminology {@code ISO 639-2} (3-letter) code.
	 */
	@Nonnull
	public String getPart2T() {
		return iso639Part2T;
	}

	/**
	 * Gets the shortest possible (as per {@link Locale} specification)
	 * {@code ISO 639} (2- or 3-letter) code.
	 *
	 * @return The {@code ISO 639-1} (2-letter), {@code ISO 639-2} (3-letter)
	 *         code.
	 */
	@Override
	@Nonnull
	public String getShortestCode() {
		return isNotBlank(iso639Part1) ? iso639Part1 : getPart2B();
	}

	@Override
	public LanguageType getType() {
		return type;
	}

	/**
	 * Verifies if the specified {@code ISO 639} code matches any of the
	 * {@code ISO 639} codes for this instance.
	 *
	 * @param code the {@code ISO 639} (2- or 3-letter) code.
	 * @return {@code true} if a match is found, {@code false} otherwise.
	 */
	public boolean matches(@Nullable String code) {
		if (isBlank(code)) {
			return false;
		}
		return matchesInternal(normalize(code));
	}

	/**
	 * Verifies if the specified lower-case {@code ISO 639} code matches any of
	 * the {@code ISO 639} codes for this instance.
	 * <p>
	 * <b>Note:</b> {@code code} must already be in lower-case.
	 *
	 * @param code the lower-case {@code ISO 639} (2- or 3-letter) code.
	 * @return {@code true} if a match is found, {@code false} otherwise.
	 */
	private boolean matchesInternal(@Nullable String code) {
		if (code == null) {
			return false;
		}
		return code.equals(iso639Part1) || code.equals(iso639Part2B) || code.equals(iso639Part2T);
	}

	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Returns a {@link String} representation of this instance.
	 *
	 * @param debug if {@code true} the result includes all fields, if
	 *            {@code false} only the the first language name is returned.
	 * @return The {@link String} representation.
	 */
	public String toString(boolean debug) {
		if (!debug) {
			return names.get(0);
		}
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append(" [");
		if (names.size() > 1) {
			sb.append("Names=").append(StringUtil.createReadableCombinedString(names, true));
		} else {
			sb.append("Name=").append("\"").append(names.get(0)).append("\"");
		}
		if (isNotBlank(iso639Part1)) {
			sb.append(", 639-1=").append(iso639Part1);
		}
		sb.append(", 639-2=");
		if (iso639Part2B != null) {
			sb.append(iso639Part2B).append(" (B), ").append(iso639Part2T);
		} else {
			sb.append(iso639Part2T);
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Gets the {@link ISO639} for an {@code ISO 639} code, or {@code null} if
	 * no match is found.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code to find.
	 * @return The matching {@link ISO639} or {@code null}.
	 */
	@Nullable
	public static ISO639 getCode(@Nullable String code) {
		return code == null ? null : LOOKUP_CODES.get(normalize(code));
	}

	/**
	 * Gets the {@link ISO639} for an English {@code ISO 639} language name or
	 * an {@code ISO 639} code, or {@code null} if no match is found.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @return The matching {@link ISO639} or {@code null}.
	 */
	@Nullable
	public static ISO639 get(@Nullable String code) {
		return get(code, false);
	}

	/**
	 * Gets the {@link ISO639} for an English {@code ISO 639} language name or
	 * an {@code ISO 639} code, or {@code null} if no match is found. Can
	 * optionally also search {@code code} for the English language name.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @param containsName if {@code true}, a search for the English language
	 *            name will also be performed.
	 * @return The matching {@link ISO639} or {@code null}.
	 */
	@Nullable
	public static ISO639 get(@Nullable String code, boolean containsName) {
		if (isBlank(code)) {
			return null;
		}
		code = normalize(code);
		ISO639 result = LOOKUP_CODES.get(code);
		if (result != null) {
			return result;
		}
		result = LOOKUP_NAMES.get(code);
		if (result != null) {
			return result;
		}

		String correctedCode = COMMON_MISSPELLINGS.get(code);
		if (correctedCode != null) {
			result = LOOKUP_NAMES.get(correctedCode);
			if (result != null) {
				return result;
			}
			code = correctedCode;
		}

		if (containsName && code.length() > 2) {
			// Do a search for a match for the language name in "code"
			for (Entry<String, String> misspelling : COMMON_MISSPELLINGS.entrySet()) {
				if (code.contains(misspelling.getKey())) {
					code = code.replace(misspelling.getKey(), misspelling.getValue());
				}
			}
			result = LOOKUP_NAMES.get(code);
			if (result != null) {
				return result;
			}

			for (Entry<String, ISO639> entry : LOOKUP_NAMES.entrySet()) {
				if (code.contains(entry.getKey())) {
					return entry.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the first defined English {@code ISO 639} language name for an
	 * English {@code ISO 639} language name or an {@code ISO 639} code, or
	 * {@code null} if no match is found.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @return The {@code ISO 639} English language name or {@code null}.
	 */
	@Nullable
	public static String getFirstName(@Nullable String code) {
		return getFirstName(code, false);
	}

	/**
	 * Gets the first defined English {@code ISO 639} language name for an
	 * English {@code ISO 639} language name or an {@code ISO 639} code, or
	 * {@code null} if no match is found. Can optionally also search
	 * {@code code} for the English language name.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @param containsName if {@code true}, a search for the English language
	 *            name will also be performed.
	 * @return The {@code ISO 639} English language name or {@code null}.
	 */
	@Nullable
	public static String getFirstName(@Nullable String code, boolean containsName) {
		ISO639 entry = get(code, containsName);
		return entry == null ? null : entry.getFirstName();
	}

	/**
	 * Gets the {@link List} of English {@code ISO 639} language names for an
	 * English {@code ISO 639} language name or an {@code ISO 639} code, or
	 * {@code null} if no match is found.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @return The array of {@code ISO 639} English language names or
	 *         {@code null}.
	 */
	@Nullable
	public static List<String> getNames(@Nullable String code) {
		return getNames(code, false);
	}

	/**
	 * Gets the array of English {@code ISO 639} language names for an English
	 * {@code ISO 639} language name or an {@code ISO 639} code, or {@code null}
	 * if no match is found. Can optionally also search {@code code} for the
	 * English language name.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @param containsName if {@code true}, a search for the English language
	 *            name will also be performed.
	 * @return The array of {@code ISO 639} English language names or
	 *         {@code null}.
	 */
	@Nullable
	public static List<String> getNames(@Nullable String code, boolean containsName) {
		ISO639 entry = get(code, containsName);
		return entry == null ? null : entry.getNames();
	}

	/**
	 * Gets the shortest possible (as per {@link Locale} specification)
	 * {@code ISO 639} (2- or 3-letter) code for an English {@code ISO 639}
	 * language name or an {@code ISO 639} code, or {@code null} if no match can
	 * be found.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @return The {@code ISO 639-1} (2-letter), {@code ISO 639-2} (three
	 *         letter) code or {@code null}.
	 */
	@Nullable
	public static String getISOCode(@Nullable String code) {
		return getISOCode(code, false);
	}

	/**
	 * Gets the shortest possible (as per {@link Locale} specification)
	 * {@code ISO 639} (2- or 3-letter) code for an English {@code ISO 639}
	 * language name or an {@code ISO 639} code, or {@code null} if no is found.
	 * Can optionally also search {@code code} for the English language name.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @param containsName if {@code true}, a search for the English language
	 *            name will also be performed.
	 * @return The {@code ISO 639-1} (2-letter), {@code ISO 639-2} (three
	 *         letter) code or {@code null}.
	 */
	@Nullable
	public static String getISOCode(@Nullable String code, boolean containsName) {
		ISO639 entry = get(code, containsName);
		return entry == null ? null : entry.getShortestCode();
	}

	/**
	 * Gets the {@code ISO 639-2} (3-letter) code for an English {@code ISO 639}
	 * language name or an {@code ISO 639} code, or {@code null} if no match is
	 * found.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @return The {@code ISO 639-2} (3-letter) code or {@code null}.
	 */
	@Nullable
	public static String getISO639Part2Code(@Nullable String code) {
		return getISO639Part2Code(code, false);
	}

	/**
	 * Gets the {@code ISO 639-2} (3-letter) code for an English {@code ISO 639}
	 * language name or an {@code ISO 639} code, or {@code null} if no match is
	 * found. Can optionally also search {@code code} for the English language
	 * name.
	 *
	 * @param code the {@code ISO 639} 2- or 3-letter code or the English
	 *            language name to find.
	 * @param containsName if {@code true}, a search for the English language
	 *            name will also be performed.
	 * @return The {@code ISO 639-2} (3-letter) code or {@code null}.
	 */
	@Nullable
	public static String getISO639Part2Code(@Nullable String code, boolean containsName) {
		ISO639 entry = get(code, containsName);
		return entry == null ? null : entry.getPart2B();
	}

	/**
	 * Returns the code after trimming and converting it to lower-case, except
	 * if the alias "{@code loc}" is used. In that case the {@code ISO 639} code
	 * of the preferred language in the DMS settings is returned.
	 *
	 * @param isoCode an {@code ISO 639} code, or "{@code loc}".
	 * @return The code.
	 */
	@Nullable
	private static String normalize(@Nullable String isoCode) {
		if (isBlank(isoCode)) {
			return isoCode;
		}
		isoCode = isoCode.trim().toLowerCase(Locale.ROOT);
		if (LOCAL_ALIAS.equals(isoCode)) {
			String tag = PMS.getLocale().toLanguageTag();
			int idx = tag.indexOf('-');
			isoCode = idx > 0 ? tag.substring(0, idx) : tag;
			if (isBlank(isoCode)) {
				isoCode = "eng"; // Fall back to English
			}
		}
		return isoCode;
	}

	/**
	 * Verifies that a {@code ISO 639} English language name is matching an
	 * {@code ISO 639} code. Returns {@code true} if a match can be made,
	 * {@code false} otherwise.
	 *
	 * @param language the full language name.
	 * @param code the {@code ISO 639} code. If "{@code loc}" is specified, the
	 *            ISO code of the preferred language is used instead.
	 * @return {@code true} if they match, {@code false} otherwise.
	 */
	public static boolean isCodeMatching(@Nullable String language, @Nullable String code) {
		if (isBlank(language) || isBlank(code)) {
			return false;
		}

		ISO639 codeEntry = getCode(code);
		if (codeEntry == null) {
			return false;
		}
		ISO639 nameEntry = LOOKUP_NAMES.get(language.trim().toLowerCase(Locale.ROOT));

		return codeEntry == nameEntry;
	}

	/**
	 * Verifies that two {@code ISO 639} codes match the same language. Returns
	 * {@code true} if a match can be made, {@code false} otherwise. If the
	 * alias "{@code loc}" is used as a code, it will be replaced by the
	 * {@code ISO 639} code of the preferred language from the DMS settings.
	 *
	 * @param code1 The first {@code ISO 639} code.
	 * @param code2 The second {@code ISO 639} code.
	 * @return {@code true} if both match, {@code false} otherwise.
	 */
	public static boolean isCodesMatching(@Nullable String code1, @Nullable String code2) {
		ISO639 code1Entry = getCode(code1);
		if (code1Entry == null) {
			return false;
		}
		ISO639 code2Entry = getCode(code2);

		return code1Entry == code2Entry;
	}

	/**
	 * Converts an {@code IETF BCP 47} language tag to an {@link ISO639}.
	 *
	 * @param bcp47Tag the {@code IETF BCP 47} language tag to convert.
	 * @return The {@link ISO639} or {@code null}.
	 */
	@Nullable
	public static ISO639 fromBCP47(@Nullable String bcp47Tag) {
		if (isBlank(bcp47Tag)) {
			return null;
		}
		int remove = bcp47Tag.indexOf('-');
		int slash = bcp47Tag.indexOf('/');
		if (remove >= 0 && slash >= 0) {
			remove = Math.min(remove, slash);
		} else if (slash >= 0) {
			remove = slash;
		}
		if (remove >= 0) {
			bcp47Tag = bcp47Tag.substring(0, remove);
		}
		return get(bcp47Tag);
	}

}
