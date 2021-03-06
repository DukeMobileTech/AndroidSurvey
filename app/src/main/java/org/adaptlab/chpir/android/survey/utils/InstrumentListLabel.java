package org.adaptlab.chpir.android.survey.utils;

import android.widget.TextView;

import org.adaptlab.chpir.android.survey.models.Instrument;

public class InstrumentListLabel {
    private Instrument mInstrument;
    private TextView mTextView;
    private Boolean mLoaded;

    public InstrumentListLabel(Instrument instrument, TextView textView) {
        this.mInstrument = instrument;
        this.mTextView = textView;
    }

    public Instrument getInstrument() {
        return mInstrument;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public void setLoaded(boolean loaded) {
        mLoaded = loaded;
    }

    public Boolean isLoaded() {
        return mLoaded;
    }
}
