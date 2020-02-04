package org.adaptlab.chpir.android.survey.views;

public interface DrawableClickListener {
    void onClick(DrawablePosition target);

    enum DrawablePosition {TOP, BOTTOM, LEFT, RIGHT}
}
