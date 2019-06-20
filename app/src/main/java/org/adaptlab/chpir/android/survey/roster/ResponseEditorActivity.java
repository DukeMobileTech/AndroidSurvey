package org.adaptlab.chpir.android.survey.roster;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Roster;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.roster.rosterfragments.RosterFragment;
import org.adaptlab.chpir.android.survey.roster.rosterfragments.RosterFragmentGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.adaptlab.chpir.android.survey.roster.RosterActivity.EXTRA_QUESTION_ID;
import static org.adaptlab.chpir.android.survey.roster.RosterActivity.EXTRA_SURVEY_IDENTIFIER;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.stripHtml;

public class ResponseEditorActivity extends AppCompatActivity {
    public final static String EXTRA_QUESTION_NUMBER =
            "org.adaptlab.chpir.android.survey.roster.question_number";
    public final static String EXTRA_SURVEY_ID =
            "org.adaptlab.chpir.android.survey.roster.survey_id";
    private DrawerLayout mDrawer;
    private NavigationView navigationView;
    private Question mQuestion;
    private HashMap<Response, Response> responsesMap;
    private List<Response> identifierResponses;
    private int currentMenuItem = 0;

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
        Long questionId = getIntent().getLongExtra(EXTRA_QUESTION_ID, -1);
        Long surveyId = getIntent().getLongExtra(EXTRA_SURVEY_IDENTIFIER, -1);
        if (rosterId == -1 || questionId == -1 || surveyId == -1) return;
        mQuestion = Question.load(Question.class, questionId);
        Roster roster = Roster.load(Roster.class, rosterId);
        Question surveyIdentifier = Question.load(Question.class, surveyId);
        if (mQuestion == null || roster == null || surveyIdentifier == null) return;
        setTitle(roster.getIdentifier() + " - " + stripHtml(mQuestion.getText()));
        identifierResponses = new ArrayList<>();
        responsesMap = new LinkedHashMap<>();
        new ResponseLoaderTask().execute(roster, mQuestion, surveyIdentifier);
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
        } else if (currentMenuItem == identifierResponses.size() - 1) {
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
            case R.id.menu_item_next:
                updateFragment(currentMenuItem + 1);
                return true;
            case R.id.menu_item_previous:
                updateFragment(currentMenuItem - 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFragment(currentMenuItem);
    }

    public Response getIdentifierResponse(int pos) {
        return identifierResponses.get(pos);
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public Response getResponse(int num) {
        return responsesMap.get(getIdentifierResponse(num));
    }

    public void updateResponses(Response key, Response value) {
        responsesMap.put(key, value);
    }

    private void setNavigationViewMenu() {
        Menu menu = navigationView.getMenu();
        int index = 0;
        for (Response response : identifierResponses) {
            menu.add(R.id.drawer_menu_group, index, Menu.NONE, response.getText())
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
        if (mQuestion != null && !responsesMap.isEmpty()) {
            currentMenuItem = position;
            Response identifierResponse = identifierResponses.get(position);
            RosterFragment rosterFragment = RosterFragmentGenerator.createQuestionFragment(
                    mQuestion.getQuestionType());
            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_QUESTION_NUMBER, position);
            bundle.putLong(EXTRA_SURVEY_ID, identifierResponse.getSurvey().getId());
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
        for (int i = 0; i < identifierResponses.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isChecked()) {
                item.setChecked(false);
            }
        }
    }

    private class ResponseLoaderTask extends AsyncTask<Object, Void, HashMap<Response, Response>> {

        @Override
        protected HashMap<Response, Response> doInBackground(Object... params) {
            Roster roster = (Roster) params[0];
            Question question = (Question) params[1];
            Question identifier = (Question) params[2];
            HashMap<Response, Response> map = new LinkedHashMap<>();

            for (Survey survey : roster.surveys()) {
                Response identifierResponse = survey.getResponseByQuestion(identifier);
                Response questionResponse = survey.getResponseByQuestion(question);
                map.put(identifierResponse, questionResponse);
            }
            return map;
        }

        @Override
        protected void onPostExecute(HashMap<Response, Response> responses) {
            responsesMap = responses;
            identifierResponses.clear();
            identifierResponses.addAll(responsesMap.keySet());
            setNavigationViewMenu();
            setNavigationViewListener(navigationView);
            updateFragment(0);
        }

    }

}