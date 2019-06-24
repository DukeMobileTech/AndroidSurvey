package org.adaptlab.chpir.android.survey;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.InstrumentRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionTranslationRelation;
import org.adaptlab.chpir.android.survey.relations.SurveyRelation;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;
import org.adaptlab.chpir.android.survey.viewmodelfactories.InstrumentRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.InstrumentRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SurveyReviewFragment extends ListFragment {
    final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";
    final static String EXTRA_DEVICE_LANGUAGE = "org.adaptlab.chpir.android.survey.EXTRA_DEVICE_LANGUAGE";
    private static final String TAG = SurveyReviewFragment.class.getName();
    private List<QuestionTranslationRelation> mQuestionsWithoutResponses;
    private String mSurveyUUID;
    private Long mInstrumentId;
    private String mLanguage;
    private SurveyViewModel mSurveyViewModel;
    private HashMap<String, QuestionTranslationRelation> mQuestionTranslations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Intent intent = getActivity().getIntent();
        if (intent.getExtras() == null) return;
        mSurveyUUID = intent.getExtras().getString(EXTRA_SURVEY_UUID);
        mInstrumentId = intent.getExtras().getLong(EXTRA_INSTRUMENT_ID);
        mLanguage = intent.getExtras().getString(EXTRA_DEVICE_LANGUAGE);
        setSurveyViewModel();
        setInstrumentViewModel();
        setSurveyRelationViewModel();
        getActivity().setTitle(getActivity().getResources().getString(R.string.skipped_questions));
    }

    private void setLocale() {
        mSurveyViewModel.setDeviceLanguage(mLanguage);
        Configuration configuration = getResources().getConfiguration();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(new Locale(mLanguage));
        } else {
            configuration.locale = new Locale(mLanguage);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getActivity().getApplicationContext().createConfigurationContext(configuration);
        } else {
            getResources().updateConfiguration(configuration, displayMetrics);
        }
    }

    private void setQuestions() {
        if (mSurveyViewModel.getQuestions() != null && mSurveyViewModel.getResponses() != null) {
            mSurveyViewModel.setQuestionsWithoutResponses();
            if (mSurveyViewModel.getQuestionsWithoutResponses() != null) {
                mQuestionsWithoutResponses = new ArrayList<>();
                for (Question question : mSurveyViewModel.getQuestionsWithoutResponses().values()) {
                    mQuestionsWithoutResponses.add(mQuestionTranslations.get(question.getQuestionIdentifier()));
                }
                sortReviewQuestions();
                setListAdapter(new QuestionAdapter((ArrayList<QuestionTranslationRelation>) mQuestionsWithoutResponses));
            }
        }
    }

    private void setSurveyViewModel() {
        SurveyViewModelFactory factory = new SurveyViewModelFactory(getActivity().getApplication(), mSurveyUUID);
        mSurveyViewModel = ViewModelProviders.of(this, factory).get(SurveyViewModel.class);
        mSurveyViewModel.getLiveDataSurvey().observe(this, new Observer<Survey>() {
            @Override
            public void onChanged(@Nullable Survey survey) {
                if (survey != null) {
                    mSurveyViewModel.setSurvey(survey);
                    mSurveyViewModel.setSkipData();
                    setQuestions();
                }
            }
        });
        setLocale();
    }

    private void setInstrumentViewModel() {
        InstrumentRelationViewModelFactory factory = new InstrumentRelationViewModelFactory(getActivity().getApplication(), mInstrumentId);
        InstrumentRelationViewModel viewModel = ViewModelProviders.of(this, factory).get(InstrumentRelationViewModel.class);
        viewModel.getInstrumentRelation().observe(this, new Observer<InstrumentRelation>() {
            @Override
            public void onChanged(@Nullable InstrumentRelation relation) {
                if (relation != null) {
                    mSurveyViewModel.setInstrumentLanguage(relation.instrument.getLanguage());
                    mSurveyViewModel.setDisplays(relation.displays);
                    List<Question> questions = new ArrayList<>();
                    mQuestionTranslations = new HashMap<>();
                    for (QuestionTranslationRelation questionTranslationRelation : relation.questions) {
                        questions.add(questionTranslationRelation.question);
                        mQuestionTranslations.put(questionTranslationRelation.question.getQuestionIdentifier(), questionTranslationRelation);
                    }
                    mSurveyViewModel.setQuestions(questions);
                    setQuestions();
                }
            }
        });
    }

    private void setSurveyRelationViewModel() {
        SurveyRelationViewModelFactory factory = new SurveyRelationViewModelFactory(getActivity().getApplication(), mSurveyUUID);
        SurveyRelationViewModel viewModel = ViewModelProviders.of(this, factory).get(SurveyRelationViewModel.class);
        viewModel.getSurveyRelation().observe(this, new Observer<SurveyRelation>() {
            @Override
            public void onChanged(@Nullable SurveyRelation surveyRelation) {
                HashMap<String, Response> map = new HashMap<>();
                for (Response response : surveyRelation.responses) {
                    map.put(response.getQuestionIdentifier(), response);
                }
                mSurveyViewModel.setResponses(map);
                setQuestions();
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        QuestionTranslationRelation relation = ((QuestionAdapter) getListAdapter()).getItem(position);
        if (relation != null) setReturnResults(relation.question.getDisplayId());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_review, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_item_back).setEnabled(true).setVisible(true);
        menu.findItem(R.id.menu_item_complete).setEnabled(true).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_back:
                if (mQuestionsWithoutResponses.size() == 0) {
                    setReturnResults(0L);
                } else {
                    setReturnResults(mQuestionsWithoutResponses.get(0).question.getDisplayId());
                }
                return true;
            case R.id.menu_item_complete:
                setReturnResults(-1L);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortReviewQuestions() {
        Collections.sort(mQuestionsWithoutResponses, new Comparator<QuestionTranslationRelation>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(QuestionTranslationRelation lhs, QuestionTranslationRelation rhs) {
                return Integer.compare(lhs.question.getNumberInInstrument(), rhs.question.getNumberInInstrument());
            }
        });
    }

    private void setReturnResults(Long displayId) {
        Intent i = new Intent();
        i.putExtra(Survey2Activity.EXTRA_DISPLAY_ID, displayId);
        getActivity().setResult(Activity.RESULT_OK, i);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().finishAfterTransition();
        } else {
            getActivity().finish();
        }
    }

    private class QuestionAdapter extends ArrayAdapter<QuestionTranslationRelation> {

        QuestionAdapter(ArrayList<QuestionTranslationRelation> questions) {
            super(getActivity(), 0, questions);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.review_page, null);
            }

            QuestionTranslationRelation relation = getItem(position);

            TextView questionNumberTextView = convertView.findViewById(R.id.review_question_number);
            questionNumberTextView.setText(String.valueOf(relation.question.getNumberInInstrument()));
            questionNumberTextView.setTextColor(Color.BLACK);

            String text = TranslationUtil.getText(relation.question, relation.translations, mSurveyViewModel);

            TextView questionTextView = convertView.findViewById(R.id.review_question_text);
            questionTextView.setText(Html.fromHtml(text));
            questionTextView.setMaxLines(2);

            return convertView;
        }

    }

}
