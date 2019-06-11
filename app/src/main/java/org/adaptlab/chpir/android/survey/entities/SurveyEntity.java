package org.adaptlab.chpir.android.survey.entities;

import org.adaptlab.chpir.android.survey.daos.BaseDao;

import java.lang.reflect.Type;
import java.util.List;

public interface SurveyEntity<T> {
    Type getType();

    List<T> getTranslations();

    void save(BaseDao<T> dao, List<T> list);

    void setDeleted(boolean deleted);
}
