package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.QuestionFragment;

/*
 * This question type exist only to add text to a screen.
 * 
 * It does not save a response, only displays the text.
 */
public class InstructionsQuestionFragment extends QuestionFragment {

	@Override
	protected void createQuestionComponent(ViewGroup questionComponent) { 
		questionComponent.setVisibility(View.INVISIBLE);
	}

	@Override
	protected String serialize() {
		return "";
	}

	@Override
	protected void deserialize(String responseText) { }

	@Override
	protected void unSetResponse() {

	}
}
