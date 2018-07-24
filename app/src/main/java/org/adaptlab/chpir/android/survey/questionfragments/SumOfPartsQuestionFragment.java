package org.adaptlab.chpir.android.survey.questionfragments;

import android.graphics.Typeface;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;

import java.util.Locale;

public class SumOfPartsQuestionFragment extends ListOfItemsQuestionFragment {

    protected void createQuestionComponent(ViewGroup questionComponent) {
        super.createQuestionComponent(questionComponent);
        addSumOfPartsView(questionComponent);
    }

    private void addSumOfPartsView(ViewGroup questionComponent) {
        TextView sumOfPartsLabel = new TextView(getActivity());
        sumOfPartsLabel.setText(R.string.sum_of_parts);
        sumOfPartsLabel.setTypeface(Typeface.DEFAULT_BOLD);
        questionComponent.addView(sumOfPartsLabel);
        EditText sumOfParts = new EditText(getActivity());
        sumOfParts.setText(String.format(Locale.getDefault(), "%f",
                getQuestion().getSumOfParts()));
        sumOfParts.setTypeface(Typeface.DEFAULT_BOLD);
        sumOfParts.setEnabled(false);
        questionComponent.addView(sumOfParts);
    }

    @Override
    protected void setResponseText() {
        super.setResponseText();
        double sum = 0.0;
        for (EditText editText : mResponses) {
            if (!FormatUtils.isEmpty(editText.getText().toString())) {
                sum += Double.parseDouble(editText.getText().toString());
            }
        }
        if (sum == getQuestion().getSumOfParts()) {
            animateValidationTextView(true, "");
        } else {
            animateValidationTextView(false, getQuestion().getRegExValidationMessage());
        }
    }

        @Override
    protected EditText createEditText() {
        EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED |
                InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return editText;
    }
}
