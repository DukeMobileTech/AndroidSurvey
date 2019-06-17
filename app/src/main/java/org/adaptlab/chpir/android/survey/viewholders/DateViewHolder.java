package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.views.ScrollableDatePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateViewHolder extends QuestionViewHolder {
    protected int mDay;
    protected int mMonth;
    protected int mYear;
    private DatePicker mDatePicker;
    private TextView mSelectedDate;
    private DatePicker.OnDateChangedListener mListener = new DatePicker.OnDateChangedListener() {
        @Override
        public void onDateChanged(DatePicker view, int newYear, int newMonth, int newDay) {
            mDay = newDay;
            mMonth = newMonth;
            mYear = newYear;
//            setResponse(null);
//            setSelectedDate();
//            if (mSpecialResponses != null) {
//                mSpecialResponses.clearCheck();
//            }
        }
    };

    DateViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected DatePicker beforeAddViewHook(ViewGroup component) {
        DatePicker datePicker = new ScrollableDatePicker(getContext());
        LinearLayout linearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 3;
        datePicker.setLayoutParams(params);
        datePicker.setCalendarViewShown(false);
        Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                mListener);
        linearLayout.addView(datePicker);
        mSelectedDate = new TextView(getContext());
//        setSelectedDate();
        linearLayout.addView(mSelectedDate);
        component.addView(linearLayout);
        return datePicker;
    }

//    private void setSelectedDate() {
//        if (mSelectedDate == null) return;
//        String dateTitle = mContext.getString(R.string.selected_date);
//        String string = dateTitle + "\n" + getResponse().getText();
//        SpannableString spannableString = new SpannableString(string);
//        spannableString.setSpan(new StyleSpan(Typeface.BOLD), dateTitle.length(), string.length(),
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
//                .primary_text)), 0, dateTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        mSelectedDate.setText(spannableString);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        params.weight = 1;
//        mSelectedDate.setLayoutParams(params);
//    }

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
        GregorianCalendar dateComponents = FormatUtils.unFormatDate(responseText);
        if (dateComponents != null) {
            mDay = dateComponents.get(GregorianCalendar.DAY_OF_MONTH);
            mMonth = dateComponents.get(GregorianCalendar.MONTH);
            mYear = dateComponents.get(GregorianCalendar.YEAR);
            mDatePicker.updateDate(mYear, mMonth, mDay);
        }
    }

//    @Override
//    protected void unSetResponse() {
//        setResponseTextBlank();
//        setSelectedDate();
//        Calendar calendar = Calendar.getInstance();
//        mDatePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH), mListener);
//    }

}
