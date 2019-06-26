package org.adaptlab.chpir.android.survey.entities;

public interface Uploadable {
    String toJSON();

    void setSent(boolean status);
}
