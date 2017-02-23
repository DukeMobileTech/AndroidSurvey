package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.text.InputType;
import android.widget.EditText;

/**
 * Created by Harry on 2/23/17.
 */
public class EmailAddressFragment extends FreeResponseFragment {
    protected void beforeAddViewHook(EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }
}
