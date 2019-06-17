package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class SliderViewHolder extends QuestionViewHolder {
    private int mProgress;
    private SeekBar mSlider;

    SliderViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected void beforeAddViewHook(ViewGroup questionComponent) {

    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        mSlider = new SeekBar(getActivity());
//        mSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                mProgress = progress;
//                if (mProgress > -1) {
//                    setResponse(null);
//                }
//            }
//
//            // Required by interface
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
//        mSlider.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (mSpecialResponses != null) {
//                    mSpecialResponses.clearCheck();
//                }
//                return false;
//            }
//        });
//        beforeAddViewHook(questionComponent);
//        questionComponent.addView(mSlider);
    }

    @Override
    protected String serialize() {
        return String.valueOf(mProgress);
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
            mSlider.setProgress(-1);
        } else {
            mSlider.setProgress(Integer.parseInt(responseText));
        }
    }

//    @Override
//    protected void unSetResponse() {
//        mSlider.setProgress(0);
//        setResponseTextBlank();
//    }
}
