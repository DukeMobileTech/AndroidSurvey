package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class MultipleQuestionsViewHolder extends QuestionViewHolder {

    MultipleQuestionsViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected abstract void createQuestionComponent(ViewGroup questionComponent);

}