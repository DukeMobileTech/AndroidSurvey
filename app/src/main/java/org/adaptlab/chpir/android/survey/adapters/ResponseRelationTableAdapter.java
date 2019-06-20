package org.adaptlab.chpir.android.survey.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolder;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolderFactory;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.SELECT_MULTIPLE_TABLE;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.SELECT_ONE_TABLE;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.TABLE_HEADER;

public class ResponseRelationTableAdapter extends ResponseRelationAdapter {

    public ResponseRelationTableAdapter(QuestionViewHolder.OnResponseSelectedListener listener) {
        super(listener);
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == QuestionViewHolderFactory.TABLE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_question_table_header, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_question_table, parent, false);
        }
        return QuestionViewHolderFactory.createViewHolder(view, parent.getContext(), viewType, getListener());
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder viewHolder, int position) {
        viewHolder.setAdapter(this);
        if (getSurveyViewModel() != null) viewHolder.setSurveyViewModel(getSurveyViewModel());
        QuestionRelation questionRelation = getQuestionRelation(position);
        if (questionRelation == null) return;
        viewHolder.setRelations(questionRelation);
    }

    @Override
    public int getItemViewType(int position) {
        QuestionRelation questionRelation = getQuestionRelation(position);
        if (questionRelation == null) return -1;
        String type = questionRelation.question.getQuestionType();
        if (position == 0 && (type.equals(Question.SELECT_ONE) || type.equals(Question.SELECT_MULTIPLE))) {
            type = TABLE_HEADER;
        } else if (type.equals(Question.SELECT_ONE)) {
            type = SELECT_ONE_TABLE;
        } else if (type.equals(Question.SELECT_MULTIPLE)) {
            type = SELECT_MULTIPLE_TABLE;
        }
        return QuestionViewHolderFactory.getQuestionViewType(type);
    }

    private QuestionRelation getQuestionRelation(int position) {
        if (position == 0) {
            return getItem(position);
        } else {
            return getItem(position - 1);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

}
