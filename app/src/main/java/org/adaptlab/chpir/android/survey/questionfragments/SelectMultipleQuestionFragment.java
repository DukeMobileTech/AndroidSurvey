package org.adaptlab.chpir.android.survey.questionfragments;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.adaptlab.chpir.android.survey.SingleQuestionFragment;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Response;

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
            final CheckBox checkbox = new CheckBox(getActivity());
            checkbox.setText(option.getText(getInstrument()));
            checkbox.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext
                    ()));
            checkbox.setId(optionId);
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSpecialResponses != null) {
                        mSpecialResponses.clearCheck();
                    }
                    toggleResponseIndex(v.getId());
                }
            });
            mCheckBoxes.add(checkbox);
            questionComponent.addView(checkbox, optionId);
        }
        beforeAddViewHook(questionComponent);
    }

    @Override
    protected String serialize() {
        StringBuilder serialized = new StringBuilder();
        for (int i = 0; i < mResponseIndices.size(); i++) {
            serialized.append(mResponseIndices.get(i));
            if (i < mResponseIndices.size() - 1) serialized.append(Response.LIST_DELIMITER);
        }
        return serialized.toString();
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
        if (mResponse != null) {
            mResponse.setResponse("");
        }
        for (CheckBox oneBox : mCheckBoxes) {
            oneBox.setChecked(false);
        }
    }
}
