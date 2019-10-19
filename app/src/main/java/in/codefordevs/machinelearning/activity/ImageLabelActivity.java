package in.codefordevs.machinelearning.activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import in.codefordevs.machinelearning.R;
import in.codefordevs.machinelearning.adapter.ImageLabelAdapter;
import in.codefordevs.machinelearning.model.ImageLabelResult;

public class ImageLabelActivity extends AppCompatActivity {

    private ImageLabelAdapter imageLabelAdapter;
    private List<ImageLabelResult> imageLabelResults;
    private RecyclerView recyclerView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_label);

        String imageLabel = getIntent().getStringExtra("RESULT");
        String imageURI = getIntent().getStringExtra("IMG");
        Gson gson = new Gson();

        Type listType = new TypeToken<ArrayList<ImageLabelResult>>(){}.getType();

        imageLabelResults = gson.fromJson(imageLabel, listType);

        imageView = findViewById(R.id.imageView2);
        recyclerView = findViewById(R.id.recyclerView);

        imageLabelAdapter = new ImageLabelAdapter(this);
        imageLabelAdapter.setImageLabelResults(imageLabelResults);

        recyclerView.setAdapter(imageLabelAdapter);

        imageView.setImageURI(Uri.fromFile(new File(imageURI)));
    }
}
