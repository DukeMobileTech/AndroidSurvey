package org.adaptlab.chpir.android.survey.questionfragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.GridFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.adaptlab.chpir.android.survey.FormatUtils.styleTextWithHtml;

public class SingleSelectGridFragment extends GridFragment {
    private static final String TAG = "SingleSelectGridFragment";
    private int mIndex;
    private List<RadioGroup> mRadioGroups;
    private List<Question> mQuestionList;
    private LinearLayout questionTextLayout;
    private LinearLayout optionsListLinearLayout;
    private View mView;
    private HashSet<Integer> mViewToHideSet;
    private Integer[] rowHeights;
    private int mOptionWidth;

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
            int checked = mRadioGroups.get(mIndex).getCheckedRadioButtonId();
            if (checked > -1) {
                ((RadioButton) mRadioGroups.get(mIndex).getChildAt(checked)).setChecked(false);
            }
        } else {
            ((RadioButton) mRadioGroups.get(mIndex).getChildAt(Integer.parseInt(responseText)))
                    .setChecked(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_table_question, parent, false);
        setTableHeaderOptions(mView);
        setTableBodyContent(mView);
        updateLayout();
        return mView;
    }

    private void setTableBodyContent(View v) {
        questionTextLayout = v.findViewById(R.id.table_body_question_text);
        optionsListLinearLayout = v.findViewById(R.id.table_body_options_choice);
        mRadioGroups = new ArrayList<>();
        mQuestionList = getQuestions();
        rowHeights = new Integer[mQuestionList.size()];
        for (int k = 0; k < mQuestionList.size(); k++) {
            final Question q = mQuestionList.get(k);
            setQuestionText(questionTextLayout, k, q);
            setRadioButtons(optionsListLinearLayout, k, q);
            mIndex = k;
            if (getSurvey().getResponseByQuestion(q) != null) {
                deserialize(getSurvey().getResponseByQuestion(q).getText());
            }
        }
    }

    private void updateViewToHideSet() {
        mViewToHideSet = new HashSet<>();
        for (int i = 0; i < mQuestionList.size(); i++) {
            if (mSurveyFragment.getQuestionsToSkipSet().contains(mQuestionList.get(i))) {
                mViewToHideSet.add(i);
            }
        }
    }

    private void updateLayout() {
        updateViewToHideSet();
        mView.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < questionTextLayout.getChildCount(); i++) {
                    View curQuestionTextView = questionTextLayout.getChildAt(i);
                    View curOptionListView = optionsListLinearLayout.getChildAt(i);
                    if (mViewToHideSet.contains(i)) {
                        setCurrentRowHeight(curQuestionTextView, 0);
                        setCurrentRowHeight(curOptionListView, 0);
                    } else {
                        if (rowHeights[i] != null) {
                            setCurrentRowHeight(curQuestionTextView, rowHeights[i]);
                            setCurrentRowHeight(curOptionListView, rowHeights[i]);
                        }
                    }
                }
            }
        });
    }

    private void setRadioButtons(LinearLayout optionsListLinearLayout, int k, final Question q) {
        LinearLayout choiceRow = new LinearLayout(getActivity());
        RadioGroup radioButtons = new RadioGroup(getActivity());
        radioButtons.setOrientation(RadioGroup.HORIZONTAL);
        RadioGroup.LayoutParams buttonParams = new RadioGroup.LayoutParams(RadioGroup
                .LayoutParams.MATCH_PARENT, MIN_HEIGHT);
        radioButtons.setLayoutParams(buttonParams);
        adjustRowHeight(radioButtons, k);
        for (int i = 0; i < getDisplay().options().size(); i++) {
            RadioButton button = new RadioButton(getActivity());
            button.setSaveEnabled(false);
            button.setId(i);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(mOptionWidth, MIN_HEIGHT);
            button.setLayoutParams(params);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateLayout();
                }
            });
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
                .LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_10);
        questionRow.setLayoutParams(params);
        TextView questionNumber = new TextView(getActivity());
        questionNumber.setText(String.valueOf(q.getNumberInInstrument() + "."));
        questionNumber.setMinHeight(MIN_HEIGHT);
        questionNumber.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams questionNumberParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        questionNumberParams.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_0);
        questionNumber.setLayoutParams(questionNumberParams);
        questionRow.addView(questionNumber);
        TextView questionText = new TextView(getActivity());
        questionText.setText(styleTextWithHtml(q.getText()));
        questionText.setMinHeight(MIN_HEIGHT);
        questionRow.addView(questionText);
        questionTextLayout.addView(questionRow, k);
        setRowHeight(questionRow, k);
    }

    private void setTableHeaderOptions(View v) {
        TextView questionTextHeader = v.findViewById(R.id.table_header_question_text);
        questionTextHeader.setMinHeight(MIN_HEIGHT);
        questionTextHeader.setPadding(10, 10, 10, 10);

        LinearLayout headerTableLayout = v.findViewById(R.id.table_options_header);
        final List<Option> headerLabels = getDisplay().options();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float margin = getActivity().getResources().getDimension(R.dimen.activity_horizontal_margin);
        float totalWidth = (displayMetrics.widthPixels - margin * 2) / 2;
        mOptionWidth = (int) totalWidth / headerLabels.size();

        for (int k = 0; k < headerLabels.size(); k++) {
            TextView textView = getHeaderTextView(headerLabels.get(k).getText(getInstrument()));
            textView.setWidth(mOptionWidth);
            headerTableLayout.addView(textView);
        }
    }

    private void setCurrentRowHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

    private void setRowHeight(final LinearLayout view, final int position) {
        view.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view
                        .getLayoutParams();
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
                params.setMargins(MARGIN_0, diff / 2, MARGIN_0, diff / 2);
                view.setLayoutParams(params);
            }
        });
    }

    @Override
    protected String serialize() {
        return null;
    }

    @Override
    protected void unSetResponse() {

    }
}
