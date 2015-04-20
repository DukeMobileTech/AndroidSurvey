package org.adaptlab.chpir.android.survey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.adaptlab.chpir.android.survey.Location.LocationServiceManager;
import org.adaptlab.chpir.android.survey.Models.Grid;
import org.adaptlab.chpir.android.survey.Models.Instrument;
import org.adaptlab.chpir.android.survey.Models.Option;
import org.adaptlab.chpir.android.survey.Models.Question;
import org.adaptlab.chpir.android.survey.Models.Question.QuestionType;
import org.adaptlab.chpir.android.survey.Models.Response;
import org.adaptlab.chpir.android.survey.Models.Section;
import org.adaptlab.chpir.android.survey.Models.Survey;
import org.adaptlab.chpir.android.survey.QuestionFragments.MultipleSelectGridFragment;
import org.adaptlab.chpir.android.survey.QuestionFragments.SingleSelectGridFragment;
import org.adaptlab.chpir.android.survey.Rules.InstrumentSurveyLimitPerMinuteRule;
import org.adaptlab.chpir.android.survey.Rules.InstrumentSurveyLimitRule;
import org.adaptlab.chpir.android.survey.Rules.InstrumentTimingRule;
import org.adaptlab.chpir.android.survey.Rules.RuleBuilder;
import org.adaptlab.chpir.android.survey.Tasks.SendResponsesTask;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.Model;
import com.crashlytics.android.Crashlytics;

public class SurveyFragment extends Fragment {
    private static final String TAG = "SurveyFragment";
    private static final int REVIEW_CODE = 100;
    public final static String EXTRA_INSTRUMENT_ID = 
            "org.adaptlab.chpir.android.survey.instrument_id";
    public final static String EXTRA_QUESTION_ID = 
            "org.adaptlab.chpir.android.survey.question_id";
    public final static String EXTRA_QUESTION_NUMBER = 
            "org.adaptlab.chpir.android.survey.question_number";
    public final static String EXTRA_SURVEY_ID = 
            "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_PREVIOUS_QUESTION_IDS = 
            "org.adaptlab.chpir.android.survey.previous_questions";
    public final static String EXTRA_PARTICIPANT_METADATA =
            "org.adaptlab.chpir.android.survey.metadata";
    public final static String EXTRA_QUESTIONS_TO_SKIP_IDS =
            "org.adaptlab.chpir.android.survey.questions_to_skip_ids";
    public final static String EXTRA_SKIPPED_QUESTIONS_IDS =
            "org.adaptlab.chpir.android.survey.skipped_questions_ids";
   
    private Question mQuestion;
    private Instrument mInstrument;
    private Survey mSurvey;
    private int mQuestionNumber;
    private String mMetadata;
    private Question mResumeQuestion = null;
    private Grid mGrid;
    
    // mPreviousQuestions is a Stack, however Android does not allow you
    // to save a Stack to the savedInstanceState, so it is represented as
    // an Integer array.
    private ArrayList<Integer> mPreviousQuestions;
    private ArrayList<Integer> mQuestionsToSkip;
    private Set<Integer> mSkippedQuestions;

    private TextView mQuestionText;
    private TextView mQuestionIndex;
    private TextView mParticipantLabel;
    private ProgressBar mProgressBar;
    QuestionFragment mQuestionFragment;
    private LocationServiceManager mLocationServiceManager;

    //drawer vars
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mDrawerTitle;
    private String mTitle;
    private ArrayList<Section> mSections;
    private String[] mSectionTitles;
    private boolean mNavDrawerSet = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        
        if (savedInstanceState != null) {
            mInstrument = Instrument.findByRemoteId(savedInstanceState.getLong(EXTRA_INSTRUMENT_ID));
            if (!checkRules()) getActivity().finish();
            mQuestion = Question.findByRemoteId(savedInstanceState.getLong(EXTRA_QUESTION_ID));
            mSurvey = Survey.load(Survey.class, savedInstanceState.getLong(EXTRA_SURVEY_ID));
            mQuestionNumber = savedInstanceState.getInt(EXTRA_QUESTION_NUMBER);
            mPreviousQuestions = savedInstanceState.getIntegerArrayList(EXTRA_PREVIOUS_QUESTION_IDS);
            mQuestionsToSkip = savedInstanceState.getIntegerArrayList(EXTRA_QUESTIONS_TO_SKIP_IDS);
            ArrayList<Integer> skippedQuestions = savedInstanceState.getIntegerArrayList(EXTRA_SKIPPED_QUESTIONS_IDS);
            mSkippedQuestions = new LinkedHashSet<Integer>(skippedQuestions);
            if (mQuestion.belongsToGrid()) {
            	mGrid = mQuestion.getGrid();
            }
        } else {
            Long instrumentId = getActivity().getIntent().getLongExtra(EXTRA_INSTRUMENT_ID, -1);
            mMetadata = getActivity().getIntent().getStringExtra(EXTRA_PARTICIPANT_METADATA);
            
            if (instrumentId == -1) return;
            
            mInstrument = Instrument.findByRemoteId(instrumentId);
            if (mInstrument == null) return;
            
            if (!checkRules()) getActivity().finish();
            
            loadOrCreateSurvey();
            loadOrCreateQuestion();             
        }
        
        if (AppUtil.PRODUCTION) {
            Crashlytics.setString("last instrument", mInstrument.getTitle());
        }
        
        if (AppUtil.getAdminSettingsInstance().getRecordSurveyLocation()) {
        	startLocationServices();
        }
    }
    
    private void setupNavigationDrawer() {
    	mSections = new ArrayList<Section>();
    	mSections = (ArrayList<Section>) mInstrument.sections();
    	mSectionTitles = new String[mSections.size()];
    	for (int i=0; i<mSections.size(); i++) {
    		mSectionTitles[i] = mSections.get(i).getTitle();
    	}
    	mTitle = mDrawerTitle = mInstrument.getTitle();
    	mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.drawer_list_item, mSectionTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(
        		getActivity(), 
        		mDrawerLayout, 
        		R.drawable.ic_drawer, 
        		R.string.drawer_open, 
        		R.string.drawer_close
        		) {
            
        	public void onDrawerClosed(View view) {
                getActivity().getActionBar().setTitle(mTitle);
                getActivity().invalidateOptionsMenu(); 
            }

            public void onDrawerOpened(View drawerView) {
                getActivity().getActionBar().setTitle(mDrawerTitle);
                getActivity().invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeButtonEnabled(true);
        mNavDrawerSet = true;
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    
    private void selectItem(int position) {
    	moveToSection(mSections.get(position).getStartQuestionIdentifier());
    	mDrawerList.setItemChecked(position, true);
        getActivity().setTitle(mInstrument.getTitle() + " : " + mSectionTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    private void moveToSection(String questionIdentifier) {
    	mPreviousQuestions.add(mQuestionNumber);
    	mQuestion = Question.findByQuestionIdentifier(questionIdentifier);
    	mQuestionNumber = mQuestion.getNumberInInstrument() - 1;
    	createQuestionFragment();
    	updateQuestionText();
    	updateQuestionCountLabel();
    }
    
    private void updateQuestionText() {
    	setQuestionText(mQuestionText);
        mQuestionText.setTypeface(mInstrument.getTypeFace(getActivity().getApplicationContext()));
    }
       
    private void startLocationServices() {
    	mLocationServiceManager = LocationServiceManager.get(getActivity());
        mLocationServiceManager.startLocationUpdates();
    }
    
    public void loadOrCreateSurvey() {
        Long surveyId = getActivity().getIntent().getLongExtra(EXTRA_SURVEY_ID, -1);
        if (surveyId == -1) {
            mSurvey = new Survey();
            mSurvey.setInstrument(mInstrument);
            mSurvey.setMetadata(mMetadata);
            mSurvey.setProjectId(mInstrument.getProjectId());
            mSurvey.save();
        } else {
            mSurvey = Model.load(Survey.class, surveyId);
        }
    }
    
    public void loadOrCreateQuestion() {
        mPreviousQuestions = new ArrayList<Integer>();  
        mQuestionsToSkip = new ArrayList<Integer>();
        mSkippedQuestions = new LinkedHashSet<Integer>();
        Long questionId = getActivity().getIntent().getLongExtra(EXTRA_QUESTION_ID, -1);
        if (questionId == -1) {
            mQuestion = mInstrument.questions().get(0);
            mQuestionNumber = 0;              
        } else {
            mQuestion = Model.load(Question.class, questionId);
            mQuestionNumber = mQuestion.getNumberInInstrument() - 1;
            for (int i = 0; i < mQuestionNumber; i++)
                mPreviousQuestions.add(i);
        } 
        if (mQuestion.belongsToGrid()) {
        	mGrid = mQuestion.getGrid();
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if (AppUtil.getAdminSettingsInstance().getRecordSurveyLocation()) {
        	getActivity().registerReceiver(mLocationServiceManager.mLocationReceiver, 
                new IntentFilter(LocationServiceManager.ACTION_LOCATION));
        }
    }
    
    @Override
    public void onStop() {
    	if (AppUtil.getAdminSettingsInstance().getRecordSurveyLocation()) {
    		getActivity().unregisterReceiver(mLocationServiceManager.mLocationReceiver);
    	}
        super.onStop();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	if (mResumeQuestion == mQuestion) {
        	mQuestionNumber = mQuestion.getNumberInInstrument() - 1;
            createQuestionFragment();
            updateQuestionText();
            updateQuestionCountLabel();
    	}
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == REVIEW_CODE) {
            Long remoteId = data.getExtras().getLong(EXTRA_QUESTION_ID);
            if (remoteId == Long.MIN_VALUE) {
            	getActivity().finish();
            } else {
				Question question = Question.findByRemoteId(remoteId);
	            if (question != null) {
	            	mQuestion = question;
	            	mResumeQuestion = mQuestion;
	            } else {
	            	getActivity().finish();
	            }
            }
		}
    }

	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_INSTRUMENT_ID, mInstrument.getRemoteId());
        outState.putLong(EXTRA_QUESTION_ID, mQuestion.getRemoteId());
        outState.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
        outState.putInt(EXTRA_QUESTION_NUMBER, mQuestionNumber);
        outState.putIntegerArrayList(EXTRA_PREVIOUS_QUESTION_IDS, mPreviousQuestions);
        outState.putIntegerArrayList(EXTRA_QUESTIONS_TO_SKIP_IDS, mQuestionsToSkip);
        outState.putIntegerArrayList(EXTRA_SKIPPED_QUESTIONS_IDS, new ArrayList<Integer>(mSkippedQuestions));
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_survey, menu);
        if (mNavDrawerSet == false) {
        	setupNavigationDrawer();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
    	switch (item.getItemId()) {
        case R.id.menu_item_previous:
            moveToPreviousQuestion();
            return true;
        case R.id.menu_item_next:
            if (mQuestionFragment.getSpecialResponse().equals(Response.SKIP)) {
                mQuestionFragment.saveSpecialResponse("");
            }
            moveToNextQuestion();
            return true;
        case R.id.menu_item_skip:
        	setSpecialResponse(Response.SKIP);
            if (isLastQuestion()) {
                finishSurvey();
            } else {
                moveToNextQuestion();
            }
        	return true;
        case R.id.menu_item_rf:
            setSpecialResponse(Response.RF);
            return true;
        case R.id.menu_item_na:
            setSpecialResponse(Response.NA);
            return true;
        case R.id.menu_item_dk:
            setSpecialResponse(Response.DK);
            return true;
        case R.id.menu_item_finish:
            finishSurvey();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	@Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_item_previous)
            .setEnabled(!isFirstQuestion());
        menu.findItem(R.id.menu_item_next)
            .setVisible(!isLastQuestion())
            .setEnabled(hasValidResponse());
        menu.findItem(R.id.menu_item_skip)
        	.setEnabled(hasValidResponse())
        	.setVisible(AppUtil.getAdminSettingsInstance().getShowSkip());
        menu.findItem(R.id.menu_item_rf)
            .setVisible(AppUtil.getAdminSettingsInstance().getShowRF());
        menu.findItem(R.id.menu_item_na)
            .setVisible(AppUtil.getAdminSettingsInstance().getShowNA());
        menu.findItem(R.id.menu_item_dk)
            .setVisible(AppUtil.getAdminSettingsInstance().getShowDK());
        menu.findItem(R.id.menu_item_finish)
            .setVisible(isLastQuestion())
            .setEnabled(hasValidResponse());
     
        showSpecialResponseSelection(menu);
    }
	
    
	/*
	 * Give a visual indication when a special response is selected
	 */
	public void showSpecialResponseSelection(Menu menu) {
        if (mQuestionFragment != null && mQuestionFragment.getSpecialResponse() != null && menu != null) {
        	if (mQuestionFragment.getSpecialResponse().equals(Response.SKIP)) {
                menu.findItem(R.id.menu_item_skip).setIcon(R.drawable.ic_menu_item_sk_selected);
            } else if (mQuestionFragment.getSpecialResponse().equals(Response.RF)) {
                menu.findItem(R.id.menu_item_rf).setIcon(R.drawable.ic_menu_item_rf_selected);                
            } else if (mQuestionFragment.getSpecialResponse().equals(Response.NA)) {
                menu.findItem(R.id.menu_item_na).setIcon(R.drawable.ic_menu_item_na_selected);                
            } else if (mQuestionFragment.getSpecialResponse().equals(Response.DK)) {
                menu.findItem(R.id.menu_item_dk).setIcon(R.drawable.ic_menu_item_dk_selected);                
            }
        }
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_survey, parent, false);

        mQuestionText = (TextView) v.findViewById(R.id.question_text);
        mParticipantLabel = (TextView) v.findViewById(R.id.participant_label);
        mQuestionIndex = (TextView) v.findViewById(R.id.question_index);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        setParticipantLabel();
        updateQuestionCountLabel();
        if (mQuestion.belongsToGrid()) {
        	setGridLabelText(mQuestionText);
        } else {
        	setQuestionText(mQuestionText);
        }
        mQuestionText.setTypeface(mInstrument.getTypeFace(getActivity().getApplicationContext()));
        createQuestionFragment();
        ActivityCompat.invalidateOptionsMenu(getActivity());
        getActivity().getActionBar().setTitle(mInstrument.getTitle());
   
        return v;
    }

	/*
     * Place the question fragment for the corresponding mQuestion
     * on the view in the question_container.
     */
	protected void createQuestionFragment() {        
        if (mQuestion == null)
        		loadOrCreateQuestion();
        if (mSurvey == null)
        		loadOrCreateSurvey();
        if (mQuestion.belongsToGrid()) {
        	createGridFragment();
        } else {
			FragmentManager fm = getChildFragmentManager();       
	        mQuestionFragment = (QuestionFragment) QuestionFragmentFactory.createQuestionFragment(mQuestion, mSurvey);
	        switchOutFragments(fm);
        } 
	}
	
	private void createGridFragment() {
		if (mQuestion.getQuestionType() == QuestionType.SELECT_ONE) {
			mQuestionFragment = new SingleSelectGridFragment();
        } else {
        	mQuestionFragment = new MultipleSelectGridFragment();
        }
    	Bundle bundle = new Bundle();
    	bundle.putLong(GridFragment.EXTRA_GRID_ID, mQuestion.getGrid().getRemoteId());
    	bundle.putLong(GridFragment.EXTRA_SURVEY_ID, mSurvey.getId());
    	mQuestionFragment.setArguments(bundle);
    	FragmentManager fm = getChildFragmentManager();
    	switchOutFragments(fm);
	}

	private void switchOutFragments(FragmentManager fm) {
		if (fm.findFragmentById(R.id.question_container) == null) {
            fm.beginTransaction()
                .add(R.id.question_container, mQuestionFragment)
                .commit();
        } else {
            fm.beginTransaction()
                .replace(R.id.question_container, mQuestionFragment)
                .commit();    
        }
    	
    	mSurvey.setLastQuestion(mQuestion);
        mSurvey.save();
        removeTextFocus();
	}
	
	private void setGridLabelText(TextView view) {
		view.append(styleTextWithHtml(mGrid.getText()));
	}
	
	/*
	 * This will remove the focus of the input as the survey is
	 * traversed.  If this is not called, then it will be possible
	 * for someone to change the answer to a question that they are
	 * not currently viewing.
	 */
	private void removeTextFocus() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
	}
    
    /*
     * If a question has a skip pattern, then read the response
     * when pressing the "next" button.  If the index of the response
     * is able to have a skip pattern, then set the next question to
     * the question indicated by the skip pattern.  "Other" responses
     * cannot have skip patterns, and the question is just set to the
     * next question in the sequence.
     */
    private Question getNextQuestion(int questionIndex) {
        setSkippedForReview();
    	Question nextQuestion = null;      
        if (mQuestion.hasSkipPattern() && mSurvey.getResponseByQuestion(mQuestion) != null) {
        	try {
                int responseIndex = Integer.parseInt(mSurvey.getResponseByQuestion(mQuestion).getText());                
                if (mQuestion.hasMultiSkipPattern()) {
            		addQuestionsToSkip(responseIndex);
                }
                nextQuestion = getNextQuestionForSkipPattern(questionIndex, responseIndex);   
            } catch (NumberFormatException nfe) {
                nextQuestion = getNextQuestionWhenNumberFormatException(questionIndex);
            }
        } else if (mQuestion.hasMultiSkipPattern() && mSurvey.getResponseByQuestion(mQuestion) != null) {
        	try {
        		int responseIndex = Integer.parseInt(mSurvey.getResponseByQuestion(mQuestion).getText());
        		addQuestionsToSkip(responseIndex);
        		nextQuestion = nextQuestionHelper(questionIndex);  
        	} catch (NumberFormatException nfe) {
        		nextQuestion = getNextQuestionWhenNumberFormatException(questionIndex);
        	}
        } else {
        	nextQuestion = nextQuestionHelper(questionIndex);
        }        
        Question question = getNextUnskippedQuestion(nextQuestion);
        return question;
    }

	private Question getNextQuestionWhenNumberFormatException(int questionIndex) {
		Question nextQuestion;
		nextQuestion = nextQuestionHelper(questionIndex);
		Log.wtf(TAG, "Received a non-numeric skip response index for " + mQuestion.getQuestionIdentifier());
		return nextQuestion;
	}

	private void addQuestionsToSkip(int responseIndex) {
		if (responseIndex < mQuestion.options().size()) {
			Option selectedOption = mQuestion.options().get(responseIndex);
			for (Question skipQuestion: selectedOption.questionsToSkip()){
				mQuestionsToSkip.add(skipQuestion.getNumberInInstrument());
			}
		}
	}

	private Question getNextQuestionForSkipPattern(int questionIndex, int responseIndex) {
		Question nextQuestion;
		if (responseIndex < mQuestion.options().size() && mQuestion.options().get(responseIndex).getNextQuestion() != null) {
		    nextQuestion = mQuestion.options().get(responseIndex).getNextQuestion();
		    mQuestionNumber = nextQuestion.getNumberInInstrument() - 1;
		} else {
		    nextQuestion = nextQuestionHelper(questionIndex);
		}
		return nextQuestion;
	}
    
    private Question getNextUnskippedQuestion(Question nextQuestion) {
    	if (mQuestionsToSkip.contains(nextQuestion.getNumberInInstrument())) {
        	if (isLastQuestion()) {
            	finishSurvey();
            } else {
            	nextQuestion = nextQuestionHelper(nextQuestion.getNumberInInstrument() - 1); 
            	nextQuestion = getNextUnskippedQuestion(nextQuestion);
            }
        }
    	return nextQuestion;
    }
    
    private Question nextQuestionHelper(int index) {
    	mQuestionNumber = index + 1;
    	return mInstrument.questions().get(mQuestionNumber);
    }
    
    private void clearSkipsForCurrentQuestion() {
    	if (!mQuestionsToSkip.isEmpty()) {
	    	for (Question question : mQuestion.questionsToSkip()) {
	    		mQuestionsToSkip.remove(Integer.valueOf(question.getNumberInInstrument()));
	    	} 
    	}
    } 
    
    private void setSkippedForReview() {
    	if (nullResponse() || emptyResponse() || skippedResponse() ) {   		
    		if (pictureResponseQuestion()) {
				if (mQuestionFragment.getResponsePhoto().getPicturePath() == null) {
					mSkippedQuestions.add(mQuestion.getNumberInInstrument());
				} else {
					mSkippedQuestions.remove(Integer.valueOf(mQuestion.getNumberInInstrument()));
				}	
    		} else {
    			mSkippedQuestions.add(mQuestion.getNumberInInstrument());
    		}
    	} else {
    		mSkippedQuestions.remove(Integer.valueOf(mQuestion.getNumberInInstrument()));
    	}
    }
    
    private boolean emptyResponse() {
    	return (mSurvey.getResponseByQuestion(mQuestion).getText().length() == 0 && 
    			mSurvey.getResponseByQuestion(mQuestion).getSpecialResponse().length() == 0);
    }
    
    private boolean nullResponse() {
    	return mSurvey.getResponseByQuestion(mQuestion) == null;
    }
    
    private boolean skippedResponse() {
    	return mSurvey.getResponseByQuestion(mQuestion).getSpecialResponse() == Response.SKIP;
    }
    
    private boolean pictureResponseQuestion() {
    	 return (mQuestion.getQuestionType() == QuestionType.FRONT_PICTURE || mQuestion.getQuestionType() == QuestionType.REAR_PICTURE);
    }
    
    private boolean isInstructionsQuestion(Question question) {
    	return (question.getQuestionType() == QuestionType.INSTRUCTIONS);
    }
    
    private void removeInstructionsQuestions() {
    	for (Iterator<Integer> iterator = mSkippedQuestions.iterator(); iterator.hasNext();) {
    	    Integer next = iterator.next();
			Question question = Question.findByNumberInInstrument(next, mInstrument.getId());
    	    if (isInstructionsQuestion(question)) {
    	        iterator.remove();
    	    }
    	}
    }
    
    /*
     * Switch out the next question with a fragment from the
     * QuestionFragmentFactory.  Increment the question to
     * the next question.
     */
    public void moveToNextQuestion() {
    	int questionsInInstrument = mInstrument.questions().size();
        if (mQuestionNumber < questionsInInstrument - 1) {    
            if (mQuestion.firstInGrid()) {
            	mQuestionNumber = mGrid.questions().get(mGrid.questions().size() - 1).getNumberInInstrument() - 1;
            	mPreviousQuestions.add(mQuestion.getNumberInInstrument() - 1);
            	mQuestion = mGrid.questions().get(mGrid.questions().size() - 1);
            } else {
            	mPreviousQuestions.add(mQuestionNumber);
            }
            mQuestion = getNextQuestion(mQuestionNumber);  
            if (mQuestion.getGrid() != null) {
            	mGrid = mQuestion.getGrid();
            }
        	createQuestionFragment();
            if (!setQuestionText(mQuestionText)) {
                setSpecialResponse(Response.LOGICAL_SKIP);
                moveToNextQuestion();
            }  
        } else if (isLastQuestion() && !setQuestionText(mQuestionText)) {
        	finishSurvey();
        }
        updateQuestionCountLabel();
    }
    
    /*
     * Move to previous question.  Takes into account if
     * this question is following up another question.  If
     * this question is not a follow up question, just move
     * to the previous question in the sequence.
     */
    public void moveToPreviousQuestion() {
        if (mQuestionNumber > 0 && mQuestionNumber < mInstrument.questions().size()) {
            mQuestionNumber = mPreviousQuestions.remove(mPreviousQuestions.size() - 1);
            mQuestion = mInstrument.questions().get(mQuestionNumber);
            if (mQuestion.getGrid() != null) {
            	mGrid = mQuestion.getGrid();
            }
            createQuestionFragment();
            if (!setQuestionText(mQuestionText)) {
                moveToPreviousQuestion();
            }
            if (mSurvey.getResponseByQuestion(mQuestion) != null && 
            		mSurvey.getResponseByQuestion(mQuestion).getText() != "" ) {
        		clearSkipsForCurrentQuestion();
            }
        }
        
        updateQuestionCountLabel();
    }

    /*
    * Destroy this activity, and save the survey and mark it as
    * complete.  Send to server if network is available.
    */
    public void finishSurvey() {
    	setSkippedForReview(); //To check if last question is skipped
    	removeInstructionsQuestions();
    	if (AppUtil.getAdminSettingsInstance().getRecordSurveyLocation()) {
    		setSurveyLocation();
    	}
    	if (!mSkippedQuestions.isEmpty()) {
    		ArrayList<String> skippedQuestions = new ArrayList<String>();
    		for (Integer questionNumber : mSkippedQuestions) {
    			Question question = Question.findByNumberInInstrument(questionNumber, mInstrument.getId());
    			skippedQuestions.add(question.getQuestionIdentifier());
    		}
    		Intent i = new Intent(getActivity(), ReviewPageActivity.class);
    		Bundle b = new Bundle();
    		b.putStringArrayList(ReviewPageFragment.EXTRA_REVIEW_QUESTION_IDS, skippedQuestions);
    		b.putLong(ReviewPageFragment.EXTRA_REVIEW_SURVEY_ID, mSurvey.getId());
    		i.putExtras(b);
    		startActivityForResult(i, REVIEW_CODE);
    	} 
    	else {
    		getActivity().finish();
	        mSurvey.setAsComplete();
	        mSurvey.save();
	        new SendResponsesTask(getActivity()).execute();
    	}
    }
       
    public boolean isFirstQuestion() {
        return mQuestionNumber == 0;
    }
    
    public boolean isLastQuestion() {
        return mInstrument.questions().size() == mQuestionNumber + 1;
    }

    public boolean hasValidResponse() {
        if (mQuestionFragment != null && mQuestionFragment.getResponse() != null) {
            return mQuestionFragment.getResponse().isValid();
        } else {
            return true;
        }
    }
    
    private void setSurveyLocation() {
    	mSurvey.setLatitude(mLocationServiceManager.getLatitude());
    	mSurvey.setLongitude(mLocationServiceManager.getLongitude());
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
    private boolean setQuestionText(TextView text) {        
    	appendInstructions(text);
        
        if (mQuestion.isFollowUpQuestion()) {
            String followUpText = mQuestion.getFollowingUpText(mSurvey, getActivity());
            
            if (followUpText == null) {
                return false;
            } else {
                text.append(styleTextWithHtml(followUpText));
            }
        } else {
            text.append(styleTextWithHtml(mQuestion.getText()));
        }
        return true;
    }
    
    /*
     * If this question has instructions, append and add new line
     */
    private void appendInstructions(TextView text) {
        if (mQuestion.getInstructions() != null) {
            text.setText(styleTextWithHtml(mQuestion.getInstructions() + "<br /><br />"));
        } else {
        	text.setText("");
        }
    }
    
    private Spanned styleTextWithHtml(String text) {
    	return Html.fromHtml(text);
    }
    
    /*
     * Save the special response field and clear the current
     * response if there is one.
     */
    private void setSpecialResponse(String response) {
        mQuestionFragment.saveSpecialResponse(response);
        if (isAdded()) {
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
    }
          
    private void setParticipantLabel() {
    	String surveyMetaData = mSurvey.getMetadata();
    	if (!TextUtils.isEmpty(surveyMetaData)) {
	    	try {
	    		JSONObject metadata = new JSONObject(surveyMetaData);
	    		if (metadata.has("survey_label")) {
	    			mParticipantLabel.setText(metadata.getString("survey_label"));
	    		}
	    	} catch (JSONException er) {
	    		Log.e(TAG, er.getMessage());
	    	}
    	}
    }
    
    private void updateQuestionCountLabel() {    	
        int numberQuestions = mInstrument.questions().size();
        
        mQuestionIndex.setText((mQuestionNumber + 1) + " " + getString(R.string.of) + " " + numberQuestions);        
        mProgressBar.setProgress((int) (100 * (mQuestionNumber + 1) / (float) numberQuestions));
        
        if (isAdded()) {
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
    }
	
	private boolean checkRules() {
	    return new RuleBuilder(getActivity())
            .addRule(new InstrumentSurveyLimitRule(mInstrument,
                    getActivity().getString(R.string.rule_failure_instrument_survey_limit)))
            .addRule(new InstrumentTimingRule(mInstrument, getResources().getConfiguration().locale,
                    getActivity().getString(R.string.rule_failure_survey_timing)))
            .addRule(new InstrumentSurveyLimitPerMinuteRule(mInstrument,
                    getActivity().getString(R.string.rule_instrument_survey_limit_per_minute)))
            .showToastOnFailure(true)
            .checkRules()
            .getResult();
	}
}
