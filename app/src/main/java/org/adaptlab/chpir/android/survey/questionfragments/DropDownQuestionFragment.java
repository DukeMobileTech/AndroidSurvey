package org.adaptlab.chpir.android.survey.questionfragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.adaptlab.chpir.android.survey.QuestionFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Option;

import java.util.ArrayList;

public class DropDownQuestionFragment extends QuestionFragment {
    private static final String TAG = "DropDownQuestionFragment";
    private int mResponseIndex;
    private Spinner mSpinner;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void unSetResponse() {
        clearAdapter();
        setSpinnerAdapter();
        if (mResponse != null) {
            mResponse.setResponse("");
        }
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.spinner, null);
        mSpinner = view.findViewById(R.id.options_spinner);
        setSpinnerAdapter();
        questionComponent.addView(view);
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
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
        setResponseText();
    }

    private void clearAdapter() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void setSpinnerAdapter() {
        ArrayList<String> optionsArray = new ArrayList<>();
        for (Option option : getOptions()) {
            optionsArray.add(option.getText(getInstrument()));
        }
        optionsArray.add(""); // Adds empty selection
        mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                optionsArray);
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
