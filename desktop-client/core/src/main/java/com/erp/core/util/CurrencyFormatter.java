package com.erp.core.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {
    private static final NumberFormat USD = NumberFormat.getCurrencyInstance(Locale.US);

    private CurrencyFormatter() {}

    public static String fromCents(long cents) {
        return USD.format(cents / 100.0);
    }

    public static String format(double amount) {
        return USD.format(amount);
    }
}
