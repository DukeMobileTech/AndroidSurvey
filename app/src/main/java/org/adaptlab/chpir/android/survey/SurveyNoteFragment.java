package org.adaptlab.chpir.android.survey;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.adaptlab.chpir.android.survey.entities.SurveyNote;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyNoteViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyNoteViewModel;

import java.util.ArrayList;
import java.util.List;

public class SurveyNoteFragment extends ListFragment {
    final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";
    private String mSurveyUUID;
    private List<SurveyNote> mSurveyNotes;
    private SurveyNoteViewModel mSurveyNoteViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Intent intent = getActivity().getIntent();
        if (intent.getExtras() == null) return;
        mSurveyUUID = intent.getExtras().getString(EXTRA_SURVEY_UUID);
        getActivity().setTitle("Survey Notes");
        setSurveyNoteRelationViewModel();
    }

    private void setSurveyNoteRelationViewModel() {
        SurveyNoteViewModelFactory factory = new SurveyNoteViewModelFactory(getActivity().getApplication(), mSurveyUUID);
        mSurveyNoteViewModel = ViewModelProviders.of(this, factory).get(SurveyNoteViewModel.class);
        mSurveyNoteViewModel.getSurveyNotes().observe(this, new Observer<List<SurveyNote>>() {
            @Override
            public void onChanged(List<SurveyNote> surveyNotes) {
                mSurveyNotes = surveyNotes;
                setListAdapter(new SurveyNoteAdapter((ArrayList<SurveyNote>) mSurveyNotes));
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_note, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.addNoteButton).setEnabled(true).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addNoteButton) {
            showSurveyNoteDialog(null);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showSurveyNoteDialog(final SurveyNote surveyNote) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setTitle(R.string.add_note)
                    .setView(R.layout.fragment_survey_note)
                    .setPositiveButton(R.string.save_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            final AlertDialog dialog = builder.create();
            dialog.show();

            final EditText referenceEditText = dialog.findViewById(R.id.referenceEditText);
            final EditText textEditText = dialog.findViewById(R.id.textEditText);
            if (surveyNote != null) {
                referenceEditText.setText(surveyNote.getReference());
                textEditText.setText(surveyNote.getText());
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String reference = referenceEditText.getText().toString();
                    String text = textEditText.getText().toString();

                    if (TextUtils.isEmpty(reference)) {
                        referenceEditText.setError("Should not be empty");
                    }
                    if (TextUtils.isEmpty(text)) {
                        textEditText.setError("Should not be empty");
                    }
                    if (!TextUtils.isEmpty(reference) && !TextUtils.isEmpty(text)) {
                        if (surveyNote == null) {
                            SurveyNote surveyNote = new SurveyNote(mSurveyUUID);
                            surveyNote.setReference(reference);
                            surveyNote.setText(text);
                            mSurveyNoteViewModel.insert(surveyNote);
                        } else {
                            surveyNote.setReference(reference);
                            surveyNote.setText(text);
                            mSurveyNoteViewModel.update(surveyNote);
                        }
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        SurveyNote surveyNote = ((SurveyNoteAdapter) getListAdapter()).getItem(position);
        if (surveyNote != null) showSurveyNoteDialog(surveyNote);
    }

    private class SurveyNoteAdapter extends ArrayAdapter<SurveyNote> {

        SurveyNoteAdapter(ArrayList<SurveyNote> surveyNotes) {
            super(getActivity(), 0, surveyNotes);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_survey_note, null);
            }

            SurveyNote surveyNote = getItem(position);
            if (surveyNote != null) {
                TextView referenceTextView = convertView.findViewById(R.id.surveyNoteReference);
                referenceTextView.setText(surveyNote.getReference());
                referenceTextView.setTextColor(Color.BLACK);

                TextView surveyNoteTextView = convertView.findViewById(R.id.surveyNoteText);
                surveyNoteTextView.setText(surveyNote.getText());
            }
            return convertView;
        }

    }

}
