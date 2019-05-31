package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.adaptlab.chpir.android.survey.utils.FormatUtils;

public class MonthAndYearViewHolder extends DateViewHolder {
    private static final String TAG = "MonthAndYearViewHolder";

    MonthAndYearViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected DatePicker beforeAddViewHook(ViewGroup component) {
//        if (getActivity() == null) return null;
//        DatePicker datePicker = null;
//        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
//                Context.LAYOUT_INFLATER_SERVICE);
//        if (inflater != null) {
//            datePicker = (DatePicker) inflater.inflate(R.layout.date_picker, null);
//        }
//        if (datePicker != null) {
//            Calendar c = Calendar.getInstance();
//            datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
//                    new DatePicker.OnDateChangedListener() {
//                        @Override
//                        public void onDateChanged(DatePicker view, int newYear, int newMonth, int
//                                newDay) {
//                            mDay = newDay;
//                            mMonth = newMonth;
//                            mYear = newYear;
//                            setResponse(null);
//                            if (mSpecialResponses != null) {
//                                mSpecialResponses.clearCheck();
//                            }
//                        }
//                    });
//            component.addView(datePicker);
//            datePicker.findViewById(Resources.getSystem().getIdentifier(
//                    "day", "id", "android"))
//                    .setVisibility(View.GONE);
//        }
//        return datePicker;
        return null;
    }

    @Override
    protected String serialize() {
        return FormatUtils.formatDate(mMonth, mYear);
    }

}