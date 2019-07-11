package org.adaptlab.chpir.android.survey.questionfragments;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.MultipleQuestionsFragment;
import org.adaptlab.chpir.android.survey.R;
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
    private static final String TAG = "MSMQF";
    private QuestionRecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void deserialize(String responseText) {
        // TODO: 11/4/18 Check if need to implement
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
        saveResponseInBackground(response);
        setLoopQuestions(question, response);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        View view = getLayoutInflater().inflate(R.layout.fragment_table_question, null);
        RecyclerView recyclerView = view.findViewById(R.id.tableRecyclerView);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                recyclerLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerViewAdapter = new QuestionRecyclerViewAdapter(getQuestions(), getTableHeaders(), getActivity());
        recyclerView.setAdapter(mRecyclerViewAdapter);
        questionComponent.addView(view);
    }

    @Override
    protected void unSetResponse() {}

    @Override
    protected void clearRegularResponseUI(int pos) {
        for (CheckBox checkBox : mRecyclerViewAdapter.mCheckBoxes.get(pos)) {
            checkBox.setChecked(false);
        }
    }

    private class QuestionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private List<List<CheckBox>> mCheckBoxes;
        private List<Question> questionList;
        private Context context;
        private String[] options;

        QuestionRecyclerViewAdapter(List<Question> questions, String[] optionList, Context ctx) {
            options = optionList;
            questionList = new ArrayList<>();
            questionList.add(new Question());
            questionList.addAll(questions);
            context = ctx;
            options = optionList;
            mCheckBoxes = new ArrayList<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = null;
            if (viewType == TYPE_HEADER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_row_layout, parent, false);
                viewHolder = new MultipleQuestionsFragment.HeaderHolder(view);
            } else if (viewType == TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_table_row_layout, parent, false);
                viewHolder = new QuestionRecyclerViewAdapter.ViewHolder(view);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            final Question question = questionList.get(position);
            if (viewHolder instanceof MultipleSelectMultipleQuestionsFragment.QuestionRecyclerViewAdapter.ViewHolder) {
                MultipleSelectMultipleQuestionsFragment.QuestionRecyclerViewAdapter.ViewHolder holder = (MultipleSelectMultipleQuestionsFragment.QuestionRecyclerViewAdapter.ViewHolder) viewHolder;
                String questionText = "<b>" + question.getPosition() + "</b>: " + getQuestionText(question);
                holder.questionText.setText(styleTextWithHtml(questionText));
                final Button specialResponseButton = new Button(context);
                List<CheckBox> checkBoxes = new ArrayList<>();
                for (int k = 0; k < options.length; k++) {
                    CheckBox checkBox = new CheckBox(getActivity());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getOptionWidth()/2,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    params.leftMargin = getOptionWidth()/2;
                    checkBox.setLayoutParams(params);
                    checkBox.setId(k);
                    checkBoxes.add(checkBox);
                    holder.optionsPart.addView(checkBox);
                    final int id = k;
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean isChecked = ((CheckBox) v).isChecked();
                            setResponseIndexes(question, id, isChecked);
                            specialResponseButton.setText("");
                        }
                    });
                }
                mCheckBoxes.add(position, checkBoxes);
                addSpecialResponseUI(position, question, holder.optionsPart, specialResponseButton);
                createResponse(question);
                deserialize(position);
                updateLayout();
            } else if (viewHolder instanceof HeaderHolder) {
                MultipleQuestionsFragment.HeaderHolder holder = (MultipleQuestionsFragment.HeaderHolder) viewHolder;
                mCheckBoxes.add(position, new ArrayList<CheckBox>()); // Empty Placeholder
                holder.questionText.setText(getString(R.string.questions));
                for (String option : options) {
                    setResponseHeader(holder, option, context);
                }
                if (questionList.get(1).hasSpecialOptions()) {
                    setResponseHeader(holder, getString(R.string.special_response), context);
                }
            }
        }

        private void deserialize(int position) {
            Response response = getSurvey().getResponseByQuestion(questionList.get(position));
            if (response != null) {
                String responseText = response.getText();
                List<CheckBox> checkBoxes = mCheckBoxes.get(position);
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
        }

        private void updateLayout() { }

        @Override
        public int getItemCount() {
            return questionList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            } else {
                return TYPE_ITEM;
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView questionText;
            LinearLayout optionsPart;

            ViewHolder(View view) {
                super(view);
                questionText = view.findViewById(R.id.questionColumn);
                questionText.setTextColor(context.getResources().getColor(R.color.blue));
                optionsPart = view.findViewById(R.id.optionsPart);
            }
        }
    }
}