package org.adaptlab.chpir.android.survey.questionfragments;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.QuestionFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Response;

public class FreeResponseQuestionFragment extends QuestionFragment {
    private static final String TAG = "FreeResponseQuestionFragment";
    private String mText = Response.BLANK;
    private EditText mFreeText;
    private TextWatcher mTextWatcher;
    private boolean initialState;


    @Override
    public void createQuestionComponent(ViewGroup questionComponent) {
        mFreeText = new EditText(getActivity());
        mFreeText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        beforeAddViewHook(mFreeText);
        mFreeText.setHint(R.string.free_response_edittext);
        initialState = true;
        mTextWatcher = new TextWatcher() {
            // Required by interface
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mText = s.toString();
                if (!initialState) {
                    clearSpecialResponseSelection();
                }
                initialState = false;
                setResponseText();
            }

            public void afterTextChanged(Editable s) {
            }
        };
        mFreeText.addTextChangedListener(mTextWatcher);
        mFreeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFreeText.addTextChangedListener(mTextWatcher);
            }
        });
        mFreeText.requestFocus();
        showKeyBoard();
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
        mText = Response.BLANK;
        mFreeText.removeTextChangedListener(mTextWatcher);
    }

}