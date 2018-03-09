package org.adaptlab.chpir.android.survey.questionfragments;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.GridFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.adaptlab.chpir.android.survey.FormatUtils.styleTextWithHtml;

public class MultipleSelectGridFragment extends GridFragment {

    private static final String TAG = "MultipleSelectGridFragment";
    private List<List<CheckBox>> mCheckBoxes;
    private List<Question> mQuestionList;
    private Integer[] rowHeights;
    private Integer[] rowWidths;
    private Integer[] labelWidths;
    private int mIndex;

    @Override
    protected void deserialize(String responseText) {
        List<CheckBox> checkBoxes = mCheckBoxes.get(mIndex);
        if (responseText.equals("")) {
            for (CheckBox box : checkBoxes) {
                if (box.isChecked()) {
                    box.setChecked(false);
                }
            }
        } else {
            String[] listOfIndices = responseText.split(LIST_DELIMITER);
            for (String index : listOfIndices) {
                if (!index.equals("")) {
                    Integer indexInteger = Integer.parseInt(index);
                    checkBoxes.get(indexInteger).setChecked(true);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_table_question, parent, false);
        setTableHeaderOptions(v);
        setTableBodyContent(v);
        return v;
    }

    private void setTableHeaderOptions(View v) {
        TextView questionTextHeader = (TextView) v.findViewById(R.id.table_header_question_text);
        questionTextHeader.setMinHeight(MIN_HEIGHT);
        questionTextHeader.setPadding(10, 10, 10, 10);
        final LinearLayout headerTableLayout = (LinearLayout) v.findViewById(R.id
                .table_options_header);
        final List<Option> gridLabels = getDisplay().options();
        rowWidths = new Integer[gridLabels.size()];
        labelWidths = new Integer[gridLabels.size()];
        final List<TextView> headers = new ArrayList<>();
        for (int k = 0; k < gridLabels.size(); k++) {
            TextView textView = getHeaderTextView(gridLabels.get(k).getText(getInstrument()));
            headerTableLayout.addView(textView);
            setRowWidth(headerTableLayout, textView, k);
            setLabelWidth(headerTableLayout, textView, k);
            headers.add(textView);
        }
        headerTableLayout.post(new Runnable() {
            @Override
            public void run() {
                int paddingLeftRight = 20;
                int width = 0;
                for (int k = 0; k < headers.size(); k++) {
                    TextView view = headers.get(k);
                    view.setWidth(labelWidths[k]);
                    view.setPadding(5, 0, 5, 10);
                    view.setMinimumWidth(width + paddingLeftRight);
                }
                for (List<CheckBox> checkBoxes : mCheckBoxes) {
                    for (int i = 0; i < checkBoxes.size(); i++) {
                        CheckBox checkBox = checkBoxes.get(i);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                                (rowWidths[i], LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.gravity = Gravity.CENTER_VERTICAL;
                        checkBox.setLayoutParams(params);
                        checkBox.setPadding(MARGIN_10, MARGIN_0, MARGIN_10, MARGIN_0);
                        checkBox.setMinimumWidth(rowWidths[i]);
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

    private void setTableBodyContent(View v) {
        LinearLayout questionTextLayout = (LinearLayout) v.findViewById(R.id
                .table_body_question_text);
        LinearLayout optionsListLinearLayout = (LinearLayout) v.findViewById(R.id
                .table_body_options_choice);
        mCheckBoxes = new ArrayList<>();
        mQuestionList = getQuestionExcludingSkip();
        rowHeights = new Integer[mQuestionList.size()];
        for (int k = 0; k < mQuestionList.size(); k++) {
            final Question q = mQuestionList.get(k);
            setQuestionText(questionTextLayout, k, q);
            setCheckBoxes(optionsListLinearLayout, k, q);
            mIndex = k;
            Response response = getSurvey().getResponseByQuestion(q);
            if (response != null) {
                deserialize(response.getText());
            }
        }
    }

    private void setQuestionText(LinearLayout questionTextLayout, int k, Question q) {
        LinearLayout questionRow = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
                .LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_10);
        questionRow.setLayoutParams(params);
        TextView questionNumber = new TextView(getActivity());
//        questionNumber.setText(String.valueOf(q.getNumberInGrid() + "."));
//        questionNumber.setText(String.valueOf((getQuestions().indexOf(q) + 1) + "."));
        questionNumber.setText(String.valueOf(q.getNumberInInstrument()+"."));
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

    private void setCheckBoxes(LinearLayout optionsListLinearLayout, int k, final Question q) {
        LinearLayout choiceRow = new LinearLayout(getActivity());
        List<CheckBox> checkBoxes = new ArrayList<>();
//        List<GridLabel> gridLabels = getGrid().labels();
        for (int i = 0; i < getDisplay().options().size(); i++) {
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setSaveEnabled(false);
            checkBox.setId(i);
            adjustRowHeight(checkBox, k);
            checkBoxes.add(checkBox);
            final int id = i;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setResponseIndexes(q, id, isChecked);
                }
            });
            choiceRow.addView(checkBox);
        }
        optionsListLinearLayout.addView(choiceRow, k);
        mCheckBoxes.add(checkBoxes);
    }

    private void adjustRowHeight(final CheckBox view, final int pos) {
        view.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view
                        .getLayoutParams();
                params.height = rowHeights[pos];
                view.setLayoutParams(params);
            }
        });
    }

    @Override
    protected String serialize() {
        return null;
    }

    protected void setResponseIndexes(Question q, int checkedId, boolean isChecked) {
        new SaveResponseTask(getSurvey(), q, checkedId, isChecked).execute();
    }

    private class SaveResponseTask extends AsyncTask<Void, Void, Void> {
        private String id;
        private Question question;
        private Survey survey;
        private boolean isChecked;

        private SaveResponseTask(Survey s, Question q, int checkedId, boolean status) {
            question = q;
            id = String.valueOf(checkedId);
            survey = s;
            isChecked = status;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Response response = survey.getResponseByQuestion(question);
            if (response == null) {
                response = new Response();
                response.setQuestion(question);
                response.setSurvey(survey);
            }
            String serialized = "";
            if (!response.getText().equals("")) {
                String[] listOfIndices = response.getText().split(LIST_DELIMITER);
                Set<String> responses = new HashSet<>(Arrays.asList(listOfIndices));
                if (responses.contains(id) && !isChecked) {
                    responses.remove(id);
                } else {
                    responses.add(id);
                }
                int size = 0;
                for (String str : responses) {
                    serialized += str;
                    if (size < responses.size() - 1)
                        serialized += LIST_DELIMITER;
                    size += 1;
                }
            } else {
                serialized = id;
            }
            response.setResponse(serialized);
            response.save();
            survey.save();
            return null;
        }
    }

    @Override
    protected void unSetResponse() {

    }

}