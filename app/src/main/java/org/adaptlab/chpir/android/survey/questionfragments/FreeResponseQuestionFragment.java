package org.adaptlab.chpir.android.survey.questionfragments;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SingleQuestionFragment;
import org.adaptlab.chpir.android.survey.models.Validation;
import org.adaptlab.chpir.android.survey.verhoeff.ParticipantIdValidator;

public class FreeResponseQuestionFragment extends SingleQuestionFragment {
    private static final String TAG = "FreeResponseQuestionFragment";
    private String mText = "";
    private EditText mFreeText;

    @Override
    public void createQuestionComponent(ViewGroup questionComponent) {
        mFreeText = new EditText(getActivity());
        mFreeText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        beforeAddViewHook(mFreeText);
        mFreeText.setHint(R.string.free_response_edittext);
        mFreeText.addTextChangedListener(new TextWatcher() {
            private boolean backspacing = false;
            // Required by interface
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                backspacing = before > count;
                mText = s.toString();
                setResponseText();
            }

            public void afterTextChanged(Editable s) {
                if (mSpecialResponses != null && s.length() > 0) {
                    mSpecialResponses.clearCheck();
                }
                if (!backspacing && getQuestion().getValidation() != null && getQuestion()
                        .getValidation().getValidationType().equals(
                                Validation.Type.VERHOEFF.toString())) {
                    mFreeText.removeTextChangedListener(this);
                    mFreeText.setText(ParticipantIdValidator.formatText(s.toString()));
                    mFreeText.setSelection(mFreeText.getText().length());
                    mFreeText.addTextChangedListener(this);
                }
            }
        });
        questionComponent.addView(mFreeText);
    }

    protected void beforeAddViewHook(EditText editText) {
        if (getQuestion().getValidation() != null && getQuestion().getValidation()
                .getValidationType().equals(Validation.Type.VERHOEFF.toString())) {
            mFreeText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        }
    }

    @Override
    protected void deserialize(String responseText) {
        mFreeText.setText(responseText);
    }

    @Override
    protected String serialize() {
        return mText;
    }

    @Override
    protected void unSetResponse() {
        mFreeText.setText("");
        mResponse.setResponse("");
    }
}