package org.adaptlab.chpir.android.survey.questionfragments;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.adaptlab.chpir.android.survey.FormatUtils;
import org.adaptlab.chpir.android.survey.R;

import java.util.Calendar;

public class MonthAndYearQuestionFragment extends DateQuestionFragment {
    private static final String TAG = "MonthAndYearQuestionFragment";

    @Override
    protected DatePicker beforeAddViewHook(ViewGroup component) {
        DatePicker datePicker = (DatePicker) component.findViewById(R.id.date_picker);
        datePicker.setVisibility(View.VISIBLE);
        Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int newYear, int newMonth, int
                            newDay) {
                        mDay = newDay;
                        mMonth = newMonth;
                        mYear = newYear;
                        setResponseText();
                        if (mSpecialResponses != null) {
                            mSpecialResponses.clearCheck();
                        }
                    }
                });

        datePicker.findViewById(Resources.getSystem().getIdentifier("day", "id", "android"))
                .setVisibility(View.GONE);

        return datePicker;
    }

    @Override
    protected String serialize() {
        return FormatUtils.formatDate(mMonth, mYear);
    }

}