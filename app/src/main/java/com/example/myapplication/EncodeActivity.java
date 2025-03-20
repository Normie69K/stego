package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;

public class EncodeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST_1 = 1;
    private static final int PICK_IMAGE_REQUEST_2 = 2;

    private ImageView baseImageView, hiddenImageView, encodedImageView;
    private Button selectBaseImageButton, selectHiddenImageButton, encodeButton, downloadButton;
    private Bitmap baseImage, hiddenImage, encodedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        // Initialize UI components
        baseImageView = findViewById(R.id.baseImagePreview);
        hiddenImageView = findViewById(R.id.hiddenImagePreview);
        encodedImageView = findViewById(R.id.encodedImagePreview);
        selectBaseImageButton = findViewById(R.id.selectBaseImageButton);
        selectHiddenImageButton = findViewById(R.id.selectHiddenImageButton);
        encodeButton = findViewById(R.id.encodeButton);
        downloadButton = findViewById(R.id.downloadButton);

        // Set click listeners
        selectBaseImageButton.setOnClickListener(v -> selectImage(PICK_IMAGE_REQUEST_1));
        selectHiddenImageButton.setOnClickListener(v -> selectImage(PICK_IMAGE_REQUEST_2));
        encodeButton.setOnClickListener(v -> encodeImage());
        downloadButton.setOnClickListener(v -> saveEncodedImage());
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
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                if (requestCode == PICK_IMAGE_REQUEST_1) {
                    baseImage = selectedBitmap;
                    baseImageView.setImageBitmap(baseImage);
                } else if (requestCode == PICK_IMAGE_REQUEST_2) {
                    hiddenImage = selectedBitmap;
                    hiddenImageView.setImageBitmap(hiddenImage);
                }
            } catch (IOException e) {
                showError("Failed to load image");
            }
        }
    }

    private void encodeImage() {
        if (baseImage == null || hiddenImage == null) {
            showError("Please select both images first");
            return;
        }

        try {
            // Get dimensions
            int baseWidth = baseImage.getWidth();
            int baseHeight = baseImage.getHeight();
            int hiddenWidth = hiddenImage.getWidth();
            int hiddenHeight = hiddenImage.getHeight();

            // Validate sizes
            if (hiddenWidth > baseWidth || hiddenHeight > baseHeight) {
                showError("Hidden image must be smaller than base image");
                return;
            }

            // Create writable copy
            encodedBitmap = baseImage.copy(Bitmap.Config.ARGB_8888, true);

            // Store metadata
            encodedBitmap.setPixel(0, 0, Color.rgb(1, 0, 0)); // Version marker
            encodedBitmap.setPixel(1, 0, Color.rgb(
                    (hiddenWidth >> 8) & 0xFF,
                    hiddenWidth & 0xFF,
                    0
            ));
            encodedBitmap.setPixel(2, 0, Color.rgb(
                    (hiddenHeight >> 8) & 0xFF,
                    hiddenHeight & 0xFF,
                    0
            ));

            // Encode hidden image data
            for (int x = 0; x < hiddenWidth; x++) {
                for (int y = 0; y < hiddenHeight; y++) {
                    int basePixel = encodedBitmap.getPixel(x, y);
                    int hiddenPixel = hiddenImage.getPixel(x, y);

                    // Embed 3-2-3 bits
                    int newRed = (Color.red(basePixel) & 0xF8) | (Color.red(hiddenPixel) >> 5);
                    int newGreen = (Color.green(basePixel) & 0xFC) | (Color.green(hiddenPixel) >> 6);
                    int newBlue = (Color.blue(basePixel) & 0xF8) | (Color.blue(hiddenPixel) >> 5);

                    encodedBitmap.setPixel(x, y, Color.rgb(newRed, newGreen, newBlue));
                }
            }

            encodedImageView.setImageBitmap(encodedBitmap);
            showToast("Encoding successful!");
        } catch (Exception e) {
            showError("Encoding failed: " + e.getMessage());
        }
    }

    private void saveEncodedImage() {
        if (encodedBitmap == null) {
            showError("No image to save");
            return;
        }

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "encoded_image.png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Stego/");

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    encodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    showToast("Image saved successfully!");
                }
            }
        } catch (Exception e) {
            showError("Save failed: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String error) {
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
    }
}