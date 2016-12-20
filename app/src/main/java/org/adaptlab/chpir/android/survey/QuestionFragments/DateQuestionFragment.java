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
    
    // This is used to hide various date fields in subclasses.
    protected void beforeAddViewHook(DatePicker datePicker) {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mDatePicker = new DatePicker(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        mDatePicker.setLayoutParams(params);
        mDatePicker.setCalendarViewShown(false);
        Calendar c = Calendar.getInstance();
        mDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                new OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int newYear,
                    int newMonth, int newDay) {
                mDay = newDay;
                mMonth = newMonth;
                mYear = newYear;
                setResponseText();
            }           
        });
        questionComponent.addView(mDatePicker);
        beforeAddViewHook(mDatePicker);
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

}
