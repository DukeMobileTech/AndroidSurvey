package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.adaptlab.chpir.android.survey.R;

public class SelectOneWriteOtherViewHolder extends SelectOneViewHolder {
    private EditText otherText = null;
    private RadioButton radioButton;

    SelectOneWriteOtherViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void beforeAddViewHook(final ViewGroup questionComponent) {
        radioButton = new RadioButton(getContext());
        otherText = new EditText(getContext());
        otherText.setSingleLine(false);
        otherText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        final int otherId = getOptionRelations().size();
        radioButton.setId(otherId);
        setOptionText(getContext().getString(R.string.other_specify), radioButton);
        toggleCarryForward(radioButton, otherId);
        radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionComponent.requestFocus();
                int id = v.getId();
                if (id != -1) {
                    if (id == otherId) {
                        otherText.setEnabled(true);
                    } else {
                        otherText.setEnabled(false);
                        otherText.getText().clear();
                    }
                    setResponseIndex(id);
                }
            }
        });
        getRadioGroup().addView(radioButton, otherId);
        addOtherResponseView(otherText);
        questionComponent.addView(otherText);
    }

    @Override
    protected void deserialize(String responseText) {
        super.deserialize(responseText);
        int checked = getRadioGroup().getCheckedRadioButtonId();
        if (checked == radioButton.getId()) {
            otherText.setFocusable(true);
        }
    }

    @Override
    protected void deserializeOtherResponse(String otherResponse) {
        otherText.setText(getResponse().getOtherResponse());
    }

}
