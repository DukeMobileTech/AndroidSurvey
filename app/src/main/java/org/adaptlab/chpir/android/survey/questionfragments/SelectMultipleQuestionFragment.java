package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.adaptlab.chpir.android.survey.QuestionFragment;
import org.adaptlab.chpir.android.survey.models.Option;

import java.util.ArrayList;

public class SelectMultipleQuestionFragment extends QuestionFragment {
    private static final String TAG = "SelectMultipleQuestionFragment";
    private ArrayList<Integer> mResponseIndices;
    private ArrayList<CheckBox> mCheckBoxes;
    
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
            checkbox.setText(option.getText());
            checkbox.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
            checkbox.setId(optionId);
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
        String serialized = "";
        for (int i = 0; i < mResponseIndices.size(); i++) {
            serialized += mResponseIndices.get(i);
            if (i <  mResponseIndices.size() - 1) serialized += LIST_DELIMITER;
        }
        return serialized;
    }

    @Override
    protected void unSetResponse() {
        for (CheckBox checkBox : mCheckBoxes) {
            if (checkBox.isChecked()) checkBox.setChecked(false);
        }
        mResponseIndices.clear();
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
			String[] listOfIndices = responseText.split(LIST_DELIMITER);
			for (String index : listOfIndices) {
				if (!index.equals("")) {
					Integer indexInteger = Integer.parseInt(index);
					mCheckBoxes.get(indexInteger).setChecked(true);
				}
			}
		}
	}
    
    protected void toggleResponseIndex(int index) {
        clearSpecialResponseSelection();
        mResponseIndices.clear();
        for (CheckBox checkbox : mCheckBoxes) {
            if (checkbox.isChecked()) mResponseIndices.add(checkbox.getId());
        }
        setResponseText();
    }
    
    protected void addCheckBox(CheckBox checkbox) {
        mCheckBoxes.add(checkbox);
    }
    
}
