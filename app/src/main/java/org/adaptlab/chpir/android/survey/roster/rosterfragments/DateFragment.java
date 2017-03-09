package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;

import org.adaptlab.chpir.android.survey.FormatUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateFragment extends RosterFragment {
    protected DatePicker datePicker;
    protected int mDay;
    protected int mMonth;
    protected int mYear;

    protected void beforeAddViewHook(ViewGroup responseComponent) {
        datePicker = new DatePicker(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        datePicker.setLayoutParams(params);
        Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                new OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int newYear, int newMonth, int newDay) {
                        mDay = newDay;
                        mMonth = newMonth;
                        mYear = newYear;
                        getResponse().setResponse((newMonth + 1) + "-" + newDay + "-" + newYear);
                    }
                });
        responseComponent.addView(datePicker);
        updateDate(getResponse().getText());
    }

    @Override
    protected void createResponseComponent(ViewGroup responseComponent) {
        beforeAddViewHook(responseComponent);
    }

    private void updateDate(String date) {
        if (date != null) {
            String[] dateComponents = date.split("-");
            int month, day, year;
            if (dateComponents.length == 3) {
                month = Integer.parseInt(dateComponents[0]) - 1;
                day = Integer.parseInt(dateComponents[1]);
                year = Integer.parseInt(dateComponents[2]);
                datePicker.updateDate(year, month, day);
            }
        }
    }


    @Override
    protected String serialize() {
        return FormatUtils.formatDate(mMonth, mDay, mYear);
    }

    @Override
    protected void deserialize(String responseText) {
        GregorianCalendar dateComponents = FormatUtils.unformatDate(responseText);
        if(dateComponents != null) {
            mDay = dateComponents.get(GregorianCalendar.DAY_OF_MONTH);
            mMonth = dateComponents.get(GregorianCalendar.MONTH);
            mYear = dateComponents.get(GregorianCalendar.YEAR);
            datePicker.updateDate(mYear, mMonth, mDay);
        }
    }
}