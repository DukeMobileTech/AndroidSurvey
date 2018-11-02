package org.adaptlab.chpir.android.survey.questionfragments;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public class SingleSelectMultipleQuestionsFragment extends MultipleQuestionsFragment {
    private static final String TAG = "SingleSelectMultipleQuestionsFragment";
    private int mIndex;
//    private List<RadioGroup> mRadioGroups;
//    private List<Question> mQuestionList;
    private LinearLayout questionTextLayout;
    private LinearLayout optionsListLinearLayout;
//    private View mView;
    private HashSet<Integer> mViewToHideSet;
//    private Integer[] rowHeights;

    private QuestionRecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void deserialize(String responseText) {
        // TODO: 11/3/18 Call adapter deserialize
//        if (responseText.equals("")) {
//            int checked = mRecyclerViewAdapter.radioGroups.get(mIndex).getCheckedRadioButtonId();
//            if (checked > -1) {
//                ((RadioButton) mRecyclerViewAdapter.radioGroups.get(mIndex).getChildAt(checked)).setChecked(false);
//            }
//        } else {
//            RadioButton button = (RadioButton) mRecyclerViewAdapter.radioGroups.get(mIndex).getChildAt(
//                    Integer.parseInt(responseText));
//            if (button != null) {
//                button.setChecked(true);
//            }
//        }
    }

    private void setTableBodyContent(View v) {
//        questionTextLayout = v.findViewById(R.id.table_body_question_text);
//        optionsListLinearLayout = v.findViewById(R.id.table_body_options_choice);
//        mRadioGroups = new ArrayList<>();
//        mQuestionList = getQuestions();
//        rowHeights = new Integer[mQuestionList.size()];
//        for (int k = 0; k < mQuestionList.size(); k++) {
//            final Question q = mQuestionList.get(k);
//            setQuestionText(questionTextLayout, k, q);
//            setRadioButtons(optionsListLinearLayout, k, q);
//            createResponse(q);
//            mIndex = k;
//            if (getSurvey().getResponseByQuestion(q) != null) {
//                deserialize(getSurvey().getResponseByQuestion(q).getText());
//            }
//        }
    }

    private void updateViewToHideSet() {
        mViewToHideSet = new HashSet<>();
        for (int i = 0; i < mRecyclerViewAdapter.mQuestionList.size(); i++) {
            if (mSurveyFragment.getQuestionsToSkipSet().contains(mRecyclerViewAdapter.mQuestionList.get(i))) {
                mViewToHideSet.add(i);
            }
        }
    }

//    private void updateLayout() {
//        updateViewToHideSet();
//        mView.post(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < questionTextLayout.getChildCount(); i++) {
//                    View curQuestionTextView = questionTextLayout.getChildAt(i);
//                    View curOptionListView = optionsListLinearLayout.getChildAt(i);
//                    if (mViewToHideSet.contains(i)) {
//                        setCurrentRowHeight(curQuestionTextView, 0);
//                        setCurrentRowHeight(curOptionListView, 0);
//                    } else if (rowHeights[i] != null) {
//                        setCurrentRowHeight(curQuestionTextView, rowHeights[i]);
//                        setCurrentRowHeight(curOptionListView, rowHeights[i]);
//                    }
//                }
//            }
//        });
//    }

//    private void setRadioButtons(LinearLayout optionsListLinearLayout, final int k, final
//    Question q) {
//        LinearLayout choiceRow = new LinearLayout(getActivity());
//        choiceRow.setOrientation(LinearLayout.HORIZONTAL);
//        final RadioGroup radioButtons = new RadioGroup(getActivity());
//        radioButtons.setOrientation(RadioGroup.HORIZONTAL);
//        RadioGroup.LayoutParams buttonParams = new RadioGroup.LayoutParams(RadioGroup
//                .LayoutParams.MATCH_PARENT, MIN_HEIGHT);
//        buttonParams.gravity = Gravity.CENTER;
//        radioButtons.setLayoutParams(buttonParams);
//        adjustRowHeight(radioButtons, k);
//        final int normalOptionsSize = getDisplay().tableOptions(getTableIdentifier()).size();
//        final Button specialResponseButton = new Button(getActivity());
//        for (int i = 0; i < normalOptionsSize; i++) {
//            RadioButton button = new RadioButton(getActivity());
//            button.setSaveEnabled(false);
//            button.setId(i);
//            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(getOptionWidth(), MIN_HEIGHT);
//            button.setLayoutParams(params);
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
////                    updateLayout();
//                    updateViewToHideSet();
//                    specialResponseButton.setText("");
//                }
//            });
//            radioButtons.addView(button, i);
//        }
//
//        LinearLayout radioGroupLayout = new LinearLayout(getActivity());
//        radioGroupLayout.setLayoutParams(new LinearLayout.LayoutParams(normalOptionsSize *
//                getOptionWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
//        radioGroupLayout.addView(radioButtons);
//        choiceRow.addView(radioGroupLayout);
//
//        addSpecialResponseUI(k, q, choiceRow, specialResponseButton);
//
//        optionsListLinearLayout.addView(choiceRow, k);
//        radioButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                setResponseIndex(q, checkedId);
//            }
//        });
////        mRadioGroups.add(radioButtons);
//    }

//    private void setQuestionText(LinearLayout questionTextLayout, int k, Question q) {
//        LinearLayout questionRow = new LinearLayout(getActivity());
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
//                .LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        params.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_10);
//        questionRow.setLayoutParams(params);
//        TextView questionNumber = new TextView(getActivity());
//        questionNumber.setText(String.valueOf(q.getNumberInInstrument() + "."));
//        questionNumber.setMinHeight(MIN_HEIGHT);
//        questionNumber.setTypeface(Typeface.DEFAULT_BOLD);
//        LinearLayout.LayoutParams questionNumberParams = new LinearLayout.LayoutParams
//                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        questionNumberParams.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_0);
//        questionNumber.setLayoutParams(questionNumberParams);
//        questionRow.addView(questionNumber);
//        TextView questionText = new TextView(getActivity());
//        questionText.setText(styleTextWithHtml(q.getText()));
//        questionText.setMinHeight(MIN_HEIGHT);
//        questionRow.addView(questionText);
//        questionTextLayout.addView(questionRow, k);
//        setRowHeight(questionRow, k);
//    }

//    private void setCurrentRowHeight(View view, int height) {
//        ViewGroup.LayoutParams params = view.getLayoutParams();
//        params.height = height;
//        view.setLayoutParams(params);
//    }

//    private void setRowHeight(final LinearLayout view, final int position) {
//        view.post(new Runnable() {
//            @Override
//            public void run() {
//                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
//                rowHeights[position] = view.getHeight() + params.topMargin + params.bottomMargin;
//            }
//        });
//    }

//    private void adjustRowHeight(final RadioGroup view, final int pos) {
//        view.post(new Runnable() {
//            @Override
//            public void run() {
//                int diff = rowHeights[pos] - view.getHeight();
//                RadioGroup.LayoutParams params = (RadioGroup.LayoutParams) view.getLayoutParams();
//                params.setMargins(MARGIN_0, diff / 2, MARGIN_0, diff / 2);
//                view.setLayoutParams(params);
//            }
//        });
//    }

    @Override
    protected String serialize() {
        return null;
    }

    @Override
    protected void unSetResponse() { }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        mView = getLayoutInflater().inflate(R.layout.fragment_table_question, null);
//        setTableHeaderOptions(mView);
//        setTableBodyContent(mView);
//        updateLayout();

        View view = getLayoutInflater().inflate(R.layout.fragment_table_question, null);
        RecyclerView recyclerView = view.findViewById(R.id.tableRecyclerView);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                        recyclerLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerViewAdapter = new QuestionRecyclerViewAdapter(getQuestions(),
                getDisplay().tableOptions(getTableIdentifier()), getActivity());
        recyclerView.setAdapter(mRecyclerViewAdapter);

        questionComponent.addView(view);
    }

    @Override
    protected void clearRegularResponseUI(int position) {
        mRecyclerViewAdapter.radioGroups.get(position).clearCheck();
    }

    private class QuestionRecyclerViewAdapter extends RecyclerView.Adapter<QuestionRecyclerViewAdapter.ViewHolder> {
        List<RadioGroup> radioGroups;
        List<Question> mQuestionList;
        private Context context;
        private List<Option> options;

        QuestionRecyclerViewAdapter(List<Question> questionList, List<Option> optionList, Context ctx) {
            mQuestionList = questionList;
            context = ctx;
            options = optionList;
            radioGroups = new ArrayList<>();
        }

        @Override
        public QuestionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.radio_table_row_layout, parent, false);
            QuestionRecyclerViewAdapter.ViewHolder viewHolder = new QuestionRecyclerViewAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(QuestionRecyclerViewAdapter.ViewHolder holder, int position) {
            final Question question = mQuestionList.get(position);
            String questionText = "<b>" + question.getNumberInInstrument() + "</b>: " + question.getText();
            holder.questionText.setText(styleTextWithHtml(questionText));
            final Button specialResponseButton = new Button(getActivity());
            for(int k = 0; k < options.size(); k++){
                RadioButton rb = new RadioButton(QuestionRecyclerViewAdapter.this.context);
                rb.setId((question.getNumberInInstrument() * 100) + position + k);
                rb.setText(options.get(k).getText(question.getInstrument()));
                holder.radioGroup.addView(rb);
                rb.setOnClickListener(new View.OnClickListener() {
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

//                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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
