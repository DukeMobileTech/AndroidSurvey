package org.adaptlab.chpir.android.survey.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.relations.ResponseRelation;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_question_table, parent, false);
        return QuestionViewHolderFactory.createViewHolder(view, parent.getContext(), viewType, getListener());
    }

    @Override
    public int getItemViewType(int position) {
        if (getQuestionRelations() == null) return -1;
        ResponseRelation responseRelation = getItem(position);
        QuestionRelation questionRelation = getQuestionRelations().get(responseRelation.response.getQuestionIdentifier());
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

}
