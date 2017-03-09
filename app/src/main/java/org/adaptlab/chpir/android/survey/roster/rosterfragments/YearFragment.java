package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.questionfragments.DateQuestionFragment;

import java.util.Calendar;

/**
 * Created by Harry on 3/3/17.
 */
public class YearFragment extends DateFragment {
    private static final String TAG = "YearFragment";

    @Override
    protected void beforeAddViewHook(ViewGroup responseComponent) {
        datePicker = (DatePicker) responseComponent.findViewById(R.id.date_picker);
        Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int newYear, int newMonth, int newDay) {
                        mDay = newDay;
                        mMonth = newMonth;
                        mYear = newYear;
                        setResponseText();
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

    @Override
    protected String serialize() {
        return String.valueOf(mYear);
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) return;
        datePicker.updateDate(Integer.parseInt(responseText), 1, 1);
    }
}
