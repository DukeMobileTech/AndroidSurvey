package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.multidex.BuildConfig;

import com.opencsv.CSVReader;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.InstructionRelation;
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

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.EDIT_TEXT_DELAY;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtmlWhitelist;

public abstract class ListOfItemsViewHolder extends QuestionViewHolder {
    private ArrayList<EditText> mEditTexts;
    private boolean textResponseOn = true;

    ListOfItemsViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected abstract EditText createEditText();

    protected void createQuestionComponent(ViewGroup questionComponent) {
        if (questionComponent == null) return;
        questionComponent.removeAllViews();
        mEditTexts = new ArrayList<>();
        for (OptionRelation optionRelation : getOptionRelations()) {
            int optionId = getOptionRelations().indexOf(optionRelation);
            final TextView optionText = new TextView(getContext());
            final InstructionRelation optionInstruction = getOptionInstruction(optionRelation.option.getIdentifier());
            if (optionInstruction != null) {
                optionText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                setCompoundDrawableRight(optionText, getContext().getResources().getDrawable(R.drawable.ic_info_outline_blue_24dp),
                        getOptionPopUpInstructions(optionInstruction));
            }
            optionText.setText(styleTextWithHtmlWhitelist(TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel())));
            toggleCarryForward(optionText, optionId);
            questionComponent.addView(optionText);
            final EditText editText = createEditText();
            editText.setHint(R.string.free_response_edittext);
            toggleCarryForward(editText, optionId);
            questionComponent.addView(editText);
            mEditTexts.add(editText);
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
                    if (!isDeserializing()) {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // Run on UI Thread
                                if (getContext() != null) {
                                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (textResponseOn) {
                                                saveResponse();
                                                editText.clearFocus();
                                            }
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
        for (int i = 0; i < mEditTexts.size(); i++) {
            serialized.append(StringEscapeUtils.escapeCsv(mEditTexts.get(i).getText().toString()));
            if (i < mEditTexts.size() - 1) serialized.append(COMMA);
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
                if (mEditTexts.size() > i)
                    mEditTexts.get(i).setText(listOfResponses[i]);
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "IOException " + e.getMessage());
        }
    }

    @Override
    protected void unSetResponse() {
        textResponseOn = false;
        for (int i = 0; i < mEditTexts.size(); i++) {
            mEditTexts.get(i).setText(BLANK);
            mEditTexts.get(i).clearFocus();
        }
    }

    @Override
    protected void showOtherText(int position) {
    }

}
