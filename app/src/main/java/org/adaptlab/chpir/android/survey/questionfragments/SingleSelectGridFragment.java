package org.adaptlab.chpir.android.survey.questionfragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.GridFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.GridLabel;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.roster.views.OHScrollView;

import java.util.ArrayList;
import java.util.List;

public class SingleSelectGridFragment extends GridFragment {
    private static final String TAG = "SingleSelectGridFragment";
    private static final int MIN_HEIGHT = 80;
    private static final int MIN_WIDTH = 200;
    private static final int MARGIN_10 = 10;
    private static final int MARGIN_50 = 50;
    private static final int MARGIN_0 = 0;
    private int mIndex;
    private List<RadioGroup> mRadioGroups;
    private Integer[] rowHeights;
    private boolean interceptScroll = true;
    private OHScrollView headerScrollView;
    private OHScrollView contentScrollView;

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
            int checked = mRadioGroups.get(mIndex).getCheckedRadioButtonId();
            if (checked > -1) {
                ((RadioButton) mRadioGroups.get(mIndex).getChildAt(checked)).setChecked(false);
            }
        } else {
            ((RadioButton) mRadioGroups.get(mIndex).getChildAt(Integer.parseInt(responseText))).setChecked(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_table_question, parent, false);

        headerScrollView = (OHScrollView) v.findViewById(R.id.table_options_header_view);
        contentScrollView = (OHScrollView) v.findViewById(R.id.table_body_options_view);
        headerScrollView.setScrollViewListener(this);
        contentScrollView.setScrollViewListener(this);

        setTableHeaderOptions(v);
        setTableBodyContent(v);
        return v;
    }

    private void setTableBodyContent(View v) {
        LinearLayout questionTextLayout = (LinearLayout) v.findViewById(R.id.table_body_question_text);
        LinearLayout optionsListLinearLayout = (LinearLayout) v.findViewById(R.id.table_body_options_choice);
        mRadioGroups = new ArrayList<>();
        List<Question> questionList = getQuestions();
        rowHeights = new Integer[questionList.size()];
        for (int k = 0; k < questionList.size(); k++) {
            final Question q = questionList.get(k);
            setQuestionText(questionTextLayout, k, q);
            setRadioButtons(optionsListLinearLayout, k, q);
            mIndex = k;
            deserialize(getSurvey().getResponseByQuestion(q).getText());
        }
    }

    private void setRadioButtons(LinearLayout optionsListLinearLayout, int k, final Question q) {
        LinearLayout choiceRow = new LinearLayout(getActivity());
        RadioGroup radioButtons = new RadioGroup(getActivity());
        radioButtons.setOrientation(RadioGroup.HORIZONTAL);
        RadioGroup.LayoutParams buttonParams = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT, MIN_HEIGHT);
        radioButtons.setLayoutParams(buttonParams);
        adjustRowHeight(radioButtons, k);
        for (GridLabel label : getGrid().labels()) {
            int id = getGrid().labels().indexOf(label);
            RadioButton button = new RadioButton(getActivity());
            button.setSaveEnabled(false);
            button.setId(id);
            button.setMinimumWidth(MIN_WIDTH);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(0, MIN_HEIGHT, 1f);
            params.setMargins(MARGIN_0, MARGIN_0, MARGIN_50, MARGIN_0);
            button.setLayoutParams(params);
            radioButtons.addView(button, id);
        }
        choiceRow.addView(radioButtons);
        optionsListLinearLayout.addView(choiceRow, k);
        radioButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setResponseIndex(q, checkedId);
            }
        });
        mRadioGroups.add(radioButtons);
    }

    private void setQuestionText(LinearLayout questionTextLayout, int k, Question q) {
        LinearLayout questionRow = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_10);
        questionRow.setLayoutParams(params);
        TextView questionNumber = new TextView(getActivity());
        questionNumber.setText(String.valueOf(q.getNumberInGrid() + "."));
        questionNumber.setMinHeight(MIN_HEIGHT);
        questionNumber.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams questionNumberParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        questionNumberParams.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_0);
        questionNumber.setLayoutParams(questionNumberParams);
        questionRow.addView(questionNumber);
        TextView questionText = new TextView(getActivity());
        questionText.setText(q.getText());
        questionText.setMinHeight(MIN_HEIGHT);
        questionRow.addView(questionText);
        questionTextLayout.addView(questionRow, k);
        setRowHeight(questionRow, k);
    }

    private void setTableHeaderOptions(View v) {
        TextView questionTextHeader = (TextView) v.findViewById(R.id.table_header_question_text);
        questionTextHeader.setMinHeight(MIN_HEIGHT);
        LinearLayout headerTableLayout = (LinearLayout) v.findViewById(R.id.table_options_header);
        for (GridLabel label : getGrid().labels()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(MARGIN_0, MARGIN_0, MARGIN_50, MARGIN_0);
            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(params);
            textView.setText(label.getLabelText());
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setMinimumWidth(MIN_WIDTH);
            textView.setMinHeight(MIN_HEIGHT);
            textView.setGravity(Gravity.START);
            headerTableLayout.addView(textView);
        }
    }

    private void setRowHeight(final LinearLayout view, final int position) {
        view.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                rowHeights[position] = view.getHeight() + params.topMargin + params.bottomMargin;
            }
        });
    }

    private void adjustRowHeight(final RadioGroup view, final int pos) {
        view.post(new Runnable() {
            @Override
            public void run() {
                int diff = rowHeights[pos] - view.getHeight();
                RadioGroup.LayoutParams params = (RadioGroup.LayoutParams) view.getLayoutParams();
                params.setMargins(MARGIN_0, diff/2, MARGIN_0, diff/2);
                view.setLayoutParams(params);
            }
        });
    }

    @Override
    protected String serialize() {
        return null;
    }

    private void setResponseIndex(Question q, int checkedId) {
        Response response = getSurvey().getResponseByQuestion(q);
        response.setResponse(String.valueOf(checkedId));
        if (isAdded() && !response.getText().equals("")) {
            response.setSpecialResponse("");
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
        response.save();
    }

    @Override
    public void onScrollChanged(OHScrollView scrollView, int x, int y, int oldX, int oldY) {
        if (interceptScroll) {
            interceptScroll = false;
            if (scrollView == headerScrollView) {
                contentScrollView.onOverScrolled(x, y, true, true);
            } else if (scrollView == contentScrollView) {
                headerScrollView.onOverScrolled(x, y, true, true);
            }
            interceptScroll = true;
        }
    }
}
