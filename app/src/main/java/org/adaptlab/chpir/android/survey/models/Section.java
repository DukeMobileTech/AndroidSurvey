package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "Sections")
public class Section extends ReceiveModel {

	private static final String TAG = "Section";
	
	@Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
	@Column(name = "Title")
    private String mTitle;
	@Column(name = "FirstQuestionNumber")
	private int mFirstQuestionNumber;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @Column(name = "Deleted")
    private boolean mDeleted;

	@Override
	public void createObjectFromJSON(JSONObject jsonObject) {
		try {
            Long remoteId = jsonObject.getLong("id");
            Section section = Section.findByRemoteId(remoteId);
            if (section == null) {
            	section = this;
            }
            section.setRemoteId(remoteId);
            section.setInstrumentRemoteId(jsonObject.getLong("instrument_id"));
            section.setTitle(jsonObject.getString("title"));
			if (!jsonObject.isNull(jsonObject.getString("first_question_number"))) {
                section.setFirstQuestionNumber(jsonObject.getInt("first_question_number"));
            }
            if (jsonObject.isNull("deleted_at")) {
                section.setDeleted(false);
            } else {
                section.setDeleted(true);
            }
            section.save();
            
            //Generate translations
            JSONArray translationsArray = jsonObject.optJSONArray("section_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    SectionTranslation translation = SectionTranslation.findByRemoteId(translationRemoteId);
                    if (translation == null) {
                        translation = new SectionTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setLanguage(translationJSON.getString("language"));
                    translation.setSection(section);
                    translation.setText(translationJSON.getString("text"));
                    translation.setInstrumentTranslation(InstrumentTranslation.findByRemoteId(
                            translationJSON.optLong("instrument_translation_id")));
                    translation.save();
                }
            }
		} catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }  
	}

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    /*
     * Find an existing translation, or return a new SectionTranslation
     * if a translation does not yet exist.
     */
    public SectionTranslation getTranslationByLanguage(String language) {
        for(SectionTranslation translation : translations()) {
            if (translation.getLanguage().equals(language)) {
                return translation;
            }
        }
        
        SectionTranslation translation = new SectionTranslation();
        translation.setLanguage(language);
        return translation;
    }
    
    public List<SectionTranslation> translations() {
    	return getMany(SectionTranslation.class, "Section");
    }

    public List<Question> questions() {
        return new Select().from(Question.class)
                .where("Section = ? AND Deleted != ?", getId(), 1)
                .orderBy("NumberInInstrument")
                .execute();
    }

	public void setInstrumentRemoteId(Long instrumentId) {
		mInstrumentRemoteId = instrumentId;
	}
	
	public Instrument getInstrument() {
		return Instrument.findByRemoteId(getInstrumentRemoteId());
	}

	public void setRemoteId(Long remoteId) {
		mRemoteId = remoteId;
	}

    public Long getRemoteId() {
        return mRemoteId;
    }
	
	public void setTitle(String title) {
		mTitle = title;
	}

	public void setFirstQuestionNumber(int questionNumber) {
		mFirstQuestionNumber = questionNumber;
	}

    private Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

	/*
     * If the language of the instrument is the same as the language setting on the
     * device (or through the Admin settings), then return the section title.
     * 
     * If another language is requested, iterate through section translations to
     * find translated title.
     * 
     * If the language requested is not available as a translation, return the non-translated
     * text for the section.
     */
	public String getTitle() {
		if (getInstrument().getLanguage().equals(Instrument.getDeviceLanguage())) return mTitle;
        if (activeTranslation() != null) return activeTranslation().getText();
        for (SectionTranslation translation : translations()) {
            if (translation.getLanguage().equals(Instrument.getDeviceLanguage())) {
                return translation.getText();
            }
        }
		//Default
		return mTitle;
	}

    private SectionTranslation activeTranslation() {
        if (getInstrument().activeTranslation() == null) return null;
        return new Select().from(SectionTranslation.class)
                .where("InstrumentTranslation = ? AND Section = ?",
                getInstrument().activeTranslation().getId(), getId()).executeSingle();
    }

	public static Section findByRemoteId(Long remoteId) {
		return new Select().from(Section.class).where("RemoteId = ?", remoteId).executeSingle();
	}
	
	public static List<Section> getAll() {
		return new Select().from(Section.class).orderBy("Id ASC").execute();
	}

}