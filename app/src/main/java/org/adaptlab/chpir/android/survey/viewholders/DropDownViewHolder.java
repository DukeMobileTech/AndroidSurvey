package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.Option;

import java.util.ArrayList;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;

public class DropDownViewHolder extends QuestionViewHolder {
    private Spinner mSpinner;
    private ArrayAdapter<String> mAdapter;
    private int mResponseIndex;

    DropDownViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    //    @Override
//    protected void unSetResponse() {
//        clearAdapter();
//        setSpinnerAdapter();
//        setResponseTextBlank();
//    }
//
    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.spinner, null);
            mSpinner = view.findViewById(R.id.options_spinner);
            setSpinnerAdapter();
            questionComponent.addView(view);
        }
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals(BLANK)) {
            clearAdapter();
            setSpinnerAdapter();
        } else {
            mResponseIndex = Integer.parseInt(responseText);
            mSpinner.setSelection(mResponseIndex);
        }
    }

    @Override
    protected String serialize() {
        return String.valueOf(mResponseIndex);
    }

    protected void setResponseIndex(int index) {
        mResponseIndex = index;
//        setResponse(null);
    }

    private void clearAdapter() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void setSpinnerAdapter() {
        ArrayList<String> optionsArray = new ArrayList<>();
        for (Option option : getOptions()) {
            optionsArray.add(option.getText());
        }
        optionsArray.add(""); // Adds empty selection
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, optionsArray);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setResponseIndex(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinner.setSelection(getOptions().size()); // Selects empty selection
    }
}
