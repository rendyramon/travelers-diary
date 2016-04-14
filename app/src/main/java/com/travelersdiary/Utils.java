package com.travelersdiary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.travelersdiary.models.Photo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Helper class with methods
 */

public class Utils {
    public static String getFirebaseUserUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID;
    }

    public static String getFirebaseUserActiveTravelUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_ACTIVE_TRAVEL;
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
        window.setStatusBarColor(activity.getResources().getColor(color));
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

    public static void tintWidget(Context context, View view, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, color));
        view.setBackground(wrappedDrawable);
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        Cursor cursor = null;
        try {
            String filePath = imageFile.getAbsolutePath();
            cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return context.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean checkFileExists(Context context, String uri) {
        if (uri == null) {
            return false;
        }

        Uri uriPath = Uri.parse(uri);
        String path = Utils.getRealPathFromURI(context, uriPath);

        if (path != null) {
            File file = new File(path);
            return file.exists();
        }

        return false;
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

    public static boolean isInternetAvailable(Context context) {
        NetworkInfo networkInfo = (NetworkInfo) ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        } else if (!networkInfo.isConnected() || networkInfo.isRoaming()) {
            return false;
        }
        return true;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getMediumDate(long timestamp) {
        // String time = DateFormat.getMediumDateFormat(this).format(timestamp)
        return SimpleDateFormat.getDateTimeInstance().format(timestamp);
    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    public static boolean isTabletLandMode(Context context) {
        return context.getResources().getBoolean(R.bool.isTabletLand);
    }
}
