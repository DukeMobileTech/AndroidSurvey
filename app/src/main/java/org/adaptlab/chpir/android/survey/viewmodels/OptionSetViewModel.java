package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.daos.InstructionDao;
import org.adaptlab.chpir.android.survey.daos.OptionSetDao;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.OptionSet;
import org.adaptlab.chpir.android.survey.repositories.InstructionRepository;
import org.adaptlab.chpir.android.survey.repositories.OptionSetRepository;

import java.util.List;

public class OptionSetViewModel extends AndroidViewModel {
    private OptionSetRepository mRepository;
    private LiveData<List<OptionSet>> mAllOptionSets;

    public OptionSetViewModel(@NonNull Application application) {
        super(application);
        mRepository = new OptionSetRepository(application);
        mAllOptionSets = ((OptionSetDao) mRepository.getDao()).getAllOptionSets();
    }

    public LiveData<List<OptionSet>> getAllOptionSets() {
        return mAllOptionSets;
    }

}
