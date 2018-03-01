package org.adaptlab.chpir.android.survey;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.crashlytics.android.Crashlytics;

import org.adaptlab.chpir.android.survey.location.LocationManager;
import org.adaptlab.chpir.android.survey.models.Display;
import org.adaptlab.chpir.android.survey.models.FollowUpQuestion;
import org.adaptlab.chpir.android.survey.models.Grid;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.MultipleSkip;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Score;
import org.adaptlab.chpir.android.survey.models.ScoreScheme;
import org.adaptlab.chpir.android.survey.models.Section;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.questionfragments.MultipleSelectGridFragment;
import org.adaptlab.chpir.android.survey.questionfragments.SingleSelectGridFragment;
import org.adaptlab.chpir.android.survey.roster.RosterActivity;
import org.adaptlab.chpir.android.survey.rules.InstrumentSurveyLimitPerMinuteRule;
import org.adaptlab.chpir.android.survey.rules.InstrumentSurveyLimitRule;
import org.adaptlab.chpir.android.survey.rules.InstrumentTimingRule;
import org.adaptlab.chpir.android.survey.rules.RuleBuilder;
import org.adaptlab.chpir.android.survey.tasks.SendResponsesTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.fabric.sdk.android.Fabric;

public class SurveyFragment extends Fragment implements NavigationView
        .OnNavigationItemSelectedListener {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey" +
            ".instrument_id";
    public final static String EXTRA_QUESTION_NUMBER = "org.adaptlab.chpir.android.survey" +
            ".question_number";
    public final static String EXTRA_SURVEY_ID = "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_PREVIOUS_QUESTION_IDS = "org.adaptlab.chpir.android.survey" +
            ".previous_questions";
    public final static String EXTRA_PARTICIPANT_METADATA = "org.adaptlab.chpir.android.survey" +
            ".metadata";
    public final static String EXTRA_QUESTIONS_TO_SKIP_IDS = "org.adaptlab.chpir.android.survey" +
            ".questions_to_skip_ids";
    public final static String EXTRA_SECTION_ID = "org.adaptlab.chpir.android.survey.section_id";
    public final static String EXTRA_AUTHORIZE_SURVEY = "org.adaptlab.chpir.android.survey" +
            ".authorize_boolean";
    public final static String EXTRA_DISPLAY_NUMBER = "org.adaptlab.chpir.android.survey" +
            ".display_number";
    private static final String TAG = "SurveyFragment";
    private static final int REVIEW_CODE = 100;
//    private static final int SECTION_CODE = 200;
    public static final int AUTHORIZE_CODE = 300;
    private static final Long REVIEW_PAGE_ID = -1L;
    private static final int ACCESS_FINE_LOCATION_CODE = 1;
    private NavigationView mNavigationView;
    //    private boolean mAllowFragmentCommit;
//    private QuestionFragment mQuestionFragment;
    private Question mQuestion;
    private LinearLayout mQuestionViewLayout;
    private Instrument mInstrument;
    private Survey mSurvey;
    private int mQuestionNumber;
    private int mQuestionCount;
    private String mMetadata;
    private Question mResumeQuestion = null;
    private Grid mGrid;
    private Section mSection;
    // mPreviousQuestions is a Stack, however Android does not allow you
    // to save a Stack to the savedInstanceState, so it is represented as
    // an Integer array.
    private ArrayList<Integer> mPreviousQuestions;
    private ArrayList<Integer> mQuestionsToSkip;
    private ArrayList<Section> mSections;
    private ArrayList<QuestionFragment> mQuestionFragments;
//    private List<Question> mQuestions;
    private ArrayList<Display> mDisplays;
    private HashMap<Question, Response> mResponses;
    private HashMap<Question, List<Option>> mOptions;
    private HashMap<Display, List<Question>> mDisplayQuestions;
    //    private TextView mQuestionText;
    private TextView mDisplayIndexLabel;
    private TextView mParticipantLabel;
    private ProgressBar mProgressBar;
    private GestureDetector mGestureDetector;
//    private ProgressDialog mProgressDialog;
    private Display mDisplay;
    private Display mSkipToDisplay;
    private int mDisplayNumber;
    private ArrayList<Integer> mPreviousDisplays;
    private String mQuestionSkipToIdentifier;
    private String mQuestionSkipStartIdentifier;
    private HashSet<String> mQuestionMultipleSkipIdentifierSet;
    private List<MultipleSkip> mMultipleSkipList;
    private LocationManager mLocationManager;
    private NestedScrollView mScrollView;

    //drawer vars
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mDrawerTitle;
    private String mTitle;
    private String[] mDisplayTitles;
    private String[] mSectionTitles;
    private boolean mNavDrawerSet = false;
    private boolean showSectionView = true;
    private boolean isActivityFinished = false;
    private boolean isScreenRotated = false;

//    public void loadOrCreateQuestion() {
//        mPreviousQuestions = new ArrayList<>();
//        mQuestionsToSkip = new ArrayList<>();
//        int questionNum = getActivity().getIntent().getIntExtra(EXTRA_QUESTION_NUMBER, -1);
//        if (questionNum == -1) {
//            mQuestion = mQuestions.get(0);
//            mQuestionNumber = 0;
//        } else if (questionNum >= mQuestions.size()) {
//            mQuestion = mQuestions.get(mQuestions.size() - 1);
//            mQuestionNumber = mQuestions.size() - 1;
//            for (int i = 0; i < mQuestionNumber; i++)
//                mPreviousQuestions.add(i);
//        } else {
//            mQuestion = mQuestions.get(questionNum);
//            mQuestionNumber = questionNum;
//            for (int i = 0; i < mQuestionNumber; i++)
//                mPreviousQuestions.add(i);
//        }
//        if (mQuestion.belongsToGrid()) {
//            mGrid = mQuestion.getGrid();
//        }
//    }

    public void refreshView() {
        AuthorizedActivity authority = (AuthorizedActivity) getActivity();
        if (authority.getAuthorize() && AppUtil.getAdminSettingsInstance() != null && AppUtil
                .getAdminSettingsInstance().getRequirePassword() && !AuthUtils.isSignedIn()) {
            authority.setAuthorize(false);
            Intent i = new Intent(getContext(), LoginActivity.class);
            getActivity().startActivityForResult(i, AUTHORIZE_CODE);
        } else {
            setParticipantLabel();
            updateDisplayCountLabel();
            createQuestionFragments();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REVIEW_CODE) {
            int displayNum = data.getExtras().getInt(EXTRA_DISPLAY_NUMBER);
            if (displayNum == Integer.MIN_VALUE) {
                checkForCriticalResponses();
            } else {
                mDisplay = mDisplays.get(displayNum);
                mDisplayNumber = displayNum;
                if (mDisplay != null) {
                    createQuestionFragments();
                } else {
                    checkForCriticalResponses();
                }
            }
        }
//        if (resultCode == Activity.RESULT_OK && requestCode == SECTION_CODE) {
//            Question previousQuestion = mQuestion;
//            int questionNum = data.getExtras().getInt(EXTRA_QUESTION_NUMBER);
//            Long instrumentId = data.getExtras().getLong(EXTRA_INSTRUMENT_ID);
//            Long surveyId = data.getExtras().getLong(EXTRA_SURVEY_ID);
//            ArrayList<Integer> previousQuestions = data.getExtras().getIntegerArrayList
//                    (EXTRA_PREVIOUS_QUESTION_IDS);
//            mQuestion = mQuestions.get(questionNum);
//            mQuestionNumber = questionNum;
//            mInstrument = Instrument.findByRemoteId(instrumentId);
//            mSurvey = Model.load(Survey.class, surveyId);
//            if (mQuestion.getSection() != null && mQuestion.getSection() == mSection)
//                showSectionView = false;
//            if (previousQuestions != null) mPreviousQuestions.addAll(previousQuestions);
//            if (previousQuestion == mQuestion) showSectionView = false;
//        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setHasOptionsMenu(true);
        if (AppUtil.getContext() == null) AppUtil.setContext(getActivity());
        boolean authority = getActivity().getIntent().getBooleanExtra(EXTRA_AUTHORIZE_SURVEY,
                false);
        if (authority) {
            AuthorizedActivity authorizedActivity = (AuthorizedActivity) getActivity();
            authorizedActivity.setAuthorize(true);
        }
        if (savedInstanceState != null) {
            mInstrument = Instrument.findByRemoteId(savedInstanceState.getLong
                    (EXTRA_INSTRUMENT_ID));
            if (!checkRules()) getActivity().finish();
            launchRosterSurvey();
            if (!mInstrument.isRoster()) {
                mSurvey = Survey.load(Survey.class, savedInstanceState.getLong(EXTRA_SURVEY_ID));
            }
            mDisplayNumber = savedInstanceState.getInt(EXTRA_DISPLAY_NUMBER);
        } else {
            Long instrumentId = getActivity().getIntent().getLongExtra(EXTRA_INSTRUMENT_ID, -1);
            mMetadata = getActivity().getIntent().getStringExtra(EXTRA_PARTICIPANT_METADATA);
            if (instrumentId == -1) return;
            mInstrument = Instrument.findByRemoteId(instrumentId);
            if (mInstrument == null) return;
            if (!checkRules()) getActivity().finish();
            launchRosterSurvey();
            if (!mInstrument.isRoster()) {
                loadOrCreateSurvey();
            }
            mDisplayNumber = mSurvey.getLastQuestion().getDisplay().getPosition() - 1;
        }
        mPreviousDisplays = new ArrayList<>();
        mQuestionFragments = new ArrayList<>();
        mQuestionMultipleSkipIdentifierSet = new HashSet<>();
        ProgressDialog progressDialog = ProgressDialog.show(getActivity(), getString(R.string
                .instrument_loading_progress_header), getString(R.string
                .background_process_progress_message));
        mDisplayQuestions = mInstrument.displayQuestions();
        mResponses = mSurvey.responsesMap();
        mOptions = mInstrument.optionsMap();
        mDisplays = (ArrayList<Display>) mInstrument.displays();
        mDisplay = mDisplays.get(mDisplayNumber);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        registerCrashlytics();

//        if (!mInstrument.isRoster()) {
//            mQuestionCount = mInstrument.getQuestionCount();
//            mQuestions = new ArrayList<>(mInstrument.getQuestionCount());
//            ProgressDialog progressDialog = ProgressDialog.show(getActivity(), getString(R
// .string.instrument_loading_progress_header), getString(R.string
// .background_process_progress_message));
//            mDisplayQuestions = mInstrument.displayQuestions();
//            mResponses = mSurvey.responsesMap();
//            mOptions = mInstrument.optionsMap();
//            if (progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.dismiss();
//            }
//        }

        if (AppUtil.getAdminSettingsInstance().getRecordSurveyLocation()) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission
                    .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest
                        .permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
            }
        }
    }

    private void registerCrashlytics() {
        if (AppUtil.PRODUCTION) {
            Fabric.with(getActivity(), new Crashlytics());
            Crashlytics.setString(getString(R.string.last_instrument), mInstrument.getTitle());
            Crashlytics.setString(getString(R.string.last_survey), mSurvey.getUUID());
            Crashlytics.setString(getString(R.string.last_question), mQuestion
                    .getNumberInInstrument() + "");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                    startLocationUpdates();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_survey, parent, false);
        mQuestionViewLayout = (LinearLayout) v.findViewById(R.id.question_component_layout);
        mParticipantLabel = (TextView) v.findViewById(R.id.participant_label);
        mDisplayIndexLabel = (TextView) v.findViewById(R.id.display_index_label);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        ActivityCompat.invalidateOptionsMenu(getActivity());
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(mInstrument.getTitle());
        LinearLayout swipeView = (LinearLayout) v.findViewById(R.id
                .linear_layout_for_question_index);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        swipeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
        mScrollView = v.findViewById(R.id.survey_fragment_scroll_view);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (mLocationManager == null) {
            mLocationManager = new LocationManager(getActivity());
            mLocationManager.startLocationUpdates();
        }
    }

    public LocationManager getLocationManager() {
        if (mLocationManager == null) startLocationUpdates();
        return mLocationManager;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_DISPLAY_NUMBER, mDisplayNumber);
        outState.putLong(EXTRA_INSTRUMENT_ID, mInstrument.getRemoteId());
        outState.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
        isScreenRotated = true;
    }

    @Override
    public void onStop() {
        if (mLocationManager != null) {
            mLocationManager.stopLocationUpdates();
        }
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_survey, menu);
        if (!mNavDrawerSet) {
            setupNavigationDrawer();
        }
        if (mNavigationView != null) {
            for (int i = 0; i < mDisplays.size(); i++) {
                mNavigationView.getMenu().getItem(i).setChecked(false);
            }
            mNavigationView.getMenu().getItem(mDisplayNumber).setChecked(true);
        }
    }

    private void setupNavigationDrawer() {
        setNavigationDrawerItems();
        mTitle = mDrawerTitle = mInstrument.getTitle();
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
//        mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
//        mDrawerList.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.drawer_list_item,
//                mDisplayTitles));
//        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mNavigationView = (NavigationView) getActivity().findViewById(R.id.navigation);
        final Menu menu = mNavigationView.getMenu();
        for (String oneTitle : mDisplayTitles) {
            menu.add(oneTitle);
        }
        mNavigationView.setNavigationItemSelectedListener(this);
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            public void onDrawerOpened(View drawerView) {
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(mDrawerTitle);
                }
                getActivity().invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(mTitle);
                }
                getActivity().invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mNavDrawerSet = true;
    }

    private void setNavigationDrawerItems() {
//        mSections = new ArrayList<>();
//        mSections.addAll(mInstrument.sections());
//        if (mInstrument.getDirectReviewNavigation()) {
//            Section reviewSection = Section.findByRemoteId(REVIEW_PAGE_ID);
//            if (reviewSection == null) {
//                reviewSection = new Section();
//                reviewSection.setRemoteId(REVIEW_PAGE_ID);
//                reviewSection.setTitle(getActivity().getString(R.string.review_section_title));
//                reviewSection.setInstrumentRemoteId(mInstrument.getRemoteId());
//                reviewSection.save();
//            }
//            if (!mSections.contains(reviewSection)) {
//                mSections.add(reviewSection);
//            }
//        }
//        mSectionTitles = new String[mSections.size()];
//        for (int i = 0; i < mSections.size(); i++) {
//            mSectionTitles[i] = mSections.get(i).getTitle();
//        }
        sortDisplayList();
        mDisplayTitles = new String[mDisplays.size()];
        for (int i = 0; i < mDisplays.size(); i++) {
            mDisplayTitles[i] = mDisplays.get(i).getTitle();
        }
    }

    private void sortDisplayList() {
        Collections.sort(mDisplays, new Comparator<Display>() {
            @Override
            public int compare(Display lhs, Display rhs) {
                return lhs.getPosition() < rhs.getPosition() ? -1 : 1;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_item_previous).setEnabled(mDisplayNumber != 0 ||
                !mPreviousDisplays.isEmpty());
        menu.findItem(R.id.menu_item_next).setVisible(mDisplayNumber != mDisplays.size() - 1)
                .setEnabled(true);
        menu.findItem(R.id.menu_item_finish).setVisible(mDisplayNumber == mDisplays.size() - 1)
                .setEnabled(true);

//        if (mQuestion != null) {
//            menu.findItem(R.id.menu_item_previous).setEnabled(!isFirstQuestion());
//            menu.findItem(R.id.menu_item_next).setVisible(!isLastQuestion()).setEnabled
// (hasValidResponse());
//            if (mQuestion.belongsToGrid()) {
//                menu.findItem(R.id.menu_item_skip).setVisible(false);
//                menu.findItem(R.id.menu_item_rf).setVisible(false);
//                menu.findItem(R.id.menu_item_na).setVisible(false);
//                menu.findItem(R.id.menu_item_dk).setVisible(false);
//            } else {
//                for (String key : mMenuItems.keySet()) {
//                    if (!mInstrument.getSpecialOptionStrings().contains(key)) {
//                        menu.findItem(mMenuItems.get(key)).setVisible(false).setEnabled(false);
//                    } else {
//                        if (key.equals(Response.SKIP)) {
//                            menu.findItem(mMenuItems.get(key)).setEnabled(hasValidResponse());
//                        }
//                    }
//                }
//            }
//            menu.findItem(R.id.menu_item_finish).setVisible(isLastQuestion()).setEnabled
// (hasValidResponse());
//            showSpecialResponseSelection(menu);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_item_previous:
                moveToPreviousDisplay();
                return true;
            case R.id.menu_item_next:
                moveToNextDisplay();
                return true;
            case R.id.menu_item_finish:
                finishSurvey();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void moveToPreviousDisplay() {
        mDrawerLayout.closeDrawer(mNavigationView);
        if (mDisplayNumber >= 0 && mDisplayNumber < mDisplays.size() && mPreviousDisplays.size()
                > 0) {
            mDisplayNumber = mPreviousDisplays.remove(mPreviousDisplays.size() - 1);
            mDisplay = mDisplays.get(mDisplayNumber);
        } else {
            mDisplayNumber -= 1;
            mDisplay = mDisplays.get(mDisplayNumber);
        }
        createQuestionFragments();
        hideQuestionInDisplay();
        hideMultipleQuestion();
        updateDisplayCountLabel();
    }

    private void moveToNextDisplay() {
        mDrawerLayout.closeDrawer(mNavigationView);
        mPreviousDisplays.add(mDisplayNumber);
        if (mSkipToDisplay == null) {
            mDisplayNumber += 1;
        } else {
            mDisplayNumber = mSkipToDisplay.getPosition() - 1;
            mSkipToDisplay = null;
        }
        if (mDisplayNumber < mDisplays.size()) {
            mDisplay = mDisplays.get(mDisplayNumber);
        }
        createQuestionFragments();
        hideQuestionInDisplay();
        hideMultipleQuestion();
        updateDisplayCountLabel();
    }

    private void moveToDisplay(int position) {
        mDrawerLayout.closeDrawer(mNavigationView);
        if (mDisplayNumber != position) {
            mPreviousDisplays.add(mDisplayNumber);
            mDisplayNumber = position;
            mDisplay = mDisplays.get(mDisplayNumber);
            createQuestionFragments();
            hideQuestionInDisplay();
            hideMultipleQuestion();
            updateDisplayCountLabel();
        }
    }

    private void hideQuestionInDisplay() {
        Question skipStartQuestion = Question.findByQuestionIdentifier
                (mQuestionSkipStartIdentifier);
        Question skipToQuestion = Question.findByQuestionIdentifier(mQuestionSkipToIdentifier);
        if (skipStartQuestion != null && skipToQuestion != null) {
            if (skipStartQuestion.getDisplay() == mDisplay && skipToQuestion.getDisplay() ==
                    mDisplay) {
                int curIndex = mDisplay.questions().indexOf(skipStartQuestion);
                int nextIndex = mDisplay.questions().indexOf(skipToQuestion);
                hideInBetweenQuestions(curIndex, nextIndex);
            } else if (skipStartQuestion.getDisplay() != mDisplay && skipToQuestion.getDisplay()
                    == mDisplay) {
                int nextIndex = mDisplay.questions().indexOf(skipToQuestion);
                hideInBetweenQuestions(-1, nextIndex);
            } else if (skipStartQuestion.getDisplay() == mDisplay && skipToQuestion.getDisplay()
                    != mDisplay) {
                int curIndex = mDisplay.questions().indexOf(skipStartQuestion);
                hideInBetweenQuestions(curIndex, -1);
            }
        }
    }

    protected void setNextQuestion(String currentQuestionIdentifier, String
            nextQuestionIdentifier) {
        mQuestionSkipStartIdentifier = currentQuestionIdentifier;
        mQuestionSkipToIdentifier = nextQuestionIdentifier;
        Question currentQuestion = Question.findByQuestionIdentifier(currentQuestionIdentifier);
        Question nextQuestion = Question.findByQuestionIdentifier(nextQuestionIdentifier);
        int currentIndex = mDisplay.questions().indexOf(currentQuestion);
        if (nextQuestion.getDisplay() == mDisplay) {
            int nextIndex = mDisplay.questions().indexOf(nextQuestion);
            hideInBetweenQuestions(currentIndex, nextIndex);
        } else {
            hideInBetweenQuestions(currentIndex, -1);
            mSkipToDisplay = nextQuestion.getDisplay();
            // TODO: 2/1/18 Implement hiding questions in next display that appear before the
            // next question skip to
            // DONE
        }
    }

    private void hideInBetweenQuestions(int currentIndex, int nextIndex) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        for (int k = 0; k < mQuestionFragments.size(); k++) {
            if (k <= currentIndex) {
                ft.show(mQuestionFragments.get(k));
            } else if (k > currentIndex && k < nextIndex) {
                ft.hide(mQuestionFragments.get(k));
            } else if (k > currentIndex && nextIndex == -1) {
                ft.hide(mQuestionFragments.get(k));
            } else {
                ft.show(mQuestionFragments.get(k));
            }
        }
        ft.commit();
    }

//    private void hideMultipleQuestion(){
//        FragmentManager fm = getChildFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        for (int k = 0; k < mQuestionFragments.size(); k++) {
//            if (mQuestionFragments.get(k).getQuestion()
// !=null&&mQuestionMultipleSkipIdentifierSet.contains(mQuestionFragments.get(k).getQuestion()
// .getQuestionIdentifier())) {
//                ft.hide(mQuestionFragments.get(k));
//                Log.i("HIDE",mQuestionFragments.get(k).toString()+"");
//            }
//            else{
//                ft.show(mQuestionFragments.get(k));
//            }
//        }
//        ft.commit();
//    }

    private void hideMultipleQuestion() {
        if (mMultipleSkipList != null && mMultipleSkipList.size() > 0) {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            HashSet<Integer> showFragmentSet = new HashSet<>();
            for (MultipleSkip questionToSkip : mMultipleSkipList) {
                Question question = Question.findByQuestionIdentifier(questionToSkip
                        .getSkipQuestionIdentifier());
                int index = mDisplay.questions().indexOf(question);
                showFragmentSet.add(index);
                if (question.getDisplay() == mDisplay) {
                    ft.hide(mQuestionFragments.get(index));
                }
            }
            for (int k = 0; k < mQuestionFragments.size(); k++) {
                if (!showFragmentSet.contains(k)) {
                    ft.show(mQuestionFragments.get(k));
                }
            }
            ft.commit();
        }
    }

    protected void reAnimateFollowUpFragment(Question currentQuestion) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        for (FollowUpQuestion question : currentQuestion.toFollowUpOnQuestions()) {
            int index = mDisplay.questions().indexOf(question.getFollowUpQuestion());
            if (index > -1 && index <= mDisplay.questions().size() - 1) {
                QuestionFragment qf = mQuestionFragments.get(index);
                ft.detach(qf);
                ft.attach(qf);
                ft.commit();
            }
        }
    }

    protected void setMultipleSkipQuestions(Option selectedOption, Question currentQuestion) {
        List<MultipleSkip> multipleSkips = new Select().from(MultipleSkip.class)
                .where("OptionIdentifier = ? AND QuestionIdentifier = ? AND RemoteInstrumentId = ?",
                        selectedOption.getIdentifier(), currentQuestion.getQuestionIdentifier(),
                        mInstrument.getRemoteId())
                .execute();
        mQuestionMultipleSkipIdentifierSet = new HashSet<>();
        mMultipleSkipList = multipleSkips;
        Log.i("multipleSkips", multipleSkips.toString() + "");
        // Questions to skip
        for (MultipleSkip questionToSkip : multipleSkips) {
            Question question = Question.findByQuestionIdentifier(questionToSkip
                    .getSkipQuestionIdentifier());
            mQuestionMultipleSkipIdentifierSet.add(question.getQuestionIdentifier());
            // TODO: 2/1/18 Implement
            //DONE
        }
        hideMultipleQuestion();
    }

//    public boolean isFirstQuestion() {
//        return mQuestionNumber == 0;
//    }

//    public boolean isLastQuestion() {
//        if (mQuestion.belongsToGrid()) {
//            Question lastGridQuestion = mGrid.questions().get(mGrid.questions().size() - 1);
//            return mQuestionCount == lastGridQuestion.getNumberInInstrument();
//        } else {
//            return mQuestionCount == mQuestionNumber + 1;
//        }
//    }

//    public boolean hasValidResponse() {
//        for (QuestionFragment fragment : mQuestionFragments) {
//            if (!(mQuestionFragment != null && mQuestionFragment.getResponse() != null) ||
//                    !fragment.getResponse().isValid())
//                return false;
//        }
//        return true;
////        return !(mQuestionFragment != null && mQuestionFragment.getResponse() != null) ||
////                mQuestionFragment.getResponse().isValid();
//    }

    /*
     * Give a visual indication when a special response is selected
     */
//    public void showSpecialResponseSelection(Menu menu) {
//        if (mQuestionFragment != null && mQuestionFragment.getSpecialResponse() != null && menu
//                != null) {
//            if (mQuestionFragment.getSpecialResponse().equals(Response.SKIP)) {
//                menu.findItem(R.id.menu_item_skip).setIcon(R.drawable.ic_menu_item_sk_selected);
//            } else if (mQuestionFragment.getSpecialResponse().equals(Response.RF)) {
//                menu.findItem(R.id.menu_item_rf).setIcon(R.drawable.ic_menu_item_rf_selected);
//            } else if (mQuestionFragment.getSpecialResponse().equals(Response.NA)) {
//                menu.findItem(R.id.menu_item_na).setIcon(R.drawable.ic_menu_item_na_selected);
//            } else if (mQuestionFragment.getSpecialResponse().equals(Response.DK)) {
//                menu.findItem(R.id.menu_item_dk).setIcon(R.drawable.ic_menu_item_dk_selected);
//            }
//        }
//    }

    private boolean checkRules() {
        return new RuleBuilder(getActivity())
                .addRule(new InstrumentSurveyLimitRule(mInstrument,
                        getActivity().getString(R.string.rule_failure_instrument_survey_limit)))
                .addRule(new InstrumentTimingRule(mInstrument, getResources().getConfiguration()
                        .locale,
                        getActivity().getString(R.string.rule_failure_survey_timing)))
                .addRule(new InstrumentSurveyLimitPerMinuteRule(mInstrument,
                        getActivity().getString(R.string.rule_instrument_survey_limit_per_minute)))
                .showToastOnFailure(true)
                .checkRules()
                .getResult();
    }

    private void launchRosterSurvey() {
        if (mInstrument.isRoster()) {
            Intent i = new Intent(getActivity(), RosterActivity.class);
            i.putExtra(RosterActivity.EXTRA_INSTRUMENT_ID, mInstrument.getRemoteId());
            i.putExtra(RosterActivity.EXTRA_PARTICIPANT_METADATA, mMetadata);
            getActivity().startActivity(i);
            getActivity().finish();
        }
    }

    public void loadOrCreateSurvey() {
        Long surveyId = getActivity().getIntent().getLongExtra(EXTRA_SURVEY_ID, -1);
        if (surveyId == -1) {
            mSurvey = new Survey();
            mSurvey.setInstrumentRemoteId(mInstrument.getRemoteId());
            mSurvey.setMetadata(mMetadata);
            mSurvey.setProjectId(mInstrument.getProjectId());
            mSurvey.setLanguage(Instrument.getDeviceLanguage());
            mSurvey.save();
        } else {
            mSurvey = Model.load(Survey.class, surveyId);
        }
    }

//    private void proceedToNextQuestion() {
//        if (isLastQuestion()) {
//            finishSurvey();
//        } else {
//            moveToNextQuestion();
//        }
//    }

//    private void unSkipAndMoveToNextQuestion() {
//        if (mQuestionFragment != null && mQuestionFragment.getSpecialResponse().equals(Response
//                .SKIP)) {
//            mQuestionFragment.setSpecialResponse("");
//        }
//        proceedToNextQuestion();
//    }

    protected void createQuestionFragments() {
        if (!isActivityFinished) {
            // Hide previous fragments
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            for (Fragment fragment : mQuestionFragments) {
                fragmentTransaction.hide(fragment);
            }
            mQuestionFragments.clear();

            if (mNavigationView != null) {
                for (int i = 0; i < mDisplays.size(); i++) {
                    mNavigationView.getMenu().getItem(i).setChecked(false);
                }
                mNavigationView.getMenu().getItem(mDisplayNumber).setChecked(true);
            }

            List<Question> displayQuestions = mDisplayQuestions.get(mDisplay);
            if (mDisplay.getMode().equals(Display.DisplayMode.TABLE.toString())) {
                // Show table
                QuestionFragment questionFragment;
                if (displayQuestions.get(0).getQuestionType() == Question.QuestionType.SELECT_ONE) {
                    questionFragment = new SingleSelectGridFragment();
                } else {
                    questionFragment = new MultipleSelectGridFragment();
                }

                FrameLayout framelayout = new FrameLayout(getContext());
                framelayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                        .MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT));

                framelayout.setId(new BigDecimal(displayQuestions.get(0).getRemoteId())
                        .intValueExact());
                mQuestionViewLayout.addView(framelayout);

                Bundle bundle = new Bundle();
                bundle.putLong(GridFragment.EXTRA_DISPLAY_ID, mDisplay.getRemoteId());
                bundle.putLong(GridFragment.EXTRA_SURVEY_ID, mSurvey.getId());
                questionFragment.setArguments(bundle);
                fragmentTransaction.add(framelayout.getId(), questionFragment);
                mQuestionFragments.add(questionFragment);
            } else {
                for (Question question : displayQuestions) {
                    // Add large offset to avoid id conflicts
                    int frameLayoutId = new BigDecimal(question.getRemoteId()).intValueExact() + 1000000;
                    FrameLayout frameLayout = getActivity().findViewById(frameLayoutId);
                    if (frameLayout == null) {
                        frameLayout = new FrameLayout(getContext());
                        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                                .MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT));
                        frameLayout.setId(frameLayoutId);
                        mQuestionViewLayout.addView(frameLayout);
                    }

                    String qfTag = mSurvey.getId().toString() + "-" + question.getId().toString();
                    QuestionFragment questionFragment = (QuestionFragment) fm.findFragmentByTag(qfTag);
                    if (questionFragment == null || isScreenRotated) {
                        Bundle bundle = new Bundle();
                        bundle.putString("QuestionIdentifier", question.getQuestionIdentifier());
                        questionFragment = (QuestionFragment) QuestionFragmentFactory
                                .createQuestionFragment(question);
                        questionFragment.setArguments(bundle);
                        fragmentTransaction.add(frameLayout.getId(), questionFragment, qfTag);
                    } else {
                        fragmentTransaction.show(questionFragment);
                    }
                    mQuestionFragments.add(questionFragment);
                }
            }
            fragmentTransaction.commit();
        }
    }

//    private void createGridFragment() {
//        if (mQuestion.getQuestionType() == QuestionType.SELECT_ONE) {
//            mQuestionFragment = new SingleSelectGridFragment();
//        } else {
//            mQuestionFragment = new MultipleSelectGridFragment();
//        }
//        Bundle bundle = new Bundle();
//        bundle.putLong(GridFragment.EXTRA_GRID_ID, mQuestion.getGrid().getRemoteId());
//        bundle.putLong(GridFragment.EXTRA_SURVEY_ID, mSurvey.getId());
//        mQuestionFragment.setArguments(bundle);
//        FragmentManager fm = getChildFragmentManager();
//        switchOutFragments(fm);
//    }

//    private void switchOutFragments(final FragmentManager fm) {
//        if (mAllowFragmentCommit) {
//            commitFragmentTransaction(fm);
//        }
//        else {
//            new Handler().post(new Runnable() {
//                public void run() {
//                    commitFragmentTransaction(fm);
//                }
//            });
//        }
//        mSurvey.setLastQuestion(mQuestion);
//        mSurvey.save();
//        removeTextFocus();
//    }

//    private void commitFragmentTransaction(FragmentManager fm) {
//        if (fm.findFragmentById(R.id.question_container) == null) {
//            fm.beginTransaction().add(R.id.question_container, mQuestionFragment).commit();
//        } else {
//            fm.beginTransaction().replace(R.id.question_container, mQuestionFragment).commit();
//        }
//    }

    public Display getDisplay() {
        return mDisplay;
    }

    public Question getQuestion(String identifier) {
        for (Question question : mDisplayQuestions.get(mDisplay)) {
            if (question.getQuestionIdentifier().equals(identifier)) {
                return question;
            }
        }
        return null;
    }

    protected NestedScrollView getScrollView() {
        return mScrollView;
    }

    protected List<Question> getQuestions(Display display) {
        return mDisplayQuestions.get(display);
    }

    public Survey getSurvey() {
        return mSurvey;
    }

    public HashMap<Question, Response> getResponses() {
        return mResponses;
    }

    public HashMap<Question, List<Option>> getOptions() {
        return mOptions;
    }

//    private void setGridLabelText(TextView view) {
//        view.setText(styleTextWithHtml(mGrid.getText()));
//    }

    /*
     * If a question has a skip pattern, then read the response
     * when pressing the "next" button.  If the index of the response
     * is able to have a skip pattern, then set the next question to
     * the question indicated by the skip pattern.  "Other" responses
     * cannot have skip patterns, and the question is just set to the
     * next question in the sequence.
     */
//    private Question getNextQuestion(int questionIndex) {
//        Question nextQuestion = null;
//        Option responseOption = null;
//        Response response = mResponses.get(mQuestion);
//        if (response == null) {
//            nextQuestion = nextQuestionHelper(questionIndex);
//        } else {
//            if (mQuestion.hasCompleteSurveyOption()) {
//                if (mQuestion.hasOptions() && !TextUtils.isEmpty(response.getText())) {
//                    int index = Integer.parseInt(response.getText());
//                    if (index < mQuestion.defaultOptions().size())
//                        responseOption = mQuestion.defaultOptions().get(index);
//                }
//                if (responseOption != null && responseOption.getCompleteSurvey()) {
//                    scoreAndCompleteSurvey();
//                    return null;
//                }
//            } else if (!TextUtils.isEmpty(response.getSpecialResponse())) {
//                Option specialOption = mQuestion.specialOptionByText(response.getSpecialResponse
//                        ().trim());
//                if (specialOption != null && specialOption.getNextQuestion() != null) {
//                    nextQuestion = specialOption.getNextQuestion();
//                } else {
//                    nextQuestion = nextQuestionHelper(questionIndex);
//                }
//            } else if (Question.AnyResponseQuestions.contains(mQuestion.getQuestionType())) {
//                Option anyResponseOption = mQuestion.anyResponseOption();
//                if (!TextUtils.isEmpty(response.getText()) && anyResponseOption != null &&
//                        anyResponseOption.getNextQuestion() != null) {
//                    nextQuestion = anyResponseOption.getNextQuestion();
//                } else {
//                    nextQuestion = nextQuestionHelper(questionIndex);
//                }
//            } else if (mQuestion.hasSkipPattern()) {
//                try {
//                    int responseIndex = Integer.parseInt(response.getText());
//                    addQuestionsToSkip(responseIndex);
//                    nextQuestion = getNextQuestionForSkipPattern(questionIndex, responseIndex);
//                } catch (NumberFormatException nfe) {
//                    nextQuestion = getNextQuestionWhenNumberFormatException(questionIndex);
//                }
//            } else {
//                nextQuestion = nextQuestionHelper(questionIndex);
//            }
//        }
//        return getNextUnSkippedQuestion(nextQuestion);
//    }

//    private Question getNextQuestionWhenNumberFormatException(int questionIndex) {
//        Question nextQuestion;
//        nextQuestion = nextQuestionHelper(questionIndex);
//        Log.wtf(TAG, "Received a non-numeric skip response index for " + mQuestion
//                .getQuestionIdentifier());
//        return nextQuestion;
//    }

//    private void addQuestionsToSkip(int responseIndex) {
//        if (responseIndex < mQuestion.defaultOptions().size()) {
//            Option selectedOption = mQuestion.defaultOptions().get(responseIndex);
//            for (Question skipQuestion : selectedOption.questionsToSkip()) {
//                mQuestionsToSkip.add(skipQuestion.getNumberInInstrument());
//            }
//        }
//    }

//    private Question getNextQuestionForSkipPattern(int questionIndex, int responseIndex) {
//        Question nextQuestion;
//        if (responseIndex < mQuestion.defaultOptions().size() && mQuestion.defaultOptions().get
//                (responseIndex).getNextQuestion() != null) {
//            nextQuestion = mQuestion.defaultOptions().get(responseIndex).getNextQuestion();
//            mQuestionNumber = nextQuestion.getNumberInInstrument() - 1;
//        } else {
//            nextQuestion = nextQuestionHelper(questionIndex);
//        }
//        return nextQuestion;
//    }

//    private Question getNextUnSkippedQuestion(Question nextQuestion) {
//        if (mQuestionsToSkip.contains(nextQuestion.getNumberInInstrument())) {
//            if (isLastQuestion()) {
//                finishSurvey();
//            } else {
//                nextQuestion = nextQuestionHelper(nextQuestion.getNumberInInstrument() - 1);
//                nextQuestion = getNextUnSkippedQuestion(nextQuestion);
//            }
//        }
//        return nextQuestion;
//    }

//    private Question nextQuestionHelper(int index) {
//        mQuestionNumber = index + 1;
//        if (mQuestionNumber >= mQuestions.size())
//            mQuestionNumber = mQuestions.size() - 1;
//        return mQuestions.get(mQuestionNumber);
//    }

//    private void clearSkipsForCurrentQuestion() {
//        if (!mQuestionsToSkip.isEmpty()) {
//            for (Question question : mQuestion.questionsToSkip()) {
//                mQuestionsToSkip.remove(Integer.valueOf(question.getNumberInInstrument()));
//            }
//        }
//    }

    /*
     * Switch out the next question with a fragment from the
     * QuestionFragmentFactory.  Increment the question to
     * the next question.
     */
//    public void moveToNextQuestion() {
//        if (mQuestion.belongsToGrid()) {
//            mPreviousQuestions.add(mQuestion.getNumberInInstrument() - 1);
//            Question lastQuestion = mGrid.questions().get(mGrid.questions().size() - 1);
//            mQuestion = nextQuestionHelper(lastQuestion.getNumberInInstrument());
//            createQuestionFragments();
//        } else {
//            if (mQuestionNumber < mQuestionCount - 1) {
//                mPreviousQuestions.add(mQuestionNumber);
//                mQuestion = getNextQuestion(mQuestionNumber);
//                createQuestionFragments();
////                if (mQuestion != null && !setQuestionText(mQuestionText)) {
////                    setSpecialResponse(Response.LOGICAL_SKIP);
////                    moveToNextQuestion();
////                }
////            } else if (isLastQuestion() && !setQuestionText(mQuestionText)) {
////                finishSurvey();
//            }
//            if (mQuestion != null) mQuestionNumber = mQuestion.getNumberInInstrument() - 1;
//        }
//        updateDisplayCountLabel();
//    }

    /*
     * Move to previous question.  Takes into account if
     * this question is following up another question.  If
     * this question is not a follow up question, just move
     * to the previous question in the sequence.
     */
//    public void moveToPreviousQuestion() {
//        if (mQuestionNumber > 0 && mQuestionNumber < mQuestionCount) {
//            mQuestionNumber = mPreviousQuestions.remove(mPreviousQuestions.size() - 1);
//            mQuestion = mQuestions.get(mQuestionNumber);
//            showSectionView = false;
//            createQuestionFragments();
//            if (mQuestion.belongsToGrid()) {
//                mGrid = mQuestion.getGrid();
//            } else {
////                if (!setQuestionText(mQuestionText)) {
////                    moveToPreviousQuestion();
////                }
//            }
//            if (mResponses.get(mQuestion) != null &&
//                    !mResponses.get(mQuestion).getText().isEmpty()) {
//                clearSkipsForCurrentQuestion();
//            }
//        }
//
//        updateDisplayCountLabel();
//    }

    /*
    * Destroy this activity, and save the survey and mark it as
    * complete.  Send to server if network is available.
    */
    public void finishSurvey() {
        if (AppUtil.getAdminSettingsInstance().getRecordSurveyLocation()) {
            setSurveyLocation();
        }
        if (mSurvey.emptyResponses().size() > 0) {
            goToReviewPage();
        } else {
            checkForCriticalResponses();
        }
    }

    private void checkForCriticalResponses() {
        final List<String> criticalResponses = getCriticalResponses();
        if (criticalResponses.size() > 0) {
            String[] criticalQuestions = new String[criticalResponses.size()];
            for (int k = 0; k < criticalResponses.size(); k++) {
                criticalQuestions[k] = Question.findByQuestionIdentifier(criticalResponses.get(k)
                ).getNumberInInstrument()
                        + ": " + criticalResponses.get(k);
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View content = LayoutInflater.from(getActivity()).inflate(R.layout
                    .critical_responses_dialog, null);
            ListView listView = (ListView) content.findViewById(R.id.critical_list);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout
                    .simple_selectable_list_item, criticalQuestions));

            builder.setTitle(R.string.critical_message_title)
                    .setMessage(mInstrument.getCriticalMessage())
                    .setView(content)
                    .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            mSurvey.setCriticalResponses(true);
                            scoreAndCompleteSurvey();
                        }
                    })
                    .setNegativeButton(R.string.review, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mQuestion = Question.findByQuestionIdentifier(criticalResponses.get(0));
                            mQuestionNumber = mQuestion.getNumberInInstrument() - 1;
                            refreshView();
                        }
                    });
            final AlertDialog criticalDialog = builder.create();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mQuestion = Question.findByQuestionIdentifier(criticalResponses.get(position));
                    mQuestionNumber = mQuestion.getNumberInInstrument() - 1;
                    refreshView();
                    criticalDialog.dismiss();
                }
            });
            criticalDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    criticalDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setBackgroundColor(getResources().getColor(R.color.green));
                    criticalDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setBackgroundColor(getResources().getColor(R.color.red));
                }
            });
            criticalDialog.show();
        } else {
            mSurvey.setCriticalResponses(false);
            scoreAndCompleteSurvey();
        }
    }

    private void scoreAndCompleteSurvey() {
        isActivityFinished = true;
        if (mInstrument.isScorable()) {
            new ScoreSurveyTask().execute(mSurvey);
        } else {
            completeAndSendSurvey(mSurvey);
            getActivity().finish();
        }
    }

    private void completeAndSendSurvey(Survey survey) {
        survey.setAsComplete(true);
        survey.save();
        if (survey.readyToSend()) {
            new SendResponsesTask(getActivity()).execute();
        }
    }

    private List<String> getCriticalResponses() {
        List<String> criticalQuestions = new ArrayList<String>();
        if (mInstrument.criticalQuestions().size() > 0) {
            for (Question question : mInstrument.criticalQuestions()) {
                Response response = mResponses.get(question);
                Set<String> optionSet = new HashSet<String>();
                Set<String> responseSet = new HashSet<String>();
                if (response != null) {
                    for (Option option : question.criticalOptions()) {
                        optionSet.add(Integer.toString(question.defaultOptions().indexOf(option)));
                    }
                    if (!TextUtils.isEmpty(response.getText())) {
                        responseSet.addAll(Arrays.asList(response.getText().split(",")));
                    }
                    optionSet.retainAll(responseSet);
                }
                if (optionSet.size() > 0) {
                    criticalQuestions.add(question.getQuestionIdentifier());
                }
            }
        }
        return criticalQuestions;
    }

    private void goToReviewPage() {
        Intent i = new Intent(getActivity(), ReviewPageActivity.class);
        Bundle b = new Bundle();
        b.putLong(ReviewPageFragment.EXTRA_REVIEW_SURVEY_ID, mSurvey.getId());
        i.putExtras(b);
        startActivityForResult(i, REVIEW_CODE);
    }

    private void setSurveyLocation() {
        if (mLocationManager == null) {
            startLocationUpdates();
        } else {
            mSurvey.setLatitude(mLocationManager.getLatitude());
            mSurvey.setLongitude(mLocationManager.getLongitude());
        }
    }

    private Spanned styleTextWithHtml(String text) {
        return Html.fromHtml(text);
    }

    /*
     * Save the special response field and clear the current
     * response if there is one.
     */
//    private void setSpecialResponse(String response) {
//        mQuestionFragment.setSpecialResponse(response);
//        if (isAdded()) {
//            ActivityCompat.invalidateOptionsMenu(getActivity());
//        }
//    }

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

    private void updateDisplayCountLabel() {
//        if (mQuestion != null) {
//            if (mQuestion.belongsToGrid()) {
//                Question first = mGrid.questions().get(0);
//                Question last = mGrid.questions().get(mGrid.questions().size() - 1);
//                mDisplayIndexLabel.setText((first.getNumberInInstrument()) + " - " + (
//                        last.getNumberInInstrument()) + " " + getString(R.string.of) + " " +
//                        mQuestionCount);
//            } else {
//                mDisplayIndexLabel.setText((mQuestionNumber + 1) + " " + getString(R.string.of)
// + "
// " +
//                        mQuestionCount);
//            }
//            mProgressBar.setProgress((int) (100 * (mQuestionNumber + 1) / (float)
// mQuestionCount));
//
//            if (isAdded()) {
//                ActivityCompat.invalidateOptionsMenu(getActivity());
//            }
//        }
        if (mDisplay != null) {
            mDisplayIndexLabel.setText(getString(R.string.screen) + " " + (mDisplayNumber + 1) + " "
                    + getString(R.string.of) + " " + mDisplays.size() + " (" + getString(R.string
                    .questions) + " " + mDisplay.questions().get(0).getNumberInInstrument() + " -" +
                    " " + mDisplay.questions().get(mDisplay.questions().size() - 1)
                    .getNumberInInstrument() + ")");
            mProgressBar.setProgress((int) (100 * (mDisplayNumber + 1) / (float) mDisplays.size()));
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = 0;
        for (String oneTitle : mDisplayTitles) {
            if (oneTitle.equals(item.getTitle())) {
                break;
            }
            id++;
        }
        moveToDisplay(id);
        return true;
    }

    private class ScoreSurveyTask extends AsyncTask<Survey, Void, Survey> {
        @Override
        protected Survey doInBackground(Survey... params) {
            Survey survey = params[0];
            for (ScoreScheme scheme : survey.getInstrument().scoreSchemes()) {
                Score score = Score.findBySurveyAndScheme(survey, scheme);
                if (score == null) {
                    score = new Score();
                    score.setSurvey(survey);
                    score.setScoreScheme(scheme);
                    score.setSurveyIdentifier(survey.identifier(AppUtil.getContext()));
                    score.save();
                }
                score.score();
            }
            return survey;
        }

        @Override
        protected void onPostExecute(Survey survey) {
            completeAndSendSurvey(survey);
            getActivity().finish();
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //moveToDisplay(position);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private float MINIMUM_FLING_DISTANCE = 100;

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float
                velocityY) {
            float horizontalDifference = event2.getX() - event1.getX();
            float absoluteHorizontalDifference = Math.abs(horizontalDifference);
            if (absoluteHorizontalDifference > MINIMUM_FLING_DISTANCE) {
                if (horizontalDifference > 0) {
//                    moveToPreviousQuestion();
                } else {
//                    unSkipAndMoveToNextQuestion();
                }
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }
    }

}