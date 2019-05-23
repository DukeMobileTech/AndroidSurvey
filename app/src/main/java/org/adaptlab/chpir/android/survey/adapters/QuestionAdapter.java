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
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolder;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolderFactory;

public class QuestionAdapter extends ListAdapter<QuestionRelation, QuestionViewHolder> {
    public static final String TAG = QuestionAdapter.class.getName();

    private static final DiffUtil.ItemCallback<QuestionRelation> DIFF_CALLBACK = new DiffUtil.ItemCallback<QuestionRelation>() {
        @Override
        public boolean areItemsTheSame(@NonNull QuestionRelation oldQuestion, @NonNull QuestionRelation newQuestion) {
            boolean same = oldQuestion.question.getResponse().getUUID().equals(newQuestion.question.getResponse().getUUID());
            Log.i(TAG, "areItemsTheSame: " + same);
            return same;
        }

        @Override
        public boolean areContentsTheSame(@NonNull QuestionRelation oldQuestion, @NonNull QuestionRelation newQuestion) {
            boolean same = oldQuestion.question.getResponse().getText().equals(newQuestion.question.getResponse().getText()) &&
                    oldQuestion.question.getResponse().getSpecialResponse().equals(newQuestion.question.getResponse().getSpecialResponse()) &&
                    oldQuestion.question.getResponse().getOtherResponse().equals(newQuestion.question.getResponse().getOtherResponse());
            Log.i(TAG, "areContentsTheSame: " + same);
            return same;
        }
    };

    public QuestionAdapter() {
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
        QuestionRelation questionRelation = getItem(position);
        viewHolder.setQuestionRelation(questionRelation);
    }

    @Override
    public int getItemViewType(int position) {
        String type = getItem(position).question.getQuestionType();
        return QuestionViewHolderFactory.getQuestionViewType(type);
    }

}
