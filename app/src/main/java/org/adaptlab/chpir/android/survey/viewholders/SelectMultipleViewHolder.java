package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;

import java.util.ArrayList;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class SelectMultipleViewHolder extends QuestionViewHolder {
    private ArrayList<Integer> mResponseIndices;
    private ArrayList<CheckBox> mCheckBoxes;

    SelectMultipleViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    // This is used to add additional UI components in subclasses.
    protected void beforeAddViewHook(ViewGroup questionComponent) {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mCheckBoxes = new ArrayList<>();
        mResponseIndices = new ArrayList<>();
        for (OptionRelation optionRelation : getOptionRelations()) {
            final int optionId = getOptionRelations().indexOf(optionRelation);
            CheckBox checkbox = new CheckBox(getContext());
            String text = TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel());
            setOptionText(text, checkbox);
            checkbox.setId(optionId);
            toggleCarryForward(checkbox, optionId);
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleResponseIndex(v.getId());
                }
            });
            mCheckBoxes.add(checkbox);
            setOptionPopUpInstruction(questionComponent, checkbox, optionId, optionRelation);
        }
        beforeAddViewHook(questionComponent);
    }

    @Override
    protected String serialize() {
        return FormatUtils.arrayListToString(mResponseIndices);
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
            for (CheckBox box : mCheckBoxes) {
                if (box.isChecked()) {
                    box.setChecked(false);
                }
            }
        } else {
            String[] listOfIndices = responseText.split(COMMA);
            for (String index : listOfIndices) {
                if (!index.equals("")) {
                    int indexInteger = Integer.parseInt(index);
                    mCheckBoxes.get(indexInteger).setChecked(true);
                    mResponseIndices.add(indexInteger);
                }
            }
        }
    }

    private void toggleResponseIndex(int index) {
        if (mResponseIndices.contains(index)) {
            mResponseIndices.remove((Integer) index);
        } else {
            mResponseIndices.add(index);
        }
        saveResponse();
    }

    void addCheckBox(CheckBox checkbox) {
        mCheckBoxes.add(checkbox);
    }

    @Override
    protected void unSetResponse() {
        mResponseIndices = new ArrayList<>();
        for (CheckBox box : mCheckBoxes) {
            if (box.isChecked()) {
                box.setChecked(false);
            }
        }
    }

    @Override
    protected void showOtherText(int position) {
    }
}
