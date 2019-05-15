package org.adaptlab.chpir.android.survey.questionfragments;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.R;

import java.util.Calendar;

public class MonthAndYearQuestionFragment extends DateQuestionFragment {
    private static final String TAG = "MonthAndYearViewHolder";

    @Override
    protected DatePicker beforeAddViewHook(ViewGroup component) {
        if (getActivity() == null) return null;
        DatePicker datePicker = null;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            datePicker = (DatePicker) inflater.inflate(R.layout.date_picker, null);
        }
        if (datePicker != null) {
            Calendar c = Calendar.getInstance();
            datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
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
            component.addView(datePicker);
            datePicker.findViewById(Resources.getSystem().getIdentifier(
                    "day", "id", "android"))
                    .setVisibility(View.GONE);
        }
        return datePicker;
    }

    @Override
    protected String serialize() {
        return FormatUtils.formatDate(mMonth, mYear);
    }

}