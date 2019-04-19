package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "Rules")
public class Rule extends ReceiveModel {
    private static final String TAG = "RuleModel";
    
    @Column(name = "RuleType")
    private RuleType mRuleType;
    @Column(name = "Params")
    private String mParams;
    @Column(name = "StoredValues")
    private String mStoredValues;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @Column(name = "Deleted")
    private boolean mDeleted;

    public enum RuleType {
      INSTRUMENT_SURVEY_LIMIT_RULE, INSTRUMENT_TIMING_RULE, INSTRUMENT_SURVEY_LIMIT_PER_MINUTE_RULE,
      INSTRUMENT_LAUNCH_RULE, PARTICIPANT_TYPE_RULE, PARTICIPANT_AGE_RULE
    };
    
    // INSTRUMENT_SURVEY_RULE_LIMIT
    public static final String MAX_SURVEYS_KEY = "max_surveys";
    public static final String INSTRUMENT_SURVEY_COUNT_KEY = "instrument_survey_count";
    
    // INSTRUMENT_TIMING_RULE
    public static final String START_TIME_KEY = "start_time";
    public static final String END_TIME_KEY = "end_time";
    
    // INSTRUMENT_SURVEY_LIMIT_PER_MINUTE_RULE
    public static final String NUM_SURVEYS_KEY = "num_surveys";
    public static final String MINUTE_INTERVAL_KEY = "minute_interval";
    public static final String SURVEY_TIMESTAMPS_KEY = "survey_timestamps";

    public Rule() {
        super();
        mStoredValues = "";
    }
    
    public static Rule findByRuleTypeAndInstrument(RuleType ruleType, Instrument instrument) {
        for (Rule rule : getAll()) {
            if (rule.getRuleType() != null && rule.getRuleType().equals(ruleType) &&
                    rule.getInstrument() != null && rule.getInstrument().equals(instrument) &&
                    rule.getParamJSON() != null) {
                return rule;
            }
        }
        return null;
    }
    
    public static List<Rule> getAll() {
        return new Select().from(Rule.class).where("Deleted != ?", 1).orderBy("Id ASC").execute();
    }
    
    public static Rule findByRemoteId(Long remoteId) {
        return new Select().from(Rule.class).where("RemoteId = ?", remoteId).executeSingle();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            
            // If a rule already exists, update it from the remote
            Rule rule = Rule.findByRemoteId(remoteId);
            if (rule == null) {
                rule = this;
            }
            
            if (BuildConfig.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            rule.setRuleType(jsonObject.getString("rule_type"));
            rule.setInstrumentRemoteId(jsonObject.getLong("instrument_id"));
            rule.setParams(jsonObject.getString("rule_params"));
            rule.setRemoteId(remoteId);
            if (jsonObject.isNull("deleted_at")) {
                rule.setDeleted(false);
            } else {
                rule.setDeleted(true);
            }
            rule.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }
    
    public Long getRemoteId() {
        return mRemoteId;
    }
       
    public JSONObject getParamJSON() {
        try {
            return new JSONObject(mParams);
        } catch (JSONException je) {
            Log.e(TAG, "Could not parse rule params: " + je);
            return null;
        }
    }
    
    public RuleType getRuleType() {
        return mRuleType;
    }
    
    public Instrument getInstrument() {
        return Instrument.findByRemoteId(getInstrumentRemoteId());
    }
    
    public <T> void setStoredValue(String key, T value) {
        try {
            JSONObject jsonObject = mStoredValues.equals("") ? new JSONObject() : new JSONObject(mStoredValues);
            
            if (BuildConfig.DEBUG) Log.i(TAG, "Setting k: " + key + " to v: " + value);
            
            jsonObject.put(key, value);
            mStoredValues = jsonObject.toString();
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error setting json for stored value: (k: " + key +", v: " + value + ") :" + je);
        }
    }
    
    public <T> T getStoredValue(String key) {
        try {
            JSONObject jsonObject = mStoredValues.equals("") ? new JSONObject() : new JSONObject(mStoredValues);
            
            if (BuildConfig.DEBUG) Log.i(TAG, "Getting value for k: " + key);
            
            if (jsonObject.has(key)) {
                return (T) jsonObject.get(key);
            } else {
                return null;
            }
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error getting json value for stored value: (k: " + key +") :" + je);
            return null;
        }
    }

    /*
     * Private
     */
    
    private void setParams(String params) {
        mParams = params;
    }
    
    private void setInstrumentRemoteId(Long instrumentId) {
        mInstrumentRemoteId = instrumentId;
    }

    private Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }
    
    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }
    
    private void setRuleType(String ruleType) {
        if (validRuleType(ruleType)) {
            mRuleType = RuleType.valueOf(ruleType);
        } else {
            // This should never happen
            // We should prevent syncing data unless app is up to date
            Log.wtf(TAG, "Received invalid rule type: " + ruleType);
        }
    }
    
    private static boolean validRuleType(String ruleType) {
        for (RuleType type : RuleType.values()) {
            if (type.name().equals(ruleType)) {
                return true;
            }
        }
        return false;
    }
}
