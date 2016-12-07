package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.roster.ParticipantEditorActivity;

import java.util.Date;

public abstract class RosterFragment extends Fragment {
    public final int MINIMUM_WIDTH = 250;
    private Question mQuestion;
    private Response mResponse;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.roster_item_fragment, container, false);
        ViewGroup responseComponent = (LinearLayout) view.findViewById(R.id.response_component);
        ParticipantEditorActivity parentActivity = (ParticipantEditorActivity) getActivity();
        int questionNum = getArguments().getInt(ParticipantEditorActivity.EXTRA_QUESTION_NUMBER, -1);
        Long surveyId = getArguments().getLong(ParticipantEditorActivity.EXTRA_SURVEY_ID, -1);
        if (questionNum != -1 && surveyId != -1 && parentActivity != null) {
            mQuestion = parentActivity.getQuestions().get(questionNum);
            Survey survey = Survey.load(Survey.class, surveyId);
            mResponse = parentActivity.getQuestionResponses().get(mQuestion);
            if (mResponse == null) {
                mResponse = new Response();
                mResponse.setQuestion(mQuestion);
                mResponse.setSurvey(survey);
                parentActivity.getQuestionResponses().put(mQuestion, mResponse);
            }
            createResponseComponent(responseComponent);
        }
        return view;
    }

    protected abstract void createResponseComponent(ViewGroup responseComponent);

    @Override
    public void onResume() {
        super.onResume();
        TextView itemText = (TextView) getActivity().findViewById(R.id.roster_item_text);
        itemText.setText(styleTextWithHtml(mQuestion.getText()));
    }

    @Override
    public void onPause() {
        super.onPause();
        new SaveResponseTask().execute(mResponse);
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public Response getResponse() {
        return mResponse;
    }

    private Spanned styleTextWithHtml(String text) {
        return Html.fromHtml(text);
    }

    private class SaveResponseTask extends AsyncTask<Response, Void, Void> {

        @Override
        protected Void doInBackground(Response... params) {
            Response response = params[0];
            response.save();
            response.getSurvey().setLastUpdated(new Date());
            response.getSurvey().save();
            return null;
        }
    }
// TODO: 12/7/16 Add way for interviewer to mark survey/roster as completed
}