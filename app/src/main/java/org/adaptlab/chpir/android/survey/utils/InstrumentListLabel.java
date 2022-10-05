package org.adaptlab.chpir.android.survey.utils;

import android.widget.TextView;

import org.adaptlab.chpir.android.survey.entities.Instrument;

public class InstrumentListLabel {
    private final Instrument mInstrument;
    private final TextView mTextView;
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
