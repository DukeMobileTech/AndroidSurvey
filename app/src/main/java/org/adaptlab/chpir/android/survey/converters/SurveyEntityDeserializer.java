package org.adaptlab.chpir.android.survey.converters;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.adaptlab.chpir.android.survey.entities.SurveyEntity;

import java.lang.reflect.Type;

public class SurveyEntityDeserializer<T extends SurveyEntity> implements JsonDeserializer<SurveyEntity> {
    private Class<T> mClass;

    public SurveyEntityDeserializer(Class<T> klass) {
        mClass = klass;
    }

    @Override
    public SurveyEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        SurveyEntity entity = new Gson().fromJson(json.getAsJsonObject(), mClass);
        if (json.getAsJsonObject().get("deleted_at").isJsonNull()) {
            entity.setDeleted(false);
        } else {
            entity.setDeleted(true);
        }
        return entity;
    }

}