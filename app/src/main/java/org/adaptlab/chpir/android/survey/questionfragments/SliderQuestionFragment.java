package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import org.adaptlab.chpir.android.survey.QuestionFragment;

public class SliderQuestionFragment extends QuestionFragment {
    private static final String TAG = "SliderQuestionFragment";
    private int mProgress;
    private SeekBar mSlider;
    
    protected void beforeAddViewHook(ViewGroup questionComponent) {}

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mSlider = new SeekBar(getActivity());
        mSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgress = progress;
                if (mProgress > -1) {
                	setResponseText();
                }
                if (fromUser) {
                    clearSpecialResponseSelection();
                }
            }

            // Required by interface
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }     
        });
        beforeAddViewHook(questionComponent);
        questionComponent.addView(mSlider);
    }

    @Override
    protected String serialize() {
        if (mProgress == -1) return "";
        return String.valueOf(mProgress);
    }

    @Override
    protected void unSetResponse() {
        mProgress = -1;
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
        	mSlider.setProgress(-1);
        } else {
            mSlider.setProgress(Integer.parseInt(responseText));
        }
    }
   
}
