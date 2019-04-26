package org.adaptlab.chpir.android.survey.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "DisplayTranslations",
        foreignKeys = @ForeignKey(entity = Display.class,
        parentColumns = "RemoteId", childColumns = "DisplayRemoteId", onDelete = CASCADE))
public class DisplayTranslation {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId")
    private Long mRemoteId;
    @SerializedName("text")
    @ColumnInfo(name = "Text")
    private String mText;
    @SerializedName("language")
    @ColumnInfo(name = "Language")
    private String mLanguage;
    @SerializedName("display_id")
    @ColumnInfo(name = "DisplayRemoteId", index = true)
    private Long mDisplayRemoteId;

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long id) {
        this.mRemoteId = id;
    }

    public String getText() {
        return mText;
    }

    public void setText(String title) {
        this.mText = title;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        this.mLanguage = language;
    }

    public Long getDisplayRemoteId() {
        return mDisplayRemoteId;
    }

    public void setDisplayRemoteId(Long remoteId) {
        this.mDisplayRemoteId = remoteId;
    }

}
