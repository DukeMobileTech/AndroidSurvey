package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import org.adaptlab.chpir.android.survey.models.Image;

import java.util.ArrayList;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class SelectMultipleImageViewHolder extends SingleQuestionViewHolder {
    private final int SELECTED = Color.GREEN;
    private final int UNSELECTED = Color.TRANSPARENT;
    private List<Integer> mSelectedViews;
    private GridView mGridView;
    private ArrayList<Image> mImages;

    SelectMultipleImageViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View v = inflater.inflate(R.layout.fragment_image, questionComponent, false);
//        mImages = (ArrayList<Image>) getQuestion().images();
//        mSelectedViews = new ArrayList<Integer>();
//        mGridView = (GridView) v.findViewById(R.id.imageGridView);
//        setUpAdapter();
//        mGridView.setOnItemClickListener(new OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                toggleImageBackgroundColor((ImageView) v);
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

//    private void toggleImageBackgroundColor(ImageView view) {
//        ColorDrawable drawable = (ColorDrawable) view.getBackground();
//        if (drawable.getColor() == SELECTED) {
//            view.setBackgroundColor(UNSELECTED);
//        } else {
//            view.setBackgroundColor(SELECTED);
//        }
//        setResponse(null);
//    }

    @Override
    protected String serialize() {
        StringBuilder serialized = new StringBuilder();
        for (int i = 0; i < mGridView.getChildCount(); i++) {
            ImageView view = (ImageView) mGridView.getChildAt(i);
            ColorDrawable drawable = (ColorDrawable) view.getBackground();
            if (drawable.getColor() == SELECTED) {
                serialized.append(i);
                if (i < mGridView.getChildCount() - 1) serialized.append(COMMA);
            }
        }
        return serialized.toString();
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) return;
        String[] listOfIndices = responseText.split(COMMA);
        for (String index : listOfIndices) {
            if (!index.equals("")) {
                Integer indexInteger = Integer.parseInt(index);
                mSelectedViews.add(indexInteger);
            }
        }
    }

//    private class ImageAdapter extends ArrayAdapter<Image> {
//
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
//            if (mSelectedViews.contains(position)) {
//                imageView.setBackgroundColor(SELECTED);
//            } else {
//                imageView.setBackgroundColor(UNSELECTED);
//            }
//            return view;
//        }
//    }
//
//    @Override
//    protected void unSetResponse() {
//
//    }

}
