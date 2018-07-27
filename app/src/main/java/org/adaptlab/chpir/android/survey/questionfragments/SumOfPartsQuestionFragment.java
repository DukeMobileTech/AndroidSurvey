package org.adaptlab.chpir.android.survey.questionfragments;

import android.graphics.Typeface;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Validation;

import java.util.Locale;

public class SumOfPartsQuestionFragment extends ListOfItemsQuestionFragment {

    @Override
    protected EditText createEditText() {
        EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED |
                InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return editText;
    }

    protected void createQuestionComponent(ViewGroup questionComponent) {
        super.createQuestionComponent(questionComponent);
        Validation mValidation = getQuestion().getValidation();
        TextView sumOfPartsLabel = new TextView(getActivity());
        sumOfPartsLabel.setText(R.string.sum_of_parts);
        sumOfPartsLabel.setTypeface(Typeface.DEFAULT_BOLD);
        questionComponent.addView(sumOfPartsLabel);
        EditText sumOfParts = new EditText(getActivity());
        if (mValidation != null && mValidation.getValidationType().equals(
                Validation.Type.SUM_OF_PARTS.toString())) {
            sumOfParts.setText(String.format(Locale.getDefault(), "%f",
                    Double.parseDouble(mValidation.getValidationText())));
        }
        sumOfParts.setTypeface(Typeface.DEFAULT_BOLD);
        sumOfParts.setEnabled(false);
        questionComponent.addView(sumOfParts);
    }

}
