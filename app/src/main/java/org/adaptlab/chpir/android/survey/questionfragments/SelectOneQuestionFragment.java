package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.adaptlab.chpir.android.survey.QuestionFragment;
import org.adaptlab.chpir.android.survey.models.Option;

public class SelectOneQuestionFragment extends QuestionFragment {
    private static final String TAG = "SelectOneQuestionFragment";
    private RadioGroup mRadioGroup;
    private int mResponseIndex;

    // This is used to add additional UI components in subclasses.
    protected void beforeAddViewHook(ViewGroup questionComponent) {}
    protected void disableOtherTextButton() {};

    protected RadioGroup getRadioGroup() {
        return mRadioGroup;
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mRadioGroup = new RadioGroup(getActivity());
        for (Option option : getOptions()) {
            int optionId = getOptions().indexOf(option);
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(option.getText());
            radioButton.setId(optionId);
            radioButton.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            mRadioGroup.addView(radioButton, optionId);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResponseIndex(v.getId());
                    disableOtherTextButton();
                }
            });
        }
        questionComponent.addView(mRadioGroup);
        beforeAddViewHook(questionComponent);
    }

    @Override
    protected String serialize() {
        if (mResponseIndex == -1) return "";
        return String.valueOf(mResponseIndex);
    }

    @Override
    protected void unSetResponse() {
        mResponseIndex = -1;
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
        	int checked = getRadioGroup().getCheckedRadioButtonId();
        	if (checked > -1)
        		((RadioButton) getRadioGroup().getChildAt(checked)).setChecked(false);
        } else {
            ((RadioButton) getRadioGroup().getChildAt(Integer.parseInt(responseText))).setChecked(true);
        }
    }
    
    protected void setResponseIndex(int index) {
        mResponseIndex = index;
        clearSpecialResponseSelection();
        setResponseText();
    }
  
}
