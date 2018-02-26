package org.adaptlab.chpir.android.survey.questionfragments;

import android.widget.EditText;

public class ListOfTextBoxesQuestionFragment extends ListOfItemsQuestionFragment {

    @Override
    protected EditText createEditText() {
        return new EditText(getActivity());
    }
}
