package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.relations.ResponseRelation;

public class TableHeaderViewHolder extends TableQuestionViewHolder {

    TableHeaderViewHolder(@NonNull View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void setQuestionRelation(ResponseRelation responseRelation, QuestionRelation questionRelation) {
        setQuestion(questionRelation.question);
        setQuestionRelation(questionRelation);
        setQuestionInstruction(questionRelation);
        setOptionSetItems(questionRelation);
        setSpecialOptions(questionRelation);
        setTableInstructions();
        createQuestionComponent(getQuestionComponent());
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        for (int k = 0; k < getOptions().size(); k++) {
            TextView textView = new TextView(getContext());
            textView.setText(getOptions().get(k).getText());
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
            textView.setWidth(getOptionWidth());
            textView.setPadding(1, 1, 1, 1);
            questionComponent.addView(textView);
        }
        getSpecialResponseRadioGroup().setVisibility(View.GONE);
        getClearButton().setVisibility(View.GONE);
    }

    @Override
    protected void deserialize(String responseText) {
    }

    @Override
    protected String serialize() {
        return null;
    }
}
