/**
 * PaymentMethodChecker.java
 * @author Vagisha Sharma
 * Jun 23, 2011
 */
package org.uwpr.www.costcenter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
public class PaymentMethodChecker {

	private static final PaymentMethodChecker instance = new PaymentMethodChecker();
	
	private PaymentMethodChecker() {}
	
	public static PaymentMethodChecker getInstance() {
		return instance;
	}
	
	private static final Pattern uwbudgetNumberPattern = Pattern.compile("\\d{2}-\\d{4}");
	private static final String invalidBudgetNumber = "00-0000";
	
	public boolean checkUwbudgetNumber(String budgetNumberString) {
		Matcher m = uwbudgetNumberPattern.matcher(budgetNumberString.trim());
		if(m.matches())
		{
			return !budgetNumberString.equals(invalidBudgetNumber);
		}
		return false;
	}
	
	public boolean checkPonumber(String ponumber) {
		return ponumber.trim().length() >= 4;
	}
	
}
