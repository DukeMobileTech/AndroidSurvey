package org.adaptlab.chpir.android.activerecordcloudsync;

import android.content.Context;

import com.activeandroid.Model;

import org.json.JSONObject;

public abstract class SendModel extends Model { 
    public abstract JSONObject toJSON();
    public abstract boolean isSent();
    public abstract boolean readyToSend();
    public abstract void setAsSent(Context context);
    public abstract boolean isPersistent();

    public String getPrimaryKey() {
        return "Id";
    }
}