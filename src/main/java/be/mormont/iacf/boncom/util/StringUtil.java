package be.mormont.iacf.boncom.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Pattern;

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

    /**
     * Build a pattern loosely matching the query.
     * Loosely matching means that the characters in the query do not have to be adjacent in the
     * filtered string to have a match.
     * @param query The query string
     * @param caseSensitive True for the final regex to be case sensitive
     * @return A compiled pattern
     */
    public static Pattern getSearchPatternFromQuery(String query, boolean caseSensitive) {
        StringBuilder regex = new StringBuilder();
        final String WILDCARD = ".*";
        regex.append("^");
        for (int i = 0; i < query.length(); ++i) {
            regex.append(WILDCARD);
            regex.append(query.charAt(i));
        }
        regex.append(WILDCARD);
        regex.append("$");
        return Pattern.compile(regex.toString(), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
    }

    public static String trimOrNull(String s) {
        return s != null ? s.trim() : s;
    }
}
