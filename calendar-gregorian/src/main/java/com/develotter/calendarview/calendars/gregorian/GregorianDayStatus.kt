package com.develotter.calendarview.calendars.gregorian

import com.develotter.calendarview.status.DayStatus
import java.time.LocalDate
import java.util.Locale

/**
 * Represents the status of a specific day in the Gregorian calendar.
 *
 * This class extends the [DayStatus] class and provides information about a given day
 * within the Gregorian calendar system. It stores the date and the locale in use for potential
 * localization of day-related information (though this is not used within this base class).
 *
 * @property localDate The [LocalDate] representing the specific day.
 * @property lcInUse The [Locale] in use for potential localization.
 *
 * @constructor Creates a [GregorianDayStatus] instance.
 * @param localDate The [LocalDate] representing the specific day.
 * @param lcInUse The [Locale] in use.
 *
 * @see DayStatus
 * @see LocalDate
 * @see Locale
 */
class GregorianDayStatus(localDate: LocalDate, lcInUse:Locale) : DayStatus(localDate,lcInUse){}