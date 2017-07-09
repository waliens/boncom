package be.mormont.iacf.boncom.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Date: 09-07-17
 * By  : Mormont Romain
 */
public class StringUtil {
    public static String getNotEmptyOrNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    public static String formatCurrency(BigDecimal currency) {
        Locale locale = Locale.getDefault();
        Currency currentCurrency = Currency.getInstance(locale);
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        return currencyFormatter.format(currency);
    }
}
