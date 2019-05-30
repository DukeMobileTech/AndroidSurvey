package org.adaptlab.chpir.android.survey.adapters;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.ResponseRelation;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolder;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolderFactory;

import java.util.HashMap;
import java.util.List;

public class ResponseRelationAdapter extends ListAdapter<ResponseRelation, QuestionViewHolder> {
    public static final String TAG = ResponseRelationAdapter.class.getName();

    private HashMap<String, QuestionRelation> mQuestionRelations;

    private static final DiffUtil.ItemCallback<ResponseRelation> DIFF_CALLBACK = new DiffUtil.ItemCallback<ResponseRelation>() {
        @Override
        public boolean areItemsTheSame(@NonNull ResponseRelation oldResponseRelation, @NonNull ResponseRelation newResponseRelation) {
            boolean same = oldResponseRelation.response.getUUID().equals(newResponseRelation.response.getUUID());
//            Log.i(TAG, "areItemsTheSame: " + same);
            return same;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ResponseRelation oldResponseRelation, @NonNull ResponseRelation newResponseRelation) {
            boolean same = oldResponseRelation.response.getText().equals(newResponseRelation.response.getText()) &&
                    oldResponseRelation.response.getSpecialResponse().equals(newResponseRelation.response.getSpecialResponse()) &&
                    oldResponseRelation.response.getOtherResponse().equals(newResponseRelation.response.getOtherResponse());
//            Log.i(TAG, "areContentsTheSame: " + same);
            return same;
        }
    };

    public ResponseRelationAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_question, parent, false);
        return QuestionViewHolderFactory.createViewHolder(view, parent.getContext(), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder viewHolder, int position) {
        if (mQuestionRelations == null) return;
        ResponseRelation responseRelation = getItem(position);
        QuestionRelation questionRelation = mQuestionRelations.get(responseRelation.response.getQuestionIdentifier());
        if (questionRelation == null) return;
        viewHolder.setQuestionRelation(responseRelation, questionRelation);
    }

    @Override
    public int getItemViewType(int position) {
        if (mQuestionRelations == null) return -1;
        ResponseRelation responseRelation = getItem(position);
        QuestionRelation questionRelation = mQuestionRelations.get(responseRelation.response.getQuestionIdentifier());
        if (questionRelation == null) return -1;
        String type = questionRelation.question.getQuestionType();
        return QuestionViewHolderFactory.getQuestionViewType(type);
    }

    public void setQuestionRelations(List<QuestionRelation> questionRelations) {
        mQuestionRelations = new HashMap<>();
        for (QuestionRelation questionRelation : questionRelations) {
            mQuestionRelations.put(questionRelation.question.getQuestionIdentifier(), questionRelation);
        }
    }

}
