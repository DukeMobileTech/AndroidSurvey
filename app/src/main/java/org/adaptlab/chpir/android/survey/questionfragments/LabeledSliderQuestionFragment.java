package org.adaptlab.chpir.android.survey.questionfragments;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.models.Option;

import java.util.List;

public class LabeledSliderQuestionFragment extends SliderQuestionFragment {

    @Override
    public void beforeAddViewHook(ViewGroup questionComponent) {
        if (getOptions() != null) {
            TableLayout tableLayout = new TableLayout(getActivity());
            TableRow tableRow = new TableRow(getActivity());
            tableLayout.setStretchAllColumns(true);

            for (Option option : getOptions()) {
                TextView optionText = new TextView(getActivity());
                optionText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                optionText.setGravity(getGravityByPosition(option));
                optionText.setText(option.getText(getInstrument()));
                tableRow.addView(optionText);
            }

            tableLayout.addView(tableRow);
            questionComponent.addView(tableLayout);
        }
    }

    private int getGravityByPosition(Option option) {
        List<Option> options = getOptions();
        if (options.isEmpty()) return Gravity.START;

        if (options.get(0).equals(option)) {
            // Left-most label
            return Gravity.START;
        } else if (options.get(options.size() - 1).equals(option)) {
            // Right most label
            return Gravity.END;
        } else {
            // All other labels
            return Gravity.CENTER_HORIZONTAL;
        }
    }
}