package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.view.ViewGroup;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.roster.rosterfragments.ListOfItemsFragment;

public class ListOfTextBoxesFragment extends ListOfItemsFragment {

    @Override
    protected EditText createEditText() {
        return new EditText(getActivity());
    }
}
