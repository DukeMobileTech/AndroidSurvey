package org.adaptlab.chpir.android.survey.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.adaptlab.chpir.android.survey.SurveyApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class EncryptUtil {
    private static final String TAG = "EncryptUtil";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String ALIAS = "SURVEY_DATABASE";
    private final static String PREFERENCE_FILE = "Survey";
    private final static String PASSWORD = "PASSWORD";
    private final static String IV = "INITIALIZATION_VECTOR";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static byte[] initializationVector;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey generateKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        keyGenerator.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return keyGenerator.generateKey();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void encrypt(final String text) throws NoSuchAlgorithmException, NoSuchProviderException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {

        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, generateKey());
        initializationVector = cipher.getIV();
        savePassword(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static String decrypt(final byte[] encryptedData, final byte[] encryptionIv) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
            CertificateException, UnrecoverableEntryException, KeyStoreException, IOException {

        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv);
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec);
        return new String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8);
    }

    private static SecretKey getKey() throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null)).getSecretKey();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void savePassword(byte[] text) {
        SharedPreferences sharedPreferences = SurveyApp.getInstance().getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PASSWORD, Base64.encodeToString(text, Base64.DEFAULT));
        editor.putString(IV, Base64.encodeToString(initializationVector, Base64.DEFAULT));
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String getPassword() {
        SharedPreferences sharedPreferences = SurveyApp.getInstance().getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        String password = sharedPreferences.getString(PASSWORD, null);
        String iv = sharedPreferences.getString(IV, null);
        if (password == null || iv == null) return password;
        try {
            return decrypt(Base64.decode(password, Base64.DEFAULT), Base64.decode(iv, Base64.DEFAULT));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException |
                BadPaddingException | IllegalBlockSizeException | CertificateException | UnrecoverableEntryException | KeyStoreException | IOException e) {
            Log.e(TAG, "Exception: " + e);
        }
        return null;
    }

}
