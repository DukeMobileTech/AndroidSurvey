package org.adaptlab.chpir.android.survey.questionfragments;

import android.graphics.Typeface;
import android.util.DisplayMetrics;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.adaptlab.chpir.android.survey.FormatUtils.styleTextWithHtml;

public class MultipleSelectGridFragment extends GridFragment {

    private static final String TAG = "MultipleSelectGridFragment";
    private List<List<CheckBox>> mCheckBoxes;
    private Integer[] rowHeights;
    private int mIndex;
    private int mOptionWidth;

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

    private void setTableHeaderOptions(View v) {
        TextView questionTextHeader = v.findViewById(R.id.table_header_question_text);
        questionTextHeader.setMinHeight(MIN_HEIGHT);
        questionTextHeader.setPadding(10, 10, 10, 10);

        final LinearLayout headerTableLayout = v.findViewById(R.id.table_options_header);
        final List<Option> optionLabels = getDisplay().options();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float margin = getActivity().getResources().getDimension(R.dimen.activity_horizontal_margin);
        float totalWidth = (displayMetrics.widthPixels - margin * 2) / 2;
        mOptionWidth = (int) totalWidth / optionLabels.size();
        for (int k = 0; k < optionLabels.size(); k++) {
            TextView textView = getHeaderTextView(optionLabels.get(k).getText(getInstrument()));
            textView.setWidth(mOptionWidth);
            headerTableLayout.addView(textView);
        }
    }

    private void setTableBodyContent(View v) {
        LinearLayout questionTextLayout = v.findViewById(R.id.table_body_question_text);
        LinearLayout optionsListLinearLayout = v.findViewById(R.id.table_body_options_choice);
        mCheckBoxes = new ArrayList<>();
        List<Question> questionList = getQuestionExcludingSkip();
        rowHeights = new Integer[questionList.size()];
        for (int k = 0; k < questionList.size(); k++) {
            final Question q = questionList.get(k);
            setQuestionText(questionTextLayout, k, q);
            setCheckBoxes(optionsListLinearLayout, k, q);
            mIndex = k;
            Response response = getSurvey().getResponseByQuestion(q);
            if (response != null) {
                deserialize(response.getText());
            }
            createResponse(q);
        }
    }

    private void setQuestionText(LinearLayout questionTextLayout, int k, Question q) {
        LinearLayout questionRow = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
                .LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_10);
        questionRow.setLayoutParams(params);
        TextView questionNumber = new TextView(getActivity());
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
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                rowHeights[position] = view.getHeight() + params.topMargin + params.bottomMargin;
            }
        });
    }

    private void setCheckBoxes(LinearLayout optionsListLinearLayout, int k, final Question q) {
        LinearLayout choiceRow = new LinearLayout(getActivity());
        List<CheckBox> checkBoxes = new ArrayList<>();
        for (int i = 0; i < getDisplay().options().size(); i++) {
            CheckBox checkBox = new CheckBox(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mOptionWidth, MIN_HEIGHT);
            checkBox.setLayoutParams(params);
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
        saveResponse(q, checkedId, isChecked);
    }

    @Override
    protected void saveResponse(Question question, int checkedId, boolean isChecked) {
        Response response = getSurvey().getResponseByQuestion(question);
        if (response == null) {
            response = new Response();
            response.setQuestion(question);
            response.setSurvey(getSurvey());
        }
        StringBuilder serialized = new StringBuilder();
        if (!response.getText().equals("")) {
            String[] listOfIndices = response.getText().split(LIST_DELIMITER);
            Set<String> responses = new HashSet<>(Arrays.asList(listOfIndices));
            if (responses.contains(String.valueOf(checkedId)) && !isChecked) {
                responses.remove(String.valueOf(checkedId));
            } else {
                responses.add(String.valueOf(checkedId));
            }
            int size = 0;
            for (String str : responses) {
                serialized.append(str);
                if (size < responses.size() - 1)
                    serialized.append(LIST_DELIMITER);
                size += 1;
            }
        } else {
            serialized = new StringBuilder(String.valueOf(checkedId));
        }
        response.setResponse(serialized.toString());
        response.setTimeEnded(new Date());
        response.save();
        getSurvey().setLastQuestion(question);
        getSurvey().save();
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        View v = getLayoutInflater().inflate(R.layout.fragment_table_question, null);
        setTableHeaderOptions(v);
        setTableBodyContent(v);
        questionComponent.addView(v);
    }

    @Override
    protected void unSetResponse() {}

}