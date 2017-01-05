#!/usr/bin/env kotlin-script.sh
package date_me

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.ISODateTimeFormat
import java.util.*

fun main(args: Array<String>) {

    val instant = if (args.isEmpty()) Instant.now() else parseDate(args[0])
    timestamp(instant)
}

fun parseDate(input: String): Instant {
    if (input.isNullOrBlank()) {
        println("No argument given, using current timestamp")
        return Instant.now()
    }
    if (input.all { it in '0'..'9' }) {
        val instantMS = Instant(input.toLong())
        val instantS = Instant(input.toLong().times(1000))
        if (instantMS.isAfter(DateTime(1971, 1, 1, 0, 0))) {
            return instantMS
        } else {
            return instantS
        }
    }
    val formatters = hashMapOf("iso" to ISODateTimeFormat.dateTimeParser(), "mysql" to mySql)
    for ((name, formatter) in formatters) {
        println("Trying formatter $name")
        try {
            return formatter.parseDateTime(input).toInstant()
        } catch (e: IllegalArgumentException) {
            println("Cannot parse with formatter $name")
        }
    }
    throw IllegalArgumentException("Cannot parse $input")
}

val mySql = DateTimeFormatterBuilder()
    .appendYear(4, 4)
    .appendLiteral('-')
    .appendMonthOfYear(2)
    .appendLiteral('-')
    .appendDayOfMonth(2)
    .appendLiteral(' ')
    .appendHourOfDay(2)
    .appendLiteral(':')
    .appendMinuteOfHour(2)
    .appendLiteral(':')
    .appendSecondOfMinute(2)
    .toFormatter()

val simpleDate = DateTimeFormatterBuilder()
    .appendYear(4, 4)
    .appendLiteral('-')
    .appendMonthOfYear(2)
    .appendLiteral('-')
    .appendDayOfMonth(2)
    .toFormatter()


fun timestamp(instant: Instant) {
    val utc = instant.toDateTime(DateTimeZone.UTC)
    val berlin = instant.toDateTime(DateTimeZone.forID("Europe/Berlin"))
    val default = instant.toDateTime(DateTimeZone.getDefault())

    val text = """"
${instant.millis / 1000}     | seconds since EPOCH
${instant.millis}            | milliseconds since EPOCH
${utc.toString(simpleDate)}  | UTC time (simple format)
${utc}                       | UTC time
${utc.toString(mySql)}       | UTC time (MySql format)
$berlin                      | Berlin Time
$default                     | Time in the JDK default time zone
${DateTimeFormat.fullDateTime().withLocale(Locale.FRANCE).print(utc)} | fullDateTime() France

    """.trim()
    text.lines().forEach {
        if (it.contains("|")) {
            val (value, explication) = it.split("|")
            println(String.format("%-40s %s", value.trimEnd(), explication))
        } else {
            println(it)
        }

    }
}
