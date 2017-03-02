package org.adaptlab.chpir.android.survey.roster.rosterfragments;

/**
 * Created by Harry on 1/31/17.
 */
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.questionfragments.SelectOneQuestionFragment;

public class SelectOneWriteOtherFragment extends
        SelectOneFragment {

    @Override
    protected void beforeAddViewHook(ViewGroup responseComponent) {
        RadioButton radioButton = new RadioButton(getActivity());
        final EditText otherText = new EditText(getActivity());

        radioButton.setText(R.string.other_specify);
        radioButton.setTypeface(getQuestion().getInstrument().getTypeFace(getActivity().getApplicationContext()));
        final int otherId = getQuestion().defaultOptions().size();
        radioButton.setId(otherId);
        radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        if (getResponse().getText() != null && getResponse().getText().equals(otherId+"")) {
            radioButton.setChecked(true);
        }
        getRadioGroup().setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == otherId) {
                    otherText.setEnabled(true);
                    otherText.requestFocus();
                    showKeyBoard();
                } else {
                    otherText.setEnabled(false);
                    hideKeyBoard();
                    otherText.getText().clear();
                }
                setResponseIndex(checkedId);
            }
        });
        getRadioGroup().addView(radioButton, otherId);
        addOtherResponseView(otherText);
        responseComponent.addView(otherText);
    }
}