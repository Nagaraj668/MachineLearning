package in.codefordevs.machinelearning;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
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

                    /*detectLabels(BitmapFactory.decodeFile(imgDecodableString),
                            selectedImage.getEncodedPath());*/

                    // detectText(BitmapFactory.decodeFile(imgDecodableString), selectedImage.getEncodedPath());

                    scanBarcode(BitmapFactory.decodeFile(imgDecodableString), selectedImage.getEncodedPath());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanBarcode(Bitmap decodeFile, String encodedPath) {
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE,
                                FirebaseVisionBarcode.FORMAT_AZTEC)
                        .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(decodeFile);

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();


        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        // Task completed successfully
                        // ...

                        List<ImageLabelResult> imageLabelResults = new ArrayList<>();

                        for (FirebaseVisionBarcode barcode: barcodes) {
                            String rawValue = barcode.getRawValue();

                            imageLabelResults.add(new ImageLabelResult(rawValue, 0));

                            int valueType = barcode.getValueType();

                            switch (valueType) {
                                case FirebaseVisionBarcode.TYPE_WIFI:
                                    String ssid = barcode.getWifi().getSsid();
                                    String password = barcode.getWifi().getPassword();
                                    int type = barcode.getWifi().getEncryptionType();
                                    break;
                                case FirebaseVisionBarcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    break;
                            }
                        }

                        Gson gson = new Gson();
                        String labelResult = gson.toJson(imageLabelResults);

                        Intent intent = new Intent(getApplicationContext()
                                , ImageLabelActivity.class);
                        intent.putExtra("RESULT", labelResult);
                        intent.putExtra("IMG", encodedPath);

                        startActivity(intent);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });

    }

    private void detectText(Bitmap decodeFile, String encodedPath) {
        imageView.setImageBitmap(decodeFile);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(decodeFile);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...

                                String resultText = firebaseVisionText.getText();

                                List<ImageLabelResult> imageLabelResults = new ArrayList<>();

                                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    imageLabelResults.add(new ImageLabelResult(blockText, 0));

                                    for (FirebaseVisionText.Line line : block.getLines()) {
                                        blockText = line.getText();
                                        imageLabelResults.add(new ImageLabelResult(blockText, 0));

                                        for (FirebaseVisionText.Element element : line.getElements()) {
                                            blockText = element.getText();
                                            imageLabelResults.add(new ImageLabelResult(blockText, 0));
                                        }
                                    }
                                }

                                Gson gson = new Gson();
                                String labelResult = gson.toJson(imageLabelResults);

                                Intent intent = new Intent(getApplicationContext()
                                        , ImageLabelActivity.class);
                                intent.putExtra("RESULT", labelResult);
                                intent.putExtra("IMG", encodedPath);

                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

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
