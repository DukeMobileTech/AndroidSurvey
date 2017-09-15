package org.adaptlab.chpir.android.survey.questionfragments;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.opencsv.CSVReader;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.QuestionFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Option;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class ListOfItemsQuestionFragment extends QuestionFragment {
    private static final String TAG = "ListOfItemsQuestionFragment";
    private ArrayList<EditText> mResponses;
    private ArrayList<TextWatcher> mTextWatchers;
    private ArrayList<Boolean> mInitialStates;
    protected abstract EditText createEditText();
    
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mResponses = new ArrayList<>();
        mTextWatchers = new ArrayList<>();
        mInitialStates = new ArrayList<>();
        int index = 0;
        for (final Option option : getOptions()) {
            mInitialStates.add(true);
            final TextView optionText = new TextView(getActivity());
            optionText.setText(option.getText());
            questionComponent.addView(optionText);
            final EditText editText = createEditText();
            editText.setHint(R.string.free_response_edittext);
            editText.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
            questionComponent.addView(editText);
            mResponses.add(editText);
            TextWatcher textWatcher = new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int position = getOptions().indexOf(option);
                    if (!mInitialStates.get(position)) clearSpecialResponseSelection();
                    mInitialStates.set(position, false);
                    setResponseText();
                }

                // Required by interface
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void afterTextChanged(Editable s) { }
            };
            editText.addTextChangedListener(textWatcher);
            mTextWatchers.add(textWatcher);
            if (index == 0) {
                editText.requestFocus();
                showKeyBoard();
            }
            index++;
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = mResponses.indexOf(editText);
                    editText.addTextChangedListener(mTextWatchers.get(index));
                }
            });
        }
    }

    protected List<EditText> getResponseEditTexts() {
        return mResponses;
    }

    protected List<TextWatcher> getTextWatchers() {
        return mTextWatchers;
    }

    protected List<Boolean> getInitialStates() {
        return mInitialStates;
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
            if(BuildConfig.DEBUG) Log.e(TAG, "IOException " + e.getMessage());
        }
    }
}
