package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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

import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.NextQuestion;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.OptionInOptionSet;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ResponsePhoto;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.adaptlab.chpir.android.survey.FormatUtils.styleTextWithHtml;

public abstract class QuestionFragment extends Fragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey" +
            ".instrument_id";
    public final static String EXTRA_RESPONSE_ID = "org.adaptlab.chpir.android.survey" +
            ".response_id";
    public final static String EXTRA_SURVEY_ID = "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_QUESTION_ID = "org.adaptlab.chpir.android.survey" +
            ".question_id";
    protected final static String LIST_DELIMITER = ",";
    private final static String TAG = "QuestionFragment";
    public TextView mValidationTextView;
    public Response mResponse;
    private Question mQuestion;
    private Survey mSurvey;
    private Instrument mInstrument;
    private SurveyFragment mSurveyFragment;
    private List<Option> mOptions;
    protected RadioGroup mSpecialResponses;
    private TextView mQuestionText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSurveyFragment = (SurveyFragment) getParentFragment();
        if (mSurveyFragment == null) return;
        if (savedInstanceState == null) {
            init();
        } else {
            mInstrument = Instrument.load(Instrument.class, savedInstanceState.getLong(EXTRA_INSTRUMENT_ID));
            mSurvey = Survey.load(Survey.class, savedInstanceState.getLong(EXTRA_SURVEY_ID));
            mQuestion = Question.load(Question.class, savedInstanceState.getLong(EXTRA_QUESTION_ID));
            mResponse = Response.load(Response.class, savedInstanceState.getLong(EXTRA_RESPONSE_ID));
            mOptions = mSurveyFragment.getOptions().get(mQuestion);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_INSTRUMENT_ID, mInstrument.getId());
        outState.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
        outState.putLong(EXTRA_QUESTION_ID, mQuestion.getId());
        outState.putLong(EXTRA_RESPONSE_ID, mResponse.getId());
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
        setResponseSkips();
        setSpecialResponseSkips();
        refreshFollowUpQuestion();
    }

    private Response loadOrCreateResponse() {
        Response response = mSurveyFragment.getResponses().get(mQuestion);
        if (response == null) {
            response = new Response();
            response.setQuestion(mQuestion);
            response.setSurvey(mSurvey);
            response.save();
            mSurveyFragment.getResponses().put(mQuestion, response);
        }
        return response;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question_factory, parent, false);

        ViewGroup questionComponent = (LinearLayout) v.findViewById(R.id.question_component);
        mQuestionText = v.findViewById(R.id.question_text);
        mValidationTextView = v.findViewById(R.id.validation_text);
        mQuestionText.setTypeface(mInstrument.getTypeFace(getActivity().getApplicationContext()));
        setQuestionText();

        // Overridden by subclasses to place their graphical elements on the fragment.
        createQuestionComponent(questionComponent);
        if (mResponse != null) {
            deserialize(mResponse.getText());
        }

        setResponseSkips();
        setSpecialResponseSkips();
        refreshFollowUpQuestion();

        mSpecialResponses = v.findViewById(R.id.special_responses_container);
        List<String> responses = new ArrayList<>();
        if (mQuestion.hasSpecialOptions()) {
            Log.i(TAG, "has special options " + mQuestion.specialOptions().size());
            for (Option option : mQuestion.specialOptions()) {
                responses.add(option.getText(mQuestion.getInstrument()));
            }
        } else {
            Log.i(TAG, "No special options");
        }

        for (String response : responses) {
            int responseId = responses.indexOf(response);
            final Button button = new RadioButton(getActivity());
            button.setText(response);
            button.setId(responseId);
            button.setTypeface(getInstrument().getTypeFace(getActivity().getApplicationContext()));

            mSpecialResponses.addView(button, responseId);
            final List<String> finalResponses = responses;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unSetResponse();
                    setSpecialResponse(finalResponses.get(v.getId()));
                }
            });
        }
        if (responses.size() == 0) mSpecialResponses.setVisibility(View.GONE);
        deserializeSpecialResponse();
        return v;
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

    protected abstract void unSetResponse();

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

    @Override
    public void onResume() {
        super.onResume();
        mSurveyFragment.getScrollView().fullScroll(View.FOCUS_UP);
    }

    @Override
    public void onPause() {
        super.onPause();
//        hideKeyBoard();
    }

//    protected void hideKeyBoard() {
//        try {
//            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams
//                    .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//            if (getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus()
//                    .getWindowToken() != null) {
//                ((InputMethodManager) getActivity().getSystemService(Context
//                        .INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getActivity()
//                        .getCurrentFocus().getWindowToken(), 0);
//            }
//        } catch (Exception ex) {
//            if (BuildConfig.DEBUG) Log.e(TAG, "Input Method Exception " + ex.getMessage());
//        }
//    }

    protected abstract void createQuestionComponent(ViewGroup questionComponent);

    protected abstract void deserialize(String responseText);

//    protected void showKeyBoard() {
//        InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context
//                .INPUT_METHOD_SERVICE);
//        manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager
//                .HIDE_IMPLICIT_ONLY);
//    }

    protected SurveyFragment getSurveyFragment() {
        return mSurveyFragment;
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public List<Option> getOptions() {
        return mOptions;
    }

    protected Survey getSurvey() {
        return mSurvey;
    }

    protected Instrument getInstrument() {
        return mInstrument;
    }

    public String getSpecialResponse() {
        return (mResponse == null) ? "" : mResponse.getSpecialResponse();
    }

    public void setSpecialResponse(String specialResponse) {
        if (mResponse != null) {
            mResponse.setSpecialResponse(specialResponse);
            mResponse.setResponse("");
            mResponse.setDeviceUser(AuthUtils.getCurrentUser());
            mResponse.setTimeEnded(new Date());
            deserialize(mResponse.getText());
            mSurvey.setLastQuestion(mQuestion);
//            new SaveResponseTask().execute(mResponse);
            mResponse.save();
            mSurvey.save();
            removeTextFocus();
            setSpecialResponseSkips();
            refreshFollowUpQuestion();
        }
    }

    public void unSetAllResponses(){
        unSetResponse();
        if(mResponse!=null){
            mResponse.setSpecialResponse("");
        }
        if(mSpecialResponses!=null){
            mSpecialResponses.clearCheck();
        }
    }

    public Response getResponse() {
        return mResponse;
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

    protected void setResponseText() {
        mResponse.setResponse(serialize());
        mResponse.setTimeEnded(new Date());
        validateResponse();
        if (isAdded() && !mResponse.getText().equals("")) {
            mResponse.setSpecialResponse("");
        }
        mSurvey.setLastQuestion(mQuestion);
//        new SaveResponseTask().execute(mResponse);
        mResponse.save();
        mSurvey.save();
        if (!mQuestion.isTextEntryQuestionType()) {
            removeTextFocus();
        }
        setResponseSkips();
        refreshFollowUpQuestion();
    }

    private void refreshFollowUpQuestion() {
        if (mQuestion.isToFollowUpOnQuestion()) {
            mSurveyFragment.reAnimateFollowUpFragment(mQuestion);
        }
    }

    private void setResponseSkips() {
        if (mQuestion.isSkipQuestionType() && !TextUtils.isEmpty(mResponse.getText())) {
            int responseIndex = Integer.parseInt(mResponse.getText());
            if (mQuestion.isOtherQuestionType() && responseIndex == mQuestion.options().size()) {
                Log.i("OtherQuestionType","isOtherQuestionType ");
                mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(), mQuestion
                        .getQuestionIdentifier(), mQuestion.getQuestionIdentifier());

            } else {
                Option selectedOption = mQuestion.options().get(Integer.parseInt(mResponse
                        .getText()));
                NextQuestion skipOption = getNextQuestion(selectedOption);
                Log.i("selectedOption",selectedOption.toString()+" ");
                if (skipOption != null) {
                    Log.i("skipOption",skipOption.toString()+" ");
//                    mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(), skipOption
//                            .getNextQuestionIdentifier());
                    mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(), skipOption
                            .getNextQuestionIdentifier(), mQuestion.getQuestionIdentifier());
                } else if (mQuestion.hasSkips(mInstrument)) {
//                    mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(), mQuestion
//                            .getQuestionIdentifier());
                    mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(), mQuestion
                            .getQuestionIdentifier(), mQuestion.getQuestionIdentifier());
                }
                mSurveyFragment.setMultipleSkipQuestions(selectedOption, mQuestion);
            }
            if (mQuestion.isMultipleSkipQuestion(mInstrument) && !TextUtils.isEmpty(mResponse
                    .getText())) {
                Option selectedOption = mQuestion.options().get(Integer.parseInt(mResponse.getText()));
                mSurveyFragment.setMultipleSkipQuestions(selectedOption, mQuestion);
            }
        } else if(!TextUtils.isEmpty(mResponse.getText())){
            mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(), mQuestion
                    .getQuestionIdentifier(), mQuestion.getQuestionIdentifier());
        }
    }

    private NextQuestion getNextQuestion(Option selectedOption) {
        return new Select().from(NextQuestion.class).where("OptionIdentifier = ? AND " +
                "QuestionIdentifier = ? AND " + "RemoteInstrumentId = ?", selectedOption
                .getIdentifier(), mQuestion.getQuestionIdentifier(), mInstrument.getRemoteId())
                .executeSingle();
    }

    private void setSpecialResponseSkips() {
        if (!TextUtils.isEmpty(mResponse.getSpecialResponse()) && mQuestion.hasSpecialOptions()) {
            Option specialOption = new Select("Options.*").distinct().from(Option.class)
                    .innerJoin(OptionInOptionSet.class)
                    .on("OptionInOptionSets.RemoteOptionSetId = ?", mQuestion.getRemoteSpecialOptionSetId())
                    .where("Options.Text = ? AND OptionInOptionSets.RemoteOptionId = Options.RemoteId",
                            mResponse.getSpecialResponse())
                    .executeSingle();
            if (specialOption != null) {
                NextQuestion specialSkipOption = getNextQuestion(specialOption);
                if (specialSkipOption != null) {
                    mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(),
                            specialSkipOption.getNextQuestionIdentifier(), mQuestion.getQuestionIdentifier());
                } else if (mQuestion.hasSpecialSkips(mInstrument)) {
                    mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(), mQuestion
                            .getQuestionIdentifier(), mQuestion.getQuestionIdentifier());
                }
            }
            if (mQuestion.isMultipleSkipQuestion(mInstrument) && !TextUtils.isEmpty(mResponse.getSpecialResponse())) {
                mSurveyFragment.setMultipleSkipQuestions(specialOption, mQuestion);
            }
        }
    }

    protected abstract String serialize();

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

        // Refresh options menu to reflect response validation status.
        if (isAdded()) {
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
    }

    private void animateValidationTextView(boolean valid) {
        Animation animation = new AlphaAnimation(0, 0);

        if (valid) {
            if (mValidationTextView.getVisibility() == TextView.VISIBLE)
                animation = new AlphaAnimation(1, 0);
            mValidationTextView.setVisibility(TextView.INVISIBLE);
        } else {
            animation = new AlphaAnimation(0, 1);
            mValidationTextView.setVisibility(TextView.VISIBLE);
            if (mQuestion.getRegExValidationMessage() != null)
                mValidationTextView.setText(mQuestion.getRegExValidationMessage());
            else
                mValidationTextView.setText(R.string.not_valid_response);
        }

        animation.setDuration(1000);
        if (mValidationTextView.getAnimation() == null ||
                mValidationTextView.getAnimation().hasEnded() ||
                !mValidationTextView.getAnimation().hasStarted()) {
            // Only animate if not currently animating
            mValidationTextView.setAnimation(animation);
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
        appendNumberAndInstructions(mQuestionText);
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

    /*
     * If this question has instructions, append and add new line
     */
    private void appendNumberAndInstructions(TextView text) {
        if (!TextUtils.isEmpty(mQuestion.getInstructions()) && !mQuestion.getInstructions()
                .equals("null")) {
            text.setText(styleTextWithHtml(mQuestion.getNumberInInstrument() + "<br />" +
                    mQuestion.getInstructions() + "<br />"));
        } else {
            text.setText(styleTextWithHtml(mQuestion.getNumberInInstrument() + "<br />"));
        }
    }


    private class SaveResponseTask extends AsyncTask<Response, Void, Survey> {

        @Override
        protected Survey doInBackground(Response... params) {
            params[0].save();
            params[0].getSurvey().save();
            return params[0].getSurvey();
        }

    }
}