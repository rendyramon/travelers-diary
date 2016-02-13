package com.travelersdiary.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.AlbumImagesAdapter;
import com.travelersdiary.models.AlbumImages;
import com.travelersdiary.models.AlbumsModel;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AlbumImagesActivity extends AppCompatActivity implements AlbumImagesAdapter.ViewHolder.ClickListener {

    public static final String SELECTED_IMAGES = "selected_images";
    public static int PHOTO_SPAN_COUNT = 3;

    @Bind(R.id.album_images_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.album_images_list)
    RecyclerView mRecyclerView;

    @Bind(R.id.btnShow)
    Button btnSelection;

    private AlbumImagesAdapter mAdapter;

    private ArrayList<AlbumsModel> albumsModels;
    private int mPosition;

    public ArrayList<String> mShareImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_images);
        ButterKnife.bind(this);

        mPosition = getIntent().getIntExtra("position", 0);
        albumsModels = (ArrayList<AlbumsModel>) getIntent().getSerializableExtra("albumsList");

        setSupportActionBar(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(albumsModels.get(mPosition).getFolderName());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PHOTO_SPAN_COUNT));

        // create an Object for Adapter
        mAdapter = new AlbumImagesAdapter(this, getAlbumImages(), this);

        // set the adapter object to the RecyclerView
        mRecyclerView.setAdapter(mAdapter);

        btnSelection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
//                intent.setType("image/jpeg"); /* This example is sharing jpeg images. */
//                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, mShareImages);
//                startActivity(intent);

                Intent i = new Intent();
                i.putStringArrayListExtra(SELECTED_IMAGES, mShareImages);
                setResult(RESULT_OK, i);
                finish();
            }
        });

    }

    private Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = this.getContentResolver().query(
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
                return this.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    private ArrayList<AlbumImages> getAlbumImages() {
        Object[] abc = albumsModels.get(mPosition).folderImages.toArray();

        Log.i("imagesLength", "" + abc.length);
        ArrayList<AlbumImages> paths = new ArrayList<>();
        int size = abc.length;
        for (int i = 0; i < size; i++) {
            AlbumImages albumImages = new AlbumImages();
            albumImages.setAlbumImages((String) abc[i]);
            paths.add(albumImages);
        }

        return paths;

    }

    @Override
    public void onItemClicked(int position) {

        toggleSelection(position);

    }

    @Override
    public boolean onItemLongClicked(int position) {

        toggleSelection(position);

        return true;
    }

    @Override
    protected void onDestroy() {
        Utils.clearImageCache(this); // clears all glide cache
        super.onDestroy();
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        Log.i("string path", "" + mAdapter.getAlbumImagesList().get(position).getAlbumImages());

        Uri uriPath = Uri.parse(mAdapter.getAlbumImagesList().get(position).getAlbumImages());
        String path = uriPath.getPath();
        File imageFile = new File(path);
        String uri = getImageContentUri(imageFile).toString();

        if (mAdapter.isSelected(position)) {
            mShareImages.add(uri);
        } else {
            mShareImages.remove(uri);
        }
        Log.i("uri path", "" + mShareImages);
    }

}
