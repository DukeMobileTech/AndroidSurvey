package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.support.multidex.BuildConfig;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.opencsv.CSVReader;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public abstract class ListOfItemsViewHolder extends SingleQuestionViewHolder {
    private ArrayList<EditText> mResponses;

    ListOfItemsViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected abstract EditText createEditText();

    protected void createQuestionComponent(ViewGroup questionComponent) {
//        mResponses = new ArrayList<>();
//        for (Option option : getOptions()) {
//            final TextView optionText = new TextView(getActivity());
//            optionText.setText(getOptionText(option));
//            questionComponent.addView(optionText);
//            EditText editText = createEditText();
//            editText.setHint(R.string.free_response_edittext);
//            editText.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
//            questionComponent.addView(editText);
//            mResponses.add(editText);
//            editText.addTextChangedListener(new TextWatcher() {
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    setResponse(null);
//                }
//
//                // Required by interface
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                }
//
//                public void afterTextChanged(Editable s) {
//                    if (mSpecialResponses != null && s.length() > 0) {
//                        mSpecialResponses.clearCheck();
//                    }
//                }
//            });
//        }
    }

    @Override
    protected String serialize() {
        StringBuilder serialized = new StringBuilder();
        for (int i = 0; i < mResponses.size(); i++) {
            serialized.append(StringEscapeUtils.escapeCsv(mResponses.get(i).getText().toString()));
            if (i < mResponses.size() - 1) serialized.append(COMMA);
        }
        return serialized.toString();
    }

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
            if (BuildConfig.DEBUG) Log.e(TAG, "IOException " + e.getMessage());
        }
    }

//    @Override
//    protected void unSetResponse() {
//        for (EditText oneEditText : mResponses) {
//            oneEditText.setText(Response.BLANK);
//        }
//        setResponseTextBlank();
//    }

}
