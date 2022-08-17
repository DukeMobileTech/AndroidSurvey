package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import org.adaptlab.chpir.android.survey.R;

public class SliderViewHolder extends QuestionViewHolder {
    private float mProgress;
    private Slider mSlider;

    SliderViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected void beforeAddViewHook(ViewGroup questionComponent) {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.slider, null);
        mSlider = layout.findViewById(R.id.discreteSlider);
        LabelFormatter formatter = value -> Math.abs((int) value) + "";
        mSlider.setLabelFormatter(formatter);
        mSlider.addOnChangeListener((slider, value, fromUser) -> {
            mProgress = value;
            saveResponse();
        });
        questionComponent.addView(layout);
    }

    @Override
    protected String serialize() {
        return String.valueOf(mProgress);
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
            mSlider.setValue((float) 0.0);
        } else {
            mSlider.setValue(Float.parseFloat(responseText));
        }
    }

    @Override
    protected void unSetResponse() {
        mSlider.setValue((float) 0.0);
    }

    @Override
    protected void showOtherText(int position) {
    }

}
