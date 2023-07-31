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

	private static final Pattern worktagPattern = Pattern.compile("^(GR|GF|PG|CC|SAG)\\d{6}$");
	private static final Pattern resourceWorktagPattern = Pattern.compile("^RS\\d{6}$");
	private static final String resourceWorkTag = "RS######";
	private static final Pattern assigneeWorktagPattern = Pattern.compile("^AS\\d{6}$");
	private static final String assigneeWorkTag = "AS######";
	private static final Pattern activityWorktagPattern = Pattern.compile("^AC\\d{6}$");
	private static final String activityWorkTag = "AC######";
	
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

	public boolean isValidWorktag(String worktag)
	{
		Matcher m = worktagPattern.matcher(worktag.trim());
		if (m.matches())
		{
			return true;
		}
		return false;
	}

	public static String getWorktagFormatMessage()
	{
		return "Worktag format should be [GR|GF|PG|CC|SAG]######. Example: GF101001";
	}

	public boolean requiresResourceTag(String worktag)
	{
		// Required for PG, CC and some SAG, GR worktags
		return worktag.startsWith("PG") || worktag.startsWith("CC");
	}

	public boolean isValidResourceWorktag(String worktag)
	{
		Matcher m = resourceWorktagPattern.matcher(worktag.trim());
		return m.matches();
	}

	public static String getResourceWorktagFormatMessage()
	{
		return "Resource worktag format should be " + resourceWorkTag + ".";
	}

	public boolean isValidAssigneeWorktag(String worktag)
	{
		Matcher m = assigneeWorktagPattern.matcher(worktag.trim());
		return m.matches();
	}

	public static String getAssigneeWorktagFormatMessage()
	{
		return "Assignee worktag format should be " + assigneeWorkTag + ".";
	}

	public boolean isValidActivityWorktag(String worktag)
	{
		Matcher m = activityWorktagPattern.matcher(worktag.trim());
		return m.matches();
	}

	public static String getActivityWorktagFormatMessage()
	{
		return "Activity worktag format should be " + activityWorkTag + ".";
	}
}
