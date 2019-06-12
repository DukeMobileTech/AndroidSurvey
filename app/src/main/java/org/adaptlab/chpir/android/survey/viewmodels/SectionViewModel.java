package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.daos.SectionDao;
import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.repositories.SectionRepository;

import java.util.List;

public class SectionViewModel extends AndroidViewModel {
    private LiveData<List<Section>> mSections;

    public SectionViewModel(@NonNull Application application, Long instrumentId) {
        super(application);
        SectionRepository repository = new SectionRepository(application);
        mSections = ((SectionDao) repository.getDao()).instrumentSections(instrumentId);
    }

    public LiveData<List<Section>> getSections() {
        return mSections;
    }

}
