package org.adaptlab.chpir.android.survey.Models;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.AppUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "Instruments")
public class Instrument extends ReceiveModel {
    private static final String TAG = "Instrument";
    
    public static final String KHMER_LANGUAGE_CODE = "km";
    public static final String KHMER_FONT_LOCATION = "fonts/khmerOS.ttf";
    
    public static final String LEFT_ALIGNMENT = "left";

    @Column(name = "Title")
    private String mTitle;
    // https://github.com/pardom/ActiveAndroid/issues/22
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Alignment")
    private String mAlignment;
    @Column(name = "VersionNumber")
    private int mVersionNumber;
    @Column(name = "QuestionCount")
    private int mQuestionCount;
    @Column(name = "ProjectId")
    private Long mProjectId;
    @Column(name = "Published")
    private boolean mPublished;

    public Instrument() {
        super();
    }

    /*
     * If the language of the instrument is the same as the language setting on the
     * device (or through the admin settings), then return the default instrument title.
     * 
     * If another language is requested, iterate through instrument translations to
     * find translated title.
     * 
     * If the language requested is not available as a translation (or is blank), return the non-translated
     * text for the title.
     */
    public String getTitle() {
        if (getLanguage().equals(getDeviceLanguage())) return mTitle;
        for(InstrumentTranslation translation : translations()) {
            if (translation.getLanguage().equals(getDeviceLanguage())
                    && !translation.getTitle().trim().equals("")) {
                return translation.getTitle();
            }
        }
        
        // Fall back to default
        return mTitle;
    }
        
    public String getAlignment() {
        if (getLanguage().equals(getDeviceLanguage())) return mAlignment;
        for(InstrumentTranslation translation : translations()) {
            if (translation.getLanguage().equals(getDeviceLanguage())) {
                return translation.getAlignment();
            }
        }

        // Fall back to default
        return mAlignment;
    }
    
    public InstrumentTranslation getTranslationByLanguage(String language) {
        for(InstrumentTranslation translation : translations()) {
            if (translation.getLanguage().equals(language)) {
                return translation;
            }
        }
        InstrumentTranslation translation = new InstrumentTranslation();
        translation.setLanguage(language);
        return translation;
    }
    
    public Typeface getTypeFace(Context context) {
        if (getDeviceLanguage().equals(KHMER_LANGUAGE_CODE)) {
            return Typeface.createFromAsset(context.getAssets(), KHMER_FONT_LOCATION); 
        } else {
            return Typeface.DEFAULT;
        }
    }
    
    public int getDefaultGravity() {
        if (getAlignment().equals(LEFT_ALIGNMENT)) {
            return Gravity.LEFT;
        } else {
            return Gravity.RIGHT;
        }
    }
    
    public static String getDeviceLanguage() {
    	//Log.i(TAG, "Custom Locale Code: " + AppUtil.getAdminSettingsInstance().getCustomLocaleCode());
    	if ( AppUtil.getAdminSettingsInstance().getCustomLocaleCode() != null && !AppUtil.getAdminSettingsInstance().getCustomLocaleCode().equals("")) {
            return AppUtil.getAdminSettingsInstance().getCustomLocaleCode();
        }
        return Locale.getDefault().getLanguage();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
                        
            // If an instrument already exists, update it from the remote
            Instrument instrument = Instrument.findByRemoteId(remoteId);
            if (instrument == null) {
                instrument = this;
            }
            
            if (AppUtil.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            instrument.setRemoteId(remoteId);
            instrument.setTitle(jsonObject.getString("title"));
            instrument.setLanguage(jsonObject.getString("language"));
            instrument.setAlignment(jsonObject.getString("alignment"));
            instrument.setVersionNumber(jsonObject.getInt("current_version_number"));
            instrument.setQuestionCount(jsonObject.getInt("question_count"));
            instrument.setProjectId(jsonObject.getLong("project_id"));
            instrument.setPublished(jsonObject.getBoolean("published"));
            instrument.save();
            
            // Generate translations
            JSONArray translationsArray = jsonObject.getJSONArray("translations");
            for(int i = 0; i < translationsArray.length(); i++) {
                JSONObject translationJSON = translationsArray.getJSONObject(i);
                InstrumentTranslation translation = instrument.getTranslationByLanguage(translationJSON.getString("language"));
                translation.setInstrument(instrument);
                translation.setAlignment(translationJSON.getString("alignment"));
                translation.setTitle(translationJSON.getString("title"));
                translation.save();
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }  
    }
    
    /*
     * Finders
     */
    public static List<Instrument> getAll() {
        return new Select().from(Instrument.class).orderBy("Title").execute();
    }
    
    public static List<Instrument> getAllProjectInstruments(Long projectId) {
    	return new Select().from(Instrument.class)
    			.where("ProjectID = ? AND Published = ?", projectId, 1)	//sqlite saves booleans as integers
    			.orderBy("Title")
    			.execute();
    }
      
    public static Instrument findByRemoteId(Long id) {
        return new Select().from(Instrument.class).where("RemoteId = ?", id).executeSingle();
    }
    
    /*
     * Relationships
     */
    public List<Question> questions() {
        return new Select().from(Question.class)
                .where("Instrument = ? AND InstrumentVersion = ?", getId(), getVersionNumber())
                .orderBy("NumberInInstrument ASC")
                .execute();
    }
    
    public List<Survey> surveys() {
        return getMany(Survey.class, "Instrument");
    }
    
    public List<InstrumentTranslation> translations() {
        return getMany(InstrumentTranslation.class, "Instrument");
    }
    
    public List<Section> sections() {
    	return new Select("Sections.*, Questions.QuestionIdentifier").from(Section.class)
    			.innerJoin(Question.class)
    			.on("Sections.StartQuestionIdentifier=Questions.QuestionIdentifier AND Sections.Instrument = " + getId())
    			.orderBy("Questions.NumberInInstrument")
    			.execute();
    }
    
    public static List<Instrument> loadedInstruments() {
        List<Instrument> instrumentList = new ArrayList<Instrument>();
        for (Instrument instrument : Instrument.getAll()) {
            if (instrument.loaded()) instrumentList.add(instrument);
        }
        return instrumentList;
    }
      
     public boolean loaded() {
         if (questions().size() != getQuestionCount()) return false;
         for (Question question : questions()) {
             if (!question.loaded()) return false;
         }
         return true;
     }
        
    /*
     * Getters/Setters
     */

    public void setTitle(String title) {
        mTitle = title;
    }
    
    public Long getRemoteId() {
        return mRemoteId;
    }
    
    public void setRemoteId(Long id) {
        mRemoteId = id;
    }
    
    public String getLanguage() {
        return mLanguage;
    }

    @Override
    public String toString() {
        return mTitle;
    }
    
    public void setVersionNumber(int version) {
        mVersionNumber = version;
    }
    
    public int getVersionNumber() {
        return mVersionNumber;
    }
    
    public int getQuestionCount() {
        return mQuestionCount;
    }
    
    public void setProjectId(Long id) {
    	mProjectId = id;
    }
    
    public Long getProjectId() {
    	return mProjectId;
    }
    
    public boolean getPublished() {
    	return mPublished;
    }
     
    public void setLanguage(String language) {
        mLanguage = language;
    }
    
    private void setAlignment(String alignment) {
        mAlignment = alignment;
    }
    
    public void setQuestionCount(int num) {
        mQuestionCount = num;
    }
    
    public void setPublished(boolean published) {
    	mPublished = published;
    }
}
