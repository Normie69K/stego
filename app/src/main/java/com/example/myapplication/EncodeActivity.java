package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class EncodeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST_1 = 1;
    private static final int PICK_IMAGE_REQUEST_2 = 2;

    private ImageView baseImageView, hiddenImageView, encodedImageView;
    private Button selectBaseImageButton, selectHiddenImageButton, encodeButton;
    private Bitmap baseImage, hiddenImage;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        // Initialize UI Components
//        scrollView = findViewById(R.id.scrollView);  // Ensure scrolling
        baseImageView = findViewById(R.id.baseImagePreview);
        hiddenImageView = findViewById(R.id.hiddenImagePreview);
        encodedImageView = findViewById(R.id.encodedImagePreview);
        selectBaseImageButton = findViewById(R.id.selectBaseImageButton);
        selectHiddenImageButton = findViewById(R.id.selectHiddenImageButton);
        encodeButton = findViewById(R.id.encodeButton);

        // Upload Base Image
        selectBaseImageButton.setOnClickListener(v -> selectImage(PICK_IMAGE_REQUEST_1));

        // Upload Hidden Image
        selectHiddenImageButton.setOnClickListener(v -> selectImage(PICK_IMAGE_REQUEST_2));

        // Encode Button
        encodeButton.setOnClickListener(v -> encodeImage());

        // Info Button Click Sound + Redirect
        findViewById(R.id.info_icon).setOnClickListener(v -> {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);
            mediaPlayer.start();
            startActivity(new Intent(EncodeActivity.this, InfoActivity.class));
        });
    }

    private void selectImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                if (requestCode == PICK_IMAGE_REQUEST_1) {
                    baseImage = selectedBitmap;
                    baseImageView.setImageBitmap(baseImage);
                } else if (requestCode == PICK_IMAGE_REQUEST_2) {
                    hiddenImage = selectedBitmap;
                    hiddenImageView.setImageBitmap(hiddenImage);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void encodeImage() {
        if (baseImage == null || hiddenImage == null) {
            Toast.makeText(this, "Please select both images first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulating encoding process (Replace with actual encoding logic)
        encodedImageView.setImageBitmap(baseImage);  // For now, just showing the base image
        encodedImageView.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Encoding successful!", Toast.LENGTH_SHORT).show();

        // Scroll to the encoded image
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
}
