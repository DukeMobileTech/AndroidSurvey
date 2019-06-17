package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.utils.FormatUtils;

public class RangeViewHolder extends QuestionViewHolder {
    private String mTextStart = "";
    private String mTextEnd = "";
    private EditText mStart;
    private EditText mEnd;

    RangeViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

//    @Override
//    protected void unSetResponse() {
//        mStart.setText(Response.BLANK);
//        mEnd.setText(Response.BLANK);
//        if (getResponse() != null) {
//            getResponse().setResponse(Response.BLANK);
//        }
//    }

    @Override
    public void createQuestionComponent(ViewGroup questionComponent) {
//        mStart = setEditTexts(0);
//        mEnd = setEditTexts(1);
//        setTextViews(questionComponent, R.string.start);
//        questionComponent.addView(mStart);
//        setTextViews(questionComponent, R.string.end);
//        questionComponent.addView(mEnd);
    }

//    private EditText setEditTexts(int pos) {
//        EditText editText = new EditText(getActivity());
//        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
//                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
//        beforeAddViewHook(editText);
//        editText.setHint(R.string.free_response_edittext);
//        setListeners(editText, pos);
//        return editText;
//    }
//
//    private void setTextViews(ViewGroup questionComponent, int text) {
//        TextView start = new TextView(getActivity());
//        start.setText(text);
//        questionComponent.addView(start);
//    }

    protected void beforeAddViewHook(EditText editText) {
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_NUMBER);
    }

//    private void setListeners(EditText editText, final int pos) {
//        editText.addTextChangedListener(new TextWatcher() {
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (pos == 0) {
//                    mTextStart = s.toString();
//                } else {
//                    mTextEnd = s.toString();
//                }
//                setResponse(null);
//            }
//
//            public void afterTextChanged(Editable s) {
//                if (mSpecialResponses != null && s.length() > 0) {
//                    mSpecialResponses.clearCheck();
//                }
//            }
//        });
//    }

    @Override
    protected void deserialize(String responseText) {
        if (!FormatUtils.isEmpty(responseText) && responseText.contains("-")) {
            String[] ranges = responseText.split("-");
            mStart.setText(ranges[0]);
            if (ranges.length == 2) mEnd.setText(ranges[1]);
        }
    }

    @Override
    protected String serialize() {
        return mTextStart + "-" + mTextEnd;
    }
}