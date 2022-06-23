package ir.mahdiparastesh.sexbook.mdtp.date;

import android.content.Context;
import android.icu.text.DateFormatSymbols;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import java.util.Locale;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.mdtp.Utils;

public class LocalDateFormat extends SimpleDateFormat {

    public LocalDateFormat(Context c, Class<? extends Calendar> calendarType, String pattern, Locale loc) {
        super(pattern, loc);
        calendar = Utils.createCalendar(calendarType);

        DateFormatSymbols symbols = DateFormatSymbols.getInstance(loc);
        //noinspection SwitchStatementWithTooFewBranches
        switch (calendarType.getSimpleName()) {
            case "PersianCalendar":
                symbols.setMonths(c.getResources().getStringArray(R.array.persianMonths));
                symbols.setShortMonths(c.getResources().getStringArray(R.array.shortPersianMonths));
                break;
        }
        setDateFormatSymbols(symbols);
    }
}
