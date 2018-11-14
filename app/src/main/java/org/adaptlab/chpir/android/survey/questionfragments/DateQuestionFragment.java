package org.adaptlab.chpir.android.survey.questionfragments;

import android.graphics.Typeface;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.SingleQuestionFragment;
import org.adaptlab.chpir.android.survey.views.ScrollableDatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateQuestionFragment extends SingleQuestionFragment {
    protected int mDay;
    protected int mMonth;
    protected int mYear;
    private DatePicker mDatePicker;
    private TextView mSelectedDate;
    private OnDateChangedListener mListener = new OnDateChangedListener() {
        @Override
        public void onDateChanged(DatePicker view, int newYear, int newMonth, int newDay) {
            mDay = newDay;
            mMonth = newMonth;
            mYear = newYear;
            setResponse(null);
            setSelectedDate();
            if (mSpecialResponses != null) {
                mSpecialResponses.clearCheck();
            }
        }
    };

    protected DatePicker beforeAddViewHook(ViewGroup component) {
        DatePicker datePicker = new ScrollableDatePicker(getActivity());
        LinearLayout linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 3;
        datePicker.setLayoutParams(params);
        datePicker.setCalendarViewShown(false);
        Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                mListener);
        linearLayout.addView(datePicker);
        mSelectedDate = new TextView(getActivity());
        setSelectedDate();
        linearLayout.addView(mSelectedDate);
        component.addView(linearLayout);
        return datePicker;
    }

    private void setSelectedDate() {
        if (mSelectedDate == null) return;
        String dateTitle = getString(R.string.selected_date);
        String string = dateTitle + "\n" + getResponse().getText();
        SpannableString spannableString = new SpannableString(string);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), dateTitle.length(), string.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                .primary_text)), 0, dateTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSelectedDate.setText(spannableString);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        mSelectedDate.setLayoutParams(params);
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
        if (dateComponents != null) {
            mDay = dateComponents.get(GregorianCalendar.DAY_OF_MONTH);
            mMonth = dateComponents.get(GregorianCalendar.MONTH);
            mYear = dateComponents.get(GregorianCalendar.YEAR);
            mDatePicker.updateDate(mYear, mMonth, mDay);
        }
    }

    @Override
    protected void unSetResponse() {
        setResponseTextBlank();
        setSelectedDate();
        Calendar calendar = Calendar.getInstance();
        mDatePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), mListener);
    }

}
