package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class SelectMultipleViewHolder extends QuestionViewHolder {
    private ArrayList<Integer> mResponseIndices;
    private ArrayList<CheckBox> mCheckBoxes;
    private HashMap<Integer, Set<Integer>> mExclusives;

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
        mExclusives = new HashMap<>();
        for (final OptionRelation optionRelation : getOptionRelations()) {
            final int optionId = getOptionRelations().indexOf(optionRelation);
            CheckBox checkbox = new CheckBox(getContext());
            String text = TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel());
            setOptionText(text, checkbox);
            checkbox.setId(optionId);
            toggleCarryForward(checkbox, optionId);
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleExclusiveOptions(v.getId(), optionRelation);
                    toggleResponseIndex(v.getId());
                }
            });
            mCheckBoxes.add(checkbox);
            setOptionPopUpInstruction(questionComponent, checkbox, optionId, optionRelation);
        }
        beforeAddViewHook(questionComponent);
    }

    private void toggleExclusiveOptions(int optionIndex, OptionRelation optionRelation) {
        if (mCheckBoxes.get(optionIndex).isChecked()) {
            String excluded = getOptionSetOptionRelation(optionRelation).optionSetOption.getExclusionIds();
            if (!TextUtils.isEmpty(excluded)) {
                for (String id : excluded.split(COMMA)) {
                    for (int k = 0; k < getOptionSetOptionRelations().size(); k++) {
                        OptionSetOptionRelation relation = getOptionSetOptionRelations().valueAt(k);
                        if (NumberUtils.isNumber(id) && relation.optionSetOption.getRemoteId().equals(Long.valueOf(id))) {
                            int index = getOptionRelations().indexOf(relation.options.get(0));
                            resetExclusives(Integer.valueOf(index));
                            Set<Integer> set = mExclusives.get(index);
                            if (set == null) set = new HashSet<>();
                            set.add(optionIndex);
                            mExclusives.put(index, set);
                        }
                    }
                }
            } else {
                Set<Integer> set = mExclusives.get(optionIndex);
                if (set == null) return;
                for (Integer index : set) {
                    resetExclusives(index);
                }
            }
        }
    }

    private void resetExclusives(Integer index) {
        if (mCheckBoxes.get(index).isChecked()) {
            mCheckBoxes.get(index).setChecked(false);
            mResponseIndices.remove(index);
        }
    }

    @Override
    protected String serialize() {
        return FormatUtils.arrayListToString(mResponseIndices);
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals(BLANK)) {
            for (CheckBox box : mCheckBoxes) {
                if (box.isChecked()) {
                    box.setChecked(false);
                }
            }
        } else {
            String[] listOfIndices = responseText.split(COMMA);
            for (String index : listOfIndices) {
                if (!index.equals(BLANK)) {
                    int indexInteger = Integer.parseInt(index);
                    mCheckBoxes.get(indexInteger).setChecked(true);
                    toggleExclusiveOptions(indexInteger, getOptionRelations().get(indexInteger));
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
