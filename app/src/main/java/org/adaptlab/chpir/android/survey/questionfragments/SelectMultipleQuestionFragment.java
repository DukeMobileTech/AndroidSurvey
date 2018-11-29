package org.adaptlab.chpir.android.survey.questionfragments;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SingleQuestionFragment;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;

import java.util.ArrayList;

public class SelectMultipleQuestionFragment extends SingleQuestionFragment {
    private final static String TAG = "SelectMultipleFragment";
    private ArrayList<Integer> mResponseIndices;
    protected ArrayList<CheckBox> mCheckBoxes;

    // This is used to add additional UI components in subclasses.
    protected void beforeAddViewHook(ViewGroup questionComponent) {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mCheckBoxes = new ArrayList<>();
        mResponseIndices = new ArrayList<>();
        for (Option option : getOptions()) {
            final int optionId = getOptions().indexOf(option);
            CheckBox checkbox = new CheckBox(getActivity());
            checkbox.setText(option.getText(getInstrument()));
            checkbox.setTypeface(getInstrument().getTypeFace(
                    getActivity().getApplicationContext()));
            checkbox.setTextColor(getResources().getColorStateList(R.color.states));
            checkbox.setId(optionId);
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSpecialResponses != null) {
                        mSpecialResponses.clearCheck();
                    }
                    checkOptionExclusivity(v);
                    toggleResponseIndex(v.getId());
                    if (getQuestion().rankResponses()) {
                        optionToggled(v.getId());
                    }
                }
            });
            mCheckBoxes.add(checkbox);
            questionComponent.addView(checkbox, optionId);
        }
        beforeAddViewHook(questionComponent);
    }

    protected void checkOptionExclusivity(View v) {
        if (getQuestion().hasExclusiveOption() && (int) v.getId() < getOptions().size()) {
            Option selectedOption = getOptions().get(v.getId());
            CheckBox selectedCheckbox = (CheckBox) v;
            if (selectedCheckbox.isChecked() && selectedOption.isExclusive(getQuestion())) {
                for (CheckBox checkBox : mCheckBoxes) {
                    if (checkBox != selectedCheckbox && checkBox.isChecked()) {
                        checkBox.setChecked(false);
                    }
                }
                mResponseIndices.clear();
            } else {
                for (CheckBox checkBox : mCheckBoxes) {
                    int index = checkBox.getId();
                    if (checkBox.isChecked() && index < getOptions().size() &&
                            getOptions().get(index).isExclusive(getQuestion())) {
                        checkBox.setChecked(false);
                        if (mResponseIndices.contains(index)) {
                            mResponseIndices.remove((Integer) index);
                        }
                    }
                }
            }
        }
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
            String[] listOfIndices = responseText.split(Response.LIST_DELIMITER);
            for (String index : listOfIndices) {
                if (!index.equals("")) {
                    Integer indexInteger = Integer.parseInt(index);
                    mCheckBoxes.get(indexInteger).setChecked(true);
                    mResponseIndices.add(indexInteger);
                }
            }
        }
    }

    protected void toggleResponseIndex(int index) {
        if (mResponseIndices.contains(index)) {
            mResponseIndices.remove((Integer) index);
        } else {
            mResponseIndices.add(index);
        }
        setResponse(null);
    }

    protected void addCheckBox(CheckBox checkbox) {
        mCheckBoxes.add(checkbox);
    }

    @Override
    protected void unSetResponse() {
        for (CheckBox box : mCheckBoxes) {
            if (box.isChecked()) {
                box.setChecked(false);
            }
        }
        setResponseTextBlank();
        if (getQuestion().rankResponses()) {
            getResponse().setRankOrder(Response.BLANK);
        }
    }

}
