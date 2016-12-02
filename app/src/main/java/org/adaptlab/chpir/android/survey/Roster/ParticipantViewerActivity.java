package org.adaptlab.chpir.android.survey.Roster;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.Models.Instrument;
import org.adaptlab.chpir.android.survey.Models.Question;
import org.adaptlab.chpir.android.survey.Models.Response;
import org.adaptlab.chpir.android.survey.Models.Survey;
import org.adaptlab.chpir.android.survey.R;

import java.util.List;

public class ParticipantViewerActivity extends AppCompatActivity {
    public final static String EXTRA_SURVEY_ID = "org.adaptlab.chpir.android.survey.survey_id";
    private static final String TAG = "ParticipantViewerActivity";
    public final int EDIT_SURVEY_REQUEST_CODE = 100;
    private Survey mSurvey;
    private RecyclerView mRecyclerView;
    private List<Response> mResponses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_viewer);
        mRecyclerView = (RecyclerView) findViewById(R.id.participant_recycler_view);
        Long surveyId = getIntent().getLongExtra(RosterActivity.EXTRA_SURVEY_ID, -1);
        if (surveyId != -1) {
            mSurvey = Survey.load(Survey.class, -1);
            new ResponseLoaderTask().execute(mSurvey);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.participant_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_participant:
                editParticipant();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editParticipant() {
        Intent intent = new Intent(ParticipantViewerActivity.this, ParticipantEditorActivity.class);
        intent.putExtra(EXTRA_SURVEY_ID, mSurvey.getId());
        startActivityForResult(intent, EDIT_SURVEY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_SURVEY_REQUEST_CODE && data != null) {
            Long surveyId = getIntent().getLongExtra(ParticipantEditorActivity.EXTRA_SURVEY_ID, -1);
            if (surveyId != -1) {
                mSurvey = Survey.load(Survey.class, surveyId);
                new ResponseLoaderTask().execute(mSurvey);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RosterActivity.class);
        intent.putExtra(RosterActivity.EXTRA_SURVEY_ID, mSurvey.getId());
        setResult(200, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showParticipantDetails();
    }

    private void showParticipantDetails() {
        ResponseAdapter adapter = new ResponseAdapter(mResponses);
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private class ResponseAdapter extends RecyclerView.Adapter<ResponseAdapter.QuestionViewHolder> {

        List<Response> responses;

        ResponseAdapter(List<Response> responses) {
            this.responses = responses;
        }

        @Override
        public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .question_card_view, parent, false);
            return new QuestionViewHolder(v);
        }

        @Override
        public void onBindViewHolder(QuestionViewHolder holder, int position) {
            holder.questionText.setText(responses.get(position).getQuestion().getText());
            holder.questionResponse.setText(mResponses.get(position).getText());
        }

        @Override
        public int getItemCount() {
            return responses.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        class QuestionViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView questionText;
            TextView questionResponse;

            QuestionViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.question_card_view);
                questionText = (TextView) itemView.findViewById(R.id.question_text);
                questionResponse = (TextView) itemView.findViewById(R.id.question_response);
            }
        }

    }

    private class ResponseLoaderTask extends AsyncTask<Survey, Void, List<Response>> {

        @Override
        protected List<Response> doInBackground(Survey... params) {
            return params[0].responses();
        }

        @Override
        protected void onPostExecute(List<Response> responses) {
            mResponses = responses;
            showParticipantDetails();
            new TitleTask().execute(mSurvey.getInstrument());
        }
    }

    private class TitleTask extends AsyncTask<Instrument, Void, Response> {

        @Override
        protected Response doInBackground(Instrument... params) {
            for (Question question : params[0].questions()) {
                if (question.identifiesSurvey()) {
                    for (Response response : mResponses) {
                        if (response.getQuestion() == question) {
                            return response;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Response response) {
           setTitle(response.getText());
        }
    }

}