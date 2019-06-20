package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.multidex.BuildConfig;

import com.opencsv.CSVReader;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.EDIT_TEXT_DELAY;

public abstract class ListOfItemsViewHolder extends QuestionViewHolder {
    private ArrayList<EditText> mResponses;

    ListOfItemsViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected abstract EditText createEditText();

    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mResponses = new ArrayList<>();
        for (OptionRelation optionRelation : getOptionRelations()) {
            final TextView optionText = new TextView(getContext());
            optionText.setText(TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel()));
            questionComponent.addView(optionText);
            EditText editText = createEditText();
            editText.setHint(R.string.free_response_edittext);
//            editText.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
            questionComponent.addView(editText);
            mResponses.add(editText);
            editText.addTextChangedListener(new TextWatcher() {
                private Timer timer;

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (timer != null) timer.cancel();
                }

                // Required by interface
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                    timer = new Timer();
                    if (!isDeserialization()) {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // Run on UI Thread
                                if (getContext() != null) {
                                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            saveResponse();
                                        }
                                    });
                                }
                            }
                        }, EDIT_TEXT_DELAY); // delay before saving to db
                    }
                }
            });
        }
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

}
