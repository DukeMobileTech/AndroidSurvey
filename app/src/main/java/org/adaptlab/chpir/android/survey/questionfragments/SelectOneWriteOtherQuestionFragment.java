package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Response;

public class SelectOneWriteOtherQuestionFragment extends
        SelectOneQuestionFragment {
    protected EditText otherText = null;

    @Override
    protected void beforeAddViewHook(ViewGroup questionComponent) {
        RadioButton radioButton = new RadioButton(getActivity());
        otherText = new EditText(getActivity());
        radioButton.setTextColor(getResources().getColorStateList(R.color.states));
        radioButton.setText(R.string.other_specify);
        radioButton.setTypeface(getInstrument().getTypeFace(
                getActivity().getApplicationContext()));
        final int otherId = getOptions().size();
        radioButton.setId(otherId);
        radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpecialResponses != null) {
                    mSpecialResponses.clearCheck();
                }
            }
        });
        getRadioGroup().setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    if (checkedId == otherId) {
                        otherText.setEnabled(true);
                    } else {
                        otherText.setEnabled(false);
                        otherText.getText().clear();
                    }
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
        getRadioGroup().addView(radioButton, otherId);
        addOtherResponseView(otherText);
        questionComponent.addView(otherText);
    }

    @Override
    protected void unSetResponse() {
        if(getRadioGroup()!=null){
            getRadioGroup().clearCheck();
        }
        otherText.setText(Response.BLANK);
        setResponseTextBlank();
    }
}
