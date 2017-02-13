package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.AuthUtils;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.roster.ParticipantEditorActivity;
import org.adaptlab.chpir.android.survey.roster.ResponseEditorActivity;

import java.util.Date;

import static org.adaptlab.chpir.android.survey.FormatUtils.styleTextWithHtml;

public abstract class RosterFragment extends Fragment {
    public final int MINIMUM_WIDTH = 250;
    protected static final String LIST_DELIMITER = ",";
    private final String TAG = "RosterFragment";
    private Question mQuestion;
    private Response mResponse;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.roster_item_fragment, container, false);
        ViewGroup responseComponent = (LinearLayout) view.findViewById(R.id.response_component);
        if (getActivity().getClass().getSimpleName().equals(
                ParticipantEditorActivity.class.getSimpleName())) {
            editSurvey(responseComponent);
        } else if (getActivity().getClass().getSimpleName().equals(
                ResponseEditorActivity.class.getSimpleName())) {
            editQuestionResponses(responseComponent);
        }
        return view;
    }

    private void editQuestionResponses(ViewGroup responseComponent) {
        ResponseEditorActivity parentActivity = (ResponseEditorActivity) getActivity();
        int questionNum = getArguments().getInt(ResponseEditorActivity.EXTRA_QUESTION_NUMBER, -1);
        Long surveyId = getArguments().getLong(ResponseEditorActivity.EXTRA_SURVEY_ID, -1);
        if (questionNum != -1 && surveyId != -1 && parentActivity != null) {
            mQuestion = parentActivity.getQuestion();
            Survey survey = Survey.load(Survey.class, surveyId);
            mResponse = parentActivity.getResponse(questionNum);
            if (mResponse == null) {
                mResponse = new Response();
                mResponse.setQuestion(mQuestion);
                mResponse.setSurvey(survey);
                parentActivity.updateResponses(parentActivity.getIdentifierResponse(questionNum),
                        mResponse);
            }
            parentActivity.setTitle(parentActivity.getIdentifierResponse(questionNum).getText());
            createResponseComponent(responseComponent);
        }
    }

    private void editSurvey(ViewGroup responseComponent) {
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
        hideKeyBoard();
    }

    public void addOtherResponseView(EditText otherText) {
        otherText.setHint(R.string.other_specify_edittext);
        otherText.setEnabled(false);
        otherText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        otherText.addTextChangedListener(new TextWatcher() {
            // Required by interface
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setOtherResponse(s.toString());
            }

            public void afterTextChanged(Editable s) {
            }
        });

        if (mResponse.getOtherResponse() != null) {
            otherText.setText(mResponse.getOtherResponse());
        }
    }

    public void setOtherResponse(String response) {
        mResponse.setOtherResponse(response);
        mResponse.setDeviceUser(AuthUtils.getCurrentUser());
    }

    protected void showKeyBoard() {
        InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager
                .HIDE_IMPLICIT_ONLY);
    }

    protected void hideKeyBoard() {
        try {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams
                    .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if (getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus()
                    .getWindowToken() != null) {
                ((InputMethodManager) getActivity().getSystemService(Context
                        .INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getActivity()
                        .getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Input Method Exception " + ex.getMessage());
        }
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public Response getResponse() {
        return mResponse;
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
// TODO: 12/7/16 Add way for interviewer to mark roster as completed
}