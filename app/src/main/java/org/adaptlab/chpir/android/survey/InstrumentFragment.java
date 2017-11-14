package org.adaptlab.chpir.android.survey;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Delete;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.NetworkNotificationUtils;
import org.adaptlab.chpir.android.survey.models.Image;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Roster;
import org.adaptlab.chpir.android.survey.models.Score;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.roster.RosterActivity;
import org.adaptlab.chpir.android.survey.rules.InstrumentLaunchRule;
import org.adaptlab.chpir.android.survey.rules.RuleBuilder;
import org.adaptlab.chpir.android.survey.rules.RuleCallback;
import org.adaptlab.chpir.android.survey.tasks.SendResponsesTask;
import org.adaptlab.chpir.android.survey.tasks.SetScoreUnitOrderingQuestionTask;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.adaptlab.chpir.android.survey.AppUtil.getProjectId;

public class InstrumentFragment extends ListFragment {
    public final static String TAG = "InstrumentFragment";
    public final static String EXTRA_AUTHORIZE_SURVEY =
            "org.adaptlab.chpir.android.survey.authorize_survey_bool";
    private SurveyAdapter mSurveyAdapter;
    private InstrumentAdapter mInstrumentAdapter;
    private RosterAdapter mRosterAdapter;
    private ScoreAdapter mScoreAdapter;
    private ListView mSurveyListView;
    private ListView mRosterListView;
    private LoaderManager.LoaderCallbacks mInstrumentCallbacks;
    private LoaderManager.LoaderCallbacks mSurveyCallbacks;
    private LoaderManager.LoaderCallbacks mRosterCallbacks;
    private LoaderManager.LoaderCallbacks mScoreCallbacks;
    private MultiChoiceModeListener choiceModeListener;
    private ProgressDialog mProgressDialog;
    private boolean mAuthorizeSurvey;

    private void setMultiChoiceModeListener() {
        choiceModeListener = new MultiChoiceModeListener() {
            List<Survey> surveys = new ArrayList<>();
            List<Roster> rosters = new ArrayList<>();

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                                  boolean checked) {
                if (getListView().getAdapter() == mSurveyAdapter) {
                    Survey survey = getSurveyAtPosition(getListView(), position);
                    if (checked) {
                        surveys.add(survey);
                        mSurveyAdapter.setNewSelection(position, true);
                    } else {
                        surveys.remove(survey);
                        mSurveyAdapter.setNewSelection(position, false);
                    }
                } else if (getListView().getAdapter() == mRosterAdapter) {
                    Roster roster = getRosterAtPosition(getListView(), position);
                    if (checked) {
                        rosters.add(roster);
                        mRosterAdapter.setNewSelection(position, true);
                    } else {
                        rosters.remove(roster);
                        mRosterAdapter.setNewSelection(position, false);
                    }
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                if (getListView().getAdapter() != mInstrumentAdapter) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.list_view_item_delete, menu);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete_item:
                        showDeleteWarning((CursorAdapter) getListView().getAdapter());
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            private void showDeleteWarning(final CursorAdapter adapter) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.delete_title)
                        .setMessage(R.string.delete_message)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (adapter == mSurveyAdapter) {
                                    deleteSurveys();
                                    setSurveysListViewAdapter();
                                } else if (adapter == mRosterAdapter) {
                                    deleteRosters();
                                    setRostersListViewAdapter();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .show();
            }

            private void deleteSurveys() {
                ActiveAndroid.beginTransaction();
                try {
                    deleteHelper(surveys);
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                }
            }

            private void deleteHelper(List<Survey> surveys) {
                for (Survey survey : surveys) {
                    if (survey.getUUID() != null) {
                        new Delete().from(Response.class).where("SurveyUUID = ?", survey.getUUID()).execute();
                    }
                    survey.delete();
                }
            }

            private void deleteRosters() {
                ActiveAndroid.beginTransaction();
                try {
                    for (Roster roster : rosters) {
                        deleteHelper(roster.surveys());
                        roster.delete();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (getListView().getAdapter() == mSurveyAdapter) {
                    mSurveyAdapter.clearSelection();
                    mSurveyAdapter.notifyDataSetChanged();
                } else if(getListView().getAdapter() == mRosterAdapter) {
                    mRosterAdapter.clearSelection();
                    mRosterAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.appInit(getActivity());
        setHasOptionsMenu(true);
        if (getActivity().getIntent() != null) {
            mAuthorizeSurvey = getActivity().getIntent().getBooleanExtra(EXTRA_AUTHORIZE_SURVEY, false);
        }
        createLoaderCallbacks();
        setMultiChoiceModeListener();
        requestNeededPermissions();
        mProgressDialog = new ProgressDialog(getActivity());
    }

    private void requestNeededPermissions() {

        if (AppUtil.getAdminSettingsInstance().getRecordSurveyLocation()) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission
                    .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission
                .CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, 2);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission
                .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }
    }

    /*
    * Used to manage cursor loaders across activity life-cycles
     */
    private void createLoaderCallbacks() {
        mInstrumentCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int arg0, Bundle cursor) {
                String selection = "ProjectID = ? AND Published = ? AND Deleted = ?";
                String[] selectionArgs = {getProjectId().toString(), "1", "0"};
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
                String selection = "ProjectID = ? AND RosterUUID IS null";
                String[] selectionArgs = {getProjectId().toString()};
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

        mRosterCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(
                        getActivity(),
                        ContentProvider.createUri(Roster.class, null),
                        null,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mRosterAdapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mRosterAdapter.swapCursor(null);
            }
        };

        mScoreCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(
                        getActivity(),
                        ContentProvider.createUri(Score.class, null),
                        null,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mScoreAdapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mScoreAdapter.swapCursor(null);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        createTabs();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_instrument, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (getResources().getBoolean(R.bool.default_hide_admin_button)) {
            menu.findItem(R.id.menu_item_settings).setEnabled(false).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent i = new Intent(getActivity(), AdminActivity.class);
                startActivity(i);
                return true;
            case R.id.menu_item_refresh:
                new RefreshInstrumentsTask().execute();
                new SendResponsesTask(getActivity()).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createTabs() {
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (showTabs()) {
            if (actionBar != null) { // TODO: 12/6/16 Use TabLayout
                ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(Tab tab, FragmentTransaction ft) {
                        if (tab.getText().equals(getActivity().getResources().getString(
                                R.string.surveys))) {
                            if (Survey.getAllProjectSurveys(getProjectId()).isEmpty()) {
                                setListAdapter(null);
                            } else {
                                setSurveysListViewAdapter();
                                mSurveyListView = getListView();
                                mSurveyListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                                mSurveyListView.setMultiChoiceModeListener
                                        (choiceModeListener);
                            }
                        } else if (tab.getText().equals(getActivity().getResources().getString(
                                R.string.rosters))) {
                            setRostersListViewAdapter();
                            mRosterListView = getListView();
                            mRosterListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                            mRosterListView.setMultiChoiceModeListener(choiceModeListener);
                        } else if (tab.getText().equals(getActivity().getResources().getString(
                                R.string.scores))) {
                            setScoresListViewAdapter();
                        } else {
                            setInstrumentsListViewAdapter();
                        }
                    }

                    @Override
                    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                    }

                    @Override
                    public void onTabReselected(Tab tab, FragmentTransaction ft) {
                    }
                };

                actionBar.removeAllTabs();
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                actionBar.addTab(actionBar.newTab().setText(getActivity().getResources().getString(
                        R.string.instruments)).setTabListener(tabListener));
                if (AppUtil.getAdminSettingsInstance().getShowSurveys()) {
                    actionBar.addTab(actionBar.newTab().setText(getActivity().getResources()
                            .getString(R.string.surveys)).setTabListener(tabListener));
                }
                if (AppUtil.getAdminSettingsInstance().getShowRosters()) {
                    actionBar.addTab(actionBar.newTab().setText(getActivity().getResources()
                            .getString(R.string.rosters)).setTabListener(tabListener));
                }
                if (AppUtil.getAdminSettingsInstance().getShowScores()) {
                    actionBar.addTab(actionBar.newTab().setText(getActivity().getResources()
                            .getString(R.string.scores)).setTabListener(tabListener));
                }
            }
        } else {
            actionBar.removeAllTabs();
            setInstrumentsListViewAdapter();
        }
    }

    private boolean showTabs() {
        return AppUtil.getAdminSettingsInstance().getShowSurveys() ||
                AppUtil.getAdminSettingsInstance().getShowRosters() ||
                AppUtil.getAdminSettingsInstance().getShowScores();
    }

    private void setSurveysListViewAdapter() {
        if (getProjectId() != Long.MAX_VALUE) {
            mSurveyAdapter = new SurveyAdapter(getActivity(), null, 0);
            setListAdapter(mSurveyAdapter);
            getActivity().getSupportLoaderManager().restartLoader(0, null, mSurveyCallbacks);
        }
    }

    private void setRostersListViewAdapter() {
        mRosterAdapter = new RosterAdapter(getActivity(), null, 0);
        setListAdapter(mRosterAdapter);
        getActivity().getSupportLoaderManager().restartLoader(0, null, mRosterCallbacks);
    }

    private void setInstrumentsListViewAdapter() {
        if (getProjectId() != Long.MAX_VALUE) {
            mInstrumentAdapter = new InstrumentAdapter(getActivity(), null, 0);
            setListAdapter(mInstrumentAdapter);
            getActivity().getSupportLoaderManager().restartLoader(0, null, mInstrumentCallbacks);
        }
    }

    private void setScoresListViewAdapter() {
        mScoreAdapter = new ScoreAdapter(getActivity(), null, 0);
        setListAdapter(mScoreAdapter);
        getActivity().getSupportLoaderManager().restartLoader(0, null, mScoreCallbacks);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (l.getAdapter() instanceof InstrumentAdapter) {
            Cursor cursor = ((InstrumentAdapter) l.getAdapter()).getCursor();
            cursor.moveToPosition(position);
            Long instrumentRemoteId = cursor.getLong(cursor.getColumnIndexOrThrow("RemoteId"));
            Instrument instrument = Instrument.findByRemoteId(instrumentRemoteId);
            if (instrument == null || instrument.getQuestionCount() == 0) return;
            new LoadInstrumentTask().execute(instrument);
        } else if (l.getAdapter() instanceof SurveyAdapter) {
            Survey survey = getSurveyAtPosition(l, position);
            if (survey == null) return;
            new LoadSurveyTask().execute(survey);
        } else if (l.getAdapter() instanceof RosterAdapter) {
            Roster roster = getRosterAtPosition(l, position);
            if (roster == null) return;
            Intent i = new Intent(getActivity(), RosterActivity.class);
            i.putExtra(RosterActivity.EXTRA_ROSTER_UUID, roster.getUUID());
            i.putExtra(RosterActivity.EXTRA_INSTRUMENT_ID, roster.getInstrument().getRemoteId());
            startActivity(i);
        } else if (l.getAdapter() instanceof ScoreAdapter) {
            Score score = getScoreAtPosition(l, position);
            if (score == null) return;
            Intent intent = new Intent(getActivity(), ScoreUnitActivity.class);
            intent.putExtra(ScoreUnitFragment.EXTRA_SCORE_ID, score.getId());
            startActivity(intent);
        }
    }

    private Survey getSurveyAtPosition(ListView l, int position) {
        Cursor cursor = ((SurveyAdapter) l.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        String surveyUUID = cursor.getString(cursor.getColumnIndexOrThrow("UUID"));
        return Survey.findByUUID(surveyUUID);
    }

    private Roster getRosterAtPosition(ListView l, int position) {
        Cursor cursor = ((RosterAdapter) l.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        String rosterUUID = cursor.getString(cursor.getColumnIndexOrThrow("UUID"));
        return Roster.findByUUID(rosterUUID);
    }

    private Score getScoreAtPosition(ListView l, int position) {
        Cursor cursor = ((ScoreAdapter) l.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        return Score.findByUUID(cursor.getString(cursor.getColumnIndexOrThrow("UUID")));
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
            TextView titleTextView = (TextView) view.findViewById(R.id.instrument_list_item_titleTextView);
            TextView questionCountTextView = (TextView) view.findViewById(R.id.instrument_list_item_questionCountTextView);
            TextView instrumentVersionTextView = (TextView) view.findViewById(R.id.instrument_list_item_instrumentVersionTextView);

            Long remoteId = cursor.getLong(cursor.getColumnIndexOrThrow("RemoteId"));
            Instrument instrument = Instrument.findByRemoteId(remoteId);
            int numQuestions = instrument.questions().size();

            titleTextView.setText(instrument.getTitle());
            titleTextView.setTypeface(instrument.getTypeFace(getActivity().getApplicationContext()));
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

        public void setNewSelection(int position, boolean value) {
            mSelectionViews.put(position, value);
            notifyDataSetChanged();
        }

        public void clearSelection() {
            mSelectionViews.clear();
            notifyDataSetChanged();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_survey, parent, false);
        }


        public boolean isPositionChecked(int position) {
            return mSelectionViews.get(position);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Activity activity = (Activity) view.getContext();
            if (activity != null) {
                if (mSelectionViews != null && isPositionChecked(cursor.getPosition())) {
                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.item_selected));
                } else {
                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                }

                TextView titleTextView = (TextView) view.findViewById(R.id.survey_list_item_titleTextView);

                TextView progressTextView = (TextView) view.findViewById(R.id.survey_list_item_progressTextView);
                TextView instrumentTitleTextView = (TextView) view.findViewById(R.id.survey_list_item_instrumentTextView);
                TextView lastUpdatedTextView = (TextView) view.findViewById(R.id.survey_list_item_lastUpdatedTextView);

                String surveyUUID = cursor.getString(cursor.getColumnIndexOrThrow("UUID"));
                Survey survey = Survey.findByUUID(surveyUUID);

                titleTextView.setText(survey.identifier(activity));
                titleTextView.setTypeface(survey.getInstrument().getTypeFace(activity.getApplicationContext()));
                progressTextView.setText(survey.responses().size() + " " + getString(R.string.of) + "" + " " + survey.getInstrument().questions().size());
                instrumentTitleTextView.setText(survey.getInstrument().getTitle());
                DateFormat df = DateFormat.getDateTimeInstance();
                lastUpdatedTextView.setText(df.format(survey.getLastUpdated()));

                if (survey.readyToSend()) progressTextView.setTextColor(Color.GREEN);
                else progressTextView.setTextColor(Color.RED);
            }
        }
    }

    private class RosterAdapter extends CursorAdapter {
        private SparseBooleanArray selectionViews = new SparseBooleanArray();

        public boolean isPositionChecked(int position) {
            return selectionViews.get(position);
        }

        public void setNewSelection(int position, boolean value) {
            selectionViews.put(position, value);
            notifyDataSetChanged();
        }

        public void clearSelection() {
            selectionViews.clear();
            notifyDataSetChanged();
        }

        public RosterAdapter(Context context, Cursor c, int flags) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_roster, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (selectionViews != null && isPositionChecked(cursor.getPosition())) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.item_selected));
            } else {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
            }

            TextView titleTextView = (TextView) view.findViewById(R.id
                    .roster_list_item_titleTextView);
            TextView surveyCountTextView = (TextView) view.findViewById(R.id
                    .roster_list_item_surveyCountTextView);

            String id = cursor.getString(cursor.getColumnIndexOrThrow("UUID"));
            Roster roster = Roster.findByUUID(id);
            int numSurveys = roster.rosterLogs().size();

            titleTextView.setText(roster.identifier(context));
            titleTextView.setTypeface(roster.getInstrument().getTypeFace(getActivity()
                    .getApplicationContext()));
            surveyCountTextView.setText(numSurveys + " " + FormatUtils.pluralize
                    (numSurveys, getString(R.string.survey), getString(R.string.surveys)) + " " +
                    getString(R.string.completed));
        }
    }

    private class ScoreAdapter extends CursorAdapter {
        public ScoreAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_score, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String uuid = cursor.getString(cursor.getColumnIndexOrThrow("UUID"));
            Score score = Score.findByUUID(uuid);
            if (score != null) {
                TextView surveyIdentifier = (TextView) view.findViewById(R.id.survey_identifier);
                surveyIdentifier.setText(String.format(Locale.getDefault(), "%s%s%s", score.getSurveyIdentifier(), " - ", score.getScoreScheme().getInstrument().getTitle()));

                TextView schemeTitle = (TextView) view.findViewById(R.id.scheme_name_text);
                schemeTitle.setText(String.valueOf(score.getScoreScheme().getTitle()));

                TextView totalRawScore = (TextView) view.findViewById(R.id.total_raw_score_value);
                totalRawScore.setText(String.valueOf(score.getRawScoreSum()));

                TextView totalWeightedScore = (TextView) view.findViewById(R.id.total_weighted_score_value);
                totalWeightedScore.setText(String.valueOf(score.getWeightedScoreSum()));
            }
        }
    }

    /*
     * Refresh the receive tables from the server
     */
    private class RefreshInstrumentsTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            if (isAdded() && NetworkNotificationUtils.checkForNetworkErrors(getActivity())) {
                ActiveRecordCloudSync.syncReceiveTables(getActivity());
                return 0;
            } else {
                return -1;
            }
        }

        @Override
        protected void onPreExecute() {
            if (mProgressDialog != null) {
                mProgressDialog.setTitle(getResources().getString(
                        R.string.instrument_loading_progress_header));
                mProgressDialog.setMessage(getResources().getString(
                        R.string.background_process_progress_message));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        protected void onPostExecute(Integer code) {
            if (isAdded()) {
                new RefreshImagesTask().execute();
                new SetScoreUnitOrderingQuestionTask().execute();
            }
            if (code == -1 && mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            SingleFragmentActivity activity = (SingleFragmentActivity) getActivity();
            if (activity != null && !activity.isDestroyed()) {
                activity.displayProjectName();
            }
        }
    }

    private class InstrumentSanitizerTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            ((Instrument) params[0]).sanitize();
            return ((Boolean) params[1]);
        }

        @Override
        protected void onPostExecute(Boolean last) {
            if (isAdded() && last) {
                if (AppUtil.getAdminSettingsInstance().getProjectId() != null) {
                    setInstrumentsListViewAdapter();
                }
            }
            AppUtil.getAdminSettingsInstance().setLastSyncTime(ActiveRecordCloudSync.getLastSyncTime());
            AppUtil.orderInstrumentsSections();
            if (isAdded() && !getActivity().isFinishing() && mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    /*
    * Refresh the images table from the server
     */
    private class RefreshImagesTask extends AsyncTask<Void, Void, Void> {
        private final static String TAG = "ImageDownloader";

        @Override
        protected Void doInBackground(Void... arg0) {
            if (isAdded() && NetworkNotificationUtils.checkForNetworkErrors(getActivity())) {
                downloadImages();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            List<Instrument> instruments = Instrument.getAllProjectInstruments(getProjectId());
            for (int k = 0; k < instruments.size(); k++) {
                new InstrumentSanitizerTask().execute(instruments.get(k), (k == instruments.size() - 1));
            }
        }

        private void downloadImages() {
            ActiveRecordCloudSync.setAccessToken(AppUtil.getAdminSettingsInstance().getApiKey());
            ActiveRecordCloudSync.setVersionCode(AppUtil.getVersionCode(getActivity()));

            for (Image img : Image.getAll()) {
                String[] imageUrl = img.getPhotoUrl().split("/");
                String url = ActiveRecordCloudSync.getEndPoint() + "images/" + imageUrl[2] + "/"
                        + ActiveRecordCloudSync.getParams();
                if (BuildConfig.DEBUG) Log.i(TAG, "Image url: " + url);
                String filename = UUID.randomUUID().toString() + ".jpg";
                FileOutputStream fileWriter = null;
                try {
                    byte[] imageBytes = getUrlBytes(url);
                    if (imageBytes != null) {
                        fileWriter = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                        fileWriter.write(imageBytes);
                        img.setBitmapPath(filename);
                        img.save();
                    }
                    if (BuildConfig.DEBUG) Log.i(TAG, "image saved in " + filename);
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "IOException ", e);
                } finally {
                    try {
                        if (fileWriter != null) {
                            fileWriter.close();
                        }
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) Log.e(TAG, "Exception ", e);
                    }
                }
            }
        }

        private byte[] getUrlBytes(String urlSpec) throws IOException {
            URL url = new URL(urlSpec);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
                InputStream in = connection.getInputStream();
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                return out.toByteArray();
            } finally {
                connection.disconnect();
            }
        }

    }

    /*
     * Check that the instrument has been fully loaded from the server before allowing
     * user to begin survey.
     */
    private class LoadInstrumentTask extends AsyncTask<Instrument, Void, Instrument> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(
                    getActivity(),
                    getString(R.string.instrument_loading_progress_header),
                    getString(R.string.background_process_progress_message)
            );
        }

        /*
         * If instrument is loaded, return it.
         * If not, return null.
         */
        @Override
        protected Instrument doInBackground(Instrument... params) {
            if (params[0].loaded()) {
                return params[0];
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Instrument instrument) {
            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
            if (isAdded()) {
                if (instrument == null) {
                    Toast.makeText(getActivity(), R.string.instrument_not_loaded, Toast.LENGTH_LONG).show();
                } else {
                    new RuleBuilder(getActivity())
                            .addRule(new InstrumentLaunchRule(instrument,
                                    getActivity().getString(R.string
                                            .rule_failure_instrument_launch)))
                            .showToastOnFailure(true)
                            .setCallbacks(new RuleCallback() {
                                public void onRulesPass() {
                                    Intent i = new Intent(getActivity(), SurveyActivity.class);
                                    i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID, instrument.getRemoteId());
                                    startActivity(i);
                                }

                                public void onRulesFail() {}
                            })
                            .checkRules();
                }
            }
        }
    }

    private class LoadSurveyTask extends AsyncTask<Survey, Void, Survey> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(
                    getActivity(),
                    getString(R.string.instrument_loading_progress_header),
                    getString(R.string.background_process_progress_message)
            );
        }

        /*
         * If instrument is loaded, return the survey.
         * If not, return null.
         */
        @Override
        protected Survey doInBackground(Survey... params) {
            if (params[0].getInstrument().loaded()) {
                return params[0];
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Survey survey) {
            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
            if (isAdded()) {
                if (survey == null) {
                    Toast.makeText(getActivity(), R.string.instrument_not_loaded, Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(getActivity(), SurveyActivity.class);
                    i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID, survey.getInstrument().getRemoteId());
                    i.putExtra(SurveyFragment.EXTRA_SURVEY_ID, survey.getId());
                    i.putExtra(SurveyFragment.EXTRA_QUESTION_NUMBER, survey.getLastQuestion().getNumberInInstrument() - 1);
                    i.putExtra(SurveyFragment.EXTRA_AUTHORIZE_SURVEY, mAuthorizeSurvey);
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
    private class SetInstrumentLabelTask extends AsyncTask<InstrumentListLabel, Void,
            InstrumentListLabel> {

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
}