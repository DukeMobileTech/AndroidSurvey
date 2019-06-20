package org.adaptlab.chpir.android.survey.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

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
