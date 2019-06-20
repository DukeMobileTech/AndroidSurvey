package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;

import java.util.List;

public class ResponseRelation {
    @Embedded
    public Response response;
    @Relation(parentColumn = "SurveyUUID", entityColumn = "UUID", entity = Survey.class)
    public List<Survey> surveys;
}
