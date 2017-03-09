package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.text.InputType;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.questionfragments.FreeResponseQuestionFragment;

/**
 * Created by Harry on 2/23/17.
 */
public class DecimalNumberFragment extends FreeResponseFragment {

    protected void beforeAddViewHook(EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }
}
