package org.adaptlab.chpir.android.survey.roster;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.adaptlab.chpir.android.survey.AppUtil;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Roster;
import org.adaptlab.chpir.android.survey.models.RosterLog;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.roster.rosterfragments.RosterFragment;
import org.adaptlab.chpir.android.survey.roster.rosterfragments.RosterFragmentGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.adaptlab.chpir.android.survey.FormatUtils.stripHtml;

public class ParticipantEditorActivity extends AppCompatActivity {
    public final static String EXTRA_QUESTION_NUMBER =
            "org.adaptlab.chpir.android.survey.roster.question_number";
    public final static String EXTRA_SURVEY_ID =
            "org.adaptlab.chpir.android.survey.roster.survey_id";
    private static final String TAG = "ParticipantEditorActivity";
    private DrawerLayout mDrawer;
    private NavigationView navigationView;
    private int currentMenuItem = 0;
    private List<Question> mQuestions;
    private Roster mRoster;
    private Survey mSurvey;
    private int mQuestionCount;
    private LinkedHashMap<Question, Response> mQuestionResponseList;
    private Question mSurveyIdentifierQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_editor);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mDrawer = (DrawerLayout) findViewById(R.id.roster_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.roster_drawer_view);

        Long rosterId = getIntent().getLongExtra(RosterActivity.EXTRA_ROSTER_ID, -1);
        if (rosterId != -1) {
            mRoster = Roster.load(Roster.class, rosterId);
            setTitle(getString(R.string.new_participant) + " for " + mRoster.getIdentifier());
        }
        if (mRoster == null) return;
        Long surveyId = getIntent().getLongExtra(ParticipantViewerActivity.EXTRA_SURVEY_ID, -1);
        loadOrCreateSurvey(surveyId);
        mQuestions = new ArrayList<>();
        new QuestionLoaderTask().execute(mSurvey);
    }

    private void loadOrCreateSurvey(Long surveyId) {
        if (surveyId == -1) {
            mSurvey = new Survey();
            mSurvey.setInstrumentRemoteId(mRoster.getInstrument().getRemoteId());
            mSurvey.setProjectId(mRoster.getInstrument().getProjectId());
            mSurvey.setRosterUUID(mRoster.getUUID());
            mSurvey.save();
        } else {
            mSurvey = Survey.load(Survey.class, surveyId);
        }
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
        if (mSurvey != null && !mQuestions.isEmpty()) {
            currentMenuItem = position;
            RosterFragment rosterFragment = RosterFragmentGenerator.createQuestionFragment(
                    mQuestions.get(position).getQuestionType());
            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_QUESTION_NUMBER, position);
            bundle.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
            rosterFragment.setArguments(bundle);
            switchOutFragment(rosterFragment);
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
        menu.findItem(R.id.submit_participant).setVisible(false).setEnabled(false);
        menu.findItem(R.id.un_submit_participant).setVisible(false).setEnabled(false);
        if (currentMenuItem == 0) {
            menu.findItem(R.id.menu_item_previous).setVisible(false);
        } else if (currentMenuItem == mQuestionCount - 1) {
            menu.findItem(R.id.menu_item_next).setVisible(false);
            if (mSurvey.readyToSend()) {
                menu.findItem(R.id.un_submit_participant).setEnabled(true).setVisible(true);
            } else {
                menu.findItem(R.id.submit_participant).setEnabled(true).setVisible(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_item_next:
                updateFragment(currentMenuItem + 1);
                return true;
            case R.id.menu_item_previous:
                updateFragment(currentMenuItem - 1);
                return true;
            case R.id.submit_participant:
                markSurveyAsReady();
                return true;
            case R.id.un_submit_participant:
                markSurveyAsNotReady();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void markSurveyAsReady() {
        mSurvey.setAsComplete(true);
        mSurvey.save();
        logRosterItemCompletion();
        mRoster.setComplete(true);
        mRoster.save();
        finish();
    }

    private void logRosterItemCompletion() {
        RosterLog log = RosterLog.findByRosterAndSurvey(mRoster.getUUID(), mSurvey.getUUID());
        if (log == null) {
            log = new RosterLog();
            log.setRosterUUID(mRoster.getUUID());
            log.setSurveyUUID(mSurvey.getUUID());
        }
        log.setComplete(true);
        log.setIdentifier(mSurvey.identifier(AppUtil.getContext()));
        log.save();
    }

    private void markSurveyAsNotReady() {
        mSurvey.setAsComplete(false);
        mSurvey.save();
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFragment(currentMenuItem);
    }

    @Override
    protected void onPause () {
        if (mSurveyIdentifierQuestion != null) {
            Response idResponse = mQuestionResponseList.get(mSurveyIdentifierQuestion);
            if (idResponse == null) {
                idResponse = new Response();
                idResponse.setQuestion(mSurveyIdentifierQuestion);
                idResponse.setSurvey(mSurvey);
                idResponse.save();
                mQuestionResponseList.put(mSurveyIdentifierQuestion, idResponse);
            }
        }
        super.onPause();
    }

    public LinkedHashMap<Question, Response> getQuestionResponses() {
        return mQuestionResponseList;
    }

    public List<Question> getQuestions() {
        return mQuestions;
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
            mQuestions.clear();
            mQuestions.addAll(map.keySet());
            mQuestionCount = mQuestions.size();
            mQuestionResponseList = map;
            setNavigationViewMenu();
            setNavigationViewListener(navigationView);
            if (dialog.isShowing()) dialog.dismiss();
            updateFragment(0);
            if (mSurveyIdentifierQuestion == null) {
                setIdentifierQuestion();
            }
        }

    }

    private void setIdentifierQuestion() {
         for (Question question : mQuestions) {
                if (question.identifiesSurvey()) {
                    mSurveyIdentifierQuestion = question;
                    break;
                }
         }
    }

}