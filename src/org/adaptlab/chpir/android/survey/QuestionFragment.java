package org.adaptlab.chpir.android.survey;

import java.util.Date;

import org.adaptlab.chpir.android.survey.Models.Instrument;
import org.adaptlab.chpir.android.survey.Models.Question;
import org.adaptlab.chpir.android.survey.Models.Response;
import org.adaptlab.chpir.android.survey.Models.ResponsePhoto;
import org.adaptlab.chpir.android.survey.Models.Survey;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.Model;

public abstract class QuestionFragment extends Fragment {
	protected final static String LIST_DELIMITER = ",";
    protected abstract void createQuestionComponent(ViewGroup questionComponent);
    protected abstract String serialize();
    protected abstract void deserialize(String responseText);
    
    public TextView mValidationTextView;

    private Question mQuestion;
    private Survey mSurvey;
    public Response mResponse;
    private Instrument mInstrument;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        init();
    }
 
    public void init() {
        long questionId = getArguments().getLong(QuestionFragmentFactory.EXTRA_QUESTION_ID, -1);
        long surveyId = getArguments().getLong(QuestionFragmentFactory.EXTRA_SURVEY_ID, -1);

        if (questionId != -1 && surveyId != -1) {
            mQuestion = Question.findByRemoteId(questionId);
            mSurvey = Model.load(Survey.class, surveyId);
            mResponse = loadOrCreateResponse();
            mResponse.setQuestion(mQuestion);
            mResponse.setSurvey(mSurvey);
            mInstrument = mSurvey.getInstrument();
        }
        
        saveTimeStarted();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question_factory, parent,
                false);

        ViewGroup questionComponent = (LinearLayout) v.findViewById(R.id.question_component);        
        mValidationTextView = (TextView) v.findViewById(R.id.validation_text);

        // Overridden by subclasses to place their graphical elements on the fragment.
        createQuestionComponent(questionComponent);
        deserialize(mResponse.getText());
        
        return v;
    }

    public Question getQuestion() {	
        return mQuestion;
    }
    
    protected Survey getSurvey() {
        return mSurvey;
    }
    
    public Response getResponse() {
        return mResponse;
    }
    
    public Instrument getInstrument() { 
        return mInstrument;
    }
    
    public String getSpecialResponse() {
    	if (getResponse() != null) {
	        return getResponse().getSpecialResponse();
    	} else {
	        return "";
    	}
    }
    
    protected ResponsePhoto getResponsePhoto() {
    	return getResponse().getResponsePhoto();
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
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) { 
                saveOtherResponse(s.toString());
            }
            
            // Required by interface
            public void beforeTextChanged(CharSequence s, int start,
                    int count, int after) { }
            public void afterTextChanged(Editable s) { }
        });
        
        if (getResponse().getOtherResponse() != null) {
            otherText.setText(getResponse().getOtherResponse());
        }
    }
    
    public void saveOtherResponse(String response) {
        getResponse().setOtherResponse(response);
        getResponse().setDeviceUser(AuthUtils.getCurrentUser());
        getResponse().save();
    }
    
    /*
     * Display warning to user if response does not match regular 
     * expression in question.  Disable next button if not valid.
     * Only save if valid.
     */
    public void saveResponseWithValidation() {
        getResponse().setDeviceUser(AuthUtils.getCurrentUser());
        
        if (getResponse().saveWithValidation()) {
            animateValidationTextView(true);
        } else {
            animateValidationTextView(false);
        }

        // Refresh options menu to reflect response validation status.
        if (isAdded()) {
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
    }
    
    protected void saveResponse() {
        getResponse().setResponse(serialize());
        saveTimeEnded();
        saveResponseWithValidation();
        if (isAdded() && !mResponse.getText().equals("")) {
            mResponse.setSpecialResponse("");
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
    }
    
    public void saveSpecialResponse(String specialResponse) {
        Response response = getResponse();
    	if (response != null) {
        	response.setSpecialResponse(specialResponse); 
        	response.setResponse("");
        	response.setDeviceUser(AuthUtils.getCurrentUser());
        	saveTimeEnded();
        	response.save();
        	deserialize(response.getText());
        }
    }
    
    private void saveTimeStarted() {
    	if (getResponse().getTimeStarted() == null) {
    		getResponse().setTimeStarted(new Date());
    		getResponse().save();
    	}
    }
    
    private void saveTimeEnded() {
    	getResponse().setTimeEnded(new Date());
    }
   
    private Response loadOrCreateResponse() {
        if (mSurvey.getResponseByQuestion(getQuestion()) != null) {
            return mSurvey.getResponseByQuestion(getQuestion());
        } else {
            return new Response();
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
}
