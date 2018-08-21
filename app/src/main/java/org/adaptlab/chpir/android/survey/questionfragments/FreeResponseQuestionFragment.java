package org.adaptlab.chpir.android.survey.questionfragments;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SingleQuestionFragment;
import org.adaptlab.chpir.android.survey.models.Validation;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.verhoeff.ParticipantIdValidator;

public class FreeResponseQuestionFragment extends SingleQuestionFragment {
    private static final String TAG = "FreeResponseQuestionFragment";
    private String mText = "";
    private EditText mFreeText;
    private TextWatcher mTextWatcher = new TextWatcher() {
        private boolean backspacing = false;
        // Required by interface
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!FormatUtils.isEmpty(s.toString())) {
                backspacing = before > count;
                mText = s.toString();
                setResponse(null);
            }
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
    };

    @Override
    public void createQuestionComponent(ViewGroup questionComponent) {
        mFreeText = new EditText(getActivity());
        mFreeText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        beforeAddViewHook(mFreeText);
        mFreeText.setHint(R.string.free_response_edittext);
        mFreeText.addTextChangedListener(mTextWatcher);
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
        mFreeText.removeTextChangedListener(mTextWatcher);
        mFreeText.setText("");
        mResponse.setResponse("");
        mFreeText.addTextChangedListener(mTextWatcher);
    }
}