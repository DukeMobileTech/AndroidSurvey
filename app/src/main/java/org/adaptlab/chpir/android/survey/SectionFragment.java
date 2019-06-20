package org.adaptlab.chpir.android.survey;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import com.activeandroid.Model;

import org.adaptlab.chpir.android.survey.models.Section;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.ArrayList;

public class SectionFragment extends Fragment {
    private static final String TAG = "SectionFragment";
    private Section mSection;
    private Survey mSurvey;
    private RadioGroup mRadioGroup;
    private ArrayList<Integer> mQuestionsToAddToPreviousList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long sectionId = getActivity().getIntent().getExtras().getLong(SurveyFragment.EXTRA_SECTION_ID);
        long surveyId = getActivity().getIntent().getExtras().getLong(SurveyFragment.EXTRA_SURVEY_ID);
        mSection = Section.findByRemoteId(sectionId);
        mSurvey = Model.load(Survey.class, surveyId);
        getActivity().setTitle(mSection.getTitle());
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_section, parent, false);
//        TextView numQuestionsLabel = (TextView) view.findViewById(R.id.number_of_questions_label);
//        numQuestionsLabel.setText(getActivity().getResources().getString(R.string.section_number_of_questions) + ":  "
//                + mSection.questions().size() + " (#" + mSection.questions().get(0).getNumberInInstrument() + " - #"
//                + mSection.questions().get(mSection.questions().size() - 1).getNumberInInstrument() + ")");
//        numQuestionsLabel.setTypeface(Typeface.DEFAULT_BOLD);
//
//        mRadioGroup = (RadioGroup) view.findViewById(R.id.default_responses_radio_group);
//        for (int k = 0; k < mSection.getInstrument().getSpecialOptionStrings().size(); k++) {
//            displayDefaultSpecialResponses(mRadioGroup, mSection.getInstrument().getSpecialOptionStrings().get(k), k);
//        }
//        showDefaultSpecialResponse();
//
//        Button mCancelButton = (Button) view.findViewById(R.id.section_cancel);
//        mCancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                unCheckSpecialSpecialResponses();
//                displayNextQuestion(mSection.questions().get(0).getNumberInInstrument() - 1);
//            }
//        });
//
//        Button mProceedButton = (Button) view.findViewById(R.id.section_proceed);
//        mProceedButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mRadioGroup.getCheckedRadioButtonId() == -1) {
//                    displayNextQuestion(mSection.questions().get(0).getNumberInInstrument() - 1);
//                } else {
//                    displayNextQuestion(getNextQuestion().getNumberInInstrument() - 1);
//                }
//            }
//        });
//
//        return view;
//    }

//    private void displayNextQuestion(int num) {
//        Intent i = new Intent(getActivity(), SurveyActivity.class);
//        i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID, mSection.getInstrument().getRemoteId());
//        i.putExtra(SurveyFragment.EXTRA_QUESTION_NUMBER, num);
//        i.putExtra(SurveyFragment.EXTRA_SURVEY_UUID, mSurvey.getId());
//        i.putIntegerArrayListExtra(SurveyFragment.EXTRA_PREVIOUS_QUESTION_IDS, mQuestionsToAddToPreviousList);
//        getActivity().setResult(Activity.RESULT_OK, i);
//        getActivity().finish();
//    }

//    private void displayDefaultSpecialResponses(RadioGroup radioGroup, String displayText, int index) {
//        RadioButton radioButton = new RadioButton(getActivity());
//        String label = getActivity().getString(getActivity().getResources().getIdentifier(displayText, "string", getActivity().getPackageName()));
//        radioButton.setText(label);
//        radioButton.setId(index);
//        radioButton.setTypeface(mSection.getInstrument().getTypeFace(getActivity().getApplicationContext()));
//        radioButton.setLayoutParams(new RadioGroup.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        radioGroup.addView(radioButton, index);
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                mQuestionsToAddToPreviousList = new ArrayList<Integer>();
//                setDefaultResponses(checkedId);
//            }
//        });
//
//    }

//    private boolean showDefaultSpecialResponse() {
//        if (mSection.questions().size() == 0) return false;
//        for (Question question : mSection.questions()) {
//            Response response = mSurvey.getResponseByQuestion(question);
//            if (response != null && !TextUtils.isEmpty(response.getSpecialResponse())) {
//                int indexToCheck = mSection.getInstrument().getSpecialOptionStrings().indexOf(response.getSpecialResponse());
//                mRadioGroup.check(indexToCheck);
//                return true;
//            }
//        }
//        return false;
//    }

//    private void setDefaultResponses(int checkedId) {
//        String specialResponse = mSection.getInstrument().getSpecialOptionStrings().get(checkedId);
//        new DefaultResponsesTask().execute(specialResponse);
//    }

//    private boolean unCheckSpecialSpecialResponses() {
//        for (Question question : mSection.questions()) {
//            if (mSurvey.getResponseByQuestion(question) == null) return false;
//            Response response = mSurvey.getResponseByQuestion(question);
//            response.setSpecialResponse(Response.BLANK);
//            if (!response.saveWithValidation()) {
//                response.save();
//            }
//        }
//        return true;
//    }

//    private Question getNextQuestion() {
//        int numOfQuestions = mSection.questions().size();
//        int lastQuestionNumber = mSection.questions().get(numOfQuestions - 1).getNumberInInstrument();
//        if (lastQuestionNumber >= mSection.getInstrument().questions().size()) {
//            return mSection.getInstrument().questions().get(mSection.getInstrument().questions().size() - 1);
//        } else {
//            return Question.findByNumberInInstrument(lastQuestionNumber + 1, mSection.getInstrument().getRemoteId());
//        }
//    }

//    private class DefaultResponsesTask extends AsyncTask<String, Void, Void> {
//
//        @Override
//        protected Void doInBackground(String... params) {
//            for (Question question : mSection.questions()) {
//                Response response;
//                if (mSurvey.getResponseByQuestion(question) == null) {
//                    response = new Response();
//                } else {
//                    response = mSurvey.getResponseByQuestion(question);
//                }
//                response.setRelations(question);
//                response.setData(mSurvey);
//                response.setSpecialResponse(params[0]);
//                response.setTimeStarted(new Date());
//                response.setTimeEnded(new Date());
//                if (!response.saveWithValidation()) {
//                    response.save();
//                }
//                if (question.getNumberInInstrument() <= mSection.getInstrument().questions().size() && !question.isLastQuestion()) {
//                    mQuestionsToAddToPreviousList.add(question.getNumberInInstrument() - 1);
//                }
//            }
//            return null;
//        }
//    }
}