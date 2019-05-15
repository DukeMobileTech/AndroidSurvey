package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

public class IntegerBoxesViewHolder extends ListOfItemsViewHolder {

    IntegerBoxesViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    protected EditText createEditText() {
        EditText editText = new EditText(getContext());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        return editText;
    }
}
