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
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;

import java.util.ArrayList;
import java.util.List;

public class SingleSelectGridFragment extends GridFragment {
    private static final String TAG = "SingleSelectGridFragment";
    private int mIndex;
    private List<RadioGroup> mRadioGroups;
    private Integer[] rowHeights;
    private Integer[] rowWidths;
    private Integer[] labelWidths;

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
        View v = inflater.inflate(R.layout.fragment_table_question, parent, false);
        setTableHeaderOptions(v);
        setTableBodyContent(v);
        return v;
    }

    private void setTableBodyContent(View v) {
        LinearLayout questionTextLayout = (LinearLayout) v.findViewById(R.id
                .table_body_question_text);
        LinearLayout optionsListLinearLayout = (LinearLayout) v.findViewById(R.id
                .table_body_options_choice);
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
        RadioGroup.LayoutParams buttonParams = new RadioGroup.LayoutParams(RadioGroup
                .LayoutParams.MATCH_PARENT, MIN_HEIGHT);
        radioButtons.setLayoutParams(buttonParams);
        adjustRowHeight(radioButtons, k);
//        List<GridLabel> gridLabels = getGrid().labels();
//        for (int i = 0; i < gridLabels.size(); i++) {
        for (int i = 0; i < getDisplay().options().size(); i++) {
            RadioButton button = new RadioButton(getActivity());
            button.setSaveEnabled(false);
            button.setId(i);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams
                    .WRAP_CONTENT, MIN_HEIGHT);
            button.setLayoutParams(params);
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
//        questionNumber.setText(String.valueOf(q.getNumberInGrid() + "."));
        questionNumber.setText(String.valueOf((getQuestions().indexOf(q) + 1) + "."));
        questionNumber.setMinHeight(MIN_HEIGHT);
        questionNumber.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams questionNumberParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
        questionTextHeader.setPadding(10, 10, 10, 10);
        final LinearLayout headerTableLayout = (LinearLayout) v.findViewById(R.id
                .table_options_header);
//        final List<GridLabel> gridLabels = getGrid().labels();
        final List<Option> gridLabels = getDisplay().options();
        final List<TextView> headerViews = new ArrayList<>();
        rowWidths = new Integer[gridLabels.size()];
        labelWidths = new Integer[gridLabels.size()];
        for (int k = 0; k < gridLabels.size(); k++) {
            TextView textView = getHeaderTextView(gridLabels.get(k).getText());
            headerTableLayout.addView(textView);
            setRowWidth(headerTableLayout, textView, k);
            setLabelWidth(headerTableLayout, textView, k);
            headerViews.add(textView);
        }

        headerTableLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = 0;
                for (int k = 0; k < headerViews.size(); k++) {
                    TextView view = headerViews.get(k);
                    view.setWidth(labelWidths[k]);
                    view.setPadding(5, 0, 5, 10);
                    view.setMinimumWidth(width + 20);
                }
                for (RadioGroup radioGroup : mRadioGroups) {
                    for (int i = 0; i < gridLabels.size(); i++) {
                        RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                                (rowWidths[i], LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.gravity = Gravity.CENTER_VERTICAL;
                        radioButton.setLayoutParams(params);
                        radioButton.setPadding(MARGIN_10, MARGIN_0, MARGIN_10, MARGIN_0);
                        radioButton.setMinimumWidth(rowWidths[i]);
                    }
                }
            }
        });
    }

    private void setRowWidth(final LinearLayout layout, final TextView view, final int position) {
        view.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view
                        .getLayoutParams();
                rowWidths[position] = layout.getWidth() / rowWidths.length + params.leftMargin +
                        params.rightMargin;
            }
        });
    }

    private void setLabelWidth(final LinearLayout layout, final TextView view, final int position) {
        view.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view
                        .getLayoutParams();
                labelWidths[position] = layout.getWidth() / labelWidths.length + params
                        .leftMargin + params.rightMargin;
            }
        });
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
