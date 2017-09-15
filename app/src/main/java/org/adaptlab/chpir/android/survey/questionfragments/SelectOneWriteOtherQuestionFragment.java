package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Response;

public class SelectOneWriteOtherQuestionFragment extends SelectOneQuestionFragment {
    private static final String TAG = "SelectOneWriteOtherQuestionFragment";
    private EditText mOtherText;
    private RadioButton mRadioButton;

    @Override
    protected void beforeAddViewHook(ViewGroup questionComponent) {
        mRadioButton = new RadioButton(getActivity());
        mOtherText = new EditText(getActivity());
        
        mRadioButton.setText(R.string.other_specify);
        mRadioButton.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
        final int otherId = getOptions().size();
        mRadioButton.setId(otherId);
        mRadioButton.setLayoutParams(new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedId = v.getId();
                if (checkedId == otherId) {
                    mOtherText.setEnabled(true);
                    mOtherText.requestFocus();
                    showKeyBoard();
                }
                setResponseIndex(checkedId);
            }
        });
        getRadioGroup().addView(mRadioButton, otherId);
        addOtherResponseView(mOtherText);
        questionComponent.addView(mOtherText);
    }

    @Override
    protected void disableOtherTextButton() {
        mOtherText.setEnabled(false);
        hideKeyBoard();
        mOtherText.getText().clear();
        setOtherResponse(Response.BLANK);
    }

    @Override
    protected void unSetResponse() {
        super.unSetResponse();
        mRadioButton.setChecked(false);
        mOtherText.setText(Response.BLANK);
    }

}
