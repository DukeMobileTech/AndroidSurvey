package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

import java.util.Calendar;

public class DateFragment extends RosterFragment {
    private DatePicker datePicker;

    @Override
    protected void createResponseComponent(ViewGroup responseComponent) {
        datePicker = new DatePicker(getActivity());
        Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                new OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int newYear, int newMonth, int newDay) {
                        getResponse().setResponse((newMonth + 1) + "-" + newDay + "-" + newYear);
                    }
                });
        responseComponent.addView(datePicker);
        updateDate(getResponse().getText());
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
}