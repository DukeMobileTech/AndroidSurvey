package org.adaptlab.chpir.android.survey.questionfragments;

import android.text.TextWatcher;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.models.Response;

public class ListOfTextBoxesQuestionFragment extends ListOfItemsQuestionFragment {
    
    @Override
    protected EditText createEditText() {
        return new EditText(getActivity());
    }

    @Override
    protected void unSetResponse() {
        for (EditText editText : getResponseEditTexts()) {
            editText.setText(Response.BLANK);
            int index = getResponseEditTexts().indexOf(editText);
            TextWatcher textWatcher = getTextWatchers().get(index);
            editText.removeTextChangedListener(textWatcher);
//            getInitialStates().set(index, false);
        }
    }
}
