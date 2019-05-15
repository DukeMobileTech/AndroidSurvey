package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/*
 * This question type exist only to add text to a screen.
 *
 * It does not save a response, only displays the text.
 */
public class InstructionsViewHolder extends SingleQuestionViewHolder {

    InstructionsViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        ((ViewGroup) questionComponent.getParent()).setVisibility(View.GONE);
    }

    @Override
    protected String serialize() {
        return "";
    }

    @Override
    protected void deserialize(String responseText) {
    }

//    @Override
//    protected void unSetResponse() {
//
//    }
}
