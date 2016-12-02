package org.adaptlab.chpir.android.survey.Roster.RosterFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.Models.Question;
import org.adaptlab.chpir.android.survey.Models.Response;
import org.adaptlab.chpir.android.survey.Models.Survey;
import org.adaptlab.chpir.android.survey.Roster.ParticipantEditorActivity;
import org.adaptlab.chpir.android.survey.R;

public abstract class RosterFragment extends Fragment {
    public final int MINIMUM_WIDTH = 250;
    private Question mQuestion;
    private Response mResponse;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.roster_item_fragment, container, false);
        ViewGroup responseComponent = (LinearLayout) view.findViewById(R.id.response_component);
        String questionId = getArguments().getString(ParticipantEditorActivity.EXTRA_QUESTION_ID);
        Long responseId = getArguments().getLong(ParticipantEditorActivity.EXTRA_RESPONSE_ID, -1);
        Long surveyId = getArguments().getLong(ParticipantEditorActivity.EXTRA_SURVEY_ID, -1);
        if (questionId != null && surveyId != -1) {
            mQuestion = Question.findByQuestionIdentifier(questionId);
            Survey survey = Survey.load(Survey.class, surveyId);
            if (responseId != -1) {
                mResponse = Response.load(Response.class, responseId);
            } else {
                mResponse = new Response();
                mResponse.setQuestion(mQuestion);
                mResponse.setSurvey(survey);
                mResponse.save();
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
        itemText.setText(mQuestion.getText());
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public Response getResponse() {
        return mResponse;
    }
}