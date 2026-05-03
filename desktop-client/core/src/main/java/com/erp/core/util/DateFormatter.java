package com.erp.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateFormatter {
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private DateFormatter() {}

    public static String format(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) return "—";
        try {
            var instant = Instant.parse(isoTimestamp);
            var ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return ldt.format(DISPLAY);
        } catch (Exception e) {
            return isoTimestamp.length() > 10 ? isoTimestamp.substring(0, 10) : isoTimestamp;
        }
    }

    public static String formatWithTime(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) return "—";
        try {
            var instant = Instant.parse(isoTimestamp);
            var ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return ldt.format(DISPLAY_TIME);
        } catch (Exception e) {
            return isoTimestamp;
        }
    }
}
