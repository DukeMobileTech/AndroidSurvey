package org.adaptlab.chpir.android.survey.questionfragments;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.QuestionFragment;

import java.util.ArrayList;

public class SelectMultipleQuestionFragment extends QuestionFragment {
    private ArrayList<Integer> mResponseIndices;
    protected ArrayList<CheckBox> mCheckBoxes;
    
    // This is used to add additional UI components in subclasses.
    protected void beforeAddViewHook(ViewGroup questionComponent) {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mCheckBoxes = new ArrayList<CheckBox>();
        mResponseIndices = new ArrayList<Integer>();
        for (Option option : getOptions()) {
            final int optionId = getOptions().indexOf(option);
            CheckBox checkbox = new CheckBox(getActivity());
            checkbox.setText(option.getText());
            checkbox.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
            checkbox.setId(optionId);     
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    toggleResponseIndex(optionId);
                }
            });
            checkbox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mSpecialResponses!=null){
                        mSpecialResponses.clearCheck();
                    }
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
        if (mResponseIndices.contains(index)) {
            mResponseIndices.remove((Integer) index);
        } else {
            mResponseIndices.add(index);
        }
        setResponseText();
    }
    
    protected void addCheckBox(CheckBox checkbox) {
        mCheckBoxes.add(checkbox);
    }

    @Override
    protected void unSetResponse() {
        if(mResponse!=null){
            mResponse.setResponse("");
        }
        for(CheckBox oneBox: mCheckBoxes){
            oneBox.setChecked(false);
        }
    }
}
