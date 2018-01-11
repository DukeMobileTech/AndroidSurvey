package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "Displays")
public class Display extends ReceiveModel {
    private static final String TAG = "Display";
    @Column(name = "RemoteId")
    private Long mRemoteId;
    @Column(name = "Mode")
    private String mMode;
    @Column(name = "Position")
    private int mPosition;
    @Column(name = "InstrumentId")
    private Long mInstrumentId;

    public Display() {
        super();
    }

    public enum DisplayMode {
        SINGLE, MULTIPLE, TABLE
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Creating Display: " + jsonObject);
        try {
            Long remoteId = jsonObject.getLong("id");
            Display display = Display.findByRemoteId(remoteId);
            if (display == null) {
                display = new Display();
            }
            display.setRemoteId(remoteId);
            display.setMode(jsonObject.optString("mode"));
            display.setPosition(jsonObject.optInt("position"));
            display.setInstrumentId(jsonObject.optLong("instrument_id"));
            display.save();
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static Display findByRemoteId(Long id) {
        return new Select().from(Display.class).where("RemoteId = ?", id).executeSingle();
    }

    public int getPosition() {
        return mPosition;
    }

    public List<Question> questions() {
        return new Select().from(Question.class)
                .where("DisplayId = ? AND Deleted != ?", getRemoteId(), 1)
                .orderBy("NumberInInstrument ASC")
                .execute();
    }

    public String getMode() {
        return mMode;
    }

    public List<Option> options() {
        return new Select().from(Option.class)
                .where("RemoteOptionSetId = ? AND Deleted != ?",
                        questions().get(0).getRemoteOptionId(), 1)
                .execute();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    private void setRemoteId(Long id) {
       mRemoteId = id;
    }

    private void setMode(String mode) {
        mMode = mode;
    }

    private void setPosition(int position) {
        mPosition = position;
    }

    private void setInstrumentId(Long id) {
        mInstrumentId = id;
    }

}
