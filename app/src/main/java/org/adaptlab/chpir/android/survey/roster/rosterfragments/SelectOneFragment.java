package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SelectOneFragment extends RosterFragment {
    private RadioGroup mRadioGroup;
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
                getResponse().setResponse(checkedId+"");
            }
        });
        responseComponent.addView(mRadioGroup);
        beforeAddViewHook(responseComponent);
    }

    protected RadioGroup getRadioGroup(){
        return mRadioGroup;
    }
}