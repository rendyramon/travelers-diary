package com.travelersdiary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.travelersdiary.models.Photo;

import java.io.File;
import java.util.ArrayList;

/**
 * Helper class with methods
 */

public class Utils {
    public static String getFirebaseUserUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID;
    }

    public static String getFirebaseUserTravelsUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_TRAVELS;
    }

    public static String getFirebaseUserDiaryUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_DIARY;
    }

    public static String getFirebaseUserTracksUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_TRACKS;
    }

    public static String getFirebaseUserReminderUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_REMINDER;
    }

    public static String getFirebaseUserWaypointsUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_WAYPOINTS;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Activity activity, int color) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    public static void clearImageCache(final Context context) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Glide.get(context).clearDiskCache();
                return true;
            }
        };
        task.execute();
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean checkFileExists(Context context, String uri) {
        Uri uriPath = Uri.parse(uri);
        String path = Utils.getRealPathFromURI(context, uriPath);
        File file = new File(path);

        return file.exists();
    }

    public static ArrayList<String> photoArrayToStringArray(Context context, ArrayList<Photo> images) {

        ArrayList<String> albumImages = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            if (checkFileExists(context, images.get(i).getLocalUri())) {
                albumImages.add(images.get(i).getLocalUri());
            } else {
                albumImages.add(images.get(i).getPicasaUri());
            }
        }

        return albumImages;
    }

}
