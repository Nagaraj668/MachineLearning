package in.codefordevs.machinelearning;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.codefordevs.machinelearning.activity.ImageLabelActivity;
import in.codefordevs.machinelearning.listener.PermissionListener;
import in.codefordevs.machinelearning.model.ImageLabelResult;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FROM_GALLERY = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.imageView);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> pickImageFromGallery());
    }

    private void pickImageFromGallery() {
        verifyStoragePermissions(() -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
        });
    }

    public void verifyStoragePermissions(PermissionListener permissionListener) {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            permissionListener.onPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Permission denied",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case PICK_FROM_GALLERY:

                if (resultCode == RESULT_OK) {
                    //pick image from gallery
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();

                    detectLabels(BitmapFactory.decodeFile(imgDecodableString),
                            selectedImage.getEncodedPath());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void detectLabels(Bitmap decodeFile, String encodedPath) {

        imageView.setImageBitmap(decodeFile);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(decodeFile);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getOnDeviceImageLabeler();

        List<ImageLabelResult> imageLabelResults = new ArrayList<>();

        labeler.processImage(image)
                .addOnSuccessListener((labels) -> {
                    for (FirebaseVisionImageLabel label : labels) {
                        String text = label.getText();
                        float confidence = label.getConfidence();
                        imageLabelResults.add(new ImageLabelResult(text, confidence));
                    }

                    Gson gson = new Gson();
                    String labelResult = gson.toJson(imageLabelResults);

                    Intent intent = new Intent(getApplicationContext()
                            , ImageLabelActivity.class);
                    intent.putExtra("RESULT", labelResult);
                    intent.putExtra("IMG", encodedPath);

                    startActivity(intent);
                })
                .addOnFailureListener((e) -> {
                    e.printStackTrace();
                });
    }
}
