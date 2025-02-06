package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class EncodeTextToImg extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView, infoButton;
    private EditText editText;
    private Button selectImageButton, encodeButton;
    private Bitmap selectedImage;
    private MediaPlayer mediaPlayer; // Click sound

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encode_text_to_img);

        // Initialize UI Components
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editTextText);
        selectImageButton = findViewById(R.id.selectImageButton);
        encodeButton = findViewById(R.id.encodeButton);
        infoButton = findViewById(R.id.info_icon); // Corrected reference

        // Initialize Click Sound
        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);

        // Info Button - Click Sound & Redirect to InfoActivity
        if (infoButton != null) {
            infoButton.setOnClickListener(v -> {
                playClickSound();
                startActivity(new Intent(EncodeTextToImg.this, InfoActivity.class));
            });
        }

        // Select Image Button - Click Sound & Open Gallery
        selectImageButton.setOnClickListener(v -> {
            playClickSound();
            openGallery();
        });

        // Encode Button - Click Sound & Encode Placeholder
        encodeButton.setOnClickListener(v -> {
            playClickSound();
            encodeTextIntoImage();
        });
    }

    // Play Click Sound
    private void playClickSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
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
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Placeholder Function for Encoding Text into Image
    private void encodeTextIntoImage() {
        String textToEncode = editText.getText().toString().trim();

        if (selectedImage == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textToEncode.isEmpty()) {
            Toast.makeText(this, "Enter text to encode", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement actual text encoding logic
        Toast.makeText(this, "Encoding text into image (Feature in progress)", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
