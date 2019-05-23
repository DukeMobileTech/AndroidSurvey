package org.adaptlab.chpir.android.survey.entities.relations;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;

import java.util.List;

public class SurveyRelation {
    @Embedded
    public Survey survey;
    @Relation(parentColumn = "UUID", entityColumn = "SurveyUUID", entity = Response.class)
    public List<Response> responses;

}
