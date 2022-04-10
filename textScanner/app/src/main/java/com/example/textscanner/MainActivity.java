package com.example.textscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    ImageView image;
    Button scan;
    TextView text;
    Button copyText;
    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = findViewById(R.id.imageView);
        scan = findViewById(R.id.button);
        text = findViewById(R.id.textView3);
        copyText = findViewById(R.id.button5);

        // to check whether permission is granted tto use camera
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // grant permission
            requestPermissions(new String[]{Manifest.permission.CAMERA},101);
        }

        // to capture image
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open the camera > for that create intent
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,101);
            }
        });

        copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("copy_text",result);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // processing of captured image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // use bundle to get image data
        Bundle bundle = data.getExtras();
        bundle.get("data");

        // use bitmap to convert that data to image
        Bitmap bitmap = (Bitmap) bundle.get("data");

        // set image in imageview
        image.setImageBitmap(bitmap);

        // processing starts
        // 1. create a FirebaseVisionImage object from a Bitmap object
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        // 2. get instance of firebase vision
        FirebaseVision firebaseVision = FirebaseVision.getInstance();

        // 3. create instance of firebase vision text recognizer
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();

        // 4. create a task to process the image
        Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);

        // 5. if task is success
        task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                result = firebaseVisionText.getText();
                text.setText(result);

            }
        });

        // 6. if task is failure
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.about) {
            Intent intent2 = new Intent(this, MainActivity2.class);
            startActivity(intent2);
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}