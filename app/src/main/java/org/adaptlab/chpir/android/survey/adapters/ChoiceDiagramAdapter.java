package org.adaptlab.chpir.android.survey.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.adaptlab.chpir.android.survey.relations.DiagramRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChoiceDiagramAdapter extends BaseAdapter {
    private final String TAG = "ChoiceDiagramAdapter";
    private final Context mContext;
    private final List<DiagramRelation> mDiagramRelations;
    private final QuestionRelation mQuestionRelation;
    private final SurveyViewModel mSurveyViewModel;
    private final int mCount;
    private List<Bitmap> mBitmaps;
    private List<Integer> mWidths;
    private List<Integer> mHeights;
    private int mHeight;

    public ChoiceDiagramAdapter(Context context, QuestionRelation questionRelation,
                                List<DiagramRelation> diagramRelations,
                                SurveyViewModel surveyViewModel, int count) {
        this.mContext = context;
        this.mQuestionRelation = questionRelation;
        this.mDiagramRelations = diagramRelations;
        this.mSurveyViewModel = surveyViewModel;
        this.mDiagramRelations.sort((dr1, dr2) -> dr1.diagram.getPosition().compareTo(dr2.diagram.getPosition()));
        this.mCount = count;
        setBitmaps();
    }

    private String getPath(DiagramRelation diagramRelation) {
        String path;
        String optionIdentifier = diagramRelation.options.get(0).option.getIdentifier();
        String translatedOptionIdentifier = "";
        if (!mSurveyViewModel.getInstrumentLanguage().equals(mSurveyViewModel.getDeviceLanguage())) {
            translatedOptionIdentifier = diagramRelation.options.get(0).option.getIdentifier() + "_" +
                    mSurveyViewModel.getDeviceLanguage().toUpperCase();
        }
        if (translatedOptionIdentifier.isEmpty()) {
            path = mContext.getFilesDir().getAbsolutePath() + "/" +
                    mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                    optionIdentifier + ".png";
        } else {
            path = mContext.getFilesDir().getAbsolutePath() + "/" +
                    mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                    translatedOptionIdentifier + ".png";
            File file = new File(path);
            if (!file.exists()) {
                path = mContext.getFilesDir().getAbsolutePath() + "/" +
                        mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                        optionIdentifier + ".png";
            }
        }
        return path;
    }

    private void setBitmaps() {
        mWidths = new ArrayList<>();
        mHeights = new ArrayList<>();
        mBitmaps = new ArrayList<>();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (displayMetrics.widthPixels - 32) / mCount;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        for (int k = 0; k < mDiagramRelations.size(); k++) {
            DiagramRelation diagramRelation = mDiagramRelations.get(k);
            Bitmap bitmap = BitmapFactory.decodeFile(getPath(diagramRelation), options);
            int height = bitmap.getHeight();
            int imageViewWidth = width / mDiagramRelations.size();
            double targetHeight = displayMetrics.heightPixels * 0.15;
            // Scale height
            if (targetHeight > height) {
                double scale = targetHeight / height;
                height = (int) Math.round(height * scale);
            }
            mBitmaps.add(bitmap);
            mHeights.add(height);
            mWidths.add(imageViewWidth);
        }
        if (mHeights.isEmpty()) {
            mHeight = 0;
        } else {
            mHeight = Collections.max(mHeights);
        }
    }

    @Override
    public int getCount() {
        return mDiagramRelations.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout linearLayout;
        if (convertView == null) {
            linearLayout = new LinearLayout(mContext);
            ImageView imageView = new ImageView(mContext);
            Bitmap bitmap = Bitmap.createScaledBitmap(mBitmaps.get(position), mWidths.get(position),
                    mHeights.get(position), true);
            imageView.setImageBitmap(bitmap);
            linearLayout.addView(imageView);
            linearLayout.setMinimumHeight(mHeight);
        } else {
            linearLayout = (LinearLayout) convertView;
        }
        return linearLayout;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
