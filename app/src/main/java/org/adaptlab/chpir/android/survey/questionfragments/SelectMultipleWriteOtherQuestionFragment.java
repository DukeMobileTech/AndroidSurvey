package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Response;

public class SelectMultipleWriteOtherQuestionFragment extends SelectMultipleQuestionFragment {
    private CheckBox mCheckbox;
    private EditText mOtherText;

    @Override
    protected void beforeAddViewHook(ViewGroup questionComponent) {
        mCheckbox = new CheckBox(getActivity());
        mOtherText = new EditText(getActivity());
        mCheckbox.setText(R.string.other_specify);
        mCheckbox.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));
        final int otherId = getOptions().size();
        mCheckbox.setId(otherId);
        mCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mOtherText.setEnabled(true);
                    mOtherText.requestFocus();
                    showKeyBoard();
                } else {
                    mOtherText.setEnabled(false);
                    hideKeyBoard();
                    mOtherText.getText().clear();
                }
                toggleResponseIndex(otherId);
            }
        });
        questionComponent.addView(mCheckbox, otherId);
        addOtherResponseView(mOtherText);
        addCheckBox(mCheckbox);
        questionComponent.addView(mOtherText);
    }

    @Override
    protected void unSetResponse() {
        super.unSetResponse();
        mCheckbox.setChecked(false);
        mOtherText.setText(Response.BLANK);
    }
}