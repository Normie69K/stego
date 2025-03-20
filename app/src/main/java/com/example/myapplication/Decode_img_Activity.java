package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

public class Decode_img_Activity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imagePreview, decodedImagePreview;
    private Bitmap selectedImage;
    private TextView decodedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        imagePreview = findViewById(R.id.imagePreview);
        decodedImagePreview = findViewById(R.id.decodedImagePreview);
        Button uploadImageButton = findViewById(R.id.uploadImageButton);
        Button decryptButton = findViewById(R.id.decryptButton);
        decodedTextView = findViewById(R.id.decodedTextView);

        uploadImageButton.setOnClickListener(v -> openGallery());
        decryptButton.setOnClickListener(v -> decodeImage());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imagePreview.setImageBitmap(selectedImage);
            } catch (IOException e) {
                showError("Failed to load image");
            }
        }
    }

    private void decodeImage() {
        if (selectedImage == null) {
            showError("Upload an image first");
            return;
        }

        try {
            int firstPixel = selectedImage.getPixel(0, 0);
            if ((Color.red(firstPixel) & 0x01) != 1) {
                showError("No hidden image");
                return;
            }

            int widthPixel = selectedImage.getPixel(1, 0);
            int hiddenWidth = ((Color.red(widthPixel) & 0xFF) << 8) | (Color.green(widthPixel) & 0xFF);

            int heightPixel = selectedImage.getPixel(2, 0);
            int hiddenHeight = ((Color.red(heightPixel) & 0xFF) << 8) | (Color.green(heightPixel) & 0xFF);

            if (hiddenWidth <= 0 || hiddenHeight <= 0) {
                showError("Invalid hidden image");
                return;
            }

            Bitmap decodedImage = Bitmap.createBitmap(hiddenWidth, hiddenHeight, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < hiddenWidth; x++) {
                for (int y = 0; y < hiddenHeight; y++) {
                    if (x >= selectedImage.getWidth() || y >= selectedImage.getHeight()) break;

                    int encodedPixel = selectedImage.getPixel(x, y);
                    int decodedRed = (Color.red(encodedPixel) & 0x07) << 5;
                    int decodedGreen = (Color.green(encodedPixel) & 0x03) << 6;
                    int decodedBlue = (Color.blue(encodedPixel) & 0x07) << 5;

                    decodedImage.setPixel(x, y, Color.rgb(decodedRed, decodedGreen, decodedBlue));
                }
            }

            decodedImagePreview.setImageBitmap(decodedImage);
            decodedTextView.setText("Decoded Image");
            showToast("Decoding successful!");
        } catch (Exception e) {
            showError("Decoding failed: " + e.getMessage());
        }
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String error) {
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
    }
}