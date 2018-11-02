package org.adaptlab.chpir.android.survey.questionfragments;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.MultipleQuestionsFragment;
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

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public class MultipleSelectMultipleQuestionsFragment extends MultipleQuestionsFragment {

    private static final String TAG = "MultiSelMultiQuestFrag";
    private List<List<CheckBox>> mCheckBoxes;
    private Integer[] rowHeights;
    private int mIndex;
    private RecyclerView mRecyclerView;


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
            String[] listOfIndices = responseText.split(Response.LIST_DELIMITER);
            for (String index : listOfIndices) {
                if (!index.equals("")) {
                    Integer indexInteger = Integer.parseInt(index);
                    checkBoxes.get(indexInteger).setChecked(true);
                }
            }
        }
    }

    private void setTableBodyContent(View v) {
//        LinearLayout questionTextLayout = v.findViewById(R.id.table_body_question_text);
//        LinearLayout optionsListLinearLayout = v.findViewById(R.id.table_body_options_choice);
        mCheckBoxes = new ArrayList<>();
        List<Question> questionList = getQuestionExcludingSkip();
        rowHeights = new Integer[questionList.size()];
        for (int k = 0; k < questionList.size(); k++) {
            final Question q = questionList.get(k);
//            setQuestionText(questionTextLayout, k, q);
//            setCheckBoxes(optionsListLinearLayout, k, q);
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
        final Button specialResponseButton = new Button(getActivity());
        for (int i = 0; i < getDisplay().options().size(); i++) {
            CheckBox checkBox = new CheckBox(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getOptionWidth(), MIN_HEIGHT);
            checkBox.setLayoutParams(params);
            checkBox.setSaveEnabled(false);
            checkBox.setId(i);
            adjustRowHeight(checkBox, k);
            checkBoxes.add(checkBox);
            final int id = i;
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = ((CheckBox) v).isChecked();
                    setResponseIndexes(q, id, isChecked);
                    specialResponseButton.setText("");
                }
            });
            choiceRow.addView(checkBox);
        }
        addSpecialResponseUI(k, q, choiceRow, specialResponseButton);
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
            String[] listOfIndices = response.getText().split(Response.LIST_DELIMITER);
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
                    serialized.append(Response.LIST_DELIMITER);
                size += 1;
            }
        } else {
            serialized = new StringBuilder(String.valueOf(checkedId));
        }
        response.setResponse(serialized.toString());
        response.setSpecialResponse("");
        response.setTimeEnded(new Date());
        response.save();
        getSurvey().save();
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        View mView = getLayoutInflater().inflate(R.layout.fragment_table_question, null);
//        setTableHeaderOptions(v);
//        setTableBodyContent(v);
//        questionComponent.addView(v);
        mRecyclerView = mView.findViewById(R.id.tableRecyclerView);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(recyclerLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                recyclerLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        String[] headers = new String[getDisplay().tableOptions(getTableIdentifier()).size()];
        for (int k = 0; k < getDisplay().tableOptions(getTableIdentifier()).size(); k++) {
            headers[k] = getDisplay().tableOptions(getTableIdentifier()).get(k).getText(getInstrument());
        }

        QuestionRecyclerViewAdapter recyclerViewAdapter = new QuestionRecyclerViewAdapter(getQuestions(),
                headers, getActivity());
        mRecyclerView.setAdapter(recyclerViewAdapter);

        questionComponent.addView(mView);
    }

    @Override
    protected void unSetResponse() {}

    @Override
    protected void clearRegularResponseUI(int pos) {
        for (CheckBox checkBox : mCheckBoxes.get(pos)) {
            checkBox.setChecked(false);
        }
    }

    private class QuestionRecyclerViewAdapter extends RecyclerView.Adapter<QuestionRecyclerViewAdapter.ViewHolder> {
        private List<List<CheckBox>> mCheckBoxes;
        private List<Question> questions;
        private Context context;
        private String[] options;

        public QuestionRecyclerViewAdapter(List<Question> questionList, String[] optionList, Context ctx) {
            questions = questionList;
            context = ctx;
            options = optionList;
            mCheckBoxes = new ArrayList<>();
        }

        @Override
        public QuestionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_table_row_layout, parent, false);
            QuestionRecyclerViewAdapter.ViewHolder viewHolder = new QuestionRecyclerViewAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(QuestionRecyclerViewAdapter.ViewHolder holder, int position) {
            Question question = questions.get(position);
            holder.questionText.setText(question.getText());
//            holder.position = position;
//            int id = (position + 1) * 100;
//            for(Option option : options){
//                RadioButton rb = new RadioButton(QuestionRecyclerViewAdapter.this.context);
//                rb.setId(id++);
//                rb.setText(option.getText(question.getInstrument()));
//                holder.optionsGroup.addView(rb);
//            }
//            radioGroups.add(position, holder.optionsGroup);

            List<CheckBox> checkBoxes = new ArrayList<>();
            for (int i = 0; i < options.length; i++) {
                CheckBox checkBox = new CheckBox(getActivity());
//                checkBox.setSaveEnabled(false);
                checkBox.setId((question.getNumberInInstrument() * 100) + i);
//                checkBox.setText(options[i]);
//                Log.i(TAG,"TEXT: " + options[i]);
//                checkBox.setText(options.get(i).getText(question.getInstrument()));
                checkBoxes.add(checkBox);
//                holder.optionsGroup.addView(checkBox, i);

                holder.optionsGroup.addView(checkBox);
//                GridLayout.LayoutParams params = (GridLayout.LayoutParams) checkBox.getLayoutParams();
//                params.rowSpec = GridLayout.spec(0, 3);
//                params.columnSpec = GridLayout.spec(i, 1);
//                params.setGravity(Gravity.FILL_HORIZONTAL);
//                checkBox.setLayoutParams(params);
            }
            mCheckBoxes.add(position, checkBoxes);
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView questionText;
            public LinearLayout optionsGroup;

            public ViewHolder(View view) {
                super(view);
                questionText = view.findViewById(R.id.questionColumn);
                optionsGroup = view.findViewById(R.id.optionsPart);

//                optionsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        //since only one package is allowed to be selected
                        //this logic clears previous selection
                        //it checks state of last radiogroup and
                        // clears it if it meets conditions
//                        if (lastCheckedRadioGroup != null
//                                && lastCheckedRadioGroup.getCheckedRadioButtonId()
//                                != radioGroup.getCheckedRadioButtonId()
//                                && lastCheckedRadioGroup.getCheckedRadioButtonId() != -1) {
//                            lastCheckedRadioGroup.clearCheck();
//
//                            Toast.makeText(QuestionRecyclerViewAdapter.this.context,
//                                    "Radio button clicked " + radioGroup.getCheckedRadioButtonId(),
//                                    Toast.LENGTH_SHORT).show();
//
//                        }
//                        lastCheckedRadioGroup = radioGroup;

//                    }
//                });
            }
        }
    }
}