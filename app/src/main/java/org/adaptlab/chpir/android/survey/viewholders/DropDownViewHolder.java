package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;

import java.util.ArrayList;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtmlWhitelist;

public class DropDownViewHolder extends QuestionViewHolder {
    private Spinner mSpinner;
    private ArrayAdapter<String> mAdapter;
    private TextView mOtherTextView;
    private int mResponseIndex;
    private boolean initialSetup;

    DropDownViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.spinner, null);
            mSpinner = view.findViewById(R.id.options_spinner);
            setSpinnerAdapter();

            mOtherTextView = new TextView(getContext());
            mOtherTextView.setVisibility(View.GONE);
            mOtherTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTextEntry(mResponseIndex);
                }
            });

            questionComponent.addView(view);
            questionComponent.addView(mOtherTextView);
        }
    }

    @Override
    protected void showOtherText(int position) {
        if (position < getOptionRelations().size()) {
            OptionRelation optionRelation = getOptionRelations().get(position);
            if (getTextEntryOptionIds().contains(optionRelation.option.getRemoteId())) {
                mOtherTextView.setVisibility(View.VISIBLE);
                mOtherTextView.setText(getResponse().getOtherText());
            } else {
                mOtherTextView.setVisibility(View.GONE);
            }
        } else {
            mOtherTextView.setVisibility(View.GONE);
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

    @Override
    protected void unSetResponse() {
        clearAdapter();
        setSpinnerAdapter();
        mOtherTextView.setVisibility(View.GONE);
    }

    private void setResponseIndex(int index) {
        mResponseIndex = index;
        saveResponse();
    }

    private void clearAdapter() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void setSpinnerAdapter() {
        ArrayList<String> optionsArray = new ArrayList<>();
        for (OptionRelation optionRelation : getOptionRelations()) {
            optionsArray.add(styleTextWithHtmlWhitelist(
                    TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel())).toString());
        }
        optionsArray.add(BLANK); // Adds empty selection
        mAdapter = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, optionsArray);
        mAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (initialSetup) {
                    showOtherText(position);
                } else {
                    setResponseIndex(position);
                    setTextEntry(position);
                }
                initialSetup = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        initialSetup = true;
        mSpinner.setSelection(getOptionRelations().size()); // Selects empty selection
    }
}
