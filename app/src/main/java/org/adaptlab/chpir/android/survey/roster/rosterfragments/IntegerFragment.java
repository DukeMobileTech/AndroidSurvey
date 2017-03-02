package org.adaptlab.chpir.android.survey.roster.rosterfragments;
import android.text.InputType;
import android.widget.EditText;

/**
 * Created by Harry on 2/23/17.
 */
public class IntegerFragment extends FreeResponseFragment {

    protected void beforeAddViewHook(EditText editText) {
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_NUMBER);
    }
}
