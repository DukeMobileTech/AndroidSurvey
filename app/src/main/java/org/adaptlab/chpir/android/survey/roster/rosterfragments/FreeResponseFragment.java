package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;

public class FreeResponseFragment extends RosterFragment {
    @Override
    protected void createResponseComponent(ViewGroup responseComponent) {
        EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        beforeAddViewHook(editText);
        editText.setMinimumWidth(MINIMUM_WIDTH);
        editText.setText(getResponse().getText());
        editText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getResponse().setResponse(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        editText.requestFocus();
        showKeyBoard();
        responseComponent.addView(editText);
    }

    // This is used to restrict allowed input in subclasses.
    protected void beforeAddViewHook(EditText editText) {
    }
}