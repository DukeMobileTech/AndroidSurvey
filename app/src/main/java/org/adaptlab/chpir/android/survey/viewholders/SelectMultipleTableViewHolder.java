package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class SelectMultipleTableViewHolder extends TableQuestionViewHolder {
    private ArrayList<String> mResponseIndices;
    private ArrayList<CheckBox> mCheckBoxes;

    SelectMultipleTableViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context);
        setOnResponseSelectedListener(listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mCheckBoxes = new ArrayList<>();
        for (int k = 0; k < getOptions().size(); k++) {
            CheckBox checkBox = new CheckBox(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getOptionWidth() / 2,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = getOptionWidth() / 2;
            checkBox.setLayoutParams(params);
            checkBox.setId(k);
            mCheckBoxes.add(checkBox);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleResponseIndex(v.getId());
                }
            });
            questionComponent.addView(checkBox);
        }
        setSpecialResponseView();
    }

    private void toggleResponseIndex(int index) {
        String selected = String.valueOf(index);
        if (mResponseIndices.contains(selected)) {
            mResponseIndices.remove(selected);
        } else {
            mResponseIndices.add(selected);
        }
        saveResponse();
    }

    @Override
    protected void deserialize(String responseText) {
        mResponseIndices = new ArrayList<>();
        if (TextUtils.isEmpty(responseText)) {
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
                    mResponseIndices.add(index);
                }
            }
        }
    }

    @Override
    protected String serialize() {
        return StringUtils.join(mResponseIndices, COMMA);
    }

}