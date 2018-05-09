package org.adaptlab.chpir.android.survey;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.MultipleSkip;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Score;
import org.adaptlab.chpir.android.survey.models.ScoreScheme;
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
import java.util.Locale;
import java.util.Map;
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
    public static final int AUTHORIZE_CODE = 300;
    private static final int ACCESS_FINE_LOCATION_CODE = 1;
    private NavigationView mNavigationView;
    private LinearLayout mQuestionViewLayout;
    private Instrument mInstrument;
    private Survey mSurvey;
    private HashSet<Integer> mHiddenDisplayNumberSet;
    private String mMetadata;
    private ArrayList<QuestionFragment> mQuestionFragments;
    private ArrayList<Display> mDisplays;
    private HashMap<Question, Response> mResponses;
    private HashMap<Question, List<Option>> mOptions;
    private HashMap<Display, List<Question>> mDisplayQuestions;
    private HashMap<String, List<Question>> mQuestionsToSkipMap;
    private HashMap<Long, List<Option>> mSpecialOptions;
    private HashSet<Question> mQuestionsToSkipSet;
    private TextView mDisplayIndexLabel;
    private TextView mParticipantLabel;
    private ProgressBar mProgressBar;
    private Display mDisplay;
    private int mDisplayNumber;
    private ArrayList<Integer> mPreviousDisplays;
    private LocationManager mLocationManager;
    private NestedScrollView mScrollView;
    private TextView mDisplayTitle;

    //drawer vars
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mDrawerTitle;
    private String mTitle;
    private String[] mDisplayTitles;
    private boolean mNavDrawerSet = false;
    private boolean isActivityFinished = false;
    private boolean isScreenRotated = false;

    public void refreshView() {
        AuthorizedActivity authority = (AuthorizedActivity) getActivity();
        if (authority.getAuthorize() && AppUtil.getAdminSettingsInstance() != null && AppUtil
                .getAdminSettingsInstance().getRequirePassword() && !AuthUtils.isSignedIn()) {
            authority.setAuthorize(false);
            Intent i = new Intent(getContext(), LoginActivity.class);
            getActivity().startActivityForResult(i, AUTHORIZE_CODE);
        } else {
            setParticipantLabel();
            updateDisplayLabels();
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
            if (!checkRules()) {
                finishActivity();
            }
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
            if (!checkRules()) {
                finishActivity();
            }
            launchRosterSurvey();
            if (!mInstrument.isRoster()) {
                loadOrCreateSurvey();
            }
            mDisplayNumber = mSurvey.getLastQuestion().getDisplay().getPosition() - 1;
        }
        mDisplays = (ArrayList<Display>) mInstrument.displays();
        mDisplay = mDisplays.get(mDisplayNumber);
        mPreviousDisplays = new ArrayList<>();
        mQuestionFragments = new ArrayList<>();
        mQuestionsToSkipMap = new HashMap<>();
        mQuestionsToSkipSet = new HashSet<>();
        mSpecialOptions = new HashMap<>();
        mDisplayQuestions = new HashMap<>();
        mResponses = new HashMap<>();
        mOptions = new HashMap<>();
        new InstrumentDataTask().execute(mInstrument, mSurvey);
        registerCrashlytics();
    }

    private void requestLocationUpdates() {
        if (AppUtil.getAdminSettingsInstance().getRecordSurveyLocation()) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission
                    .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest
                        .permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
            }
        }
    }

    private void finishActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().finishAfterTransition();
        } else {
            getActivity().finish();
        }
    }

    private void registerCrashlytics() {
        if (AppUtil.PRODUCTION) {
            Fabric.with(getActivity(), new Crashlytics());
            Crashlytics.setString(getString(R.string.last_instrument), mInstrument.getTitle());
            Crashlytics.setString(getString(R.string.last_survey), mSurvey.getUUID());
            Crashlytics.setString(getString(R.string.last_display), mDisplay.getTitle());
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
        mDisplayTitle = v.findViewById(R.id.display_title);
        mQuestionViewLayout = (LinearLayout) v.findViewById(R.id.question_component_layout);
        mParticipantLabel = (TextView) v.findViewById(R.id.participant_label);
        mDisplayIndexLabel = (TextView) v.findViewById(R.id.display_index_label);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        ActivityCompat.invalidateOptionsMenu(getActivity());
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(mInstrument.getTitle());
        mScrollView = v.findViewById(R.id.survey_fragment_scroll_view);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        requestLocationUpdates();
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
        if (mDisplayQuestions.size() > 0) {
            refreshView();
        }
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
        setSelectedDrawerItemChecked();
    }

    private void setupNavigationDrawer() {
        updateHiddenDisplayNumberSet();
        setNavigationDrawerItems();
        mTitle = mDrawerTitle = mInstrument.getTitle();
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mNavigationView = (NavigationView) getActivity().findViewById(R.id.navigation);
        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i < mDisplayTitles.length; i++) {
            if (!mHiddenDisplayNumberSet.contains(i)) {
                menu.add(mDisplayTitles[i]);
            }
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
                updateHiddenDisplayNumberSet();
                Menu menu = mNavigationView.getMenu();
                menu.clear();
                for (int i = 0; i < mDisplayTitles.length; i++) {
                    if (!mHiddenDisplayNumberSet.contains(i)) {
                        menu.add(mDisplayTitles[i]);
                    }
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
                if (lhs.getPosition() == rhs.getPosition()) {
                    return 0;
                } else {
                    return lhs.getPosition() < rhs.getPosition() ? -1 : 1;
                }
            }
        });
    }

    private void updateHiddenDisplayNumberSet() {
        mHiddenDisplayNumberSet = new HashSet<>();
        for (Map.Entry<Display, List<Question>> curEntry : mDisplayQuestions.entrySet()) {
            boolean isSkip = true;
            for (Question curQuestion : curEntry.getValue()) {
                if (!mQuestionsToSkipSet.contains(curQuestion)) {
                    isSkip = false;
                    break;
                }
            }
            if (isSkip) {
                mHiddenDisplayNumberSet.add(mDisplays.indexOf(curEntry.getKey()));
            }
        }
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
        showDisplayQuestions();
    }

    private void showDisplayQuestions() {
        createQuestionFragments();
        hideQuestionsInDisplay();
        updateDisplayLabels();
    }

    private void moveToNextDisplay() {
        mDrawerLayout.closeDrawer(mNavigationView);
        mPreviousDisplays.add(mDisplayNumber);
        for (int i = mDisplayNumber + 1; i < mDisplays.size(); i++) {
            boolean skipDisplay = true;
            for (Question curQuestion : mDisplayQuestions.get(mDisplays.get(i))) {
                if (!mQuestionsToSkipSet.contains(curQuestion)) {
                    skipDisplay = false;
                    break;
                }
            }
            if (!skipDisplay) {
                mDisplayNumber = i;
                mDisplay = mDisplays.get(mDisplayNumber);
                break;
            } else if (i == mDisplays.size() - 1) {
                goToReviewPage();
            }
        }
        showDisplayQuestions();
    }

    private void moveToDisplay(int position) {
        mDrawerLayout.closeDrawer(mNavigationView);
        if (mDisplayNumber != position) {
            mPreviousDisplays.add(mDisplayNumber);
            mDisplayNumber = position;
            mDisplay = mDisplays.get(mDisplayNumber);
            showDisplayQuestions();
        }
    }

    private void updateQuestionsToSkipMap(String questionIdentifier, List<Question>
            questionsToSkip) {
        if (questionsToSkip == null || questionsToSkip.size() == 0) {
            if (mQuestionsToSkipMap.containsKey(questionIdentifier)) {
                mQuestionsToSkipMap.remove(questionIdentifier);
            }
        } else {
            mQuestionsToSkipMap.put(questionIdentifier, questionsToSkip);
        }
    }

    private void updateQuestionsToSkipSet() {
        mQuestionsToSkipSet = new HashSet<>();
        for (HashMap.Entry<String, List<Question>> curPair : mQuestionsToSkipMap.entrySet()) {
            mQuestionsToSkipSet.addAll(curPair.getValue());
        }
    }

    private void unSetSkipQuestionResponse() {
        for (Question curSkip : mQuestionsToSkipSet) {
            if (curSkip != null) {
                Response curResponse = mResponses.get(curSkip);
                if (curResponse != null) {
                    curResponse.setResponse("");
                    curResponse.setSpecialResponse("");
                    curResponse.setOtherResponse("");
                    curResponse.setDeviceUser(AuthUtils.getCurrentUser());
                    curResponse.save();
                }
            }
        }
    }

    private void hideQuestionsInDisplay() {
        updateQuestionsToSkipSet();
        if (!mDisplay.getMode().equals(Display.DisplayMode.TABLE.toString())) {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            HashSet<Integer> hideSet = new HashSet<>();
            for (Question curSkip : mQuestionsToSkipSet) {
                int index = mDisplay.questions().indexOf(curSkip);
                if (index != -1) {
                    hideSet.add(index);
                    ft.hide(mQuestionFragments.get(index));
                }
            }
            for (int i = 0; i < mQuestionFragments.size(); i++) {
                if (!hideSet.contains(i)) {
                    ft.show(mQuestionFragments.get(i));
                }
            }
            ft.commit();
        }
    }

    protected void setNextQuestion(String currentQuestionIdentifier, String
            nextQuestionIdentifier, String questionIdentifier) {
        List<Question> skipList = new ArrayList<>();
        boolean skipStart = false;
        for (Question curQuestion : getQuestions()) {
            if (skipStart) skipList.add(curQuestion);
            if (curQuestion.getQuestionIdentifier().equals(currentQuestionIdentifier)) {
                skipStart = true;
            }
            if (curQuestion.getQuestionIdentifier().equals(nextQuestionIdentifier)) {
                break;
            }
        }
        updateQuestionsToSkipMap(questionIdentifier + "/skipTo", skipList);
        hideQuestionsInDisplay();
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
        List<Question> skipList = new ArrayList<>();
        if (selectedOption != null) {
            List<MultipleSkip> multipleSkips = new Select().from(MultipleSkip.class)
                    .where("OptionIdentifier = ? AND QuestionIdentifier = ? AND " +
                                    "RemoteInstrumentId = ?",
                            selectedOption.getIdentifier(), currentQuestion.getQuestionIdentifier(),
                            mInstrument.getRemoteId())
                    .execute();
            for (MultipleSkip questionToSkip : multipleSkips) {
                Question question = Question.findByQuestionIdentifier(questionToSkip
                        .getSkipQuestionIdentifier());
                skipList.add(question);
            }
        }
        updateQuestionsToSkipMap(currentQuestion.getQuestionIdentifier() + "/multi", skipList);
        hideQuestionsInDisplay();
    }

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity
                        ()).toBundle());
                getActivity().finishAfterTransition();
            } else {
                getActivity().startActivity(i);
                getActivity().finish();
            }
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

    private void setSelectedDrawerItemChecked() {
        if (mNavigationView != null) {
            int index = mDisplayNumber;
            for (int num : mHiddenDisplayNumberSet) {
                if (num < mDisplayNumber) {
                    index--;
                }
            }
            for (int i = 0; i < mDisplays.size() - mHiddenDisplayNumberSet.size(); i++) {
                mNavigationView.getMenu().getItem(i).setChecked(false);
            }
            if (index > -1 && index < mNavigationView.getMenu().size()) {
                mNavigationView.getMenu().getItem(index).setChecked(true);
            }
        }
    }

    protected void createQuestionFragments() {
        if (!isActivityFinished) {
            // Hide previous fragments
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            for (Fragment fragment : mQuestionFragments) {
                fragmentTransaction.hide(fragment);
            }
            fragmentTransaction.commitNow();

            // Add/show new fragments
            fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            mQuestionFragments.clear();
            setSelectedDrawerItemChecked();
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
                        .intValueExact() + 1000000);
                mQuestionViewLayout.addView(framelayout);

                ArrayList<String> questionsToSkip = new ArrayList<>();
                for (Question curSkip : mQuestionsToSkipSet) {
                    if (curSkip != null) questionsToSkip.add(curSkip.getQuestionIdentifier());
                }

                Bundle bundle = new Bundle();
                bundle.putStringArrayList(GridFragment.EXTRA_SKIPPED_QUESTION_ID_LIST,
                        questionsToSkip);
                bundle.putLong(GridFragment.EXTRA_DISPLAY_ID, mDisplay.getRemoteId());
                bundle.putLong(GridFragment.EXTRA_SURVEY_ID, mSurvey.getId());
                questionFragment.setArguments(bundle);
                fragmentTransaction.add(framelayout.getId(), questionFragment);
                mQuestionFragments.add(questionFragment);
            } else {
                for (Question question : getQuestions(mDisplay)) {
                    // Add large offset to avoid id conflicts
                    int frameLayoutId = new BigDecimal(question.getRemoteId()).intValueExact() +
                            1000000;
                    FrameLayout frameLayout = getActivity().findViewById(frameLayoutId);
                    if (frameLayout == null) {
                        frameLayout = new FrameLayout(getContext());
                        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup
                                .LayoutParams
                                .MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT));
                        frameLayout.setId(frameLayoutId);
                        mQuestionViewLayout.addView(frameLayout);
                    }

                    String qfTag = mSurvey.getId().toString() + "-" + question.getId().toString();
                    QuestionFragment questionFragment = (QuestionFragment) getChildFragmentManager().findFragmentByTag
                            (qfTag);
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

    public HashSet<Question> getQuestionsToSkipSet() {
        return mQuestionsToSkipSet;
    }

    protected NestedScrollView getScrollView() {
        return mScrollView;
    }

    protected List<Question> getQuestions(Display display) {
        return mDisplayQuestions.get(display);
    }

    protected List<Question> getQuestions() {
        List<Question> questions = new ArrayList<>();
        for(Map.Entry<Display, List<Question>> entry : mDisplayQuestions.entrySet()) {
            questions.addAll(entry.getValue());
        }
        return questions;
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

    public HashMap<Long, List<Option>> getSpecialOptions() {
        return mSpecialOptions;
    }

    /*
    * Destroy this activity, and save the survey and mark it as
    * complete.  Send to server if network is available.
    */
    public void finishSurvey() {
        unSetSkipQuestionResponse();
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
                            refreshView();
                        }
                    });
            final AlertDialog criticalDialog = builder.create();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            finishActivity();
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
        ArrayList<String> questionsToSkip = new ArrayList<>();
        for (Question curSkip : mQuestionsToSkipSet) {
            if (curSkip != null) questionsToSkip.add(curSkip.getQuestionIdentifier());
        }
        Intent i = new Intent(getActivity(), ReviewPageActivity.class);
        Bundle b = new Bundle();
        b.putLong(ReviewPageFragment.EXTRA_REVIEW_SURVEY_ID, mSurvey.getId());
        b.putStringArrayList(ReviewPageFragment.EXTRA_SKIPPED_QUESTION_ID_LIST, questionsToSkip);
        i.putExtras(b);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(i, REVIEW_CODE, ActivityOptions.makeSceneTransitionAnimation(getActivity
                    ()).toBundle());
        } else {
            startActivityForResult(i, REVIEW_CODE);
        }
    }

    private void setSurveyLocation() {
        if (mLocationManager == null) {
            startLocationUpdates();
        } else {
            mSurvey.setLatitude(mLocationManager.getLatitude());
            mSurvey.setLongitude(mLocationManager.getLongitude());
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

    private void updateDisplayLabels() {
        if (mDisplay != null) {
            // Screen title
            if (!mDisplay.getMode().equals(Display.DisplayMode.SINGLE.toString())) {
                mDisplayTitle.setText(String.format(Locale.getDefault(), "%s %s%d %s %d%s", mDisplay.getTitle(), "(", mDisplay.questions().get(0).getNumberInInstrument(), "-", mDisplay.questions().get(mDisplay.questions().size() - 1).getNumberInInstrument(), ")"));
            } else {
                mDisplayTitle.setText(mDisplay.getTitle());
            }
            // Progress text
            mDisplayIndexLabel.setText(String.format(Locale.getDefault(), "%s %d %s %d %s%d %s" +
                    " %d%s", getString(R.string.screen), mDisplayNumber + 1, getString(R.string
                    .of), mDisplays.size(), "(", mDisplay
                    .questions().get(0).getNumberInInstrument(), "-", mDisplay.questions().get
                    (mDisplay.questions().size() - 1).getNumberInInstrument(), ")"));
            // Progress bar
            mProgressBar.setProgress((int) (100 * (mDisplayNumber + 1) / (float) mDisplays.size()));
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = 0;
        for (String oneTitle : mDisplayTitles) {
            if (oneTitle.equals(item.getTitle().toString())) {
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
            finishActivity();
        }
    }

    private class InstrumentDataTask extends AsyncTask<Object, Void, InstrumentDataWrapper> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string
                    .instrument_loading_progress_header), getString(R.string
                    .background_process_progress_message));
        }

        @Override
        protected InstrumentDataWrapper doInBackground(Object... params) {
            InstrumentDataWrapper instrumentData = new InstrumentDataWrapper();
            instrumentData.displayQuestions = ((Instrument) params[0]).displayQuestions();
            instrumentData.responses = ((Survey) params[1]).responsesMap();
            instrumentData.options = ((Instrument) params[0]).optionsMap();
            instrumentData.specialOptions = ((Instrument) params[0]).specialOptionsMap();
            return instrumentData;
        }

        @Override
        protected void onPostExecute(InstrumentDataWrapper instrumentData) {
            mDisplayQuestions = instrumentData.displayQuestions;
            mResponses = instrumentData.responses;
            mOptions = instrumentData.options;
            mSpecialOptions = instrumentData.specialOptions;
            refreshView();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private class InstrumentDataWrapper {
        public HashMap<Question, Response> responses;
        public HashMap<Question, List<Option>> options;
        public HashMap<Display, List<Question>> displayQuestions;
        public HashMap<Long, List<Option>> specialOptions;
    }
}