package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.removeNonNumericCharacters;

public class SelectOneTableViewHolder extends TableQuestionViewHolder {
    private int mResponseIndex;
    private RadioGroup mRadioGroup;

    SelectOneTableViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context);
        setOnResponseSelectedListener(listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mRadioGroup = new RadioGroup(getContext());
        mRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
        for (int k = 0; k < getOptionRelations().size(); k++) {
            RadioButton radioButton = new RadioButton(getContext());
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(getOptionWidth() / 2,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = getOptionWidth() / 2;
            radioButton.setLayoutParams(params);
            radioButton.setId(k);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setResponseIndex(view.getId());
                }
            });
            mRadioGroup.addView(radioButton);
        }
        questionComponent.addView(mRadioGroup);
        setSpecialResponseView();
    }

    @Override
    protected void unSetResponse() {
    }

    @Override
    protected void deserialize(String responseText) {
        if (TextUtils.isEmpty(responseText.trim())) {
            int checked = mRadioGroup.getCheckedRadioButtonId();
            if (checked > -1)
                ((RadioButton) mRadioGroup.getChildAt(checked)).setChecked(false);
        } else {
            ((RadioButton) mRadioGroup.getChildAt(Integer.parseInt(removeNonNumericCharacters(responseText)))).setChecked(true);
        }
    }

    @Override
    protected String serialize() {
        return String.valueOf(mResponseIndex);
    }

    private void setResponseIndex(int index) {
        mResponseIndex = index;
        saveResponse();
    }

}
