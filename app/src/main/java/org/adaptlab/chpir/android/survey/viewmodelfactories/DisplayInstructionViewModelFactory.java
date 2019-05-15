package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayInstructionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayQuestionViewModel;

public class DisplayInstructionViewModelFactory implements ViewModelProvider.Factory {
    private long mInstrumentId;
    private long mDisplayId;
    private Application mApplication;

    public DisplayInstructionViewModelFactory(@NonNull Application application, long instrumentId, long displayId) {
        this.mApplication = application;
        this.mInstrumentId = instrumentId;
        this.mDisplayId = displayId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DisplayInstructionViewModel.class)) {
            return (T) new DisplayInstructionViewModel(mApplication, mInstrumentId, mDisplayId);
        } else {
            return null;
        }
    }

}
