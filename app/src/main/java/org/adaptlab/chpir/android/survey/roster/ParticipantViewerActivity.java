package org.adaptlab.chpir.android.survey.roster;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.stripHtml;

public class ParticipantViewerActivity extends AppCompatActivity {
    public final static String EXTRA_SURVEY_ID =
            "org.adaptlab.chpir.android.survey.roster.survey_id";
    public final static String EXTRA_ROSTER_ID =
            "org.adaptlab.chpir.android.survey.roster.roster_id";
    private static final String TAG = "ParticipantViewerActivity";
    private Survey mSurvey;
    private RecyclerView mRecyclerView;
    private List<Response> mResponses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_viewer);
        mRecyclerView = findViewById(R.id.participant_recycler_view);
        Long surveyId = getIntent().getLongExtra(RosterActivity.EXTRA_SURVEY_ID, -1);
        if (surveyId != -1) {
            mSurvey = Survey.load(Survey.class, surveyId);
            if (mSurvey == null) return;
            new ResponseLoaderTask().execute(mSurvey);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mSurvey.readyToSend() || mSurvey.isSent()) {
            menu.findItem(R.id.completed_participant).setVisible(false).setEnabled(false);
        }
        if (mSurvey.isSent()) {
            menu.findItem(R.id.edit_participant).setVisible(false).setEnabled(false);
        } else {
            menu.findItem(R.id.submitted_participant).setVisible(false).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
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
            case R.id.delete_participant:
                deleteParticipant();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteParticipant() {
        mSurvey.destroy();
        finish();
    }

    private void editParticipant() {
        Intent intent = new Intent(ParticipantViewerActivity.this, ParticipantEditorActivity.class);
        intent.putExtra(EXTRA_SURVEY_ID, mSurvey.getId());
        intent.putExtra(EXTRA_ROSTER_ID, mSurvey.getRoster().getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        if (mSurvey != null) new ResponseLoaderTask().execute(mSurvey);
    }

    private void showParticipantDetails() {
        if (mResponses != null) {
            ResponseAdapter adapter = new ResponseAdapter(mResponses);
            if (mRecyclerView != null) {
                mRecyclerView.setAdapter(adapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            }
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
            holder.questionText.setText(stripHtml(responses.get(position).getQuestion().getText()));
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
                cardView = itemView.findViewById(R.id.question_card_view);
                questionText = itemView.findViewById(R.id.question_text);
                questionResponse = itemView.findViewById(R.id.question_response);
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
            if (response != null) setTitle(response.getText());
        }
    }

}