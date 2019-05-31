package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

public class YearViewHolder extends DateViewHolder {
    private DatePicker mDatePicker;

    YearViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected DatePicker beforeAddViewHook(ViewGroup component) {
//        if (getActivity() == null) return null;
//        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
//                Context.LAYOUT_INFLATER_SERVICE);
//        if (inflater != null) {
//            mDatePicker = (DatePicker) inflater.inflate(R.layout.date_picker, null);
//        }
//        if (mDatePicker != null) {
//            Calendar c = Calendar.getInstance();
//            mDatePicker = new DatePicker(getActivity());
//            mDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
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
//            component.addView(mDatePicker);
//            mDatePicker.findViewById(Resources.getSystem().getIdentifier(
//                    "day", "id", "android"))
//                    .setVisibility(View.GONE);
//            mDatePicker.findViewById(Resources.getSystem().getIdentifier(
//                    "month", "id", "android"))
//                    .setVisibility(View.GONE);
//        }
//        return mDatePicker;
        return null;
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