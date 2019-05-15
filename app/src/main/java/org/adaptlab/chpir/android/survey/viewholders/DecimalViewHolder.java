package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

public class DecimalViewHolder extends FreeResponseViewHolder {

    DecimalViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    protected void beforeAddViewHook(EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_NUMBER_FLAG_SIGNED);
    }
}
