package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

public class AddressViewHolder extends FreeResponseViewHolder {

    AddressViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    protected void beforeAddViewHook(EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }
}