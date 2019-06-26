package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


public class SelectOneImageViewHolder extends QuestionViewHolder {
    private final int SELECTED = Color.GREEN;
    private int mResponseIndex;
    //    private ArrayList<Image> mImages;
    private Integer mPreviouslySelectedViewIndex = Integer.MAX_VALUE;
    private GridView mGridView;

    SelectOneImageViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View v = inflater.inflate(R.layout.fragment_image, questionComponent, false);
//        mImages = (ArrayList<Image>) getQuestion().images();
//        mGridView = (GridView) v.findViewById(R.id.imageGridView);
//        setUpAdapter();
//        mGridView.setOnItemClickListener(new OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                clearBackgroundColor();
//                setBackgroundColor((ImageView) v);
//                setResponseIndex(position);
//            }
//        });
//        questionComponent.addView(mGridView);
    }

//    private void setUpAdapter() {
//        if (getActivity() == null || mGridView == null) return;
//        if (mImages != null) {
//            mGridView.setAdapter(new ImageAdapter(mImages));
//        } else {
//            mGridView.setAdapter(null);
//        }
//    }
//
//    private class ImageAdapter extends ArrayAdapter<Image> {
//        public ImageAdapter(ArrayList<Image> images) {
//            super(getActivity(), 0, images);
//        }
//
//        public View getView(int position, View view, ViewGroup parent) {
//            if (view == null) {
//                view = getActivity().getLayoutInflater().inflate(R.layout.image_item, parent,
//                        false);
//            }
//            Image img = getItem(position);
//            ImageView imageView = (ImageView) view.findViewById(R.id.image_item_view);
//            String path = getActivity().getFileStreamPath(img.getBitmapPath()).getAbsolutePath();
//            BitmapDrawable bitmap = PictureUtils.getScaledDrawable(getActivity(), path);
//            imageView.setImageDrawable(bitmap);
//            if (mPreviouslySelectedViewIndex == position) {
//                imageView.setBackgroundColor(SELECTED);
//            }
//            return view;
//        }
//    }
//
//    private void setBackgroundColor(ImageView view) {
//        view.setBackgroundColor(SELECTED);
//    }
//
//    private void clearBackgroundColor() {
//        for (int i = 0; i < mGridView.getChildCount(); i++) {
//            mGridView.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
//        }
//    }
//
//    private void setResponseIndex(int index) {
//        mResponseIndex = index;
//        setResponse(null);
//    }

    @Override
    protected String serialize() {
        return String.valueOf(mResponseIndex);
    }

    @Override
    protected void deserialize(String responseText) {
        if (!responseText.equals("")) {
            mPreviouslySelectedViewIndex = Integer.parseInt(responseText);
        }
    }

//    @Override
//    protected void unSetResponse() {
//
//    }

}
