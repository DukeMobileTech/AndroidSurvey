package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.SingleQuestionFragment;

/*
 * This question type exist only to add text to a screen.
 * 
 * It does not save a response, only displays the text.
 */
public class InstructionsQuestionFragment extends SingleQuestionFragment {

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

    @Override
    protected void unSetResponse() {

    }
}
