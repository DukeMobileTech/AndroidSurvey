package org.adaptlab.chpir.android.activerecordcloudsync;

import com.activeandroid.Model;

import org.json.JSONObject;

public abstract class ReceiveModel extends Model {
    public abstract void createObjectFromJSON(JSONObject jsonObject);
}
