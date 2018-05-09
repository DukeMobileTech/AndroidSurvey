package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.R;

public class SelectMultipleWriteOtherQuestionFragment extends
        SelectMultipleQuestionFragment {
    protected EditText otherText = null;

    @Override
    protected void beforeAddViewHook(ViewGroup questionComponent) {
        CheckBox checkbox = new CheckBox(getActivity());
        otherText = new EditText(getActivity());
        checkbox.setText(R.string.other_specify);
        checkbox.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
        final int otherId = getOptions().size();
        checkbox.setId(otherId);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    otherText.setEnabled(true);
//                    otherText.requestFocus();
//                    showKeyBoard();
                } else {
                    otherText.setEnabled(false);
//                    hideKeyBoard();
                    otherText.getText().clear();
                }
                toggleResponseIndex(otherId);
            }
        });
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpecialResponses != null) {
                    mSpecialResponses.clearCheck();
                }
            }
        });
        questionComponent.addView(checkbox, otherId);
        addOtherResponseView(otherText);
        addCheckBox(checkbox);
        questionComponent.addView(otherText);
    }

    @Override
    protected void unSetResponse() {
        otherText.setText("");
        for (CheckBox oneBox : mCheckBoxes) {
            oneBox.setChecked(false);
        }
        if (mResponse != null) {
            mResponse.setResponse("");
        }
    }
}