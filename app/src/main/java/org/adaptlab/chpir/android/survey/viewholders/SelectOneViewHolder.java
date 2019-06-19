package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.removeNonNumericCharacters;

public class SelectOneViewHolder extends QuestionViewHolder {
    private RadioGroup mRadioGroup;
    private int mResponseIndex;

    SelectOneViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
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
        for (OptionRelation optionRelation : getOptionRelations()) {
            int optionId = getOptionRelations().indexOf(optionRelation);
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            radioButton.setText(TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel()));
            radioButton.setId(optionId);
            radioButton.setTextColor(getContext().getResources().getColorStateList(R.color.states));
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    if (id != -1) {
                        setResponseIndex(id);
                    }
                }
            });
            mRadioGroup.addView(radioButton, optionId);
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

}
