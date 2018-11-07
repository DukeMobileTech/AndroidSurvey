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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.MultipleQuestionsFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public class SingleSelectMultipleQuestionsFragment extends MultipleQuestionsFragment {
    private static final String TAG = "SSMQF";
    private HashSet<Integer> mViewToHideSet;
    private QuestionRecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void deserialize(String responseText) {
        // TODO: 11/3/18 Call adapter deserialize
    }

    private void updateViewToHideSet() {
        mViewToHideSet = new HashSet<>();
        for (int i = 0; i < mRecyclerViewAdapter.mQuestionList.size(); i++) {
            if (mSurveyFragment.getQuestionsToSkipSet().contains(mRecyclerViewAdapter.mQuestionList.get(i).getQuestionIdentifier())) {
                mViewToHideSet.add(i);
            }
        }
    }

    @Override
    protected String serialize() {
        return null;
    }

    @Override
    protected void unSetResponse() { }

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
    protected void clearRegularResponseUI(int position) {
        mRecyclerViewAdapter.radioGroups.get(position).clearCheck();
    }

    private class QuestionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        List<RadioGroup> radioGroups;
        List<Question> mQuestionList;
        private Context context;
        private String[] options;

        QuestionRecyclerViewAdapter(List<Question> questionList, String[] optionList, Context ctx) {
            mQuestionList = new ArrayList<>();
            mQuestionList.add(new Question());
            mQuestionList.addAll(questionList);
            context = ctx;
            options = optionList;
            radioGroups = new ArrayList<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = null;
            if (viewType == TYPE_HEADER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_row_layout, parent, false);
                viewHolder = new MultipleQuestionsFragment.HeaderHolder(view);
            } else if (viewType == TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.radio_table_row_layout, parent, false);
                viewHolder = new QuestionRecyclerViewAdapter.ViewHolder(view);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            final Question question = mQuestionList.get(position);
            if (viewHolder instanceof ViewHolder) {
                QuestionRecyclerViewAdapter.ViewHolder holder = (QuestionRecyclerViewAdapter.ViewHolder) viewHolder;
                String questionText = "<b>" + question.getNumberInInstrument() + "</b>: " + question.getText();
                holder.questionText.setText(styleTextWithHtml(questionText));
                final Button specialResponseButton = new Button(context);
                for (int k = 0; k < options.length; k++) {
                    RadioButton radioButton = new RadioButton(context);
                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(getOptionWidth()/2,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    params.leftMargin = getOptionWidth()/2;
                    radioButton.setLayoutParams(params);
                    radioButton.setId(k);
                    holder.radioGroup.addView(radioButton);
                    radioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateViewToHideSet();
                            specialResponseButton.setText("");
                            updateLayout();
                        }
                    });
                }
                radioGroups.add(position, holder.radioGroup);
                holder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        setResponseIndex(question, checkedId);
                    }
                });
                addSpecialResponseUI(position, question, holder.optionsPart, specialResponseButton);
                createResponse(question);
                deserialize(position);
                updateLayout();
            } else if (viewHolder instanceof HeaderHolder) {
                MultipleQuestionsFragment.HeaderHolder holder = (MultipleQuestionsFragment.HeaderHolder) viewHolder;
                radioGroups.add(position, new RadioGroup(context)); // Empty Placeholder
                holder.questionText.setText(getString(R.string.questions));
                for (String option : options) {
                    setResponseHeader(holder, option, context);
                }
                if (mQuestionList.get(1).hasSpecialOptions()) {
                    setResponseHeader(holder, getString(R.string.special_response), context);
                }
            }
        }

        private void deserialize(int position) {
            Response response = getSurvey().getResponseByQuestion(mQuestionList.get(position));
            if (response != null) {
                String responseText = response.getText();
                if (responseText.equals("")) {
                    int checked = radioGroups.get(position).getCheckedRadioButtonId();
                    if (checked > -1) {
                        ((RadioButton) radioGroups.get(position).getChildAt(checked)).setChecked(false);
                    }
                } else {
                    RadioButton button = (RadioButton) radioGroups.get(position).getChildAt(
                            Integer.parseInt(responseText));
                    if (button != null) {
                        button.setChecked(true);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return mQuestionList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            } else {
                return TYPE_ITEM;
            }
        }

        void updateLayout() {

        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView questionText;
            RadioGroup radioGroup;
            LinearLayout optionsPart;

            ViewHolder(View view) {
                super(view);
                questionText = view.findViewById(R.id.questionColumn);
                radioGroup = view.findViewById(R.id.optionsRadioGroup);
                optionsPart = view.findViewById(R.id.optionsPart);
            }
        }
    }

}
