package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class LabeledSliderViewHolder extends SliderViewHolder {

    LabeledSliderViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    public void beforeAddViewHook(ViewGroup questionComponent) {
//        if (getOptionRelations() != null) {
//            TableLayout tableLayout = new TableLayout(getActivity());
//            TableRow tableRow = new TableRow(getActivity());
//            tableLayout.setStretchAllColumns(true);
//
//            for (Option option : getOptionRelations()) {
//                TextView optionText = new TextView(getActivity());
//                optionText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
//                optionText.setGravity(getGravityByPosition(option));
//                optionText.setText(getOptionText(option));
//                tableRow.addView(optionText);
//            }
//
//            tableLayout.addView(tableRow);
//            questionComponent.addView(tableLayout);
//        }
    }

//    private int getGravityByPosition(Option option) {
//        List<Option> options = getOptionRelations();
//        if (options.isEmpty()) return Gravity.START;
//
//        if (options.get(0).equals(option)) {
//            // Left-most label
//            return Gravity.START;
//        } else if (options.get(options.size() - 1).equals(option)) {
//            // Right most label
//            return Gravity.END;
//        } else {
//            // All other labels
//            return Gravity.CENTER_HORIZONTAL;
//        }
//    }
}