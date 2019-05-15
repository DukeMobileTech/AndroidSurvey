package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.daos.InstructionDao;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.repositories.InstructionRepository;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRepository;

import java.util.List;

public class InstructionViewModel extends AndroidViewModel {
    private InstructionRepository mRepository;
    private LiveData<List<Instruction>> mAllInstructions;

    public InstructionViewModel(@NonNull Application application) {
        super(application);
        mRepository = new InstructionRepository(application);
        mAllInstructions = ((InstructionDao) mRepository.getDao()).getAllInstructions();
    }

    public LiveData<List<Instruction>> getAllInstructions() {
        return mAllInstructions;
    }

}
