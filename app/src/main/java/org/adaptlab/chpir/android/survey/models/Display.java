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

@Table(name = "Displays")
public class Display extends ReceiveModel {
    private static final String TAG = "Display";
    @Column(name = "RemoteId", index = true)
    private Long mRemoteId;
    @Column(name = "Mode")
    private String mMode;
    @Column(name = "Position", index = true)
    private int mPosition;
    @Column(name = "InstrumentId", index = true)
    private Long mInstrumentId;
    @Column(name = "Title", index = true)
    private String mTitle;
    @Column(name = "SectionId")
    private Long mSectionId;
    @Column(name = "Deleted", index = true)
    private boolean mDeleted;
    @Column(name = "QuestionCount")
    private int mQuestionCount;

    private double mDisplayPosition;

    public Display() {
        super();
    }

    public static Display findByRemoteId(Long id) {
        return new Select().from(Display.class).where("RemoteId = ?", id).executeSingle();
    }

    public static Display findByTitleAndInstrument(String title, Long instrumentId) {
        return new Select().from(Display.class).where("Title = ? AND InstrumentId = ?",
                title, instrumentId).executeSingle();
    }

    public String getSectionTitle() {
        Section section = new Select().from(Section.class)
                .where("RemoteId = ?", mSectionId).executeSingle();
        if (section == null) return null;
        return section.getTitle();
    }

    Long getSectionId() {
        return mSectionId;
    }

    void setSectionId(Long sectionId) {
        mSectionId = sectionId;
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
            display.setTitle(jsonObject.optString("title"));
            display.setSectionId(jsonObject.optLong("section_id"));
            display.setQuestionCount(jsonObject.optInt("question_count"));
            if (jsonObject.isNull("deleted_at")) {
                display.setDeleted(false);
            } else {
                display.setDeleted(true);
            }
            display.save();

            JSONArray translationsArray = jsonObject.optJSONArray("display_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    DisplayTranslation translation = DisplayTranslation.findByRemoteId(translationRemoteId);
                    if (translation == null) {
                        translation = new DisplayTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setLanguage(translationJSON.getString("language"));
                    translation.setDisplay(display);
                    translation.setText(translationJSON.getString("text"));
                    translation.save();
                }
            }
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error parsing object json", je);
        }
    }

    public boolean getDeleted() {
        return mDeleted;
    }

    void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public int getPosition() {
        return mPosition;
    }

    void setPosition(int position) {
        mPosition = position;
    }

    public String getMode() {
        return mMode;
    }

    void setMode(String mode) {
        mMode = mode;
    }

    public List<DisplayInstruction> displayInstructions() {
        return new Select().from(DisplayInstruction.class).where("RemoteDisplayId = ? AND Deleted != 1", getRemoteId()).execute();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public String getTitle() {
        String iLanguage = getInstrument().getLanguage();
        String dLanguage = AppUtil.getDeviceLanguage();
        if (iLanguage.equals(dLanguage)) return mTitle;
        List<DisplayTranslation> translations = translations();
        for (DisplayTranslation translation : translations) {
            if (translation.getLanguage().equals(dLanguage)) {
                return translation.getText();
            }
        }
        if (dLanguage.contains("-")) {
            dLanguage = dLanguage.split("-")[0];
            for (DisplayTranslation translation : translations) {
                if (translation.getLanguage().equals(dLanguage)) {
                    return translation.getText();
                }
            }
        }
        //Default
        return mTitle;
    }

    void setTitle(String title) {
        mTitle = title;
    }

    public List<DisplayTranslation> translations() {
        return getMany(DisplayTranslation.class, "Display");
    }

    public Instrument getInstrument() {
        return Instrument.findByRemoteId(mInstrumentId);
    }

    void setInstrumentId(Long id) {
        mInstrumentId = id;
    }

    public int getQuestionCount() {
        return mQuestionCount;
    }

    public void setQuestionCount(int count) {
        mQuestionCount = count;
    }

    public double getDisplayPosition() {
        return mDisplayPosition;
    }

    public void setDisplayPosition(double mDisplayPosition) {
        this.mDisplayPosition = mDisplayPosition;
    }

    public enum DisplayMode {
        SINGLE, MULTIPLE, TABLE
    }

}
