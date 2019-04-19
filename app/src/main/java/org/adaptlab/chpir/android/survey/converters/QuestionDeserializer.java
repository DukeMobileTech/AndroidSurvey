package org.adaptlab.chpir.android.survey.converters;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.adaptlab.chpir.android.survey.entities.Question;

import java.lang.reflect.Type;

public class QuestionDeserializer implements JsonDeserializer<Question> {
    @Override
    public Question deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Question question = new Gson().fromJson(json.getAsJsonObject(), Question.class);
        // QuestionType is saved as a String in the database, but it is passed through a converter first
        Question.QuestionType questionType = new Question.QuestionType(json.getAsJsonObject().get("question_type").getAsString());
        question.setQuestionType(questionType);
        return question;
    }
}
