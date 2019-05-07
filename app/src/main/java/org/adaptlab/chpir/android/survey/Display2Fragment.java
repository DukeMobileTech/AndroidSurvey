package org.adaptlab.chpir.android.survey;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.SurveyResponse;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;
import org.json.JSONException;
import org.json.JSONObject;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.isEmpty;

public class Display2Fragment extends Fragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";

    private final static String TAG = "Display2Fragment";

    private TextView mDisplayIndexLabel;
    private TextView mParticipantLabel;
    private ProgressBar mProgressBar;
    private ScrollView mScrollView;

    private int mDisplayNumber;

    private Instrument mInstrument;
    private Survey mSurvey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity() == null) return;
        long instrumentId = getActivity().getIntent().getLongExtra(EXTRA_INSTRUMENT_ID, -1);
        if (instrumentId == -1) return;
        String surveyUUID = getActivity().getIntent().getStringExtra(EXTRA_SURVEY_UUID);
        SurveyViewModelFactory factory = new SurveyViewModelFactory(getActivity().getApplication(), instrumentId, surveyUUID);
        SurveyViewModel surveyViewModel = ViewModelProviders.of(getActivity(), factory).get(SurveyViewModel.class);
        surveyViewModel.getSurveyResponse().observe(this, new Observer<SurveyResponse>() {
            @Override
            public void onChanged(@Nullable SurveyResponse surveyResponse) {
                mInstrument = surveyResponse.survey.getInstrument();
                mSurvey = surveyResponse.survey;
                updateActionBarTitle(mInstrument.getTitle());
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_survey_2, parent, false);
        mParticipantLabel = v.findViewById(R.id.participant_label);
        mDisplayIndexLabel = v.findViewById(R.id.display_index_label);
        mProgressBar = v.findViewById(R.id.progress_bar);
        mScrollView = v.findViewById(R.id.survey_fragment_scroll_view);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_survey, menu);
//        if (!mNavDrawerSet) {
//            setupNavigationDrawer();
//        }
//        setLanguageSelection(menu);
//        mProgressView = menu.findItem(R.id.menu_item_progress);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
//        menu.findItem(R.id.menu_item_previous).setEnabled(mDisplayNumber != 0 ||
//                !mPreviousDisplays.isEmpty());
//        menu.findItem(R.id.menu_item_next).setVisible(mDisplayNumber != mDisplays.size() - 1)
//                .setEnabled(true);
//        menu.findItem(R.id.menu_item_finish).setVisible(mDisplayNumber == mDisplays.size() - 1)
//                .setEnabled(true);
//        menu.findItem(R.id.menu_item_progress).setVisible(showProgressBar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
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

    private void updateActionBarTitle(String title) {
        if (getActivity() == null) return;
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
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

}
