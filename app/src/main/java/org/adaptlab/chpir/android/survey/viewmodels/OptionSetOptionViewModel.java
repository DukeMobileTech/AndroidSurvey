package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.daos.OptionSetOptionDao;
import org.adaptlab.chpir.android.survey.entities.OptionSetOption;
import org.adaptlab.chpir.android.survey.repositories.OptionSetOptionRepository;

import java.util.List;

public class OptionSetOptionViewModel extends AndroidViewModel {
    private OptionSetOptionRepository mRepository;
    private LiveData<List<OptionSetOption>> mAllOptionSetOptions;

    public OptionSetOptionViewModel(@NonNull Application application) {
        super(application);
        mRepository = new OptionSetOptionRepository(application);
        mAllOptionSetOptions = ((OptionSetOptionDao) mRepository.getDao()).getAllOptionSetOptions();
    }

    public LiveData<List<OptionSetOption>> getAllOptionSetOptions() {
        return mAllOptionSetOptions;
    }

}
