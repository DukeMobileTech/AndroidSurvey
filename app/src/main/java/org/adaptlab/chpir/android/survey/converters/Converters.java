package org.adaptlab.chpir.android.survey.converters;

import android.arch.persistence.room.TypeConverter;

import org.adaptlab.chpir.android.survey.entities.Question;

import java.util.Date;

public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
