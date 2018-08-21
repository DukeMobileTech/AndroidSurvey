package org.adaptlab.chpir.android.survey.questionfragments;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.adaptlab.chpir.android.survey.R;

import java.util.Calendar;

public class YearQuestionFragment extends DateQuestionFragment {
    private static final String TAG = "YearQuestionFragment";

    private DatePicker mDatePicker;

    @Override
    protected DatePicker beforeAddViewHook(ViewGroup component) {
        mDatePicker = (DatePicker) component.findViewById(R.id.date_picker);
        mDatePicker.setVisibility(View.VISIBLE);
        Calendar c = Calendar.getInstance();
        mDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int newYear, int newMonth, int
                            newDay) {
                        mDay = newDay;
                        mMonth = newMonth;
                        mYear = newYear;
                        setResponse(null);
                        if (mSpecialResponses != null) {
                            mSpecialResponses.clearCheck();
                        }
                    }
                });
        mDatePicker.findViewById(Resources.getSystem().getIdentifier("day", "id", "android"))
                .setVisibility(View.GONE);
        mDatePicker.findViewById(Resources.getSystem().getIdentifier("month", "id", "android"))
                .setVisibility(View.GONE);
        return mDatePicker;
    }

    @Override
    protected String serialize() {
        return String.valueOf(mYear);
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) return;
        mDatePicker.updateDate(Integer.parseInt(responseText), 1, 1);
    }
}