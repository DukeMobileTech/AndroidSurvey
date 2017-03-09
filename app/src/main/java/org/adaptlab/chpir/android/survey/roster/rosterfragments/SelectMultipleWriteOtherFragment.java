package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.questionfragments.SelectMultipleQuestionFragment;

/**
 * Created by Harry on 2/14/17.
 */
public class SelectMultipleWriteOtherFragment extends
        SelectMultipleFragment {

    @Override
    protected void beforeAddViewHook(ViewGroup responseComponent) {
        CheckBox checkbox = new CheckBox(getActivity());
        final EditText otherText = new EditText(getActivity());

        checkbox.setText(R.string.other_specify);
        checkbox.setTypeface(getQuestion().getInstrument().getTypeFace(getActivity().getApplicationContext()));
        final int otherId = getQuestion().defaultOptions().size();
        checkbox.setId(otherId);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    otherText.setEnabled(true);
                    otherText.requestFocus();
                    showKeyBoard();
                } else {
                    otherText.setEnabled(false);
                    hideKeyBoard();
                    otherText.getText().clear();
                }
                toggleResponseIndex(otherId);
            }
        });
        responseComponent.addView(checkbox, otherId);
        addOtherResponseView(otherText);
        addCheckBox(checkbox);
        responseComponent.addView(otherText);
    }
}