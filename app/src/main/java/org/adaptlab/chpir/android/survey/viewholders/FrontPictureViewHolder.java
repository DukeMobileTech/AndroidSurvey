package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class FrontPictureViewHolder extends PictureViewHolder {
    FrontPictureViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }
//    private static final String TAG = "FrontPictureFragment";
//    private ResponsePhoto mPhoto;

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        loadOrCreateResponsePhoto();
//        if (isCameraAvailable()) {
//            mPhoto = getResponsePhoto();
//            Button mCameraButton = new Button(getActivity());
//            mCameraButton.setText(R.string.enable_camera);
//            LinearLayout.LayoutParams buttonLayout = new LinearLayout.LayoutParams(500, 120);
//            buttonLayout.gravity = Gravity.CENTER | Gravity.TOP;
//            buttonLayout.bottomMargin = 250;
//            mCameraButton.setLayoutParams(buttonLayout);
//            mCameraButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    FragmentManager fm = getActivity().getSupportFragmentManager();
//                    FragmentTransaction transaction = fm.beginTransaction();
//                    mCameraFragment = CameraFragment.newCameraFragmentInstance(mPhoto,
//                            FRONT_CAMERA);
//                    transaction.replace(R.id.fragmentContainer, mCameraFragment);
//                    transaction.addToBackStack(null);
//                    transaction.commit();
//                }
//            });
//            mPhotoView = new ImageView(getActivity());
//            LinearLayout.LayoutParams imageLayout = new LinearLayout.LayoutParams(LayoutParams
//                    .MATCH_PARENT, LayoutParams.MATCH_PARENT);
//            imageLayout.bottomMargin = 250;
//            mPhotoView.setLayoutParams(imageLayout);
//            boolean picturePresent = showPhoto();
//            questionComponent.addView(mCameraButton);
//            questionComponent.addView(mPhotoView);
//            if (picturePresent) {
//                questionComponent.addView(setDeleteButton(mPhoto, mPhotoView));
//            }
//        } else {
//            Log.i(TAG, "Camera Not Available");
//        }
    }

}
