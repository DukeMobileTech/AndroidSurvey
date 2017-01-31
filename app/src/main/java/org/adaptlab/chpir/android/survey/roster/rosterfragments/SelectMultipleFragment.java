package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.adaptlab.chpir.android.survey.models.Option;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Harry on 1/19/17.
 */
public class SelectMultipleFragment extends RosterFragment {
    ArrayList<Integer> mResponseIndices = new ArrayList<>();
    private ArrayList<CheckBox> mCheckBoxes = new ArrayList<>();
    @Override
    protected void createResponseComponent(ViewGroup responseComponent) {
        for (int i = 0; i < getQuestion().defaultOptions().size(); i++) {
            String option = getQuestion().defaultOptions().get(i).getText();
            CheckBox checkbox = new CheckBox(getActivity());
            checkbox.setText(option);
            checkbox.setId(i);
            final int checkIndex = i;
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    toggleResponseIndex(checkIndex);
                }
            });
            mCheckBoxes.add(checkbox);
            responseComponent.addView(checkbox,i);
        }
        Log.i("Response",getResponse().getText());
        deserialize(getResponse().getText());
    }

    protected String serialize() {
        String serialized = "";
        for (int i = 0; i < mResponseIndices.size(); i++) {
            serialized += mResponseIndices.get(i);
            if (i <  mResponseIndices.size() - 1) serialized += LIST_DELIMITER;
        }
        return serialized;
    }

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
        Collections.sort(mResponseIndices);
        getResponse().setResponse(serialize());
    }
}
