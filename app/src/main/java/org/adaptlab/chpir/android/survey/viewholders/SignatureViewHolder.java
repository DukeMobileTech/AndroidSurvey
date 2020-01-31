package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.gcacace.signaturepad.views.SignaturePad;


public class SignatureViewHolder extends QuestionViewHolder {
    private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;
    //    private ResponsePhoto mResponsePhoto;
    private Bitmap mSignatureBitmap;

    SignatureViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void unSetResponse() {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        loadOrCreateResponsePhoto();
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels / 5;
//        View view = getLayoutInflater().inflate(R.layout.fragment_signature, null);
//        questionComponent.addView(view);
//
//        mSignaturePad = view.findViewById(R.id.signaturePad);
//        mSignaturePad.getLayoutParams().height = height;
//        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
//            @Override
//            public void onStartSigning() {
//
//            }
//
//            @Override
//            public void onSigned() {
//                mSaveButton.setEnabled(true);
//                mClearButton.setEnabled(true);
//            }
//
//            @Override
//            public void onClear() {
//                mSaveButton.setEnabled(false);
//                mClearButton.setEnabled(false);
//            }
//        });
//
//        mClearButton = view.findViewById(R.id.clearButton);
//        mClearButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mSignaturePad.clear();
//                mResponsePhoto.setPicturePath(null);
//                mResponsePhoto.save();
//            }
//        });
//
//        mSaveButton = view.findViewById(R.id.saveButton);
//        mSaveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mSignatureBitmap = mSignaturePad.getSignatureBitmap();
//                if (saveJpgSignature(mSignatureBitmap)) {
//                    Toast.makeText(getActivity(), R.string.signature_saved,
//                            Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getActivity(), R.string.signature_not_saved,
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

//    private void loadOrCreateResponsePhoto() {
//        if (getResponse() != null) {
//            if (getResponse().getId() == null) getResponse().save();
//            if (getResponse().getResponsePhoto() == null) {
//                mResponsePhoto = new ResponsePhoto();
//                mResponsePhoto.setResponse(getResponse());
//                mResponsePhoto.save();
//            } else {
//                mResponsePhoto = getResponse().getResponsePhoto();
//            }
//        }
//    }
//
//    private boolean saveJpgSignature(Bitmap signature) {
//        boolean result = false;
//        try {
//            String filename = UUID.randomUUID().toString() + ".jpg";
//            File photo = new File(getActivity().getFilesDir(), filename);
//            saveBitmapToJPG(signature, photo);
//            result = true;
//            mResponsePhoto.setPicturePath(filename);
//            mResponsePhoto.save();
//        } catch (IOException e) {
//            Log.e(TAG, "IOException ", e);
//        }
//        return result;
//    }
//
//    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
//        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
//                Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(newBitmap);
//        canvas.drawColor(Color.WHITE);
//        canvas.drawBitmap(bitmap, 0, 0, null);
//        OutputStream stream = new FileOutputStream(photo);
//        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
//        stream.close();
//    }

    @Override
    protected void deserialize(String responseText) {
//        String filename = mResponsePhoto.getPicturePath();
//        if (filename != null && !filename.isEmpty()) {
//            String path = getContext().getFileStreamPath(filename).getAbsolutePath();
//            mSignatureBitmap = BitmapFactory.decodeFile(path);
//            if (mSignaturePad != null) {
//                mSignaturePad.setSignatureBitmap(mSignatureBitmap);
//            }
//        }
    }

    @Override
    protected String serialize() {
        return null;
    }
}
