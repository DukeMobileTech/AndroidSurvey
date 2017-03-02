package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SelectOneFragment extends RosterFragment {
    private RadioGroup mRadioGroup;
    private int mResponseIndex;

    // This is used to add additional UI components in subclasses.
    protected void beforeAddViewHook(ViewGroup responseComponent) {
    }

    @Override
    protected void createResponseComponent(ViewGroup responseComponent) {
        mRadioGroup = new RadioGroup(getActivity());

        for (int i = 0; i < getQuestion().defaultOptions().size(); i++) {
            String option = getQuestion().defaultOptions().get(i).getText();
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(option);
            radioButton.setId(i);
            if (getResponse().getText() != null && getResponse().getText().equals(i+"")) {
                radioButton.setChecked(true);
            }
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            mRadioGroup.addView(radioButton, i);
        }

        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setResponseIndex(checkedId);
            }
        });
        responseComponent.addView(mRadioGroup);
        beforeAddViewHook(responseComponent);
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
            ((RadioButton) getRadioGroup().getChildAt(Integer.parseInt(responseText))).setChecked(true);
        }
    }

    protected RadioGroup getRadioGroup(){
        return mRadioGroup;
    }

    protected void setResponseIndex(int index) {
        mResponseIndex = index;
        setResponseText();
    }
}