package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.text.InputType;
import android.widget.EditText;

/**
 * Created by Harry on 3/3/17.
 */
public class PhoneNumberFragment extends FreeResponseFragment{
    protected void beforeAddViewHook(EditText editText) {
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_PHONE);
    }
}

