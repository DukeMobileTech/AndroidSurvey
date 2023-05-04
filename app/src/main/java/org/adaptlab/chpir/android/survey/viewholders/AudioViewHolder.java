package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class AudioViewHolder extends QuestionViewHolder {
    AudioViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        new AudioComponent(getContext(), getAudioComponent(), getAudioFolder(), this);
        questionComponent.setVisibility(View.GONE);
    }

    @Override
    protected void unSetResponse() {

    }

    @Override
    protected void showOtherText(int position) {

    }

    @Override
    protected void deserialize(String responseText) {

    }

    @Override
    protected String serialize() {
        return null;
    }

}
