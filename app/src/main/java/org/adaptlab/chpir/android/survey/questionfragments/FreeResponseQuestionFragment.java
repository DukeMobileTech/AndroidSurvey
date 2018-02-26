package org.adaptlab.chpir.android.survey.questionfragments;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.QuestionFragment;
import org.adaptlab.chpir.android.survey.R;

public class FreeResponseQuestionFragment extends QuestionFragment {
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
            // Required by interface
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mText = s.toString();
                setResponseText();
            }

            public void afterTextChanged(Editable s) {
                if (mSpecialResponses != null && s.length() > 0) {
                    mSpecialResponses.clearCheck();
                }
            }
        });

//        showKeyBoard();
        questionComponent.addView(mFreeText);
    }

    // This is used to restrict allowed input in subclasses.
    protected void beforeAddViewHook(EditText editText) {
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