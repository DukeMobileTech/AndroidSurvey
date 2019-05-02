package org.adaptlab.chpir.android.survey.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public abstract class BaseDao<T> {
    @Insert
    public abstract void insert(T t);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertAll(List<T> t);

    @Update
    public abstract void update(T t);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    public abstract void updateAll(List<T> t);

    @Delete
    public abstract void delete(T t);

    public void save(List<T> t) {
        insertAll(t);
    }

}
