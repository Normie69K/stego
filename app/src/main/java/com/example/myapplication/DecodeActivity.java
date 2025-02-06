package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class DecodeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imagePreview;
    private Button uploadImageButton, decryptButton;
    private TextView decodedTextView;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        // Initialize UI Components
        imagePreview = findViewById(R.id.imagePreview);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        decryptButton = findViewById(R.id.decryptButton);
        decodedTextView = findViewById(R.id.decodedTextView);

        // Upload Image Button Click Listener
        uploadImageButton.setOnClickListener(v -> openGallery());

        // Decrypt Button Click Listener
        decryptButton.setOnClickListener(v -> decodeTextFromImage());

        // Info Button Click Listener with Sound Effect
        findViewById(R.id.info_icon).setOnClickListener(v -> {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);  // ✅ Click sound
            mediaPlayer.start();
            startActivity(new Intent(DecodeActivity.this, InfoActivity.class));
        });
    }

    // Open Gallery to Choose an Image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle Selected Image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imagePreview.setImageBitmap(selectedImage);  // ✅ Image now displays correctly
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Placeholder Function for Decoding Text from Image
    private void decodeTextFromImage() {
        if (selectedImage == null) {
            Toast.makeText(this, "Please upload an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement actual text extraction logic
        decodedTextView.setText("Decoded text will be shown here (Feature in progress)");
        Toast.makeText(this, "Decoding text from image (Feature in progress)", Toast.LENGTH_SHORT).show();
    }
}