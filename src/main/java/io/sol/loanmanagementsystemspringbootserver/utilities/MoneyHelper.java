package io.sol.loanmanagementsystemspringbootserver.utilities;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyHelper {
    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    static {
        MONEY_FORMAT.setMinimumFractionDigits(0);
        MONEY_FORMAT.setMaximumFractionDigits(2);
    }

    public static String formatMoney(BigDecimal value){
        return MONEY_FORMAT.format(value == null ? BigDecimal.ZERO : value);
    }

}
