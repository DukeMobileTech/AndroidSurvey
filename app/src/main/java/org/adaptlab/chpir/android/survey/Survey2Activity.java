package org.adaptlab.chpir.android.survey;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import org.adaptlab.chpir.android.survey.adapters.DisplayPagerAdapter;
import org.adaptlab.chpir.android.survey.adapters.NavigationDrawerAdapter;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.InstrumentRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.viewmodelfactories.InstrumentRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SectionViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.InstrumentRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SectionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Survey2Activity extends AppCompatActivity {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";
    private final String TAG = this.getClass().getName();
    private DisplayPagerAdapter mDisplayPagerAdapter;
    private ViewPager mViewPager;
    private Instrument mInstrument;
    private Survey mSurvey;
    private SurveyViewModel mSurveyViewModel;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ExpandableListView mExpandableListView;
    private ActionBar mActionBar;
    private int mLastPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }

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
        setSectionViewModel(instrumentId);
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
                mSurveyViewModel.setDisplayPosition(position);
            }

            @Override
            public void onPageSelected(int position) {
                mSurveyViewModel.setDisplayPosition(position);
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
                    mActionBar.setTitle(mInstrument.getTitle());
                    mSurveyViewModel.setDisplays(relation.displays);
                    mDisplayPagerAdapter.setDisplays(relation.displays);
                    mSurveyViewModel.setQuestions(relation.questions);
                    if (mSurveyViewModel.getPreviousDisplays() == null) {
                        mSurveyViewModel.setPreviousDisplays(new ArrayList<Integer>());
                    }
                    setNavigationListData();
                    invalidateOptionsMenu();
                }
            }
        });
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

    private void setSectionViewModel(Long instrumentId) {
        SectionViewModelFactory factory = new SectionViewModelFactory(getApplication(), instrumentId);
        SectionViewModel viewModel = ViewModelProviders.of(this, factory).get(SectionViewModel.class);
        viewModel.getSections().observe(this, new Observer<List<Section>>() {
            @Override
            public void onChanged(@Nullable List<Section> sections) {
                LongSparseArray<Section> longSparseArray = new LongSparseArray<>();
                for (Section section : sections) {
                    longSparseArray.put(section.getRemoteId(), section);
                }
                mSurveyViewModel.setSections(longSparseArray);
                setNavigationListData();
            }
        });
    }

    private void setNavigationListData() {
        if (mSurveyViewModel.getSections() == null || mSurveyViewModel.getDisplays() == null)
            return;
        LinkedHashMap<String, List<String>> listData = new LinkedHashMap<>();
        for (int i = 0; i < mSurveyViewModel.getDisplays().size(); i++) {
            Section section = mSurveyViewModel.getSections().get(mSurveyViewModel.getDisplays().get(i).getSectionId());
            List<String> displayTitles = listData.get(section.getTitle());
            if (displayTitles == null) displayTitles = new ArrayList<>();
            displayTitles.add(mSurveyViewModel.getDisplays().get(i).getTitle());
            listData.put(section.getTitle(), displayTitles);
        }
        mSurveyViewModel.setExpandableListData(listData);
        mSurveyViewModel.setExpandableListTitle(new ArrayList<>(listData.keySet()));
        setNavigationDrawer();
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
        int position = mViewPager.getCurrentItem();
        mSurveyViewModel.setDisplayPosition(position);
        menu.findItem(R.id.menu_item_previous).setVisible(position != 0 && !mSurveyViewModel.getPreviousDisplays().isEmpty()).setEnabled(true);
        if (mSurveyViewModel.getDisplays() != null) {
            menu.findItem(R.id.menu_item_next).setVisible(position != mSurveyViewModel.getDisplays().size() - 1).setEnabled(true);
            menu.findItem(R.id.menu_item_finish).setVisible(position == mSurveyViewModel.getDisplays().size() - 1).setEnabled(true);
        }
        return true;
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

    private void setNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mExpandableListView = findViewById(R.id.navigation);
        int width = getResources().getDisplayMetrics().widthPixels / 3;
        ViewGroup.LayoutParams params = mExpandableListView.getLayoutParams();
        params.width = width;
        mExpandableListView.setLayoutParams(params);

        ExpandableListAdapter adapter = new NavigationDrawerAdapter(getApplicationContext(),
                mSurveyViewModel.getExpandableListTitle(), mSurveyViewModel.getExpandableListData());
        mExpandableListView.setAdapter(adapter);
        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (mLastPosition != -1 && groupPosition != mLastPosition) {
                    mExpandableListView.collapseGroup(mLastPosition);
                }
                mLastPosition = groupPosition;
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
                String selectedItem = ((List) (mSurveyViewModel.getExpandableListData().get(mSurveyViewModel.getExpandableListTitle().get(groupPosition)))).get(childPosition).toString();
                int index = 0;
                for (Display display : mSurveyViewModel.getDisplays()) {
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

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void moveToPreviousDisplay() {
        int position = mSurveyViewModel.getDisplayPosition();
        if (position > 0 && position < mSurveyViewModel.getDisplays().size() && mSurveyViewModel.getPreviousDisplays().size() > 0) {
            mSurveyViewModel.setDisplayPosition(mSurveyViewModel.getPreviousDisplays().remove(mSurveyViewModel.getPreviousDisplays().size() - 1));
        } else {
            mSurveyViewModel.decrementDisplayPosition();
        }
        mViewPager.setCurrentItem(mSurveyViewModel.getDisplayPosition());
        invalidateOptionsMenu();
    }

    private void moveToNextDisplay() {
        mSurveyViewModel.getPreviousDisplays().add(mSurveyViewModel.getDisplayPosition());
        mSurveyViewModel.incrementDisplayPosition();
        mViewPager.setCurrentItem(mSurveyViewModel.getDisplayPosition());
        invalidateOptionsMenu();
    }

    private void moveToDisplay(int position) {
        int displayPosition = mSurveyViewModel.getDisplayPosition();
        if (position < displayPosition) {
            mSurveyViewModel.getPreviousDisplays().add(displayPosition);
            mSurveyViewModel.setDisplayPosition(position);
            mViewPager.setCurrentItem(mSurveyViewModel.getDisplayPosition());
        } else if (position > displayPosition) {
            mSurveyViewModel.setDisplayPosition(position - 1);
            moveToNextDisplay();
        }
        mDrawerLayout.closeDrawer(mExpandableListView);
    }

    public void finishSurvey() {
        finish();
    }

}