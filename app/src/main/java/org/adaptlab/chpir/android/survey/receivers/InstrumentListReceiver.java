package org.adaptlab.chpir.android.survey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.adaptlab.chpir.android.survey.models.AdminSettings;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Rule;
import org.adaptlab.chpir.android.survey.models.Rule.RuleType;

public class InstrumentListReceiver extends BroadcastReceiver {

    private static final String TAG = "InstrumentListReceiver";
    private static final String INSTRUMENT_LIST = "org.adaptlab.chpir.android.survey.instrument_list";
    private static final String INSTRUMENT_TITLE_LIST = "org.adaptlab.chpir.android.survey.instrument_title_list";
    private static final String INSTRUMENT_ID_LIST = "org.adaptlab.chpir.android.survey.instrument_id_list";
    private static final String INSTRUMENT_PARTICIPANT_TYPE = "org.adaptlab.chpir.android.survey.instrument_participant_type";
    private static final String INSTRUMENT_PARTICIPANT_AGE = "org.adaptlab.chpir.android.survey.instrument_participant_age";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast to send list of available instruments");
        
        Long currentProjectId;
        
        try {
            currentProjectId = Long.valueOf(AdminSettings.getInstance().getProjectId());
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Project ID is not a number: " + nfe);
            return;
        }
        
        int instrumentListSize = Instrument.getAllProjectInstruments(currentProjectId).size();
        
        String[] instrumentTitleList = new String[instrumentListSize];
        long[] instrumentIdList = new long[instrumentListSize];
        String[] instrumentParticipantTypes = new String[instrumentListSize];
        String[] instrumentParticipantAges = new String[instrumentListSize];

        for (int i = 0; i < instrumentListSize; i++) {
            instrumentTitleList[i] = Instrument.getAllProjectInstruments(currentProjectId).get(i).getTitle();
            instrumentIdList[i] = Instrument.getAllProjectInstruments(currentProjectId).get(i).getRemoteId();
            Rule participantTypeRule = Rule.findByRuleTypeAndInstrument(RuleType.PARTICIPANT_TYPE_RULE, Instrument.getAllProjectInstruments(currentProjectId).get(i));
            if (participantTypeRule == null) {
                instrumentParticipantTypes[i] = "";
            } else {
                instrumentParticipantTypes[i] = participantTypeRule.getParamJSON().toString();
            }
            Rule participantAgeRule = Rule.findByRuleTypeAndInstrument(RuleType.PARTICIPANT_AGE_RULE, Instrument.getAllProjectInstruments(currentProjectId).get(i));
            if (participantAgeRule == null) {
                instrumentParticipantAges[i] = "";
            } else {
                instrumentParticipantAges[i] = participantAgeRule.getParamJSON().toString();
            }
        }
        
        Intent i = new Intent();
        i.setAction(INSTRUMENT_LIST);
        i.putExtra(INSTRUMENT_TITLE_LIST, instrumentTitleList);
        i.putExtra(INSTRUMENT_ID_LIST, instrumentIdList);
        i.putExtra(INSTRUMENT_PARTICIPANT_TYPE, instrumentParticipantTypes);
        i.putExtra(INSTRUMENT_PARTICIPANT_AGE, instrumentParticipantAges);
        context.sendBroadcast(i);
    }
}
