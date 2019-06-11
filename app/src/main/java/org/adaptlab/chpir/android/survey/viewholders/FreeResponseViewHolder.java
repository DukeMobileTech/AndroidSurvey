package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;

import java.util.Timer;
import java.util.TimerTask;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.EDIT_TEXT_DELAY;

public class FreeResponseViewHolder extends SingleQuestionViewHolder {
    private EditText mFreeText;
    private String mText = "";
    private TextWatcher mTextWatcher = new TextWatcher() {
        private boolean backspacing = false;
        private Timer timer;

        // Required by interface
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (timer != null) timer.cancel();
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            getSpecialResponses().clearCheck();
            if (!FormatUtils.isEmpty(s.toString())) {
                backspacing = before > count;
                mText = s.toString();
            }
        }

        public void afterTextChanged(Editable s) {
//            if (!backspacing && getQuestion().getValidation() != null && getQuestion()
//                    .getValidation().getValidationType().equals(
//                            Validation.Type.VERHOEFF.toString())) {
            if (!backspacing) {
                mFreeText.removeTextChangedListener(this);
//                mFreeText.setText(ParticipantIdValidator.formatText(s.toString()));
                mFreeText.setSelection(mFreeText.getText().length());
                mFreeText.addTextChangedListener(this);
            }
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
    };

    FreeResponseViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    public void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mFreeText = new EditText(getContext());
        mFreeText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        beforeAddViewHook(mFreeText);
        mFreeText.setHint(R.string.free_response_edittext);
        mFreeText.addTextChangedListener(mTextWatcher);
        questionComponent.addView(mFreeText);
    }

    protected void beforeAddViewHook(EditText editText) {
//        if (getQuestion().getValidation() != null && getQuestion().getValidation()
//                .getValidationType().equals(Validation.Type.VERHOEFF.toString())) {
//            mFreeText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
//        }
    }

    @Override
    protected void deserialize(String responseText) {
        mFreeText.setText(responseText);
    }

    @Override
    protected String serialize() {
        return mText;
    }
}