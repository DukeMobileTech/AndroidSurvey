package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Response;

public class SelectMultipleWriteOtherQuestionFragment extends
        SelectMultipleQuestionFragment {
    protected EditText otherText = null;

    @Override
    protected void beforeAddViewHook(ViewGroup questionComponent) {
        CheckBox checkbox = new CheckBox(getActivity());
        otherText = new EditText(getActivity());
        checkbox.setText(R.string.other_specify);
        checkbox.setTypeface(getInstrument().getTypeFace(
                getActivity().getApplicationContext()));
        checkbox.setTextColor(getResources().getColorStateList(R.color.states));
        final int otherId = getOptions().size();
        checkbox.setId(otherId);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    otherText.setEnabled(true);
                } else {
                    otherText.setEnabled(false);
                    otherText.getText().clear();
                }
            }
        });
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpecialResponses != null) {
                    mSpecialResponses.clearCheck();
                }
                checkOptionExclusivity(v);
                toggleResponseIndex(otherId);
            }
        });
        questionComponent.addView(checkbox, otherId);
        addOtherResponseView(otherText);
        addCheckBox(checkbox);
        questionComponent.addView(otherText);
    }

    @Override
    protected void unSetResponse() {
        if (otherText.getText().length() > 0) {
            otherText.setText(Response.BLANK);
            otherText.setEnabled(false);
        }
        super.unSetResponse();
    }

}