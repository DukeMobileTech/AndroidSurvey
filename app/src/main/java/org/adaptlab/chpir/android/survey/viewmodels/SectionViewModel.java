package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.daos.SectionDao;
import org.adaptlab.chpir.android.survey.relations.SectionRelation;
import org.adaptlab.chpir.android.survey.repositories.SectionRepository;

import java.util.List;

public class SectionViewModel extends AndroidViewModel {
    private LiveData<List<SectionRelation>> mSectionRelations;

    public SectionViewModel(@NonNull Application application, Long instrumentId) {
        super(application);
        SectionRepository repository = new SectionRepository(application);
        mSectionRelations = ((SectionDao) repository.getDao()).instrumentSections(instrumentId);
    }

    public LiveData<List<SectionRelation>> getSectionRelations() {
        return mSectionRelations;
    }

}
