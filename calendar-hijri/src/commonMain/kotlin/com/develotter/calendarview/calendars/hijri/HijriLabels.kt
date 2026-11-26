package com.develotter.calendarview.calendars.hijri

import com.develotter.calendarview.locale.CalendarLocale

/**
 * Hijri label tables for an explicitly selected [CalendarLocale].
 *
 * Only used when the caller passes a locale; `null` keeps `HijriPlatform` and its platform
 * default. Unknown language tags fall back to English transliterations.
 */
internal object HijriLabels {
    private val MONTHS: Map<String, List<String>> = mapOf(
        "en" to listOf(
            "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani", "Jumada al-Awwal",
            "Jumada al-Thani", "Rajab", "Shaban", "Ramadan", "Shawwal", "Dhu al-Qadah",
            "Dhu al-Hijjah",
        ),
        "de" to listOf(
            "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani", "Jumada al-Awwal",
            "Jumada al-Thani", "Rajab", "Shaban", "Ramadan", "Shawwal", "Dhu al-Qadah",
            "Dhu al-Hijjah",
        ),
        "fa" to listOf(
            "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه",
        ),
        "ar" to listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
        ),
        "fr" to listOf(
            "Mouharram", "Safar", "Rabia al awal", "Rabia ath-thani", "Joumada al oula",
            "Joumada ath-thania", "Rajab", "Chaabane", "Ramadan", "Chawwal", "Dhou al qi'da",
            "Dhou al hijja",
        ),
        "es" to listOf(
            "Muharram", "Safar", "Rabi al-awwal", "Rabi al-thani", "Yumada al-ula",
            "Yumada al-zania", "Rayab", "Shaaban", "Ramadán", "Shawwal", "Dhu al-Qada",
            "Dhu al-Hiyya",
        ),
        "zh" to listOf(
            "穆哈兰姆月", "色法尔月", "赖比尔·敖外鲁月", "赖比尔·阿色尼月", "主马达·敖外鲁月",
            "主马达·阿色尼月", "赖哲卜月", "舍尔邦月", "斋月", "闪瓦鲁月", "都尔喀尔德月",
            "都尔黑哲月",
        ),
    )

    private val SHORT_MONTHS: Map<String, List<String>> = mapOf(
        "en" to listOf(
            "Muh", "Saf", "Rab I", "Rab II", "Jum I", "Jum II",
            "Raj", "Sha", "Ram", "Shaw", "Qid", "Hij",
        ),
        "de" to listOf(
            "Muh", "Saf", "Rab I", "Rab II", "Jum I", "Jum II",
            "Raj", "Sha", "Ram", "Shaw", "Qid", "Hij",
        ),
        "fa" to listOf(
            "محرم", "صفر", "ربیع۱", "ربیع۲", "جمادی۱", "جمادی۲",
            "رجب", "شعبان", "رمضان", "شوال", "قعده", "حجه",
        ),
        "ar" to listOf(
            "محرم", "صفر", "ربيع١", "ربيع٢", "جمادى١", "جمادى٢",
            "رجب", "شعبان", "رمضان", "شوال", "قعدة", "حجة",
        ),
        "fr" to listOf(
            "Mou", "Saf", "Rab I", "Rab II", "Jou I", "Jou II",
            "Raj", "Cha", "Ram", "Chaw", "Qid", "Hij",
        ),
        "es" to listOf(
            "Muh", "Saf", "Rab I", "Rab II", "Yum I", "Yum II",
            "Ray", "Sha", "Ram", "Shaw", "Qad", "Hiy",
        ),
        "zh" to listOf(
            "穆哈兰姆月", "色法尔月", "赖比尔·敖外鲁月", "赖比尔·阿色尼月", "主马达·敖外鲁月",
            "主马达·阿色尼月", "赖哲卜月", "舍尔邦月", "斋月", "闪瓦鲁月", "都尔喀尔德月",
            "都尔黑哲月",
        ),
    )

    private val WEEKDAYS_SHORT: Map<String, List<String>> = mapOf(
        "en" to listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        "de" to listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"),
        "fa" to listOf("د", "س", "چ", "پ", "ج", "ش", "ی"),
        "ar" to listOf("إث", "ثل", "أر", "خم", "جم", "سب", "أح"),
        "fr" to listOf("lun.", "mar.", "mer.", "jeu.", "ven.", "sam.", "dim."),
        "es" to listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom"),
        "zh" to listOf("一", "二", "三", "四", "五", "六", "日"),
    )

    private val WEEKDAYS_FULL: Map<String, List<String>> = mapOf(
        "en" to listOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
        ),
        "de" to listOf(
            "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag",
        ),
        "fa" to listOf(
            "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه",
        ),
        "ar" to listOf(
            "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد",
        ),
        "fr" to listOf("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"),
        "es" to listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"),
        "zh" to listOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"),
    )

    private val DIGITS: Map<String, String> = mapOf(
        "en" to "0123456789",
        "de" to "0123456789",
        "fa" to "۰۱۲۳۴۵۶۷۸۹",
        "ar" to "٠١٢٣٤٥٦٧٨٩",
        "fr" to "0123456789",
        "es" to "0123456789",
        "zh" to "0123456789",
    )

    /** Full month name of [month] (`1..12`). */
    fun monthName(locale: CalendarLocale, month: Int): String {
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        return table(MONTHS, locale)[month - 1]
    }

    /** Abbreviated month name of [month] (`1..12`). */
    fun shortMonthName(locale: CalendarLocale, month: Int): String {
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        return table(SHORT_MONTHS, locale)[month - 1]
    }

    /** Month caption such as `Shawwal 1447`, `Shawwal de 1447` (`es`) or `1447年闪瓦鲁月` (`zh`). */
    fun monthYear(locale: CalendarLocale, month: Int, year: Int): String = when (languageOf(locale)) {
        "es" -> "${monthName(locale, month)} de ${number(locale, year)}"
        "zh" -> "${number(locale, year)}年${monthName(locale, month)}"
        else -> "${monthName(locale, month)} ${number(locale, year)}"
    }

    /** Localized full date label including weekday, day, month name and year. */
    fun fullDate(locale: CalendarLocale, date: HijriDate, weekdayEpochDay: Long): String {
        val weekday = weekdayFull(locale, isoWeekdayOf(weekdayEpochDay))
        val day = number(locale, date.dayOfMonth)
        val monthName = monthName(locale, date.month)
        val year = number(locale, date.year)
        return when (languageOf(locale)) {
            "de" -> "$weekday, $day. $monthName $year"
            "fr" -> "$weekday $day $monthName $year"
            "es" -> "$weekday, $day de $monthName de $year"
            "zh" -> "${year}年$monthName${day}日$weekday"
            "fa", "ar" -> "$weekday، $day $monthName $year"
            else -> "$weekday, $day $monthName $year"
        }
    }

    /** Short day and month label such as `20 Shaw`, `闪瓦鲁月20日` or `۲۰ شوال`. */
    fun shortDayMonth(locale: CalendarLocale, date: HijriDate): String =
        if (languageOf(locale) == "zh") {
            "${shortMonthName(locale, date.month)}${number(locale, date.dayOfMonth)}日"
        } else {
            "${number(locale, date.dayOfMonth)} ${shortMonthName(locale, date.month)}"
        }

    /** Short day, month and year label such as `20 Shaw 1447`. */
    fun shortDayMonthYear(locale: CalendarLocale, date: HijriDate): String =
        if (languageOf(locale) == "zh") {
            "${number(locale, date.year)}年${shortDayMonth(locale, date)}"
        } else {
            "${shortDayMonth(locale, date)} ${number(locale, date.year)}"
        }

    /** Short standalone weekday name for an ISO weekday (`1` = Monday .. `7` = Sunday). */
    fun weekdayShort(locale: CalendarLocale, isoWeekday: Int): String {
        require(isoWeekday in 1..7) { "isoWeekday must be in 1..7 but was $isoWeekday" }
        return table(WEEKDAYS_SHORT, locale)[isoWeekday - 1]
    }

    /** Full weekday name for an ISO weekday (`1` = Monday .. `7` = Sunday). */
    fun weekdayFull(locale: CalendarLocale, isoWeekday: Int): String {
        require(isoWeekday in 1..7) { "isoWeekday must be in 1..7 but was $isoWeekday" }
        return table(WEEKDAYS_FULL, locale)[isoWeekday - 1]
    }

    /** [value] without digit grouping using the language's digits. */
    fun number(locale: CalendarLocale, value: Int): String {
        val digits = DIGITS.getValue(languageOf(locale))
        return value.toString().map { char -> if (char in '0'..'9') digits[char - '0'] else char }
            .joinToString("")
    }

    /** ISO weekday (Monday 1 .. Sunday 7) of an absolute epoch day. */
    private fun isoWeekdayOf(epochDay: Long): Int = (((epochDay + 3) % 7 + 7) % 7).toInt() + 1

    private fun languageOf(locale: CalendarLocale): String =
        locale.language.takeIf { it in MONTHS } ?: "en"

    private fun table(tables: Map<String, List<String>>, locale: CalendarLocale): List<String> =
        tables.getValue(languageOf(locale))
}
