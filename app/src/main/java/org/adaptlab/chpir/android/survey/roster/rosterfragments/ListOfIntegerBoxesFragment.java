package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.questionfragments.ListOfItemsQuestionFragment;

import java.util.List;

/**
 * Created by Harry on 2/23/17.
 */
public class ListOfIntegerBoxesFragment extends ListOfItemsFragment {

    @Override
    protected EditText createEditText() {
        EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        return editText;
    }
}