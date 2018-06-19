package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import org.adaptlab.chpir.android.survey.SingleQuestionFragment;
import org.adaptlab.chpir.android.survey.models.Option;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.removeNonNumericCharacters;

public class SelectOneQuestionFragment extends SingleQuestionFragment {
    private RadioGroup mRadioGroup;
    private int mResponseIndex;

    // This is used to add additional UI components in subclasses.
    protected void beforeAddViewHook(ViewGroup questionComponent) {
    }

    protected RadioGroup getRadioGroup() {
        return mRadioGroup;
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mRadioGroup = new RadioGroup(getActivity());
        for (Option option : getOptions()) {
            int optionId = getOptions().indexOf(option);
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(option.getText(getInstrument()));
            radioButton.setId(optionId);
            radioButton.setTypeface(getInstrument().getTypeFace(getActivity()
                    .getApplicationContext()));
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            mRadioGroup.addView(radioButton, optionId);
        }

        getRadioGroup().setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    setResponseIndex(checkedId);
                }
            }
        });
        for (int i = 0; i < getRadioGroup().getChildCount(); i++) {
            getRadioGroup().getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSpecialResponses != null) {
                        mSpecialResponses.clearCheck();
                    }
                }
            });
        }
        questionComponent.addView(mRadioGroup);
        beforeAddViewHook(questionComponent);
    }

    @Override
    protected String serialize() {
        return String.valueOf(mResponseIndex);
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
            int checked = getRadioGroup().getCheckedRadioButtonId();
            if (checked > -1)
                ((RadioButton) getRadioGroup().getChildAt(checked)).setChecked(false);
        } else {
            ((RadioButton) getRadioGroup().getChildAt(Integer.parseInt(removeNonNumericCharacters
                    (responseText)))).setChecked(true);
        }
    }

    protected void setResponseIndex(int index) {
        mResponseIndex = index;
        setResponseText();
    }

    @Override
    protected void unSetResponse() {
        if (getRadioGroup() != null) {
            getRadioGroup().clearCheck();
        }
        if (mResponse != null) {
            mResponse.setResponse("");
        }
    }

}
