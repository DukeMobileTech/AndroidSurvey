package org.adaptlab.chpir.android.survey;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.adaptlab.chpir.android.survey.adapters.DisplayPagerAdapter;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.viewmodelfactories.DisplayViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.InstrumentViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.InstrumentViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.List;

public class Survey2Activity extends AppCompatActivity {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";
    public final static String EXTRA_DISPLAY_POSITION = "org.adaptlab.chpir.android.survey.EXTRA_DISPLAY_POSITION";

    private final static String TAG = "Survey2Fragment";

    private DisplayPagerAdapter mDisplayPagerAdapter;

    private Instrument mInstrument;
    private Survey mSurvey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        long instrumentId = getIntent().getLongExtra(EXTRA_INSTRUMENT_ID, -1);
        if (instrumentId == -1) return;
        int displayPosition = getIntent().getIntExtra(EXTRA_DISPLAY_POSITION, 1);
        String surveyUUID = getIntent().getStringExtra(EXTRA_SURVEY_UUID);
        if (TextUtils.isEmpty(surveyUUID)) {
            SurveyRepository surveyRepository = new SurveyRepository(getApplication());
            mSurvey = surveyRepository.initializeSurvey(AppUtil.getProjectId(), instrumentId);
            surveyUUID = mSurvey.getUUID();
        }

        mDisplayPagerAdapter = new DisplayPagerAdapter(getSupportFragmentManager(), displayPosition);
        setDisplayViewPagers();

        setInstrumentViewModel(instrumentId);
        setSurveyViewModel(surveyUUID);
        setDisplayViewModels(instrumentId);
    }

    private void setInstrumentViewModel(long instrumentId) {
        InstrumentViewModelFactory factory = new InstrumentViewModelFactory(getApplication(), instrumentId);
        InstrumentViewModel viewModel = ViewModelProviders.of(this, factory).get(InstrumentViewModel.class);
        viewModel.getInstrument().observe(this, new Observer<Instrument>() {
            @Override
            public void onChanged(@Nullable Instrument instrument) {
                mInstrument = instrument;
                updateActionBarTitle(mInstrument.getTitle());
            }
        });
    }

    private void setDisplayViewPagers() {
        ViewPager viewPager = findViewById(R.id.displayPager);
        viewPager.setAdapter(mDisplayPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.displayTabs);
        tabLayout.setMinimumWidth(getWindow().getDecorView().getWidth());
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setSurveyViewModel(String surveyUUID) {
        SurveyViewModelFactory factory = new SurveyViewModelFactory(getApplication(), surveyUUID);
        SurveyViewModel surveyViewModel = ViewModelProviders.of(this, factory).get(SurveyViewModel.class);
        surveyViewModel.getSurvey().observe(this, new Observer<Survey>() {
            @Override
            public void onChanged(@Nullable Survey survey) {
                if (survey != null) mSurvey = survey;
                mDisplayPagerAdapter.setSurvey(mSurvey);
            }
        });
    }

    private void setDisplayViewModels(long instrumentId) {
        DisplayViewModelFactory factory = new DisplayViewModelFactory(getApplication(), instrumentId);
        DisplayViewModel displayViewModel = ViewModelProviders.of(this, factory).get(DisplayViewModel.class);
        displayViewModel.getDisplays().observe(this, new Observer<List<Display>>() {
            @Override
            public void onChanged(@Nullable List<Display> displays) {
                mDisplayPagerAdapter.setDisplays(displays);
            }
        });
    }

    private void updateActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_survey, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_previous:
//                moveToPreviousDisplay();
                return true;
            case R.id.menu_item_next:
//                moveToNextDisplay();
                return true;
            case R.id.menu_item_finish:
//                finishSurvey();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}