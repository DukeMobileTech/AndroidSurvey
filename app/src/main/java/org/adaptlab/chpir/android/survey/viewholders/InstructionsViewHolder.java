package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/*
 * This question type exist only to add text to a screen.
 *
 * It does not save a response, only displays the text.
 */
public class InstructionsViewHolder extends QuestionViewHolder {

    InstructionsViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
    }

    @Override
    protected String serialize() {
        return "";
    }

    @Override
    protected void deserialize(String responseText) {
    }

}
