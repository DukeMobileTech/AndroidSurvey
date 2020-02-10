package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.InstructionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;
import org.adaptlab.chpir.android.survey.views.CustomRadioButton;
import org.adaptlab.chpir.android.survey.views.DrawableClickListener;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;
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
    protected void createQuestionComponent(final ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mRadioGroup = new RadioGroup(getContext());
        for (final OptionRelation optionRelation : getOptionRelations()) {
            final int optionId = getOptionRelations().indexOf(optionRelation);
            CustomRadioButton radioButton = new CustomRadioButton(getContext());
            String text = TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel());
            radioButton.setId(optionId);
            setOptionText(text, radioButton);
            toggleCarryForward(radioButton, optionId);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    questionComponent.requestFocus();
                    int id = v.getId();
                    if (id != -1) {
                        setResponseIndex(id);
                        setTextEntry(optionId);
                    }
                }
            });
            final InstructionRelation optionInstruction = getOptionInstruction(optionRelation.option.getIdentifier());
            if (optionInstruction == null) {
                radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            } else {
                radioButton.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getContext().getResources().getDrawable(R.drawable.ic_info_outline_blue_24dp), null);
                radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                radioButton.setDrawableClickListener(new DrawableClickListener() {
                    @Override
                    public void onClick(DrawablePosition target) {
                        if (target == DrawablePosition.RIGHT) {
                            showPopUpInstruction(getOptionPopUpInstructions(optionInstruction));
                        }
                    }
                });
            }
            mRadioGroup.addView(radioButton, optionId);
        }
        questionComponent.addView(mRadioGroup);
        beforeAddViewHook(questionComponent);
    }

    @Override
    protected void showOtherText(int position) {
        String response = getResponse().getText();
        for (int i = 0; i < getRadioGroup().getChildCount(); i++) {
            if (getOptionRelations().size() > i) { // Handles SELECT_ONE_WRITE_OTHER
                OptionRelation optionRelation = getOptionRelations().get(i);
                RadioButton radioButton = ((RadioButton) getRadioGroup().getChildAt(i));
                String text = TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel());
                if (getTextEntryOptionIds().contains(optionRelation.option.getRemoteId())
                        && !TextUtils.isEmpty(response) && Integer.parseInt(response) == i) {
                    text = text + "  " + getResponse().getOtherText();
                    setOptionText(text, radioButton);
                } else {
                    setOptionText(text, radioButton);
                }
            }
        }
    }

    @Override
    protected String serialize() {
        if (mResponseIndex == -1) return BLANK;
        return String.valueOf(mResponseIndex);
    }

    @Override
    protected void deserialize(String responseText) {
        if (TextUtils.isEmpty(responseText.trim())) {
            int checked = getRadioGroup().getCheckedRadioButtonId();
            if (checked > -1)
                ((RadioButton) getRadioGroup().getChildAt(checked)).setChecked(false);
        } else {
            int index = Integer.parseInt(removeNonNumericCharacters(responseText));
            ((RadioButton) getRadioGroup().getChildAt(index)).setChecked(true);
            showOtherText(index);
        }
    }

    @Override
    protected void unSetResponse() {
        mResponseIndex = -1;
        if (getRadioGroup() != null) {
            getRadioGroup().clearCheck();
        }
        showOtherText(0);
    }

    void setResponseIndex(int index) {
        mResponseIndex = index;
        saveResponse();
    }

}
