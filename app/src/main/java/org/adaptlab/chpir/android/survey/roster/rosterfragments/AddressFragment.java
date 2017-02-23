package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.text.InputType;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.questionfragments.FreeResponseQuestionFragment;

/**
 * Created by Harry on 2/23/17.
 */
public class AddressFragment extends FreeResponseFragment {

    protected void beforeAddViewHook(EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }
}