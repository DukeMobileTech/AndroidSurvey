package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

public class TextBoxesViewHolder extends ListOfItemsViewHolder {

    TextBoxesViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected EditText createEditText() {
        return new EditText(getContext());
    }
}
