package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;

@Table(name = "ResponsePhotos")
public class ResponsePhoto extends SendModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAG = "ResponsePhoto";
    @Column(name = "SentToRemote")
    private boolean mSent;
    @Column(name = "Response", onDelete = Column.ForeignKeyAction.SET_NULL)
    private Response mResponse;
    @Column(name = "PicturePath")
    private String mPicturePath;
    @Column(name = "CameraOrientation")
    private Integer mCameraOrientation;
    @Column(name = "Camera")
    private Integer mCamera;

    public ResponsePhoto() {
        super();
        mSent = false;
    }

    public static List<ResponsePhoto> getAll() {
        return new Select().from(ResponsePhoto.class).orderBy("Id ASC").execute();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("response_uuid", getResponse().getUUID());
            jsonObject.put("picture_data", getEncodedImage());
            json.put("response_image", jsonObject);
        } catch (JSONException je) {
            Log.e(TAG, "JSON exception", je);
        }
        return json;
    }

    private String getEncodedImage() {
        String encodedImage = "";
        if (getPicturePath() != null && !getPicturePath().equals("")) {
            String filepath = AppUtil.getContext().getFileStreamPath(getPicturePath()).getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            encodedImage = encodeImage(bitmap);
        } else {
            encodedImage = null;
        }
        return encodedImage;
    }

    private String encodeImage(Bitmap bitmap) {
        String encodedImage;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] pictureBytes = outputStream.toByteArray();
        encodedImage = Base64.encodeToString(pictureBytes, Base64.DEFAULT);
        return encodedImage;
    }

    @Override
    public boolean isSent() {
        return mSent;
    }

    @Override
    public boolean readyToSend() {
        if (getResponse() == null) {
            return getEncodedImage() != null;
        } else {
            return getResponse().getSurvey().readyToSend();
        }
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    public Response getResponse() {
        return mResponse;
    }

    public void setResponse(Response response) {
        mResponse = response;
    }

    public String getPicturePath() {
        return mPicturePath;
    }

    public void setPicturePath(String path) {
        mPicturePath = path;
    }

    @Override
    public void setAsSent(Context context) {
        mSent = true;
        this.delete();
        if (getResponse() != null) getResponse().delete();
        if (AppUtil.DEBUG) Log.d(TAG, getAll().size() + " response photos left on device");
    }

    public int getCameraOrientation() {
        return mCameraOrientation;
    }

    public void setCameraOrientation(int orientation) {
        mCameraOrientation = orientation;
    }

    public int getCamera() {
        return mCamera;
    }

    public void setCamera(int camera) {
        mCamera = camera;
    }

}
