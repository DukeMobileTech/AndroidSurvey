package org.adaptlab.chpir.android.survey;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.content.ContentProvider;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.NetworkNotificationUtils;
import org.adaptlab.chpir.android.survey.Models.AdminSettings;
import org.adaptlab.chpir.android.survey.Models.Instrument;
import org.adaptlab.chpir.android.survey.Models.Response;
import org.adaptlab.chpir.android.survey.Models.Survey;
import org.adaptlab.chpir.android.survey.Rules.InstrumentLaunchRule;
import org.adaptlab.chpir.android.survey.Rules.RuleBuilder;
import org.adaptlab.chpir.android.survey.Rules.RuleCallback;
import org.adaptlab.chpir.android.survey.Tasks.DownloadImagesTask;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class InstrumentFragment extends ListFragment {
    public final static String TAG = "InstrumentFragment";
    private SurveyAdapter mSurveyAdapter;
    private InstrumentAdapter mInstrumentAdapter;
    private ListView mSurveyListView;
    private LoaderManager.LoaderCallbacks mInstrumentCallbacks;
    private LoaderManager.LoaderCallbacks mSurveyCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.appInit(getActivity());
        setHasOptionsMenu(true);
        createLoaderCallbacks();
    }

    /*
    * Used to manage cursor loaders across activity life-cycles
     */
    private void createLoaderCallbacks() {
        mInstrumentCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int arg0, Bundle cursor) {
                String selection = "ProjectID = ? AND Published = ? AND Deleted = ?";
                String[] selectionArgs = { getProjectId().toString(), "1", "0" };
                String orderBy = "Title";
                return new CursorLoader(
                        getActivity(),
                        ContentProvider.createUri(Instrument.class, null),
                        null,
                        selection,
                        selectionArgs,
                        orderBy
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
                mInstrumentAdapter.swapCursor(cursor);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {
                mInstrumentAdapter.swapCursor(null);
            }
        };

        mSurveyCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int arg0, Bundle cursor) {
                String selection = "ProjectID = ?";
                String[] selectionArgs = { getProjectId().toString() };
                String orderBy = "LastUpdated DESC";
                return new CursorLoader(
                        getActivity(),
                        ContentProvider.createUri(Survey.class, null),
                        null,
                        selection,
                        selectionArgs,
                        orderBy
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
                mSurveyAdapter.swapCursor(cursor);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {
                mSurveyAdapter.swapCursor(null);
            }
        };
    }

    private void setInstrumentsListViewAdapter() {
        if (getProjectId() != Long.MAX_VALUE) {
            Cursor instrumentsCursor = Instrument.getProjectInstrumentsCursor(getProjectId());
            mInstrumentAdapter = new InstrumentAdapter(getActivity(), instrumentsCursor, 0);
            setListAdapter(mInstrumentAdapter);
            getActivity().getSupportLoaderManager().restartLoader(0, null, mInstrumentCallbacks);
        }
    }

    private void downloadInstrumentImages() {
        new DownloadImagesTask(getActivity()).execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_instrument, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (getResources().getBoolean(R.bool.default_admin_settings)) {
            menu.findItem(R.id.menu_item_admin).setEnabled(false);
            menu.findItem(R.id.menu_item_admin).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_admin:
                displayPasswordPrompt();
                return true;
            case R.id.menu_item_refresh:
                new RefreshInstrumentsTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        createTabs();
    }

    public void createTabs() {
        if (AppUtil.getAdminSettingsInstance().getShowSurveys()) {
            final ActionBar actionBar = getActivity().getActionBar();
            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                @Override
                public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
                    if (tab.getText().equals(getActivity().getResources().getString(R.string.surveys))) {
                        if (Survey.getAllProjectSurveys(getProjectId()).isEmpty()) {
                            setListAdapter(null);
                        } else {
                            setSurveysListViewAdapter();
                            mSurveyListView = getListView();
                            mSurveyListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                            mSurveyListView.setMultiChoiceModeListener(mSurveyMultiChoiceModeListener);
                        }
                    } else {
                        setInstrumentsListViewAdapter();
                    }
                }

                // Required by interface
                public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
                }

                public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
                }
            };

            actionBar.removeAllTabs();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.addTab(actionBar.newTab().setText(getActivity().getResources().getString(R.string.instruments)).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText(getActivity().getResources().getString(R.string.surveys)).setTabListener(tabListener));
        } else {
            setInstrumentsListViewAdapter();
        }
    }

    private void setSurveysListViewAdapter() {
        if (getProjectId() != Long.MAX_VALUE) {
            Cursor surveysCursor = Survey.getProjectSurveysCursor(getProjectId());
            mSurveyAdapter = new SurveyAdapter(getActivity(), surveysCursor, 0);
            setListAdapter(mSurveyAdapter);
            getActivity().getSupportLoaderManager().restartLoader(0, null, mSurveyCallbacks);
        }
    }

    private Long getProjectId() {
        AdminSettings adminSettings = AppUtil.getAdminSettingsInstance();
        if (adminSettings.getProjectId() != null)
            return Long.parseLong(adminSettings.getProjectId());
        return Long.MAX_VALUE;
    }

    private MultiChoiceModeListener mSurveyMultiChoiceModeListener = new MultiChoiceModeListener() {
        List<Survey> selected = new ArrayList<Survey>();

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            Survey survey = getSurveyAtPosition(getListView(), position);
            if (checked) {
                selected.add(survey);
                mSurveyAdapter.setNewSelection(position, true);
            } else {
                selected.remove(survey);
                mSurveyAdapter.setNewSelection(position, false);
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete_item:
                    showDeleteSurveysWarning();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        private void showDeleteSurveysWarning() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.delete_surveys_title)
                    .setMessage(R.string.delete_surveys_message)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteSurveys();
                            setSurveysListViewAdapter();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .show();
        }

        private void deleteSurveys() {
            for (Survey survey : selected) {
                for (Response response : survey.responses()) {
                    response.delete();
                }
                survey.delete();
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.list_view_item_delete, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mSurveyAdapter.clearSelection();
            mSurveyAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }
    };

    private class InstrumentAdapter extends CursorAdapter {

        public InstrumentAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_instrument, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor.getPosition() % 2 == 0) {
                view.setBackgroundResource(R.drawable.list_background_color);
            } else {
                view.setBackgroundResource(R.drawable.list_background_color_alternate);
            }

            TextView titleTextView = (TextView) view.findViewById(R.id.instrument_list_item_titleTextView);
            TextView questionCountTextView = (TextView) view.findViewById(R.id.instrument_list_item_questionCountTextView);
            TextView instrumentVersionTextView = (TextView) view.findViewById(R.id.instrument_list_item_instrumentVersionTextView);

            String title = cursor.getString(cursor.getColumnIndexOrThrow("Title"));
            Long remoteId = cursor.getLong(cursor.getColumnIndexOrThrow("RemoteId"));
            Instrument instrument = Instrument.findByRemoteId(remoteId);
            int numQuestions = instrument.questions().size();

            titleTextView.setText(title);
            titleTextView.setTypeface(instrument.getTypeFace(getActivity().getApplicationContext()));
            titleTextView.setTextColor(Color.BLACK);
            questionCountTextView.setText(numQuestions + " " + FormatUtils.pluralize(numQuestions, getString(R.string.question), getString(R.string.questions)));
            instrumentVersionTextView.setText(getString(R.string.version) + ": " + instrument.getVersionNumber());

            new SetInstrumentLabelTask().execute(new InstrumentListLabel(instrument, titleTextView));
        }
    }

    private class SurveyAdapter extends CursorAdapter {
        private SparseBooleanArray mSelectionViews = new SparseBooleanArray();

        public SurveyAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_survey, parent, false);
        }

        public void setNewSelection(int position, boolean value) {
            mSelectionViews.put(position, value);
            notifyDataSetChanged();
        }

        public boolean isPositionChecked(int position) {
            Boolean result = mSelectionViews.get(position);
            return result == null ? false : result;
        }

        public void clearSelection() {
            mSelectionViews.clear();
            notifyDataSetChanged();
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor.getPosition() % 2 == 0) {
                view.setBackgroundResource(R.drawable.list_background_color);
            } else {
                view.setBackgroundResource(R.drawable.list_background_color_alternate);
            }

            if (mSelectionViews != null && mSurveyAdapter.isPositionChecked(cursor.getPosition())) {
                view.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            }

            TextView titleTextView = (TextView) view.findViewById(R.id.survey_list_item_titleTextView);
            TextView progressTextView = (TextView) view.findViewById(R.id.survey_list_item_progressTextView);
            TextView instrumentTitleTextView = (TextView) view.findViewById(R.id.survey_list_item_instrumentTextView);
            TextView lastUpdatedTextView = (TextView) view.findViewById(R.id.survey_list_item_lastUpdatedTextView);

            String surveyUUID = cursor.getString(cursor.getColumnIndexOrThrow("UUID"));
            Survey survey = Survey.findByUUID(surveyUUID);

            titleTextView.setText(survey.identifier(getActivity()));
            titleTextView.setTypeface(survey.getInstrument().getTypeFace(getActivity().getApplicationContext()));
            progressTextView.setText(survey.responses().size() + " " + getString(R.string.of) + " " + survey.getInstrument().questions().size());
            instrumentTitleTextView.setText(survey.getInstrument().getTitle());
            DateFormat df = DateFormat.getDateTimeInstance();
            lastUpdatedTextView.setText(df.format(survey.getLastUpdated()));

            if (survey.readyToSend()) progressTextView.setTextColor(Color.GREEN);
            else progressTextView.setTextColor(Color.RED);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (l.getAdapter() instanceof InstrumentAdapter) {
            Cursor cursor = ((InstrumentAdapter) l.getAdapter()).getCursor();
            cursor.moveToPosition(position);
            Long instrumentRemoteId = cursor.getLong(cursor.getColumnIndexOrThrow("RemoteId"));
            Instrument instrument = Instrument.findByRemoteId(instrumentRemoteId);
            if (instrument == null) return;
            new LoadInstrumentTask().execute(instrument);
        } else if (l.getAdapter() instanceof SurveyAdapter) {
            Survey survey = getSurveyAtPosition(l, position);
            if (survey == null) return;
            new LoadSurveyTask().execute(survey);
        }
    }

    private Survey getSurveyAtPosition(ListView l, int position) {
        Cursor cursor = ((SurveyAdapter) l.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        String surveyUUID = cursor.getString(cursor.getColumnIndexOrThrow("UUID"));
        return Survey.findByUUID(surveyUUID);
    }


    /*
     * Only display admin area if correct password.
     */
    private void displayPasswordPrompt() {
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.password_title)
                .setMessage(R.string.password_message)
                .setView(input)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button) {
                        if (AppUtil.checkAdminPassword(input.getText().toString())) {
                            Intent i = new Intent(getActivity(), AdminActivity.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(getActivity(), R.string.incorrect_password, Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
            }
        }).show();
    }

    /*
     * Refresh the receive tables from the server
     */
    private class RefreshInstrumentsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
            setListAdapter(null);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (isAdded() && NetworkNotificationUtils.checkForNetworkErrors(getActivity())) {
                ActiveRecordCloudSync.syncSendTables(getActivity());
                ActiveRecordCloudSync.syncReceiveTables(getActivity());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (isAdded()) {
                downloadInstrumentImages();
                if (AppUtil.getAdminSettingsInstance().getProjectId() != null) {
                    setInstrumentsListViewAdapter();
                }
                getActivity().setProgressBarIndeterminateVisibility(false);
            }
        }
    }

    /*
     * Check that the instrument has been fully loaded from the server before allowing
     * user to begin survey.
     */
    private class LoadInstrumentTask extends AsyncTask<Instrument, Void, Long> {
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(
                    getActivity(),
                    getString(R.string.instrument_loading_progress_header),
                    getString(R.string.instrument_loading_progress_message)
            );
        }

        /*
         * If instrument is loaded, return the instrument id.
         * If not, return -1.
         */
        @Override
        protected Long doInBackground(Instrument... params) {
            Instrument instrument = params[0];
            if (instrument.loaded()) {
                return instrument.getRemoteId();
            } else {
                return Long.valueOf(-1);
            }
        }

        @Override
        protected void onPostExecute(final Long instrumentId) {
            try {
                mProgressDialog.dismiss();
            } catch (IllegalArgumentException iae) {
                Log.e(TAG, "Tried to close progress dialog that does not exist.");
            }
            if (isAdded()) {
                if (instrumentId == Long.valueOf(-1)) {
                    Toast.makeText(getActivity(), R.string.instrument_not_loaded, Toast.LENGTH_LONG).show();
                } else {
                    new RuleBuilder(getActivity())
                            .addRule(new InstrumentLaunchRule(Instrument.findByRemoteId(instrumentId),
                                    getActivity().getString(R.string.rule_failure_instrument_launch)))
                            .showToastOnFailure(true)
                            .setCallbacks(new RuleCallback() {
                                public void onRulesPass() {
                                    Intent i = new Intent(getActivity(), SurveyActivity.class);
                                    i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID, instrumentId);
                                    startActivity(i);
                                }

                                public void onRulesFail() {
                                }
                            })
                            .checkRules();
                }
            }
        }
    }

    private class LoadSurveyTask extends AsyncTask<Survey, Void, Survey> {
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(
                    getActivity(),
                    getString(R.string.instrument_loading_progress_header),
                    getString(R.string.instrument_loading_progress_message)
            );
        }

        /*
         * If instrument is loaded, return the survey.
         * If not, return null.
         */
        @Override
        protected Survey doInBackground(Survey... params) {
            Survey survey = params[0];
            Instrument instrument = survey.getInstrument();
            if (instrument.loaded()) {
                return survey;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Survey survey) {
            try {
                mProgressDialog.dismiss();
            } catch (IllegalArgumentException iae) {
                Log.e(TAG, "Tried to close progress dialog that does not exist.");
            }

            if (isAdded()) {
                if (survey == null) {
                    Toast.makeText(getActivity(), R.string.instrument_not_loaded, Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(getActivity(), SurveyActivity.class);
                    i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID, survey.getInstrument().getRemoteId());
                    i.putExtra(SurveyFragment.EXTRA_SURVEY_ID, survey.getId());
                    i.putExtra(SurveyFragment.EXTRA_QUESTION_ID, survey.getLastQuestion().getId());
                    startActivity(i);
                }
            }
        }
    }

    /*
     * Check that the instrument has been fully loaded from the server and sets
     * the color of instrument label red if it has not.
     *
     */
    private class SetInstrumentLabelTask extends AsyncTask<InstrumentListLabel, Void, InstrumentListLabel> {

        @Override
        protected InstrumentListLabel doInBackground(InstrumentListLabel... params) {
            InstrumentListLabel instrumentListLabel = params[0];
            Instrument instrument = instrumentListLabel.getInstrument();
            instrumentListLabel.setLoaded(instrument.loaded());
            return instrumentListLabel;
        }

        @Override
        protected void onPostExecute(InstrumentListLabel instrumentListLabel) {
            if (isAdded()) {
                if (instrumentListLabel.isLoaded()) {
                    instrumentListLabel.getTextView().setTextColor(Color.BLACK);
                } else {
                    instrumentListLabel.getTextView().setTextColor(Color.RED);
                }
            }
        }
    }

    private static class InstrumentListLabel {
        private Instrument mInstrument;
        private TextView mTextView;
        private Boolean mLoaded;

        public InstrumentListLabel(Instrument instrument, TextView textView) {
            this.mInstrument = instrument;
            this.mTextView = textView;
        }

        public Instrument getInstrument() {
            return mInstrument;
        }

        public TextView getTextView() {
            return mTextView;
        }

        public void setLoaded(boolean loaded) {
            mLoaded = loaded;
        }

        public Boolean isLoaded() {
            return mLoaded;
        }
    }
}