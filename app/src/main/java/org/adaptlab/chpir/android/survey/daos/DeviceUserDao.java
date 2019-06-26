package org.adaptlab.chpir.android.survey.daos;

import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.DeviceUser;

@Dao
public abstract class DeviceUserDao extends BaseDao<DeviceUser> {
    @Query("SELECT * FROM DeviceUsers WHERE UserName=:name")
    public abstract DeviceUser findByUserName(String name);

}
