package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ResponsePhoto;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.Date;
import java.util.List;

public abstract class QuestionFragment extends Fragment {
    protected final static String LIST_DELIMITER = ",";
    private final static String TAG = "QuestionFragment";
    public TextView mValidationTextView;
    public Response mResponse;
    private Question mQuestion;
    private List<Question> mQuestions;
    private Survey mSurvey;
    private Instrument mInstrument;
    private SurveyFragment mSurveyFragment;
    private List<Option> mOptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSurveyFragment = (SurveyFragment) getParentFragment();
        if (mSurveyFragment == null) return;
        init();
    }

    public void init() {
        Bundle bundle = this.getArguments();
        String questionIdentifier = "";
        if (bundle != null) {
            questionIdentifier = bundle.getString("QuestionIdentifier");
        }
        mSurvey = mSurveyFragment.getSurvey();
        mQuestions = mSurveyFragment.getQuestions();
        for(Question question:mQuestions){
            if(questionIdentifier.equals(question.getQuestionIdentifier())){
                mQuestion = question;
                break;
            }
        }
        if (mSurvey == null || mQuestion == null) return;
        mResponse = loadOrCreateResponse();
        mResponse.setQuestion(mQuestion);
        mResponse.setSurvey(mSurvey);
        mResponse.setTimeStarted(new Date());
        mInstrument = mSurvey.getInstrument();
        if (mSurveyFragment.getOptions() != null) {
            mOptions = mSurveyFragment.getOptions().get(mQuestion);
        }
    }

    private Response loadOrCreateResponse() {
        Response response = mSurveyFragment.getResponses().get(mQuestion);
        if (response == null) {
            response = new Response();
            response.setQuestion(mQuestion);
            response.setSurvey(mSurvey);
            response.save();
            mSurveyFragment.getResponses().put(mQuestion, response);
            mSurveyFragment.refreshView();
        }
        return response;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question_factory, parent, false);

        ViewGroup questionComponent = (LinearLayout) v.findViewById(R.id.question_component);
        TextView questionText = (TextView) v.findViewById(R.id.question_text);
        mValidationTextView = (TextView) v.findViewById(R.id.validation_text);

        String instructions = "";
        if (!TextUtils.isEmpty(mQuestion.getInstructions()) && !mQuestion.getInstructions().equals("null")) {
            instructions = mQuestion.getInstructions();
        }

        questionText.setText(Html.fromHtml(mQuestion.getNumberInInstrument() + "<br />" + instructions + "<br />" + mQuestion.getText()));

        // Overridden by subclasses to place their graphical elements on the fragment.
        createQuestionComponent(questionComponent);
        if (mResponse != null) {
            deserialize(mResponse.getText());
        }
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyBoard();
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

    protected abstract void createQuestionComponent(ViewGroup questionComponent);

    protected abstract void deserialize(String responseText);

    protected void showKeyBoard() {
        InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager
                .HIDE_IMPLICIT_ONLY);
    }

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

    public Instrument getInstrument() {
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
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
        new SaveResponseTask().execute(mResponse);
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

    private class SaveResponseTask extends AsyncTask<Response, Void, Survey> {

        @Override
        protected Survey doInBackground(Response... params) {
            params[0].save();
            params[0].getSurvey().save();
            return params[0].getSurvey();
        }

    }
}