package org.adaptlab.chpir.android.survey.QuestionFragments;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.opencsv.CSVReader;

import org.adaptlab.chpir.android.survey.Models.Option;
import org.adaptlab.chpir.android.survey.QuestionFragment;
import org.adaptlab.chpir.android.survey.R;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public abstract class ListOfItemsQuestionFragment extends QuestionFragment {
    private static final String TAG = "ListOfItemsQuestionFragment";
    private ArrayList<EditText> mResponses;
    protected abstract EditText createEditText();
    
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mResponses = new ArrayList<EditText>();
        for (Option option : getOptions()) {
            final TextView optionText = new TextView(getActivity());
            optionText.setText(option.getText());
            questionComponent.addView(optionText);
            EditText editText = createEditText();
            editText.setHint(R.string.free_response_edittext);
            editText.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
            questionComponent.addView(editText);
            mResponses.add(editText);
            editText.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setResponseText();
                }
                
                // Required by interface
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void afterTextChanged(Editable s) { }
            });
        }
    }

    @Override
    protected String serialize() {
        String serialized = "";
        for (int i = 0; i < mResponses.size(); i++) {
            serialized += StringEscapeUtils.escapeCsv(mResponses.get(i).getText().toString());
            if (i <  mResponses.size() - 1) serialized += LIST_DELIMITER;
        }
        return serialized;
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) return;    
        InputStream input = new ByteArrayInputStream(responseText.getBytes(Charsets.UTF_8));
        InputStreamReader inputReader = new InputStreamReader(input);
        CSVReader reader = new CSVReader(inputReader);
        String[] listOfResponses;
        try {
            listOfResponses = reader.readNext();
            for (int i = 0; i < listOfResponses.length; i++) {
                if (mResponses.size() > i)
                    mResponses.get(i).setText(listOfResponses[i]);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException " + e.getMessage());
        }
    }
}
