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
    @Column(name = "RemoteId")
    private Long mRemoteId;
    @Column(name = "Mode")
    private String mMode;
    @Column(name = "Position")
    private int mPosition;
    @Column(name = "InstrumentId")
    private Long mInstrumentId;
    @Column(name = "Title")
    private String mTitle;
    @Column(name = "SectionId")
    private Long mSectionId;
    @Column(name = "Deleted")
    private boolean mDeleted;

    public Display() {
        super();
    }

    public static Display findByRemoteId(Long id) {
        return new Select().from(Display.class).where("RemoteId = ?", id).executeSingle();
    }

    public String getSectionTitle() {
        Section section = new Select().from(Section.class)
                .where("RemoteId = ?", mSectionId).executeSingle();
        if (section == null) return null;
        return section.getTitle();
    }

    private void setSectionId(Long sectionId) {
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

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public int getPosition() {
        return mPosition;
    }

    private void setPosition(int position) {
        mPosition = position;
    }

    public List<Question> questions() {
        return new Select().from(Question.class)
                .where("DisplayId = ? AND Deleted != ?", getRemoteId(), 1)
                .orderBy("NumberInInstrument ASC")
                .execute();
    }

    public List<Question> tableQuestions(String tableIdentifier) {
        return new Select().from(Question.class)
                .where("DisplayId = ? AND Deleted != ? AND TableIdentifier = ?", getRemoteId(), 1, tableIdentifier)
                .orderBy("NumberInInstrument ASC")
                .execute();
    }

    public String getMode() {
        return mMode;
    }

    private void setMode(String mode) {
        mMode = mode;
    }

    public List<Option> options() {
        return new Select("Options.*").distinct().from(Option.class)
                .innerJoin(OptionInOptionSet.class)
                .on("OptionInOptionSets.RemoteOptionSetId = ?", questions().get(0).getRemoteOptionSetId())
                .where("Options.Deleted != 1 AND OptionInOptionSets.Special = 0 AND OptionInOptionSets.RemoteOptionId = Options.RemoteId")
                .orderBy("OptionInOptionSets.NumberInQuestion ASC")
                .execute();
    }

    public List<Option> tableOptions(String tableIdentifier) {
        return new Select("Options.*").distinct().from(Option.class)
                .innerJoin(OptionInOptionSet.class)
                .on("OptionInOptionSets.RemoteOptionSetId = ?", tableQuestions(tableIdentifier).get(0).getRemoteOptionSetId())
                .where("Options.Deleted != 1 AND OptionInOptionSets.Special = 0 AND OptionInOptionSets.RemoteOptionId = Options.RemoteId")
                .orderBy("OptionInOptionSets.NumberInQuestion ASC")
                .execute();
    }

    public List<DisplayInstruction> displayInstructions() {
        return new Select().from(DisplayInstruction.class).where("RemoteDisplayId = ? AND Deleted != 1", getRemoteId()).execute();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public String getTitle() {
        if (getInstrument().getLanguage().equals(AppUtil.getDeviceLanguage())) return mTitle;
        for (DisplayTranslation translation : translations()) {
            if (translation.getLanguage().equals(AppUtil.getDeviceLanguage())) {
                return translation.getText();
            }
        }
        //Default
        return mTitle;
    }

    public List<DisplayTranslation> translations() {
        return getMany(DisplayTranslation.class, "Display");
    }

    public Instrument getInstrument() {
        return Instrument.findByRemoteId(mInstrumentId);
    }

    private void setTitle(String title) {
        mTitle = title;
    }

    private void setInstrumentId(Long id) {
        mInstrumentId = id;
    }

    public enum DisplayMode {
        SINGLE, MULTIPLE, TABLE
    }

}
