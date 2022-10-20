package org.adaptlab.chpir.android.survey.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.adaptlab.chpir.android.survey.relations.CollageRelation;
import org.adaptlab.chpir.android.survey.relations.DiagramRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionDiagramAdapter extends BaseAdapter {
    private final String TAG = "QuestionDiagramAdapter";
    private final Context mContext;
    private final List<DiagramRelation> mDiagramRelations;
    private final QuestionRelation mQuestionRelation;
    private final SurveyViewModel mSurveyViewModel;
    private List<Bitmap> mBitmaps;
    private List<Integer> mWidths;
    private List<Integer> mHeights;
    private int mHeight;
    private int mLayoutWidth = 0;
    private int mDisplayWidth;

    public QuestionDiagramAdapter(Context context, QuestionRelation questionRelation,
                                  CollageRelation collageRelation, SurveyViewModel surveyViewModel) {
        this.mContext = context;
        this.mQuestionRelation = questionRelation;
        this.mDiagramRelations = collageRelation.diagrams;
        this.mSurveyViewModel = surveyViewModel;
        this.mDiagramRelations.sort((dr1, dr2) -> dr1.diagram.getPosition().compareTo(dr2.diagram.getPosition()));
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
        Log.i(TAG, "PATH: " + path);
        return path;
    }

    private void setBitmaps() {
        mWidths = new ArrayList<>();
        mHeights = new ArrayList<>();
        mBitmaps = new ArrayList<>();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        mDisplayWidth = displayMetrics.widthPixels - 32;
        double targetHeight = displayMetrics.heightPixels * 0.1;
        double targetWidth = ((mDisplayWidth - (mDisplayWidth * 0.1)) / mDiagramRelations.size());
        for (int k = 0; k < mDiagramRelations.size(); k++) {
            DiagramRelation diagramRelation = mDiagramRelations.get(k);
            Bitmap bitmap = BitmapFactory.decodeFile(getPath(diagramRelation), options);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            Log.i(TAG, "Target width: " + targetWidth + " Actual width: " + bitmap.getWidth());
            Log.i(TAG, "Target height: " + targetHeight + " Actual height: " + bitmap.getHeight());
            // Scale based on height
            if (targetHeight > height) {
                double scale = targetHeight / height;
                width = (int) Math.round(width * scale);
                height = (int) Math.round(height * scale);
            }
            // TODO : Use different target widths for each image
            // default to original sizes
//            if (width > targetWidth) {
//                if (bitmap.getWidth() > targetWidth) {
//                    double scale = bitmap.getWidth() / targetWidth;
//                    width = (int) Math.round(bitmap.getWidth() * scale);
//                    height = (int) Math.round(bitmap.getHeight() * scale);
//                }
//                else {
//                    height = bitmap.getHeight();
//                    width = bitmap.getWidth();
//                }
//            }
            mBitmaps.add(bitmap);
            mHeights.add(height);
            mWidths.add(width);
        }
        mHeight = Collections.max(mHeights);
        for (int imgWidth : mWidths) {
            mLayoutWidth += (imgWidth + (0.1 * mDisplayWidth));
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
            ImageView imageView;
            imageView = new ImageView(mContext);
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

    public int getViewWidth() {
        return Math.min(mLayoutWidth, mDisplayWidth);
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
