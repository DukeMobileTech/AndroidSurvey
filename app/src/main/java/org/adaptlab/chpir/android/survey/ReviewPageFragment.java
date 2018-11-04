package org.adaptlab.chpir.android.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ListFragment;
import android.text.Html;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class ReviewPageFragment extends ListFragment {
    public final static String EXTRA_REVIEW_SURVEY_ID = "org.adaptlab.chpir.android.survey.review_survey_id";
    public final static String EXTRA_SKIPPED_QUESTION_ID_LIST = "org.adaptlab.chpir.android.survey.skipped_question_id_list";
    private static final String TAG = "ReviewPageFragment";
    private ArrayList<Question> mSkippedQuestions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Intent intent = getActivity().getIntent();
        mSkippedQuestions = new ArrayList<>();
        Survey mSurvey = Survey.load(Survey.class, intent.getExtras().getLong(EXTRA_REVIEW_SURVEY_ID));
        HashSet<String> questionSkipSet = new HashSet<>((intent.getExtras().getStringArrayList(EXTRA_SKIPPED_QUESTION_ID_LIST)));
        List<Response> emptyResponses = mSurvey.emptyResponses();
        for (Response response : emptyResponses) {
            Question curQuestion = response.getQuestion();
            if (!questionSkipSet.contains(curQuestion.getQuestionIdentifier())) {
                mSkippedQuestions.add(curQuestion);
            }
        }
        sortReviewQuestions();
        setListAdapter(new QuestionAdapter(mSkippedQuestions));
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
                if (mSkippedQuestions.size() == 0) {
                    setReturnResults(0);
                } else {
                    setReturnResults(mSkippedQuestions.get(0).getDisplay().getPosition() - 1);
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
        Collections.sort(mSkippedQuestions, new Comparator<Question>() {
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
        public View getView(int position, View convertView, ViewGroup parent) {
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
