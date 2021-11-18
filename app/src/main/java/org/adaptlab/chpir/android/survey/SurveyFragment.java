package org.adaptlab.chpir.android.survey;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.activeandroid.Model;

import org.adaptlab.chpir.android.survey.location.LocationManager;
import org.adaptlab.chpir.android.survey.models.ConditionSkip;
import org.adaptlab.chpir.android.survey.models.CriticalResponse;
import org.adaptlab.chpir.android.survey.models.Display;
import org.adaptlab.chpir.android.survey.models.DisplayInstruction;
import org.adaptlab.chpir.android.survey.models.FollowUpQuestion;
import org.adaptlab.chpir.android.survey.models.Instruction;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.LoopQuestion;
import org.adaptlab.chpir.android.survey.models.MultipleSkip;
import org.adaptlab.chpir.android.survey.models.NextQuestion;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.OptionSet;
import org.adaptlab.chpir.android.survey.models.OptionSetTranslation;
import org.adaptlab.chpir.android.survey.models.OptionTranslation;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Score;
import org.adaptlab.chpir.android.survey.models.ScoreScheme;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.roster.RosterActivity;
import org.adaptlab.chpir.android.survey.rules.InstrumentSurveyLimitPerMinuteRule;
import org.adaptlab.chpir.android.survey.rules.InstrumentSurveyLimitRule;
import org.adaptlab.chpir.android.survey.rules.InstrumentTimingRule;
import org.adaptlab.chpir.android.survey.rules.RuleBuilder;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.AuthUtils;
import org.adaptlab.chpir.android.survey.utils.LocaleManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import static org.adaptlab.chpir.android.survey.utils.FormatUtils.isEmpty;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public class SurveyFragment extends Fragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey" +
            ".instrument_id";
    public final static String EXTRA_QUESTION_NUMBER = "org.adaptlab.chpir.android.survey" +
            ".question_number";
    public final static String EXTRA_SURVEY_ID = "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_PARTICIPANT_METADATA = "org.adaptlab.chpir.android.survey" +
            ".metadata";
    public final static String EXTRA_SECTION_ID = "org.adaptlab.chpir.android.survey.section_id";
    public final static String EXTRA_AUTHORIZE_SURVEY = "org.adaptlab.chpir.android.survey" +
            ".authorize_boolean";
    public final static String EXTRA_DISPLAY_NUMBER = "org.adaptlab.chpir.android.survey" +
            ".display_number";
    public static final int AUTHORIZE_CODE = 300;
    private static final String TAG = "SurveyFragment";
    private static final int REVIEW_CODE = 100;
    private static final int ACCESS_FINE_LOCATION_CODE = 1;

    private HashMap<String, Response> mResponses;
    private HashMap<Question, List<Option>> mOptions;
    private HashMap<Long, List<Question>> mDisplayQuestions;
    private HashMap<String, List<String>> mQuestionsToSkipMap;
    private HashMap<Long, List<Option>> mSpecialOptions;
    private HashMap<Display, List<DisplayInstruction>> mDisplayInstructions;
    private LongSparseArray<OptionSet> mOptionSets;
    private LongSparseArray<Instruction> mInstructions;
    private HashMap<String, Question> mQuestions;
    private HashMap<String, List<NextQuestion>> mNextQuestions;
    private HashMap<String, List<ConditionSkip>> mConditionSkips;
    private HashMap<String, List<MultipleSkip>> mMultipleSkips;
    private HashMap<String, List<FollowUpQuestion>> mFollowUpQuestions;
    private HashMap<String, List<CriticalResponse>> mCriticalResponses;
    private HashMap<String, List<LoopQuestion>> mLoopQuestions;
    private HashMap<Long, List<OptionSetTranslation>> mOptionSetTranslation;
    private LinkedHashMap<String, List<String>> mExpandableListData;
    private List<String> mExpandableListTitle;
    private HashSet<String> mQuestionsToSkipSet;
    private ArrayList<Integer> mPreviousDisplays;
    private ArrayList<Display> mDisplays;
    private TextView mDisplayIndexLabel;
    private TextView mParticipantLabel;
    private ProgressBar mProgressBar;
    private Display mDisplay;
    private Instrument mInstrument;
    private Survey mSurvey;
    private String mMetadata;
    private LocationManager mLocationManager;
    private NestedScrollView mScrollView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ExpandableListView mExpandableListView;
    private DisplayFragment mDisplayFragment;
    private MenuItem mProgressView;
    private int mDisplayNumber;
    private boolean mNavDrawerSet = false;
    private boolean isLoadingDisplay = false;
    private boolean showProgressBar = true;

    public void refreshView() {
        AuthorizedActivity authority = (AuthorizedActivity) getActivity();
        if (authority != null && authority.getAuthorize() && AppUtil.getAdminSettingsInstance() != null && AppUtil
                .getAdminSettingsInstance().getRequirePassword() && !AuthUtils.isSignedIn()) {
            authority.setAuthorize(false);
            Intent i = new Intent(getContext(), LoginActivity.class);
            getActivity().startActivityForResult(i, AUTHORIZE_CODE);
        } else {
            refreshUIComponents();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REVIEW_CODE) {
            int displayNum = 0;
            if (data.getExtras() != null) {
                displayNum = data.getExtras().getInt(EXTRA_DISPLAY_NUMBER);
            }
            if (displayNum == Integer.MIN_VALUE) {
                showCriticalResponses();
            } else {
                mDisplay = mDisplays.get(displayNum);
                mDisplayNumber = displayNum;
                refreshUIComponents();
            }
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // TODO: 11/9/18 Why do this?
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
            if (mSurvey != null && mSurvey.getLastQuestion() != null &&
                    mSurvey.getLastQuestion().getDisplay() != null) {
                mDisplayNumber = mSurvey.getLastQuestion().getDisplay().getPosition() - 1;
            } else {
                mDisplayNumber = 0;
            }
        }
        mDisplays = (ArrayList<Display>) mInstrument.displays();
        if (mDisplays == null || mDisplays.size() == 0) return;
        mDisplay = mDisplays.get(mDisplayNumber);
        getSkipData();
        init();
        new InstrumentDataTask().execute(mInstrument, mSurvey);
        registerCrashlytics();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_survey, parent, false);
        mParticipantLabel = v.findViewById(R.id.participant_label);
        mDisplayIndexLabel = v.findViewById(R.id.display_index_label);
        mProgressBar = v.findViewById(R.id.progress_bar);
        updateActionBarTitle(mInstrument.getTitle());
        mScrollView = v.findViewById(R.id.survey_fragment_scroll_view);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        requestLocationUpdates();
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_DISPLAY_NUMBER, mDisplayNumber);
        outState.putLong(EXTRA_INSTRUMENT_ID, mInstrument.getRemoteId());
        outState.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
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
        setLanguageSelection(menu);
        mProgressView = menu.findItem(R.id.menu_item_progress);
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
        menu.findItem(R.id.menu_item_progress).setVisible(showProgressBar);
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

    private void updateActionBarTitle(String title) {
        if (getActivity() == null) return;
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
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

    private void finishActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().finishAfterTransition();
        } else {
            getActivity().finish();
        }
    }

    private void launchRosterSurvey() {
        if (mInstrument.isRoster()) {
            Intent i = new Intent(getActivity(), RosterActivity.class);
            i.putExtra(RosterActivity.EXTRA_INSTRUMENT_ID, mInstrument.getRemoteId());
            i.putExtra(RosterActivity.EXTRA_PARTICIPANT_METADATA, mMetadata);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().startActivity(i, ActivityOptions.makeSceneTransitionAnimation
                        (getActivity
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
            mSurvey.setLanguage(AppUtil.getDeviceLanguage());
            mSurvey.save();
        } else {
            mSurvey = Model.load(Survey.class, surveyId);
        }
    }

    private void getSkipData() {
        mQuestionsToSkipSet = new HashSet<>();
        mQuestionsToSkipMap = new HashMap<>();
        if (mSurvey.getSkippedQuestions() != null) {
            mQuestionsToSkipSet = new HashSet<>(Arrays.asList(
                    mSurvey.getSkippedQuestions().split(Response.LIST_DELIMITER)));
        }
        if (mSurvey.getSkipMaps() != null) {
            try {
                JSONObject jsonObject = new JSONObject(mSurvey.getSkipMaps());
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String skipString = jsonObject.getString(key);
                    String[] skipArray = skipString.split(Response.LIST_DELIMITER);
                    mQuestionsToSkipMap.put(key, Arrays.asList(skipArray));
                }
            } catch (JSONException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Exception: ", e);
            }
        }
    }

    private void init() {
        mPreviousDisplays = new ArrayList<>();
        mSpecialOptions = new HashMap<>();
        mDisplayQuestions = new HashMap<>();
        mResponses = new HashMap<>();
        mOptions = new HashMap<>();
        mOptionSets = new LongSparseArray<>();
        mInstructions = new LongSparseArray<>();
        mNextQuestions = new HashMap<>();
        mConditionSkips = new HashMap<>();
        mMultipleSkips = new HashMap<>();
        mFollowUpQuestions = new HashMap<>();
        mCriticalResponses = new HashMap<>();
        mLoopQuestions = new HashMap<>();
        mOptionSetTranslation = new HashMap<>();
    }

    private void registerCrashlytics() {
        if (AppUtil.PRODUCTION) {
//            Fabric.with(getActivity(), new Crashlytics());
//            Crashlytics.setString(getString(R.string.last_instrument), mInstrument.getTitle());
//            Crashlytics.setString(getString(R.string.last_survey), mSurvey.getUUID());
//            Crashlytics.setString(getString(R.string.last_display), mDisplay.getTitle());
        }
    }

    private void startLocationUpdates() {
        if (mLocationManager == null) {
            mLocationManager = new LocationManager(getActivity());
            mLocationManager.startLocationUpdates();
        }
    }

    private void createDisplayView() {
        if (getActivity() == null) return;
        isLoadingDisplay = true;
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mDisplayFragment = DisplayFragment.newInstance();
        fragmentTransaction.replace(R.id.question_component_layout, mDisplayFragment);
        fragmentTransaction.commit();
    }

    public LocationManager getLocationManager() {
        if (mLocationManager == null) startLocationUpdates();
        return mLocationManager;
    }

    private LinkedHashMap<String, List<String>> getListData() {
        LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();
        for (int i = 0; i < mDisplays.size(); i++) {
            List<String> displayTitles = map.get(mDisplays.get(i).getSectionTitle());
            if (displayTitles == null) displayTitles = new ArrayList<>();
            displayTitles.add(mDisplays.get(i).getTitle());
            map.put(mDisplays.get(i).getSectionTitle(), displayTitles);
        }
        return map;
    }

    private void setDrawerItems() {
        ExpandableListAdapter mExpandableListAdapter = new DisplayTitlesListAdapter(getContext(),
                mExpandableListTitle, mExpandableListData);
        mExpandableListView.setAdapter(mExpandableListAdapter);
        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });

        mExpandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });

        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String selectedItem = ((List) (mExpandableListData.get(mExpandableListTitle.get(groupPosition)))).get(childPosition).toString();
                int index = 0;
                for (Display display : mDisplays) {
                    if (display.getTitle().equals(selectedItem)) {
                        moveToDisplay(index);
                        break;
                    }
                    index++;
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mExpandableListView.collapseGroup(groupPosition);
                return false;
            }
        });
    }

    private void setDrawerListViewWidth() {
        int width = getResources().getDisplayMetrics().widthPixels / 2;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mExpandableListView.getLayoutParams();
        params.width = width;
        mExpandableListView.setLayoutParams(params);
    }

    private void setupNavigationDrawer() {
        updateHiddenDisplayNumberSet();
        mDrawerLayout = getActivity().findViewById(R.id.drawer_layout);
        mExpandableListView = getActivity().findViewById(R.id.navigation);
        setDrawerListViewWidth();
        mExpandableListData = getListData();
        mExpandableListTitle = new ArrayList<>(mExpandableListData.keySet());

        setDrawerItems();
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                updateHiddenDisplayNumberSet();
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mNavDrawerSet = true;
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setHomeButtonEnabled(true);
    }

    private void updateHiddenDisplayNumberSet() {
        HashSet<Integer> mHiddenDisplayNumberSet = new HashSet<>();
        for (Map.Entry<Long, List<Question>> curEntry : mDisplayQuestions.entrySet()) {
            boolean isSkip = true;
            for (Question curQuestion : curEntry.getValue()) {
                if (!mQuestionsToSkipSet.contains(curQuestion.getQuestionIdentifier())) {
                    isSkip = false;
                    break;
                }
            }
            if (isSkip) {
                mHiddenDisplayNumberSet.add(mDisplays.indexOf(curEntry.getKey()));
            }
        }
    }

    private void setLanguageSelection(Menu menu) {
        if (getActivity() == null) return;
        MenuItem item = menu.findItem(R.id.language_spinner);
        Spinner spinner = (Spinner) item.getActionView();
        final List<String> languageCodes = Instrument.getLanguages();
        ArrayList<String> displayLanguages = new ArrayList<>();
        for (String languageCode : languageCodes) {
            displayLanguages.add(new Locale(languageCode).getDisplayLanguage());
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, displayLanguages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != languageCodes.indexOf(AppUtil.getAdminSettingsInstance().getLanguage())) {
                    AppUtil.getAdminSettingsInstance().setLanguage(languageCodes.get(position));
                    LocaleManager.setNewLocale(getActivity(), languageCodes.get(position));
                    recreateActivity();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setSelection(languageCodes.indexOf(AppUtil.getAdminSettingsInstance().getLanguage()));
    }

    private void recreateActivity() {
        persistSkipMaps();
        persistSkippedQuestions();
        Intent i = new Intent(getActivity(), SurveyActivity.class);
        i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID, mInstrument.getRemoteId());
        i.putExtra(SurveyFragment.EXTRA_SURVEY_ID, mSurvey.getId());
        i.putExtra(SurveyFragment.EXTRA_QUESTION_NUMBER, mSurvey.getLastQuestion().getNumberInInstrument() - 1);
        i.putExtra(SurveyFragment.EXTRA_AUTHORIZE_SURVEY, false);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        } else {
            startActivity(i);
        }
    }

    protected void persistSkipMaps() {
        try {
            JSONObject jsonObject = new JSONObject();
            for (HashMap.Entry<String, List<String>> pair : mQuestionsToSkipMap.entrySet()) {
                if (pair.getValue() != null && pair.getValue().size() != 0) {
                    StringBuilder serialized = new StringBuilder();
                    int count = 0;
                    for (String question : pair.getValue()) {
                        serialized.append(question);
                        if (count < pair.getValue().size() - 1)
                            serialized.append(Response.LIST_DELIMITER);
                        count += 1;
                    }
                    jsonObject.put(pair.getKey(), serialized.toString());
                }
            }
            mSurvey.setSkipMaps(jsonObject.toString());
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", je);
        }
    }

    private void moveToPreviousDisplay() {
        mScrollView.scrollTo(0, 0);
        closeDrawer();
        if (mDisplayNumber >= 0 && mDisplayNumber < mDisplays.size() && mPreviousDisplays.size()
                > 0) {
            mDisplayNumber = mPreviousDisplays.remove(mPreviousDisplays.size() - 1);
            mDisplay = mDisplays.get(mDisplayNumber);
        } else {
            mDisplayNumber -= 1;
            mDisplay = mDisplays.get(mDisplayNumber);
        }
        refreshUIComponents();
    }

    private void closeDrawer() {
        showProgressBar = true;
        mDrawerLayout.closeDrawer(mExpandableListView);
        mProgressView.setVisible(true);
    }

    protected void toggleLoadingStatus() {
        isLoadingDisplay = false;
        showProgressBar = false;
        mProgressView.setVisible(false);
        hideQuestionsInDisplay();
    }

    private void refreshUIComponents() {
        if (isInIllegalState()) return;
        hideSoftInputWindow();
        createDisplayView();
        hideQuestionsInDisplay();
        updateDisplayLabels();
        setParticipantLabel();
    }

    private void hideSoftInputWindow() {
        SurveyActivity activity = (SurveyActivity) getActivity();
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    private void moveToNextDisplay() {
        if (mDisplayFragment == null) return;
        String emptyResponses = mDisplayFragment.checkForEmptyResponses();
        if (emptyResponses.length() > 0) {
            promptForResponses(emptyResponses, false);
        } else {
            proceedToNextDisplay();
        }
    }

    private void proceedToNextDisplay() {
        mScrollView.scrollTo(0, 0);
        closeDrawer();
        mPreviousDisplays.add(mDisplayNumber);
        for (int i = mDisplayNumber + 1; i < mDisplays.size(); i++) {
            boolean skipDisplay = true;
            List<Question> displayQuestions = mDisplayQuestions.get(mDisplays.get(i).getRemoteId());
            if (displayQuestions != null) {
                for (Question curQuestion : displayQuestions) {
                    if (!mQuestionsToSkipSet.contains(curQuestion.getQuestionIdentifier())) {
                        skipDisplay = false;
                        break;
                    }
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
        refreshUIComponents();
    }

    private void promptForResponses(String empty, final boolean finish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(empty)
                .setTitle(R.string.response_prompt)
                .setNegativeButton(R.string.respond, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (finish) {
                    proceedFinishingSurvey();
                } else {
                    proceedToNextDisplay();
                }
            }
        }).create().show();
    }

    private void moveToDisplay(int position) {
        if (position < mDisplayNumber) {
            closeDrawer();
            mPreviousDisplays.add(mDisplayNumber);
            mDisplayNumber = position;
            mDisplay = mDisplays.get(mDisplayNumber);
            refreshUIComponents();
        } else if (position > mDisplayNumber) {
            mDisplayNumber = position - 1;
            moveToNextDisplay();
        } else {
            toggleLoadingStatus();
        }
    }

    private void unSetSkipQuestionResponse() {
        // Use iterator to avoid concurrent modification exception
        Iterator<String> iterator = mQuestionsToSkipSet.iterator();
        while (iterator.hasNext()) {
            String questionIdentifier = iterator.next();
            if (questionIdentifier != null) {
                Response response = mResponses.get(questionIdentifier);
                if (response != null) {
                    response.clearResponse();
                    response.setDeviceUser(AuthUtils.getCurrentUser());
                    response.save();
                }
            }
        }
    }

    protected void setIntegerLoopQuestions(Question question, String response) {
        List<LoopQuestion> loopQuestions = mLoopQuestions.get(question.getQuestionIdentifier());
        List<String> questionsToHide = new ArrayList<>();
        for (LoopQuestion lq : loopQuestions) {
            questionsToHide.add(lq.getLooped());
        }
        int start = 0;
        if (!TextUtils.isEmpty(response)) {
            start = Integer.parseInt(response);
        }
        for (int k = start + 1; k <= Instrument.LOOP_MAX; k++) {
            for (LoopQuestion lq : loopQuestions) {
                String id = question.getQuestionIdentifier() + "_" + lq.getLooped() + "_" + k;
                questionsToHide.add(id);
            }
        }
        updateQuestionsToSkipMap(question.getQuestionIdentifier() + "/loop", questionsToHide);
        hideQuestionsInDisplay();
    }

    private void updateQuestionsToSkipMap(String questionIdentifier, List<String> questionsToSkip) {
        if (questionsToSkip == null || questionsToSkip.size() == 0) {
            mQuestionsToSkipMap.remove(questionIdentifier);
        } else {
            mQuestionsToSkipMap.put(questionIdentifier, questionsToSkip);
        }
    }

    private void hideQuestionsInDisplay() {
        if (isInIllegalState()) return;
        if (!isLoadingDisplay) {
            updateQuestionsToSkipSet();
            mDisplayFragment.hideQuestions();
        }
    }

    private boolean isInIllegalState() {
        return getActivity() == null || !isAdded() || isRemoving();
    }

    private void updateQuestionsToSkipSet() {
        mQuestionsToSkipSet = new HashSet<>();
        for (HashMap.Entry<String, List<String>> curPair : mQuestionsToSkipMap.entrySet()) {
            mQuestionsToSkipSet.addAll(curPair.getValue());
        }
    }

    protected void setMultipleResponseLoopQuestions(Question question, String text) {
        List<String> responses;
        if (question.hasListResponses()) {
            responses = Arrays.asList(text.split(Response.LIST_DELIMITER, -1)); // Keep empty values
        } else {
            responses = Arrays.asList(text.split(Response.LIST_DELIMITER)); // Ignore empty values
        }
        List<LoopQuestion> loopQuestions = mLoopQuestions.get(question.getQuestionIdentifier());
        List<String> questionsToHide = new ArrayList<>();
        for (LoopQuestion lq : loopQuestions) {
            questionsToHide.add(lq.getLooped());
        }
        int optionsSize = mOptions.get(question).size() - 1;
        if (question.isOtherQuestionType()) {
            optionsSize += 1;
        }
        for (int k = 0; k <= optionsSize; k++) {
            for (LoopQuestion lq : loopQuestions) {
                if (!TextUtils.isEmpty(lq.getLooped())) {
                    String id = question.getQuestionIdentifier() + "_" +
                            lq.getLooped() + "_" + k;
                    if (question.hasMultipleResponses()) {
                        if (!responses.contains(String.valueOf(k))) {
                            questionsToHide.add(id);
                        }
                    } else if (question.hasListResponses()) {
                        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(responses.get(k))) {
                            questionsToHide.add(id);
                        }
                    }
                }
            }
        }
        updateQuestionsToSkipMap(question.getQuestionIdentifier() + "/loop", questionsToHide);
        hideQuestionsInDisplay();
    }

    protected void setNextQuestion(String currentQuestionIdentifier, String nextQuestionIdentifier,
                                   String questionIdentifier) {
        List<String> skipList = new ArrayList<>();
        boolean toBeSkipped = false;
        boolean found = false;
        for (int k = mDisplayNumber; k < mDisplays.size(); k++) {
            if (found) break;
            for (Question curQuestion : getDisplayQuestions(mDisplays.get(k))) {
                if (curQuestion.getQuestionIdentifier().equals(nextQuestionIdentifier)) {
                    found = true;
                    break;
                }
                if (toBeSkipped) {
                    skipList.add(curQuestion.getQuestionIdentifier());
                    // Skip loop children questions
                    for (Question question : loopChildren(curQuestion.getQuestionIdentifier())) {
                        skipList.add(question.getQuestionIdentifier());
                    }
                }
                if (curQuestion.getQuestionIdentifier().equals(currentQuestionIdentifier))
                    toBeSkipped = true;
            }
        }
        updateQuestionsToSkipMap(questionIdentifier + "/skipTo", skipList);
        hideQuestionsInDisplay();
    }

    private List<Question> loopChildren(String sourceIdentifier) {
        List<Question> questions = new ArrayList<>();
        if (mDisplayNumber + 1 < mDisplays.size()) {
            List<Question> dQuestions = getDisplayQuestions(mDisplays.get(mDisplayNumber + 1));
            if (dQuestions != null) questions.addAll(dQuestions);
        }
        List disQuestions = getDisplayQuestions(mDisplay);
        if (disQuestions != null) questions.addAll(disQuestions);
        List<Question> loopChildren = new ArrayList<>();
        if (sourceIdentifier != null) {
            for (Question question : questions) {
                if (question.getLoopSource() != null && !question.isDeleted() &&
                        question.getLoopSource().equals(sourceIdentifier)) {
                    loopChildren.add(question);
                }
            }
        }
        return loopChildren;
    }

    protected List<Question> getDisplayQuestions(Display display) {
        return mDisplayQuestions.get(display.getRemoteId());
    }

    protected OptionSet getOptionSet(Long id) {
        return mOptionSets.get(id);
    }

    protected Instruction getInstruction(Long id) {
        return mInstructions.get(id);
    }

    protected List<NextQuestion> getNextQuestions(String questionIdentifier) {
        return mNextQuestions.get(questionIdentifier);
    }

    protected List<ConditionSkip> getConditionSkips(String questionIdentifier) {
        return mConditionSkips.get(questionIdentifier);
    }

    protected List<FollowUpQuestion> getFollowUpQuestions(String questionIdentifier) {
        return mFollowUpQuestions.get(questionIdentifier);
    }

    protected List<CriticalResponse> getCriticalResponses(String questionIdentifier) {
        return mCriticalResponses.get(questionIdentifier);
    }

    protected void startSurveyCompletion(Question question) {
        List<String> displayQuestions = new ArrayList<>();
        for (Question q : getDisplayQuestions(mDisplay)) {
            displayQuestions.add(q.getQuestionIdentifier());
        }
        List<String> skipList = new ArrayList<>(displayQuestions.subList(
                displayQuestions.indexOf(question.getQuestionIdentifier()) + 1, displayQuestions.size()));
        updateQuestionsToSkipMap(question.getQuestionIdentifier() + "/skipTo", skipList);
        hideQuestionsInDisplay();
    }

    protected void setMultipleSkipQuestions(Option selectedOption, String value, Question currentQuestion) {
        List<String> skipList = new ArrayList<>();
        List<MultipleSkip> multipleSkips = mMultipleSkips.get(currentQuestion.getQuestionIdentifier());
        if (selectedOption != null && multipleSkips != null) {
            for (MultipleSkip multipleSkip : multipleSkips) {
                if (multipleSkip.getOptionIdentifier().equals(selectedOption.getIdentifier())) {
                    addToSkipList(skipList, multipleSkip);
                }
            }
        }
        if (value != null && multipleSkips != null) {
            for (MultipleSkip multipleSkip : multipleSkips) {
                if (multipleSkip.getValue().equals(value)) {
                    addToSkipList(skipList, multipleSkip);
                }
            }
        }
        updateQuestionsToSkipMap(currentQuestion.getQuestionIdentifier() + "/multi", skipList);
        hideQuestionsInDisplay();
    }

    private void addToSkipList(List<String> skipList, MultipleSkip multipleSkip) {
        if (multipleSkip.getSkipQuestionIdentifier() != null) {
            skipList.add(multipleSkip.getSkipQuestionIdentifier());
        }
    }

    protected void setMultipleSkipQuestions2(List<Option> options, Question currentQuestion) {
        HashSet<String> skipSet = new HashSet<>();
        List<MultipleSkip> multipleSkips = mMultipleSkips.get(currentQuestion.getQuestionIdentifier());
        for (Option option : options) {
            for (MultipleSkip multipleSkip : multipleSkips) {
                if (multipleSkip.getOptionIdentifier().equals(option.getIdentifier())) {
                    if (multipleSkip.getSkipQuestionIdentifier() != null) {
                        skipSet.add(multipleSkip.getSkipQuestionIdentifier());
                    }
                }
            }
        }
        updateQuestionsToSkipMap(currentQuestion.getQuestionIdentifier() + "/multi",
                new ArrayList<>(skipSet));
        hideQuestionsInDisplay();
    }

    public Display getDisplay() {
        return mDisplay;
    }

    public HashMap<String, Question> getQuestions() {
        return mQuestions;
    }

    public HashSet<String> getQuestionsToSkipSet() {
        return mQuestionsToSkipSet;
    }

    protected NestedScrollView getScrollView() {
        return mScrollView;
    }

    public Survey getSurvey() {
        return mSurvey;
    }

    public Instrument getInstrument() {
        return mInstrument;
    }

    public HashMap<String, Response> getResponses() {
        return mResponses;
    }

    public HashMap<Question, List<Option>> getOptions() {
        return mOptions;
    }

    public HashMap<Long, List<Option>> getSpecialOptions() {
        return mSpecialOptions;
    }

    public List<DisplayInstruction> getDisplayInstructions() {
        return mDisplayInstructions.get(mDisplay);
    }

    public HashMap<String, List<LoopQuestion>> getLoopQuestions() {
        return mLoopQuestions;
    }

    public void finishSurvey() {
        String emptyResponses = mDisplayFragment.checkForEmptyResponses();
        if (emptyResponses.length() > 0) {
            promptForResponses(emptyResponses, true);
        } else {
            proceedFinishingSurvey();
        }
    }

    private void proceedFinishingSurvey() {
        unSetSkipQuestionResponse();
        if (AppUtil.getAdminSettingsInstance().getRecordSurveyLocation()) {
            setSurveyLocation();
        }
        if (mSurvey.emptyResponses().size() > 0) {
            goToReviewPage();
        } else {
            showCriticalResponses();
        }
    }

    private void scoreAndCompleteSurvey() {
        if (mInstrument.isScorable()) {
            new ScoreSurveyTask().execute(mSurvey);
        } else {
            mSurvey.setAsComplete(true);
            mSurvey.save();
            finishActivity();
        }
    }

    private void showCriticalResponses() {
        List<CriticalResponse> activatedResponses = new ArrayList<>();
        for (Question question : mQuestions.values()) {
            List<CriticalResponse> criticalResponses = getCriticalResponses(question.getQuestionIdentifier());
            if (criticalResponses != null && criticalResponses.size() > 0) {
                Response response = mResponses.get(question.getQuestionIdentifier());
                if (response != null) {
                    String[] indices = response.getText().split(Response.LIST_DELIMITER);
                    List<Option> selectedOptions = new ArrayList<>();
                    for (int k = 0; k < indices.length; k++) {
                        if (!TextUtils.isEmpty(indices[k])) {
                            int index = Integer.parseInt(indices[k]);
                            selectedOptions.add(mOptions.get(question).get(index));
                        }
                    }
                    for (Option option : selectedOptions) {
                        for (CriticalResponse criticalResponse : criticalResponses) {
                            if (criticalResponse.getOptionIdentifier().equals(option.getIdentifier())) {
                                activatedResponses.add(criticalResponse);
                            }
                        }
                    }
                }
            }
        }

        if (activatedResponses.size() > 0) {
            Spanned[] warnings = new Spanned[activatedResponses.size()];
            for (int k = 0; k < activatedResponses.size(); k++) {
                Instruction instruction = mInstructions.get(activatedResponses.get(k).getInstructionId());
                String string = "<i>Question " + "<b>" + activatedResponses.get(k).getQuestionIdentifier() + "</b>" +
                        " has response " + "<b>" + activatedResponses.get(k).getOptionIdentifier() +
                        "</b> which requires the following action: </i><b>" + instruction.getText(mInstrument) + "</b>";
                warnings[k] = styleTextWithHtml(string);
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View content = LayoutInflater.from(getActivity()).inflate(R.layout
                    .critical_responses_dialog, null);
            ListView listView = content.findViewById(R.id.critical_list);
            listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout
                    .simple_selectable_list_item, warnings));

            builder.setTitle(R.string.critical_message_title)
                    .setView(content)
                    .setCancelable(false)
                    .setPositiveButton(R.string.okay, null);
            final AlertDialog criticalDialog = builder.create();
            criticalDialog.show();
            Button button = criticalDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    criticalDialog.dismiss();
                    scoreAndCompleteSurvey();
                }
            });
        } else {
            scoreAndCompleteSurvey();
        }
    }

    private void goToReviewPage() {
        Intent i = new Intent(getActivity(), ReviewPageActivity.class);
        Bundle b = new Bundle();
        b.putLong(ReviewPageFragment.EXTRA_REVIEW_SURVEY_ID, mSurvey.getId());
        i.putExtras(b);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(i, REVIEW_CODE, ActivityOptions.makeSceneTransitionAnimation
                    (getActivity()).toBundle());
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
        if (!isEmpty(surveyMetaData)) {
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
        List<Question> questions = mDisplayQuestions.get(mDisplay.getRemoteId());
        if (mDisplay != null && questions.size() > 0) {
            // Screen title
            if (!mDisplay.getMode().equals(Display.DisplayMode.SINGLE.toString())) {
                updateActionBarTitle(String.format(Locale.getDefault(), "%s %s%s %s %s%s",
                        mDisplay.getTitle(), "(", questions.get(0).getPosition(),
                        "-", questions.get(questions.size() - 1).getPosition(), ")"));
            } else {
                updateActionBarTitle(mDisplay.getTitle());
            }
            // Progress text
            mDisplayIndexLabel.setText(String.format(Locale.getDefault(), "%s %d %s %d %s%s %s" +
                            " %s%s", getString(R.string.screen), mDisplayNumber + 1, getString(R.string
                            .of), mDisplays.size(), "(", questions.get(0).getPosition(), "-",
                    questions.get(questions.size() - 1).getPosition(), ")"));
            // Progress bar
            mProgressBar.setProgress((int) (100 * (mDisplayNumber + 1) / (float) mDisplays.size()));
        }
    }

    protected void persistSkippedQuestions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSurvey.setSkippedQuestions(String.join(Response.LIST_DELIMITER, getQuestionsToSkipSet()));
        } else {
            StringBuilder serialized = new StringBuilder();
            int count = 0;
            for (String identifier : getQuestionsToSkipSet()) {
                serialized.append(identifier);
                if (count < getQuestionsToSkipSet().size() - 1)
                    serialized.append(Response.LIST_DELIMITER);
                count += 1;
            }
            mSurvey.setSkippedQuestions(serialized.toString());
        }
        new Handler().post(new Runnable() {
            public void run() {
                mSurvey.save();
            }
        });
    }

    public HashMap<Long, List<OptionSetTranslation>> getOptionSetTranslation() {
        return mOptionSetTranslation;
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
            survey.setAsComplete(true);
            survey.save();
            finishActivity();
        }
    }

    private class InstrumentDataTask extends AsyncTask<Object, Void, InstrumentDataWrapper> {

        @Override
        protected InstrumentDataWrapper doInBackground(Object... params) {
            InstrumentDataWrapper instrumentData = new InstrumentDataWrapper();
            instrumentData.questions = (ArrayList<Question>) ((Instrument) params[0]).questions();
            instrumentData.responses = ((Survey) params[1]).responsesMap();
            instrumentData.options = ((Instrument) params[0]).optionsMap(instrumentData.questions);
            instrumentData.specialOptions = ((Instrument) params[0]).specialOptionsMap();
            instrumentData.displayInstructions = ((Instrument) params[0]).displayInstructions();
            instrumentData.optionSets = ((Instrument) params[0]).optionSets();
            instrumentData.instructions = ((Instrument) params[0]).instructions();
            instrumentData.nextQuestions = ((Instrument) params[0]).nextQuestions();
            instrumentData.conditionSkips = ((Instrument) params[0]).conditionSkips();
            instrumentData.multipleSkips = ((Instrument) params[0]).multipleSkips();
            instrumentData.followUpQuestions = ((Instrument) params[0]).followUpQuestions();
            instrumentData.criticalResponses = ((Instrument) params[0]).criticalResponses();
            instrumentData.loopQuestions = ((Instrument) params[0]).loopQuestions();
            instrumentData.optionSetTranslation = ((Instrument) params[0]).optionSetTranslations();
            return instrumentData;
        }

        @Override
        protected void onPostExecute(InstrumentDataWrapper instrumentData) {
            setQuestions(instrumentData.questions);
            mResponses = instrumentData.responses;
            mOptions = instrumentData.options;
            mSpecialOptions = instrumentData.specialOptions;
            mDisplayInstructions = instrumentData.displayInstructions;
            mOptionSets = instrumentData.optionSets;
            mNextQuestions = instrumentData.nextQuestions;
            mConditionSkips = instrumentData.conditionSkips;
            mMultipleSkips = instrumentData.multipleSkips;
            mFollowUpQuestions = instrumentData.followUpQuestions;
            mInstructions = instrumentData.instructions;
            mCriticalResponses = instrumentData.criticalResponses;
            mLoopQuestions = instrumentData.loopQuestions;
            mOptionSetTranslation = instrumentData.optionSetTranslation;
            refreshView();
        }

        private void setQuestions(List<Question> questions) {
            mQuestions = new HashMap<>();
            for (Question question : questions) {
                mQuestions.put(question.getQuestionIdentifier(), question);
                List<Question> displayQuestions = mDisplayQuestions.get(question.getDisplayId());
                if (displayQuestions == null) {
                    displayQuestions = new ArrayList<>();
                }
                displayQuestions.add(question);
                mDisplayQuestions.put(question.getDisplayId(), displayQuestions);
            }
        }
    }

    private class InstrumentDataWrapper {
        HashMap<String, Response> responses;
        HashMap<Question, List<Option>> options;
        HashMap<Long, List<Option>> specialOptions;
        HashMap<Display, List<DisplayInstruction>> displayInstructions;
        LongSparseArray<OptionSet> optionSets;
        HashMap<String, List<NextQuestion>> nextQuestions;
        HashMap<String, List<ConditionSkip>> conditionSkips;
        HashMap<String, List<MultipleSkip>> multipleSkips;
        HashMap<String, List<FollowUpQuestion>> followUpQuestions;
        LongSparseArray<Instruction> instructions;
        HashMap<String, List<CriticalResponse>> criticalResponses;
        HashMap<String, List<LoopQuestion>> loopQuestions;
        ArrayList<Question> questions;
        HashMap<Long, List<OptionSetTranslation>> optionSetTranslation;
    }

    private class DisplayTitlesListAdapter extends BaseExpandableListAdapter {

        private Context mContext;
        private List<String> mExpandableListTitle;
        private Map<String, List<String>> mExpandableListDetail;
        private LayoutInflater mLayoutInflater;

        DisplayTitlesListAdapter(Context context, List<String> expandableListTitle,
                                 Map<String, List<String>> expandableListDetail) {
            mContext = context;
            mExpandableListTitle = expandableListTitle;
            mExpandableListDetail = expandableListDetail;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return mExpandableListTitle.size();
        }

        @Override
        public int getChildrenCount(int listPosition) {
            return mExpandableListDetail.get(mExpandableListTitle.get(listPosition)).size();
        }

        @Override
        public Object getGroup(int listPosition) {
            return mExpandableListTitle.get(listPosition);
        }

        @Override
        public Object getChild(int listPosition, int expandedListPosition) {
            return mExpandableListDetail.get(mExpandableListTitle.get(listPosition)).get(expandedListPosition);
        }

        @Override
        public long getGroupId(int listPosition) {
            return listPosition;
        }

        @Override
        public long getChildId(int listPosition, int expandedListPosition) {
            return expandedListPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int listPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String listTitle = (String) getGroup(listPosition);
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_group, null);
            }
            TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
            listTitleTextView.setTypeface(mInstrument.getTypeFace(mContext), Typeface.BOLD);
            listTitleTextView.setText(listTitle);
            return convertView;
        }

        @Override
        public View getChildView(int listPosition, final int expandedListPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String expandedListText = (String) getChild(listPosition, expandedListPosition);
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_item_text_view, null);
            }
            TextView expandedListTextView = (TextView) convertView.findViewById(R.id.expandedListItem);
            expandedListTextView.setText(expandedListText);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int listPosition, int expandedListPosition) {
            return true;
        }
    }

}