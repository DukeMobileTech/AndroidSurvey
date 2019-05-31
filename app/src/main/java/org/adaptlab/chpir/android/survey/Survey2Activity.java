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
import org.adaptlab.chpir.android.survey.entities.relations.InstrumentRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.viewmodelfactories.InstrumentRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.InstrumentRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.List;

public class Survey2Activity extends AppCompatActivity {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";
    private final String TAG = this.getClass().getName();
    private List<Display> mDisplays;
    private List<Integer> mPreviousDisplays;
    private DisplayPagerAdapter mDisplayPagerAdapter;
    private ViewPager mViewPager;
    private Instrument mInstrument;
    private Survey mSurvey;
    private int mDisplayPosition = 0;
    private SurveyViewModel mSurveyViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        long instrumentId = getIntent().getLongExtra(EXTRA_INSTRUMENT_ID, -1);
        if (instrumentId == -1) return;
        String surveyUUID = getIntent().getStringExtra(EXTRA_SURVEY_UUID);
        if (TextUtils.isEmpty(surveyUUID)) {
            SurveyRepository surveyRepository = new SurveyRepository(getApplication());
            Survey survey = surveyRepository.initializeSurvey(AppUtil.getProjectId(), instrumentId);
            surveyUUID = survey.getUUID();
        }

        mDisplayPagerAdapter = new DisplayPagerAdapter(getSupportFragmentManager(), surveyUUID);
        setDisplayViewPagers();
        addOnPageChangeListener();
        setInstrumentViewModel(instrumentId);
        setSurveyViewModel(surveyUUID);
        mPreviousDisplays = new ArrayList<>();
        mDisplays = new ArrayList<>();
    }

    private void setDisplayViewPagers() {
        mViewPager = findViewById(R.id.displayPager);
        mViewPager.setAdapter(mDisplayPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.displayTabs);
        tabLayout.setMinimumWidth(getWindow().getDecorView().getWidth());
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void addOnPageChangeListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mDisplayPosition = position;
            }

            @Override
            public void onPageSelected(int position) {
                mDisplayPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setInstrumentViewModel(Long instrumentId) {
        InstrumentRelationViewModelFactory factory = new InstrumentRelationViewModelFactory(getApplication(), instrumentId);
        InstrumentRelationViewModel viewModel = ViewModelProviders.of(this, factory).get(InstrumentRelationViewModel.class);
        viewModel.getInstrumentRelation().observe(this, new Observer<InstrumentRelation>() {
            @Override
            public void onChanged(@Nullable InstrumentRelation relation) {
                if (relation != null) {
                    mInstrument = relation.instrument;
                    updateActionBarTitle(mInstrument.getTitle());
                    mDisplays = relation.displays;
                    mDisplayPagerAdapter.setDisplays(mDisplays);
                }
            }
        });
    }

    private void updateActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
    }

    private void setSurveyViewModel(final String surveyUUID) {
        SurveyViewModelFactory factory = new SurveyViewModelFactory(getApplication(), surveyUUID);
        mSurveyViewModel = ViewModelProviders.of(this, factory).get(SurveyViewModel.class);
        mSurveyViewModel.getLiveDataSurvey().observe(this, new Observer<Survey>() {
            @Override
            public void onChanged(@Nullable Survey survey) {
                mSurvey = survey;
                if (mSurveyViewModel.getSurvey() == null) {
                    mSurveyViewModel.setSurvey(mSurvey);
                    mSurveyViewModel.setSkipData();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mSurveyViewModel.persistSkipMaps();
        mSurveyViewModel.persistSkippedQuestions();
        mSurveyViewModel.update();
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
        mDisplayPosition = mViewPager.getCurrentItem();
        menu.findItem(R.id.menu_item_previous).setVisible(mDisplayPosition != 0 && !mPreviousDisplays.isEmpty()).setEnabled(true);
        menu.findItem(R.id.menu_item_next).setVisible(mDisplayPosition != mDisplays.size() - 1).setEnabled(true);
        menu.findItem(R.id.menu_item_finish).setVisible(mDisplayPosition == mDisplays.size() - 1).setEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        if (mDisplayPosition > 0 && mDisplayPosition < mDisplays.size() && mPreviousDisplays.size() > 0) {
            mDisplayPosition = mPreviousDisplays.remove(mPreviousDisplays.size() - 1);
        } else {
            mDisplayPosition -= 1;
        }
        mViewPager.setCurrentItem(mDisplayPosition);
        invalidateOptionsMenu();
    }

    private void moveToNextDisplay() {
        mPreviousDisplays.add(mDisplayPosition);
        mDisplayPosition += 1;
        mViewPager.setCurrentItem(mDisplayPosition);
        invalidateOptionsMenu();
    }

    public void finishSurvey() {
        finish();
    }

}