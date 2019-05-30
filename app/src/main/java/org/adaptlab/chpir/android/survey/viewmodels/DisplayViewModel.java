package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.repositories.DisplayRepository;

import java.util.List;

public class DisplayViewModel extends AndroidViewModel {
    private LiveData<List<Display>> mDisplays;

    public DisplayViewModel(@NonNull Application application, long instrumentId) {
        super(application);
        DisplayRepository displayRepository = new DisplayRepository(application);
        mDisplays = displayRepository.getDisplayDao().instrumentDisplays(instrumentId);
    }

    public LiveData<List<Display>> getDisplays() {
        return mDisplays;
    }

}
