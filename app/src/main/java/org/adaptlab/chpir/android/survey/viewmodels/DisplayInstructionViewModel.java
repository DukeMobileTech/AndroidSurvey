package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.daos.DisplayInstructionDao;
import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;
import org.adaptlab.chpir.android.survey.repositories.DisplayInstructionRepository;

import java.util.List;

public class DisplayInstructionViewModel extends AndroidViewModel {
    private LiveData<List<DisplayInstruction>> mDisplayInstructions;

    public DisplayInstructionViewModel(@NonNull Application application, long instrumentId, long displayId) {
        super(application);
        DisplayInstructionRepository displayInstructionRepository = new DisplayInstructionRepository(application);
        mDisplayInstructions = ((DisplayInstructionDao) displayInstructionRepository.getDao()).displayInstructions(instrumentId, displayId);
    }

    public LiveData<List<DisplayInstruction>> getDisplayInstructions() {
        return mDisplayInstructions;
    }

}
