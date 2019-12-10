package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.multidex.BuildConfig;

import com.opencsv.CSVReader;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
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

public class IntegerBoxesTableViewHolder extends TableQuestionViewHolder {
    private ArrayList<EditText> mResponses;

    IntegerBoxesTableViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context);
        setOnResponseSelectedListener(listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        if (questionComponent == null) return;
        questionComponent.removeAllViews();
        mResponses = new ArrayList<>();
        for (int k = 0; k < getOptionRelations().size(); k++) {
            EditText editText = createEditText();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getOptionWidth() / 2,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = getOptionWidth() / 2;
            editText.setLayoutParams(params);
            editText.setHint(R.string.free_response_edittext);
            questionComponent.addView(editText);
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
            mResponses.add(editText);
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
        if (TextUtils.isEmpty(responseText)) return;
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

    private EditText createEditText() {
        EditText editText = new EditText(getContext());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        return editText;
    }

}
