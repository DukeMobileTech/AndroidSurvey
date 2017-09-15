package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;

import org.adaptlab.chpir.android.survey.FormatUtils;
import org.adaptlab.chpir.android.survey.QuestionFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateQuestionFragment extends QuestionFragment {
    private static final String TAG = "DateQuestionFragment";
    protected int mDay;
    protected int mMonth;
    protected int mYear;
    protected boolean hasBeenReset = false;

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
                    public void onDateChanged(DatePicker view, int newYear, int newMonth, int newDay) {
                        mDay = newDay;
                        mMonth = newMonth;
                        mYear = newYear;
                        if (!hasBeenReset) {
                            clearSpecialResponseSelection();
                        }
                        setResponseText();
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
        if (hasBeenReset) {
            hasBeenReset = false;
            return "";
        }
        return FormatUtils.formatDate(mMonth, mDay, mYear);
    }

    @Override
    protected void unSetResponse() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        mMonth = cal.get(Calendar.DAY_OF_MONTH);
        mDay = cal.get(Calendar.MONTH);
        mYear = cal.get(Calendar.YEAR);
        hasBeenReset = true;
    }

    @Override
    protected void deserialize(String responseText) {
        GregorianCalendar dateComponents = FormatUtils.unformatDate(responseText);
        if (dateComponents != null) {
            mDay = dateComponents.get(GregorianCalendar.DAY_OF_MONTH);
            mMonth = dateComponents.get(GregorianCalendar.MONTH);
            mYear = dateComponents.get(GregorianCalendar.YEAR);
            mDatePicker.updateDate(mYear, mMonth, mDay);
        }
    }

}
