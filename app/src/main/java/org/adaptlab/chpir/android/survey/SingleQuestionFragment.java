package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.models.Display;
import org.adaptlab.chpir.android.survey.models.DisplayInstruction;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.MultipleSkip;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.OptionSet;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ResponsePhoto;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.utils.AuthUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.isEmpty;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public abstract class SingleQuestionFragment extends QuestionFragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey" +
            ".instrument_id";
    public final static String EXTRA_RESPONSE_ID = "org.adaptlab.chpir.android.survey" +
            ".response_id";
    public final static String EXTRA_SURVEY_ID = "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_QUESTION_ID = "org.adaptlab.chpir.android.survey" +
            ".question_id";
    private final static String TAG = "SingleQuestionFragment";
    public TextView mValidationTextView;
    public Response mResponse;
    protected RadioGroup mSpecialResponses;
    private Question mQuestion;
    private Survey mSurvey;
    private Instrument mInstrument;
    private List<Option> mOptions;
    private List<Option> mSpecialOptions;
    private TextView mQuestionText;
    private TextView mQuestionInstructions;
    private TextView mDisplayInstructionsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question, parent, false);
        TextView questionNumber = v.findViewById(R.id.questionNumber);
        questionNumber.setText(String.valueOf(mQuestion.getNumberInInstrument()));
        TextView questionIdentifier = v.findViewById(R.id.questionIdentifier);
        questionIdentifier.setText(mQuestion.getQuestionIdentifier());
        mQuestionInstructions = v.findViewById(R.id.question_instructions);
        mQuestionText = v.findViewById(R.id.question_text);
        mValidationTextView = v.findViewById(R.id.validation_text);
        mDisplayInstructionsText = v.findViewById(R.id.displayInstructions);
        mQuestionText.setTypeface(mInstrument.getTypeFace(getActivity().getApplicationContext()));
        setDisplayInstructions();
        setQuestionInstructions();
        setQuestionText();

        // Overridden by subclasses to place their graphical elements on the fragment.
        ViewGroup questionComponent = (LinearLayout) v.findViewById(R.id.question_component);
        setChoiceSelectionInstructions(v);
        createQuestionComponent(questionComponent);

        if (mResponse != null) {
            deserialize(mResponse.getText());
        }

        setSkipPatterns();
        refreshFollowUpQuestion();
        setSpecialResponseUI(v);
        deserializeSpecialResponse();
        return v;
    }

    private void setSpecialResponseUI(View v) {
        if (!mQuestion.getDisplay().getMode().equals(Display.DisplayMode.TABLE.toString())) {
            mSpecialResponses = v.findViewById(R.id.special_responses_container);
            List<String> responses = new ArrayList<>();
            if (mQuestion.hasSpecialOptions()) {
                for (Option option : mSpecialOptions) {
                    responses.add(option.getText(mQuestion.getInstrument()));
                }
            }

            for (String response : responses) {
                int responseId = responses.indexOf(response);
                final Button button = new RadioButton(getActivity());
                button.setText(response);
                button.setId(responseId);
                button.setTypeface(getInstrument().getTypeFace(
                        getActivity().getApplicationContext()));
                button.setTextColor(getResources().getColorStateList(R.color.states));

                mSpecialResponses.addView(button, responseId);
                final List<String> finalResponses = responses;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unSetResponse();
                        setResponse(finalResponses.get(v.getId()));
                    }
                });
            }
            if (responses.size() == 0) mSpecialResponses.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mInstrument != null) {
            outState.putLong(EXTRA_INSTRUMENT_ID, mInstrument.getId());
        }
        if (mSurvey != null) {
            outState.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
        }
        if (mQuestion != null) {
            outState.putLong(EXTRA_QUESTION_ID, mQuestion.getId());
        }
        if (mResponse != null) {
            outState.putLong(EXTRA_RESPONSE_ID, mResponse.getId());
        }
    }

    protected void setDisplayInstructions() {
        List<DisplayInstruction> displayInstructions = mSurveyFragment.getDisplayInstructions
                (mQuestion.getDisplay());
        if (displayInstructions != null && displayInstructions.size() > 0) {
            StringBuilder instructions = new StringBuilder();
            for (DisplayInstruction instruction : displayInstructions) {
                if (instruction.getPosition() == mQuestion.getNumberInInstrument()) {
                    instructions.append(instruction.getInstructions()).append("<br>");
                }
            }
            if (instructions.length() > 0) {
                ((LinearLayout) mDisplayInstructionsText.getParent()).setVisibility(View.VISIBLE);
                mDisplayInstructionsText.setText(styleTextWithHtml(instructions.toString()));
            }
        }
    }

    protected void hideIndeterminateProgressBar() {
        List<Question> displayQuestions = mSurveyFragment.getQuestions(mQuestion.getDisplay());
        if (displayQuestions.get(displayQuestions.size() - 1).equals(mQuestion)) {
            mSurveyFragment.hideIndeterminateProgressBar();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            init();
        } else {
            mInstrument = Instrument.load(Instrument.class, savedInstanceState.getLong
                    (EXTRA_INSTRUMENT_ID));
            mSurvey = Survey.load(Survey.class, savedInstanceState.getLong(EXTRA_SURVEY_ID));
            mQuestion = Question.load(Question.class, savedInstanceState.getLong
                    (EXTRA_QUESTION_ID));
            mResponse = Response.load(Response.class, savedInstanceState.getLong
                    (EXTRA_RESPONSE_ID));
            mOptions = mSurveyFragment.getOptions().get(mQuestion);
        }
        mSpecialOptions = mSurveyFragment.getSpecialOptions().get(mQuestion
                .getRemoteSpecialOptionSetId());
    }

    public void init() {
        Bundle bundle = this.getArguments();
        String questionIdentifier = "";
        if (bundle != null) {
            questionIdentifier = bundle.getString("QuestionIdentifier");
        }
        mSurvey = mSurveyFragment.getSurvey();
        mQuestion = mSurveyFragment.getQuestion(questionIdentifier);
        if (mSurvey == null || mQuestion == null) return;
        mResponse = loadOrCreateResponse();
        mResponse.setQuestion(mQuestion);
        mResponse.setSurvey(mSurvey);
        mResponse.setTimeStarted(new Date());
        mInstrument = mSurvey.getInstrument();
        if (mSurveyFragment.getOptions() != null) {
            mOptions = mSurveyFragment.getOptions().get(mQuestion);
            if (mOptions == null) mOptions = new ArrayList<>();
        }
        refreshFollowUpQuestion();
    }

    private Response loadOrCreateResponse() {
        Response response = mSurveyFragment.getResponses().get(mQuestion);
        if (response == null) {
            response = new Response();
            response.setQuestion(mQuestion);
            response.setSurvey(mSurvey);
            saveResponseInBackground(response);
            mSurveyFragment.getResponses().put(mQuestion, response);
        }
        return response;
    }

    private void refreshFollowUpQuestion() {
        if (mQuestion.isToFollowUpOnQuestion()) {
            mSurveyFragment.reAnimateFollowUpFragment(mQuestion);
        }
    }

    private void setQuestionInstructions() {
        if (!TextUtils.isEmpty(mQuestion.getInstructions()) && !mQuestion.getInstructions()
                .equals("null")) {
            mQuestionInstructions.setText(styleTextWithHtml(mQuestion.getInstructions()));
        } else {
            mQuestionInstructions.setVisibility(View.GONE);
        }
    }

    /*
     * If this question is a follow up question, then attempt
     * to get the response to the question that is being followed up on.
     *
     * If the question being followed up on was skipped by the user,
     * then return false. This gives the calling function an opportunity
     * to handle this accordingly.  Likely this will involve skipping
     * the question that is a follow up question.
     *
     * If this question is not a following up question, then just
     * set the text as normal.
     */
    protected void setQuestionText() {
        if (mQuestion.isFollowUpQuestion()) {
            String followUpText = mQuestion.getFollowingUpText(mSurveyFragment.getResponses(),
                    getActivity());
            if (followUpText != null) {
                mQuestionText.append(styleTextWithHtml(followUpText));
            }
        } else if (mQuestion.hasRandomizedFactors()) {
            mQuestionText.append(styleTextWithHtml(mQuestion.getRandomizedText(mSurveyFragment
                    .getResponses().get(mQuestion))));
        } else {
            mQuestionText.append(styleTextWithHtml(mQuestion.getText()));
        }
    }

    private void setChoiceSelectionInstructions(View view) {
        OptionSet optionSet = OptionSet.findByRemoteId(getQuestion().getRemoteOptionSetId());
        if (optionSet != null && !isEmpty(optionSet.getInstructions())) {
            TextView instructionsView = view.findViewById(R.id.optionSetInstructions);
            if (instructionsView != null) {
                instructionsView.setText(styleTextWithHtml(optionSet.getInstructions()));
                instructionsView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setSkipPatterns() {
        Option selectedOption = null;
        String nextQuestion = null;
        String enteredValue = null;
        List<Option> selectedOptions = new ArrayList<>();
        if (!TextUtils.isEmpty(mResponse.getText())) {
            if (mQuestion.hasSingleResponse()) {
                selectedOption = getSelectedOption(mResponse.getText());
            } else if (mQuestion.getQuestionType().equals(Question.QuestionType.INTEGER)) {
                enteredValue = mResponse.getText();
            } else if (mQuestion.hasMultipleResponses()) {
                String[] responses = mResponse.getText().split(Response.LIST_DELIMITER);
                if (responses.length == 1) {
                    selectedOption = getSelectedOption(responses[0]);
                } else {
                    for (String response : responses) {
                        Option option = getSelectedOption(response);
                        if (option != null) selectedOptions.add(option);
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(mResponse.getSpecialResponse())) {
            selectedOption = Option.findByQuestionAndSpecialResponse(mQuestion,
                    mResponse.getSpecialResponse());
        }
        if (selectedOption == null && enteredValue == null && selectedOptions.isEmpty()) {
            showAllSubsequentQuestions();
        } else if (selectedOption != null && enteredValue == null && selectedOptions.isEmpty()) {
            nextQuestion = mQuestion.getNextQuestionIdentifier(selectedOption, mResponse);
        } else if (selectedOption == null && enteredValue != null && selectedOptions.isEmpty()) {
            nextQuestion = mQuestion.getNextQuestionIdentifier(enteredValue);
        } else if (!selectedOptions.isEmpty()) {
            nextQuestion = mQuestion.getNextQuestionIdentifier(selectedOptions);
        }
        if (nextQuestion == null) {
            showAllSubsequentQuestions();
        } else {
            if (nextQuestion.equals(Question.COMPLETE_SURVEY)) {
                mSurveyFragment.startSurveyCompletion(mQuestion);
            } else {
                mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(),
                        nextQuestion, mQuestion.getQuestionIdentifier());
            }
        }
        if (selectedOption != null && mQuestion.isMultipleSkipQuestion(mInstrument)) {
            mSurveyFragment.setMultipleSkipQuestions(selectedOption, mQuestion);
        } else if (!selectedOptions.isEmpty() && mQuestion.isMultipleSkipQuestion(mInstrument)) {
            mSurveyFragment.setMultipleSkipQuestions2(selectedOptions, mQuestion);
        }
    }

    private Option getSelectedOption(String responseText) {
        int responseIndex = Integer.parseInt(responseText);
        if (responseIndex < mQuestion.defaultOptions().size()) {
            return mQuestion.defaultOptions().get(responseIndex);
        } else {
            return null;
        }
    }

    private void showAllSubsequentQuestions() {
        mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(),
                mQuestion.getQuestionIdentifier(), mQuestion.getQuestionIdentifier());
    }

    protected Instrument getInstrument() {
        return mInstrument;
    }

    private void deserializeSpecialResponse() {
        if (TextUtils.isEmpty(mResponse.getSpecialResponse())) return;
        for (int i = 0; i < mSpecialResponses.getChildCount(); i++) {
            if (((RadioButton) mSpecialResponses.getChildAt(i)).getText().equals(mResponse
                    .getSpecialResponse())) {
                mSpecialResponses.check(i);
            }
        }
    }

    public Question getQuestion() {
        return mQuestion;
    }

    /*
     * This will remove the focus of the input as the survey is
     * traversed.  If this is not called, then it will be possible
     * for someone to change the answer to a question that they are
     * not currently viewing.
     */
    private void removeTextFocus() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService
                    (Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken()
                    , InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void animateValidationTextView(boolean valid) {
        Animation animation = new AlphaAnimation(0, 0);
        if (valid) {
            if (mValidationTextView.getVisibility() == TextView.VISIBLE)
                animation = new AlphaAnimation(1, 0);
            mValidationTextView.setVisibility(TextView.GONE);
        } else {
            animation = new AlphaAnimation(0, 1);
            mValidationTextView.setVisibility(TextView.VISIBLE);
            if (mQuestion.getValidation() != null) {
                mValidationTextView.setText(styleTextWithHtml(mQuestion.getValidation()
                        .getValidationMessage(mInstrument)));
            } else {
                mValidationTextView.setText(styleTextWithHtml(
                        getString(R.string.not_valid_response)));
            }
        }

        animation.setDuration(1000);
        if (mValidationTextView.getAnimation() == null ||
                mValidationTextView.getAnimation().hasEnded() ||
                !mValidationTextView.getAnimation().hasStarted()) {
            // Only animate if not currently animating
            mValidationTextView.setAnimation(animation);
        }
    }

    protected SurveyFragment getSurveyFragment() {
        return mSurveyFragment;
    }

    public List<Option> getOptions() {
        return mOptions;
    }

    protected Survey getSurvey() {
        return mSurvey;
    }

    public Response getResponse() {
        return mResponse;
    }

    protected void setResponse(String specialResponse) {
        if (mResponse == null) return;
        if (specialResponse == null) {
            // Set responseText
            mResponse.setResponse(serialize());
            mResponse.setSpecialResponse("");
            validateResponse();
            if (!mQuestion.isTextEntryQuestionType()) {
                removeTextFocus();
            }
        } else {
            // Set specialResponse
            mResponse.setSpecialResponse(specialResponse);
            mResponse.setResponse("");
            deserialize(mResponse.getText());
            removeTextFocus();
            animateValidationTextView(true);
        }
        mResponse.setTimeEnded(new Date());
        mSurvey.setLastQuestion(mQuestion);
        mResponse.setDeviceUser(AuthUtils.getCurrentUser());
        saveResponseInBackground(mResponse);
        setSkipPatterns();
        refreshFollowUpQuestion();
    }

    protected ResponsePhoto getResponsePhoto() {
        return mResponse.getResponsePhoto();
    }

    /*
     * An otherText is injected from a subclass.  This gives
     * the majority of the control to the otherText to the subclass,
     * but the things that all other text fields have in common
     * can go here.
     */
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

    /*
     * Display warning to user if response does not match regular
     * expression in question.  Disable next button if not valid.
     */
    public void validateResponse() {
        if (mResponse.isValid()) {
            mResponse.setDeviceUser(AuthUtils.getCurrentUser());
            mResponse.setQuestionVersion(mQuestion.getQuestionVersion());
            mSurvey.setLastUpdated(new Date());
            animateValidationTextView(true);
        } else {
            animateValidationTextView(false);
        }
    }

}