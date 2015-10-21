package org.adaptlab.chpir.android.survey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.activeandroid.Model;

import org.adaptlab.chpir.android.survey.Models.AdminSettings;
import org.adaptlab.chpir.android.survey.Models.Question;
import org.adaptlab.chpir.android.survey.Models.Response;
import org.adaptlab.chpir.android.survey.Models.Section;
import org.adaptlab.chpir.android.survey.Models.Survey;

import java.util.Date;

public class SectionFragment extends Fragment {
    private static final String TAG = "SectionFragment";
    private Section mSection;
    private Survey mSurvey;
    private RadioGroup mRadioGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long sectionId = getActivity().getIntent().getExtras().getLong(SurveyFragment.EXTRA_SECTION_ID);
        long surveyId = getActivity().getIntent().getExtras().getLong(SurveyFragment.EXTRA_SURVEY_ID);
        mSection = Section.findByRemoteId(sectionId);
        mSurvey = Model.load(Survey.class, surveyId);
        getActivity().setTitle(mSection.getTitle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section, parent, false);
        TextView numQuestionsLabel = (TextView) view.findViewById(R.id.number_of_questions_label);
        numQuestionsLabel.setText(getActivity().getResources().getString(R.string.section_number_of_questions) + ":  ");
        TextView numberOfQuestions = (TextView) view.findViewById(R.id.number_of_questions_text);
        numberOfQuestions.setText(Integer.toString(mSection.questions().size()));
        TextView defaultResponsesLabel = (TextView) view.findViewById(R.id.default_responses_label);
        defaultResponsesLabel.setText(R.string.section_default_responses_label);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.default_responses_radio_group);
        AdminSettings adminSettings = AppUtil.getAdminSettingsInstance();
        for (int k = 0; k < adminSettings.getSpecialResponses().size(); k++) {
            displayDefaultSpecialResponses(mRadioGroup, adminSettings.getSpecialResponses().get(k), k);
        }

        Button mCancelButton = (Button) view.findViewById(R.id.section_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayNextQuestion(mSection.questions().get(0).getId());
            }
        });

        Button mProceedButton = (Button) view.findViewById(R.id.section_proceed);
        mProceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRadioGroup.getCheckedRadioButtonId() == -1 ) {
                    displayNextQuestion(mSection.questions().get(0).getId());
                } else {
                    displayNextQuestion(getNextQuestion().getId());
                }
            }
        });

        return view;
    }

    private void displayNextQuestion(Long questionId) {
        Intent i = new Intent(getActivity(), SurveyActivity.class);
        i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID, mSection.getInstrument().getRemoteId());
        i.putExtra(SurveyFragment.EXTRA_QUESTION_ID, questionId);
        i.putExtra(SurveyFragment.EXTRA_SURVEY_ID, mSurvey.getId());
        getActivity().setResult(Activity.RESULT_OK, i);
        getActivity().finish();
    }

    private void displayDefaultSpecialResponses(RadioGroup radioGroup, String displayText, int index) {
        RadioButton radioButton = new RadioButton(getActivity());
        radioButton.setText(displayText);
        radioButton.setId(index);
        radioButton.setTypeface(mSection.getInstrument().getTypeFace(getActivity().getApplicationContext()));
        radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        radioGroup.addView(radioButton, index);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setDefaultResponses(checkedId);
            }
        });
    }

    private void setDefaultResponses(int checkedId) {
        String specialResponse = AppUtil.getAdminSettingsInstance().getSpecialResponses().get(checkedId);
        for (Question question : mSection.questions()) {
            Response response;
            if (mSurvey.getResponseByQuestion(question) == null) {
                response = new Response();
            } else {
                response = mSurvey.getResponseByQuestion(question);
            }
            response.setQuestion(question);
            response.setSurvey(mSurvey);
            response.setSpecialResponse(specialResponse);
            response.setTimeStarted(new Date());
            response.setTimeEnded(new Date());
            if (!response.saveWithValidation()) {
                response.save();
            }
        }
    }

    private Question getNextQuestion() {
        int numOfQuestions = mSection.questions().size();
        int lastQuestionNumber = mSection.questions().get(numOfQuestions - 1).getNumberInInstrument();
        if (lastQuestionNumber >= mSection.getInstrument().questions().size()) {
            return mSection.getInstrument().questions().get(mSection.getInstrument().questions().size() - 1);
        } else {
            return Question.findByNumberInInstrument(lastQuestionNumber + 1, mSection.getInstrument().getId());
        }
    }
}