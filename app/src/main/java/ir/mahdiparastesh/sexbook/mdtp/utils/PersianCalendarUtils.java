package ir.mahdiparastesh.sexbook.mdtp.utils;

public class PersianCalendarUtils {
	public static boolean isPersianLeapYear(int persianYear) {
		return PersianCalendarUtils.ceil((38D + (
				PersianCalendarUtils.ceil(persianYear - 474L, 2820L) + 474L)) * 682D, 2816D) < 682L;
	}

	public static long ceil(double double1, double double2) {
		return (long) (double1 - double2 * Math.floor(double1 / double2));
	}
}
