package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;

import org.adaptlab.chpir.android.survey.FormatUtils;
import org.adaptlab.chpir.android.survey.QuestionFragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateQuestionFragment extends QuestionFragment {
    protected int mDay;
    protected int mMonth;
    protected int mYear;
    private DatePicker mDatePicker;

    protected DatePicker beforeAddViewHook(ViewGroup component) {
        DatePicker datePicker = new DatePicker(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        datePicker.setLayoutParams(params);
        datePicker.setCalendarViewShown(false);
        Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                new OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int newYear,
                                              int newMonth, int newDay) {
                        mDay = newDay;
                        mMonth = newMonth;
                        mYear = newYear;
                        setResponseText();
                        if (mSpecialResponses != null) {
                            mSpecialResponses.clearCheck();
                        }
                    }
                });
        component.addView(datePicker);
        return datePicker;
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mDatePicker = beforeAddViewHook(questionComponent);
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
            mDatePicker.updateDate(mYear, mMonth, mDay);
        }
    }

    @Override
    protected void unSetResponse() {
        if (mResponse != null) {
            mResponse.setResponse("");
        }
    }

}
