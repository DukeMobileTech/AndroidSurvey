package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;

public class MultipleSelectMultipleViewHolder extends MultipleQuestionsViewHolder {

//    private QuestionRecyclerViewAdapter mRecyclerViewAdapter;

    public MultipleSelectMultipleViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    protected Question getQuestion() {
        return null;
    }

    @Override
    protected void deserialize(String responseText) {
    }

    @Override
    protected void deserializeSpecialResponse() {
    }

    @Override
    protected void deserializeOtherResponse(String otherResponse) {
    }

    @Override
    protected String serialize() {
        return null;
    }

//    protected void setResponseIndexes(Question q, int checkedId, boolean isChecked) {
//        saveResponse(q, checkedId, isChecked);
//    }
//
//    @Override
//    protected void saveResponse(Question question, int checkedId, boolean isChecked) {
//        Response response = getSurveyRelation().getResponseByQuestion(question);
//        if (response == null) {
//            response = new Response();
//            response.setQuestionRelation(question);
//            response.setData(getSurveyRelation());
//        }
//        StringBuilder serialized = new StringBuilder();
//        if (!response.getText().equals("")) {
//            String[] listOfIndices = response.getText().split(Response.LIST_DELIMITER);
//            Set<String> responses = new HashSet<>(Arrays.asList(listOfIndices));
//            if (responses.contains(String.valueOf(checkedId)) && !isChecked) {
//                responses.remove(String.valueOf(checkedId));
//            } else {
//                responses.add(String.valueOf(checkedId));
//            }
//            int size = 0;
//            for (String str : responses) {
//                serialized.append(str);
//                if (size < responses.size() - 1)
//                    serialized.append(Response.LIST_DELIMITER);
//                size += 1;
//            }
//        } else {
//            serialized = new StringBuilder(String.valueOf(checkedId));
//        }
//        response.setResponse(serialized.toString());
//        response.setSpecialResponse("");
//        response.setTimeEnded(new Date());
//        saveResponseInBackground(response);
//        setLoopQuestions(question, response);
//    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        View view = getLayoutInflater().inflate(R.layout.fragment_table_question, null);
//        RecyclerView recyclerView = view.findViewById(R.id.tableRecyclerView);
//        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
//        recyclerView.setLayoutManager(recyclerLayoutManager);
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
//                recyclerLayoutManager.getOrientation());
//        recyclerView.addItemDecoration(dividerItemDecoration);
//        mRecyclerViewAdapter = new QuestionRecyclerViewAdapter(getQuestions(), getTableHeaders(), getActivity());
//        recyclerView.setAdapter(mRecyclerViewAdapter);
//        questionComponent.addView(view);
    }

    @Override
    public void setQuestionRelation(QuestionRelation questionRelation) {

    }

//    @Override
//    protected void unSetResponse() {}
//
//    @Override
//    protected void clearRegularResponseUI(int pos) {
//        for (CheckBox checkBox : mRecyclerViewAdapter.mCheckBoxes.get(pos)) {
//            checkBox.setChecked(false);
//        }
//    }
//
//    private class QuestionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//        private static final int TYPE_HEADER = 0;
//        private static final int TYPE_ITEM = 1;
//        private List<List<CheckBox>> mCheckBoxes;
//        private List<Question> questionList;
//        private Context context;
//        private String[] options;
//
//        QuestionRecyclerViewAdapter(List<Question> questions, String[] optionList, Context ctx) {
//            options = optionList;
//            questionList = new ArrayList<>();
//            questionList.add(new Question());
//            questionList.addAll(questions);
//            context = ctx;
//            options = optionList;
//            mCheckBoxes = new ArrayList<>();
//        }
//
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            RecyclerView.ViewHolder viewHolder = null;
//            if (viewType == TYPE_HEADER) {
//                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_row_layout, parent, false);
//                viewHolder = new HeaderHolder(view);
//            } else if (viewType == TYPE_ITEM) {
//                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_table_row_layout, parent, false);
//                viewHolder = new QuestionRecyclerViewAdapter.ViewHolder(view);
//            }
//            return viewHolder;
//        }
//
//        @Override
//        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
//            final Question question = questionList.get(position);
//            if (viewHolder instanceof MultipleSelectMultipleViewHolder.QuestionRecyclerViewAdapter.ViewHolder) {
//                MultipleSelectMultipleViewHolder.QuestionRecyclerViewAdapter.ViewHolder holder = (MultipleSelectMultipleViewHolder.QuestionRecyclerViewAdapter.ViewHolder) viewHolder;
//                String questionText = "<b>" + question.getNumberInInstrument() + "</b>: " + getQuestionText(question);
//                holder.questionText.setText(styleTextWithHtml(questionText));
//                final Button specialResponseButton = new Button(context);
//                List<CheckBox> checkBoxes = new ArrayList<>();
//                for (int k = 0; k < options.length; k++) {
//                    CheckBox checkBox = new CheckBox(getActivity());
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getOptionWidth()/2,
//                            ViewGroup.LayoutParams.MATCH_PARENT);
//                    params.leftMargin = getOptionWidth()/2;
//                    checkBox.setLayoutParams(params);
//                    checkBox.setId(k);
//                    checkBoxes.add(checkBox);
//                    holder.optionsPart.addView(checkBox);
//                    final int id = k;
//                    checkBox.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            boolean isChecked = ((CheckBox) v).isChecked();
//                            setResponseIndexes(question, id, isChecked);
//                            specialResponseButton.setText("");
//                        }
//                    });
//                }
//                mCheckBoxes.add(position, checkBoxes);
//                addSpecialResponseUI(position, question, holder.optionsPart, specialResponseButton);
//                createResponse(question);
//                deserialize(position);
//                updateLayout();
//            } else if (viewHolder instanceof HeaderHolder) {
//                HeaderHolder holder = (HeaderHolder) viewHolder;
//                mCheckBoxes.add(position, new ArrayList<CheckBox>()); // Empty Placeholder
//                holder.questionText.setText(getString(R.string.questions));
//                for (String option : options) {
//                    setResponseHeader(holder, option, context);
//                }
//                if (questionList.get(1).hasSpecialOptions()) {
//                    setResponseHeader(holder, getString(R.string.special_response), context);
//                }
//            }
//        }
//
//        private void deserialize(int position) {
//            Response response = getSurveyRelation().getResponseByQuestion(questionList.get(position));
//            if (response != null) {
//                String responseText = response.getText();
//                List<CheckBox> checkBoxes = mCheckBoxes.get(position);
//                if (responseText.equals("")) {
//                    for (CheckBox box : checkBoxes) {
//                        if (box.isChecked()) {
//                            box.setChecked(false);
//                        }
//                    }
//                } else {
//                    String[] listOfIndices = responseText.split(Response.LIST_DELIMITER);
//                    for (String index : listOfIndices) {
//                        if (!index.equals("")) {
//                            Integer indexInteger = Integer.parseInt(index);
//                            checkBoxes.get(indexInteger).setChecked(true);
//                        }
//                    }
//                }
//            }
//        }
//
//        private void updateLayout() { }
//
//        @Override
//        public int getItemCount() {
//            return questionList.size();
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            if (position == 0) {
//                return TYPE_HEADER;
//            } else {
//                return TYPE_ITEM;
//            }
//        }
//
//        class ViewHolder extends RecyclerView.ViewHolder {
//            TextView questionText;
//            LinearLayout optionsPart;
//
//            ViewHolder(View view) {
//                super(view);
//                questionText = view.findViewById(R.id.questionColumn);
//                questionText.setTextColor(context.getResources().getColor(R.color.blue));
//                optionsPart = view.findViewById(R.id.optionsPart);
//            }
//        }
//    }
}