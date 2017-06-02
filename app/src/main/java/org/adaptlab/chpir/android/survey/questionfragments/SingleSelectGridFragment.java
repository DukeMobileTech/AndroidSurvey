package org.adaptlab.chpir.android.survey.questionfragments;

import android.graphics.Typeface;
import android.os.Bundle;
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
import org.adaptlab.chpir.android.survey.roster.views.OHScrollView;

import java.util.ArrayList;
import java.util.List;

public class SingleSelectGridFragment extends GridFragment {
    private static final String TAG = "SingleSelectGridFragment";
    private static final int MIN_HEIGHT = 80;
    private static final int MARGIN_10 = 10;
    private static final int MARGIN_0 = 0;
    private int mIndex;
    private List<RadioGroup> mRadioGroups;
    private List<TextView> mHeaderViews;
    private Integer[] rowHeights;
    private boolean interceptScroll = true;
    private OHScrollView headerScrollView;
    private OHScrollView contentScrollView;
    private int minimumWidth = 100;

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
            if (getSurvey().getResponseByQuestion(q) != null) {
                deserialize(getSurvey().getResponseByQuestion(q).getText());
            }
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
        List<GridLabel> gridLabels = getGrid().labels();
        for (int i = 0; i < gridLabels.size(); i++) {
            RadioButton button = new RadioButton(getActivity());
            button.setSaveEnabled(false);
            button.setId(i);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT, MIN_HEIGHT);
            button.setLayoutParams(params);
            adjustRowWidth(button, i);
            radioButtons.addView(button, i);
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
        List<GridLabel> gridLabels = getGrid().labels();
        mHeaderViews = new ArrayList<>();
        for (int k = 0; k < gridLabels.size(); k++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(params);
            textView.setText(gridLabels.get(k).getLabelText());
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setMinHeight(MIN_HEIGHT);
            textView.setGravity(Gravity.CENTER);
            headerTableLayout.addView(textView);
            setRowWidth(textView, k);
            mHeaderViews.add(textView);
        }
    }

    private void setRowWidth(final TextView view, final int position) {
        view.post(new Runnable() {
            @Override
            public void run() {
                if (view.getWidth() > minimumWidth) {
                    minimumWidth = view.getWidth();
                }
            }
        });
    }

    private void adjustRowWidth(final RadioButton view, final int position) {
        view.post(new Runnable() {
            @Override
            public void run() {
                RadioGroup.LayoutParams params = (RadioGroup.LayoutParams) view.getLayoutParams();
                int totalMargin = minimumWidth - view.getWidth();
                params.setMargins(totalMargin/2, 0, totalMargin/2, 0);
                view.setLayoutParams(params);
                mHeaderViews.get(position).setWidth(minimumWidth);
            }
        });
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
