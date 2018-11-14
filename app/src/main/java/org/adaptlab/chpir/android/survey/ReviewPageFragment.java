package org.adaptlab.chpir.android.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class ReviewPageFragment extends ListFragment {
    public final static String EXTRA_REVIEW_SURVEY_ID = "org.adaptlab.chpir.android.survey.review_survey_id";
    private static final String TAG = "ReviewPageFragment";
    private List<Question> mQuestionsWithoutResponses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() == null) return;
        setHasOptionsMenu(true);
        Intent intent = getActivity().getIntent();
        if (intent.getExtras() == null) return;
        Survey survey = Survey.load(Survey.class, intent.getExtras().getLong(EXTRA_REVIEW_SURVEY_ID));
        if (survey == null) return;
        mQuestionsWithoutResponses = survey.getInstrument().questions();

        HashSet<String> questionSkipSet;
        if (survey.getSkippedQuestions() == null) {
            questionSkipSet = new HashSet<>();
        } else {
            questionSkipSet = new HashSet<>(Arrays.asList(
                    survey.getSkippedQuestions().split(Response.LIST_DELIMITER)));
        }

        List<Question> answeredQuestions = new ArrayList<>();
        for (Question question : mQuestionsWithoutResponses) {
            if (questionSkipSet.size() == 0) break;
            if (questionSkipSet.contains(question.getQuestionIdentifier())) {
                answeredQuestions.add(question);
            }
        }
        for (Response response : survey.responses()) {
            if (!TextUtils.isEmpty(response.getText()) ||
                    !TextUtils.isEmpty(response.getSpecialResponse())) {
                answeredQuestions.add(response.getQuestion());
            }
        }
        mQuestionsWithoutResponses.removeAll(answeredQuestions);

        sortReviewQuestions();
        setListAdapter(new QuestionAdapter((ArrayList<Question>) mQuestionsWithoutResponses));
        getActivity().setTitle(getActivity().getResources().getString(R.string.skipped_questions));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Question question = ((QuestionAdapter) getListAdapter()).getItem(position);
        if (question != null) setReturnResults(question.getDisplay().getPosition() - 1);
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
                    setReturnResults(0);
                } else {
                    setReturnResults(mQuestionsWithoutResponses.get(0).getDisplay().getPosition() - 1);
                }
                return true;
            case R.id.menu_item_complete:
                setReturnResults(Integer.MIN_VALUE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortReviewQuestions() {
        Collections.sort(mQuestionsWithoutResponses, new Comparator<Question>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(Question lhs, Question rhs) {
                return Integer.compare(lhs.getNumberInInstrument(), rhs.getNumberInInstrument());
            }
        });
    }

    private void setReturnResults(int num) {
        Intent i = new Intent();
        i.putExtra(SurveyFragment.EXTRA_DISPLAY_NUMBER, num);
        getActivity().setResult(Activity.RESULT_OK, i);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().finishAfterTransition();
        } else {
            getActivity().finish();
        }
    }

    private class QuestionAdapter extends ArrayAdapter<Question> {
        public QuestionAdapter(ArrayList<Question> questions) {
            super(getActivity(), 0, questions);
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.review_page, null);
            }

            Question question = getItem(position);

            TextView questionNumberTextView = (TextView) convertView.findViewById(R.id.review_question_number);
            questionNumberTextView.setText(String.valueOf(question.getNumberInInstrument()));
            questionNumberTextView.setTextColor(Color.BLACK);

            TextView questionTextView = (TextView) convertView.findViewById(R.id.review_question_text);
            questionTextView.setText(Html.fromHtml(question.getText()));
            questionTextView.setMaxLines(2);

            return convertView;
        }
    }
}
