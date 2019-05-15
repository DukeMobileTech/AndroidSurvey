package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.Option;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.removeNonNumericCharacters;

public class SelectOneViewHolder extends SingleQuestionViewHolder {
    private RadioGroup mRadioGroup;
    private int mResponseIndex;

    SelectOneViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    // This is used to add additional UI components in subclasses.
    protected void beforeAddViewHook(ViewGroup questionComponent) {
    }

    RadioGroup getRadioGroup() {
        return mRadioGroup;
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mRadioGroup = new RadioGroup(getContext());
        for (Option option : getOptions()) {
            int optionId = getOptions().indexOf(option);
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            radioButton.setText(option.getText());
            radioButton.setId(optionId);
//            radioButton.setTypeface(getInstrument().getTypeFace(mContext.getApplicationContext()));
            radioButton.setTextColor(getContext().getResources().getColorStateList(R.color.states));
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    Log.i(TAG, "onClick view id: " + v.getId());
                    if (id != -1) {
                        setResponseIndex(id);
//                        if (mSpecialResponses != null) {
//                            mSpecialResponses.clearCheck();
//                        }
                    }
                }
            });
            mRadioGroup.addView(radioButton, optionId);
        }

//        getRadioGroup().setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                Log.i(TAG, "onCheckedChanged");
//                Log.i(TAG, "checkedId: " + checkedId);
//                if (checkedId != -1) {
//                    setResponseIndex(checkedId);
//                }
//            }
//        });
//        for (int i = 0; i < getRadioGroup().getChildCount(); i++) {
//            getRadioGroup().getChildAt(i).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mSpecialResponses != null) {
//                        mSpecialResponses.clearCheck();
//                    }
//                }
//            });
//        }
        questionComponent.addView(mRadioGroup);
        beforeAddViewHook(questionComponent);
    }

    @Override
    protected String serialize() {
        return String.valueOf(mResponseIndex);
    }

    @Override
    protected void deserialize(String responseText) {
        if (TextUtils.isEmpty(responseText.trim())) {
            int checked = getRadioGroup().getCheckedRadioButtonId();
            if (checked > -1)
                ((RadioButton) getRadioGroup().getChildAt(checked)).setChecked(false);
        } else {
            ((RadioButton) getRadioGroup().getChildAt(Integer.parseInt(removeNonNumericCharacters(responseText)))).setChecked(true);
        }
    }

    protected void setResponseIndex(int index) {
        mResponseIndex = index;
        saveResponse();
    }

//    @Override
//    protected void unSetResponse() {
//        if (getRadioGroup() != null) {
//            getRadioGroup().clearCheck();
//        }
//        setResponseTextBlank();
//    }

}
