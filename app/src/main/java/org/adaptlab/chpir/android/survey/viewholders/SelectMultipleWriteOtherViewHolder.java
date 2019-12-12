package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.R;

public class SelectMultipleWriteOtherViewHolder extends SelectMultipleViewHolder {
    private EditText otherText = null;

    SelectMultipleWriteOtherViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void beforeAddViewHook(ViewGroup questionComponent) {
        CheckBox checkbox = new CheckBox(getContext());
        otherText = new EditText(getContext());
        checkbox.setText(R.string.other_specify);
        final int otherId = getOptionRelations().size();
        checkbox.setId(otherId);
        setOptionTextColor(checkbox);
        toggleCarryForward(checkbox, otherId);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    otherText.setEnabled(true);
                } else {
                    otherText.setEnabled(false);
                    otherText.getText().clear();
                }
            }
        });
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mSpecialResponses != null) {
//                    mSpecialResponses.clearCheck();
//                }
//                checkOptionExclusivity(v);
//                toggleResponseIndex(otherId);
            }
        });
        questionComponent.addView(checkbox, otherId);
        addOtherResponseView(otherText);
        addCheckBox(checkbox);
        questionComponent.addView(otherText);
    }

//    @Override
//    protected void unSetResponse() {
//        if (otherText.getText().length() > 0) {
//            otherText.setText(Response.BLANK);
//            otherText.setEnabled(false);
//        }
//        super.unSetResponse();
//    }

}