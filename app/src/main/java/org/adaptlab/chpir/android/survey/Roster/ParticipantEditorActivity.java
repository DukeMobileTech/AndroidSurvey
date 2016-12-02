package org.adaptlab.chpir.android.survey.Roster;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import org.adaptlab.chpir.android.survey.Models.Question;
import org.adaptlab.chpir.android.survey.Models.Response;
import org.adaptlab.chpir.android.survey.Models.Roster;
import org.adaptlab.chpir.android.survey.Models.Survey;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.Roster.RosterFragments.RosterFragment;
import org.adaptlab.chpir.android.survey.Roster.RosterFragments.RosterFragmentGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ParticipantEditorActivity extends FragmentActivity {
    public final static String EXTRA_QUESTION_ID =
            "org.adaptlab.chpir.android.survey.question_id";
    public final static String EXTRA_SURVEY_ID =
            "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_RESPONSE_ID =
            "org.adaptlab.chpir.android.survey.response_id";
    private static final String TAG = "ParticipantEditorActivity";
    private DrawerLayout mDrawer;
    private NavigationView navigationView;
    private int currentMenuItem = 0;
//    private Participant mParticipant;
    private List<Question> mQuestions;
    private RosterFragment mRosterFragment;
    private Roster mRoster;
    private Survey mSurvey;
    private int mQuestionCount;
    private LinkedHashMap<Question, Response> mQuestionResponseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_participant_editor);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeButtonEnabled(true);
//        }
        setTitle(getString(R.string.new_participant));

        mDrawer = (DrawerLayout) findViewById(R.id.roster_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.roster_drawer_view);

        Long rosterId = getIntent().getLongExtra(RosterActivity.EXTRA_ROSTER_ID, -1);
        if (rosterId != -1) {
            mRoster = Roster.load(Roster.class, rosterId);
        }
        Long surveyId = getIntent().getLongExtra(ParticipantViewerActivity.EXTRA_SURVEY_ID, -1);
        loadOrCreateSurvey(surveyId);
        mQuestions = new ArrayList<>();
        new QuestionLoaderTask().execute(mSurvey);

//        String participantId = getIntent().getStringExtra(ParticipantViewerActivity
//                .EXTRA_PARTICIPANT_ID);
//        if (participantId == null && rosterId != -1) {
//            Log.i(TAG, "Number of questions: " + Question.findAll().size());
//            new NewParticipantTask().execute(centerId);
//        } else {
//            mParticipant = Participant.findByIdentifier(participantId);
//        }

//        mQuestions = Question.findAll();

//        mDrawer = (DrawerLayout) findViewById(R.id.roster_drawer_layout);
//        navigationView = (NavigationView) findViewById(R.id.roster_drawer_view);
//        setNavigationViewMenu();
//        setNavigationViewListener(navigationView);
    }

    private void loadOrCreateSurvey(Long surveyId) {
        if (surveyId == -1) {
            mSurvey = new Survey();
            mSurvey.setInstrumentRemoteId(mRoster.getInstrument().getRemoteId());
            mSurvey.setProjectId(mRoster.getInstrument().getProjectId());
            mSurvey.setRosterUUID(mRoster.getUUID());
            mSurvey.save();
        } else {
            mSurvey = Survey.load(Survey.class, -1);
        }
        if (mRoster == null) {
            mRoster = mSurvey.getRoster();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void setNavigationViewMenu() {
        Menu menu = navigationView.getMenu();
        int index = 0;
        for (Question question : mQuestions) {
            menu.add(R.id.drawer_menu_group, index, Menu.NONE, stripHtml(question.getText()))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            index++;
        }
        menu.getItem(currentMenuItem).setChecked(true);
    }

    private String stripHtml(String withHtml) {
        return Html.fromHtml(withHtml).toString().trim();
    }


    private void setNavigationViewListener(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mDrawer.closeDrawers();
                        updateFragment(menuItem.getItemId());
                        return true;
                    }
                });
    }

    private void updateFragment(int position) {
        if (mSurvey != null) {
            currentMenuItem = position;
            mRosterFragment = RosterFragmentGenerator.createQuestionFragment(
                    mQuestions.get(position).getQuestionType());
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_QUESTION_ID, mQuestions.get(position).getQuestionIdentifier());
            bundle.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
            Response response = mQuestionResponseList.get(mQuestions.get(position));
            if (response == null) {
                bundle.putLong(EXTRA_RESPONSE_ID, Long.parseLong(null));
            } else {
                bundle.putLong(EXTRA_RESPONSE_ID, response.getId());
            }
            mRosterFragment.setArguments(bundle);
            switchOutFragment(mRosterFragment);
            invalidateOptionsMenu();
            checkMenuItem(navigationView.getMenu().getItem(currentMenuItem));
        }
    }

    private void switchOutFragment(RosterFragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(R.id.roster_item_container) == null) {
            manager.beginTransaction().add(R.id.roster_item_container, fragment).commit();
        } else {
            manager.beginTransaction().replace(R.id.roster_item_container, fragment).commit();
        }
    }

    private void checkMenuItem(MenuItem current) {
        unCheckAllMenuItems(navigationView);
        current.setChecked(true);
    }

    private void unCheckAllMenuItems(NavigationView navigationView) {
        final Menu menu = navigationView.getMenu();
        for (int i = 0; i < mQuestionCount; i++) {
            MenuItem item = menu.getItem(i);
            if (item.isChecked()) {
                item.setChecked(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.participant_editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentMenuItem == 0) {
            menu.findItem(R.id.menu_item_previous).setVisible(false);
        } else if (currentMenuItem == mQuestionCount - 1) {
            menu.findItem(R.id.menu_item_next).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.save_new_participant:
                saveParticipant();
                return true;
            case R.id.menu_item_next:
                mRosterFragment.getResponse().save();
                updateFragment(currentMenuItem + 1);
                return true;
            case R.id.menu_item_previous:
                mRosterFragment.getResponse().save();
                updateFragment(currentMenuItem - 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveParticipant() {
        mRosterFragment.getResponse().save();
        Class callingClass = RosterActivity.class;
        if (getCallingActivity() != null) {
            callingClass = getCallingActivity().getClass();
        }
        Intent intent = new Intent(ParticipantEditorActivity.this, callingClass);
        intent.putExtra(EXTRA_SURVEY_ID, mSurvey.getId());
        setResult(100, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFragment(currentMenuItem);
    }

    private class QuestionLoaderTask extends AsyncTask<Survey, Void,
            LinkedHashMap<Question, Response>> {
        private ProgressDialog dialog = new ProgressDialog(ParticipantEditorActivity.this);

        @Override
        protected LinkedHashMap<Question, Response> doInBackground(Survey... params) {
            Survey survey = params[0];
            LinkedHashMap<Question, Response> map = new LinkedHashMap<>();
            for (Question question : survey.getInstrument().questions()) {
                map.put(question, survey.getResponseByQuestion(question));
            }
            return map;
        }

        @Override
        protected void onPreExecute() {
            dialog.setTitle(R.string.instrument_loading_progress_header);
            dialog.setMessage(getString(R.string.background_process_progress_message));
            dialog.show();
        }

        @Override
        protected void onPostExecute(LinkedHashMap<Question, Response> map) {
            mQuestions.addAll(map.keySet());
            mQuestionCount = mQuestions.size();
            mQuestionResponseList = map;
            setNavigationViewMenu();
            setNavigationViewListener(navigationView);
            if (dialog.isShowing()) dialog.dismiss();
        }

    }

}