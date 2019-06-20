package org.adaptlab.chpir.android.survey.roster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Roster;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.adaptlab.chpir.android.survey.roster.RosterActivity.EXTRA_QUESTION_ID;
import static org.adaptlab.chpir.android.survey.roster.RosterActivity.EXTRA_SURVEY_IDENTIFIER;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.stripHtml;

public class ResponseViewerActivity extends AppCompatActivity {
    private static final String TAG = "ResponseViewerActivity";
    private HashMap<Response, Response> mResponses;
    private Question mQuestion;
    private Roster mRoster;
    private Question mSurveyIdentifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_viewer);
        Long rosterId = getIntent().getLongExtra(RosterActivity.EXTRA_ROSTER_ID, -1);
        Long questionId = getIntent().getLongExtra(EXTRA_QUESTION_ID, -1);
        Long surveyId = getIntent().getLongExtra(EXTRA_SURVEY_IDENTIFIER, -1);
        if (rosterId == -1 || questionId == -1 || surveyId == -1) return;
        mQuestion = Question.load(Question.class, questionId);
        mRoster = Roster.load(Roster.class, rosterId);
        mSurveyIdentifier = Question.load(Question.class, surveyId);
        if (mQuestion == null || mRoster == null || mSurveyIdentifier == null) return;
        setTitle(mRoster.getIdentifier() + " - " + stripHtml(mQuestion.getText()));
        new ResponseLoaderTask().execute(mRoster, mQuestion, mSurveyIdentifier);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mQuestion != null && mRoster != null && mSurveyIdentifier != null) {
            new ResponseLoaderTask().execute(mRoster, mQuestion, mSurveyIdentifier);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.response_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_question_responses:
                editResponses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editResponses() {
        Intent intent = new Intent(this, ResponseEditorActivity.class);
        intent.putExtra(RosterActivity.EXTRA_ROSTER_ID, mRoster.getId());
        intent.putExtra(RosterActivity.EXTRA_QUESTION_ID, mQuestion.getId());
        intent.putExtra(RosterActivity.EXTRA_SURVEY_IDENTIFIER, mSurveyIdentifier.getId());
        startActivity(intent);
    }

    private void setListView() {
        ResponseViewerAdapter adapter = new ResponseViewerAdapter(mResponses, this);
        ListView lView = (ListView) findViewById(R.id.responseListView);
        if (lView != null) {
            lView.setAdapter(adapter);
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
            mResponses = responses;
            setListView();
        }

    }

    private class ResponseViewerAdapter extends ArrayAdapter<Response> {
        private Context context;
        private HashMap<Response, Response> responseMap;
        private List<Response> responseIdentifiers;

        ResponseViewerAdapter(HashMap<Response, Response> responses, Context context) {
            super(context, 0, new ArrayList<>(responses.keySet()));
            this.responseMap = responses;
            this.responseIdentifiers = new ArrayList<>(responses.keySet());
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(final int position, View view, @NonNull ViewGroup parent) {

            if (view == null) {
                view = ((Activity) context).getLayoutInflater().inflate(
                        R.layout.response_viewer_list_item, null);
            }

            Response id = responseIdentifiers.get(position);
            Response response = responseMap.get(id);
            TextView surveyIdView = (TextView) view.findViewById(R.id.survey_response);
            if (id != null) surveyIdView.setText(id.getText());
            TextView responseView = (TextView) view.findViewById(R.id.question_response);
            if (response != null) responseView.setText(response.getText());
            return view;
        }

    }

}