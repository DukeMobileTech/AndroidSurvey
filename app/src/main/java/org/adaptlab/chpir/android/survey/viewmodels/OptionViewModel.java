package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.daos.OptionDao;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.repositories.OptionRepository;

import java.util.List;

public class OptionViewModel extends AndroidViewModel {
    private OptionRepository mRepository;
    private LiveData<List<Option>> mAllOptions;

    public OptionViewModel(@NonNull Application application) {
        super(application);
        mRepository = new OptionRepository(application);
        mAllOptions = ((OptionDao) mRepository.getDao()).getAllOptions();
    }

    public LiveData<List<Option>> getAllOptions() {
        return mAllOptions;
    }

}
