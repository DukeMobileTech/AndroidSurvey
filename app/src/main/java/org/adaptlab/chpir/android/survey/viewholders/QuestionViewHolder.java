package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.ResponseRelation;

public abstract class QuestionViewHolder extends RecyclerView.ViewHolder {
    public final String TAG = this.getClass().getName();
    private Context mContext;

    QuestionViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    protected abstract Question getQuestion();

    protected abstract void createQuestionComponent(ViewGroup questionComponent);

    public abstract void setQuestionRelation(ResponseRelation responseRelation, QuestionRelation questionRelation);

    protected abstract void deserialize(String responseText);

    protected abstract void deserializeSpecialResponse();

    protected abstract void deserializeOtherResponse(String otherResponse);

    protected abstract String serialize();

}
