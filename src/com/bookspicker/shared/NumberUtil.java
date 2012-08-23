package com.bookspicker.shared;

import com.google.gwt.i18n.client.NumberFormat;

public class NumberUtil {
	
	private static final NumberFormat CURRENCY_FORMATTER = 
		NumberFormat.getFormat("$#,##0.00;($#,##0.00)");
	private static final NumberFormat CURRENCY_FORMATTER_NO_SIGN = 
		NumberFormat.getFormat("#,##0.00;(#,##0.00)");
	private static final NumberFormat CURRENCY_FORMATTER_NO_COMMA = 
		NumberFormat.getFormat("$0.00;($0.00)");
	private static final NumberFormat CURRENCY_FORMATTER_NO_COMMA_NO_SIGN = 
		NumberFormat.getFormat("0.00;(0.00)");
	
	public static String getDisplayPrice(int priceInPennies) {
		return getDisplayPrice(priceInPennies, true);
	}
	
	public static String getDisplayPrice(int priceInPennies, boolean includeSign) {
		return getDisplayPrice(priceInPennies, includeSign, true);
	}
	
	public static String getDisplayPrice(int priceInPennies, boolean includeSign, boolean includeComma) {
		double priceInDollars = priceInPennies * 1.0 / 100;
		if (includeSign)
			if (includeComma)
				return CURRENCY_FORMATTER.format(priceInDollars);
			else
				return CURRENCY_FORMATTER_NO_COMMA.format(priceInDollars);
		else
			if (includeComma)
				return CURRENCY_FORMATTER_NO_SIGN.format(priceInDollars);
			else
				return CURRENCY_FORMATTER_NO_COMMA_NO_SIGN.format(priceInDollars);
	}

}
