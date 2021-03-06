package com.example.ps6;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ps6.App.AppCamera;
import com.example.ps6.Model.Student;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;



public class PhotographyActivity extends AppCompatActivity {

    //for app log
    private final String TAG = getClass().getSimpleName();
    private final static boolean D = true;

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // Bitmap sampling size
    public static final int BITMAP_SAMPLE_SIZE = 8;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "App2Student35";

    // Image and Video file extensions
    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    private static String imageStoragePath;

    Bundle args;
    Student s;

    private TextView txtDescription;
    private ImageView imgPreview;
    private VideoView videoPreview;
    private Button btnCapturePicture, btnRecordVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        this.getWindow().setBackgroundDrawableResource(R.color.transparent);
        this.getWindow().setGravity(Gravity.CENTER);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Checking availability of the camera
        if (!AppCamera.isDeviceSupportCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device doesn't have camera
            finish();
        }

        if (D) Log.i(TAG, "just started!!");

        //Activity?????? ????????? ???????????? ???????????????
        args = getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        if (args.getSerializable("student") != null) {
            s = (Student) args.getSerializable("student");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(s.getName()+"??? ?????????/????????? ??????!")
                .setPositiveButton("photo", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (AppCamera.checkPermissions(getApplicationContext())) {
                            captureImage();
                        } else {
                            requestCameraPermission(MEDIA_TYPE_IMAGE);
                        }
                    }
                })
                .setNegativeButton("video", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (AppCamera.checkPermissions(getApplicationContext())) {
                            captureVideo();
                        } else {
                            requestCameraPermission(MEDIA_TYPE_VIDEO);
                        }
                    }
                })
                .setCancelable(true)
                .setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).show();

        // restoring storage image path from saved instance state
        // otherwise the path will be null on device rotation
        // ????????? ???????????? ???????????? ????????? ????????? ????????? ???????????????.
        // ????????? ????????? ????????? ?????? ????????? null????????????.
        restoreFromBundle(savedInstanceState);
    }

    /**
     * Restoring store image path from saved instance state
     * (????????? ???????????? ???????????? ????????? ????????? ?????? ??????)
     */
    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_IMAGE_STORAGE_PATH)) {
                imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
                if (!TextUtils.isEmpty(imageStoragePath)) {
                    if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + IMAGE_EXTENSION)) {
                        //previewCapturedImage();
                    } else if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + VIDEO_EXTENSION)) {
                        //previewVideo();
                    }
                }
            }
        }
    }

    /**
     * Requesting permissions using Dexter library
     * (Dexter ?????????????????? ???????????? ?????? ??????)
     */
    private void requestCameraPermission(final int type) {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            if (type == MEDIA_TYPE_IMAGE) {
                                // capture picture
                                captureImage();
                            } else {
                                captureVideo();
                            }

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    /**
     * Capturing Camera Image will launch camera app requested image capture
     * ????????? ????????? ???????????? ????????? ??? ?????? ????????? ????????? ???????????????.
     */
    private void captureImage() {

        File file = AppCamera.getOutputMediaFile(getApplicationContext(), MEDIA_TYPE_IMAGE);

        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = AppCamera.getOutputMediaFileUri(getApplicationContext(), file);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Saving stored image path to saved instance state
     * ????????? ????????? ????????? ????????? ???????????? ??????(instance state)??? ??????
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes(?????? ?????? ????????? null??? ????????? ????????? ?????? URL ??????)
        outState.putString(KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }

    /**
     * Restoring image path from saved instance state
     * ????????? ???????????? ??????(instance state)?????? ????????? ?????? ??????
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
    }

    /**
     * Launching camera app to record video
     */
    private void captureVideo() {

        File file = AppCamera.getOutputMediaFile(getApplicationContext(),MEDIA_TYPE_VIDEO);

        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = AppCamera.getOutputMediaFileUri(getApplicationContext(), file);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                AppCamera.refreshGallery(getApplicationContext(), imageStoragePath);
                if (D) Log.i(TAG, "imageStoragePath: " + imageStoragePath);

                // successfully captured the image
                Intent i = this.getIntent();
                i.putExtra("imageStoragePath", imageStoragePath);
                this.setResult(RESULT_OK, i);
                // display it in image view
                //previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }

        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                AppCamera.refreshGallery(getApplicationContext(), imageStoragePath);

                // video successfully recorded
                Intent i = this.getIntent();
                i.putExtra("imageStoragePath", imageStoragePath);
                this.setResult(RESULT_OK, i);
                // preview the recorded video
                //previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        finish();
    }


    /**
     * Alert dialog to navigate to app settings to enable necessary permissions
     * ??? ???????????? ???????????? ????????? ????????? ??????????????? ?????? ?????? ??????
     */
    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AppCamera.openSettings(PhotographyActivity.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

}