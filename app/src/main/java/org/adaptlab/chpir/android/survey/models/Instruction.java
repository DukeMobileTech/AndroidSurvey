package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "Instructions")
public class Instruction extends ReceiveModel {
    private static final String TAG = "Instructions";
    @Column(name = "RemoteId")
    private Long mRemoteId;
    @Column(name = "Text")
    private String mText;
    @Column(name = "Deleted")
    private boolean mDeleted;

    public Instruction() {
        super();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Creating Instruction: " + jsonObject);
        try {
            Long remoteId = jsonObject.getLong("id");
            Instruction instruction = Instruction.findByRemoteId(remoteId);
            if (instruction == null) {
                instruction = new Instruction();
            }
            instruction.setRemoteId(remoteId);
            instruction.setText(jsonObject.getString("text"));
            if (jsonObject.isNull("deleted_at")) {
                instruction.setDeleted(false);
            } else {
                instruction.setDeleted(true);
            }
            instruction.save();

            JSONArray translationsArray = jsonObject.optJSONArray("instruction_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    InstructionTranslation translation = InstructionTranslation.findByRemoteId(translationRemoteId);
                    if (translation == null) {
                        translation = new InstructionTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setLanguage(translationJSON.getString("language"));
                    translation.setInstruction(instruction);
                    translation.setText(translationJSON.getString("text"));
                    translation.save();
                }
            }
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static Instruction findByRemoteId(Long id) {
        return new Select().from(Instruction.class).where("RemoteId = ?", id).executeSingle();
    }

    public static List<Instruction> getAll() {
        return new Select().from(Instruction.class).where("Deleted = 0").execute();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    void setRemoteId(Long id) {
       mRemoteId = id;
    }

    void setDeleted(boolean status) {
        mDeleted = status;
    }

    void setText(String text) {
        mText = text;
    }

    public String getText(Instrument instrument) {
        if (instrument.getLanguage().equals(AppUtil.getDeviceLanguage())) return mText;
        for (InstructionTranslation translation : translations()) {
            if (translation.getLanguage().equals(AppUtil.getDeviceLanguage())) {
                return translation.getText();
            }
        }
        // Fall back to default
        return mText;
    }

    private List<InstructionTranslation> translations() {
        return getMany(InstructionTranslation.class, "Instruction");
    }

}
