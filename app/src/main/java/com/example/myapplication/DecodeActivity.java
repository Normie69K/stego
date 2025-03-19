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

public class DecodeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imagePreview, decodedImagePreview;
    private Button uploadImageButton, decryptButton, downloadDecodedImageButton;
    private TextView decodedTextView;
    private Bitmap selectedImage, decodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        // Initialize UI Components
        imagePreview = findViewById(R.id.imagePreview);
        decodedImagePreview = findViewById(R.id.decodedImagePreview);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        decryptButton = findViewById(R.id.decryptButton);
        downloadDecodedImageButton = findViewById(R.id.downloadDecodedImageButton);
        decodedTextView = findViewById(R.id.decodedTextView);

        // Set Click Listeners
        uploadImageButton.setOnClickListener(v -> openGallery());
        decryptButton.setOnClickListener(v -> decodeImage());
        downloadDecodedImageButton.setOnClickListener(v -> saveDecodedImage());
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
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imagePreview.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void decodeImage() {
        if (selectedImage == null) {
            Toast.makeText(this, "Please upload an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        int width = selectedImage.getWidth();
        int height = selectedImage.getHeight();

        // Check if the image contains the encoding flag (LSB of the red channel in the first pixel)
        int firstPixel = selectedImage.getPixel(0, 0);
        boolean isEncoded = (Color.red(firstPixel) & 0x01) == 1; // Check the LSB of the red channel

        if (!isEncoded) {
            // If the image is not encoded, show a message and return
            decodedTextView.setText("Nothing to decode: This image was not encoded using this app.");
            decodedImagePreview.setVisibility(ImageView.GONE);
            Toast.makeText(this, "Nothing to decode", Toast.LENGTH_SHORT).show();
            return;
        }

        // Decode the hidden image
        decodedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = selectedImage.getPixel(x, y);

                // Extract the hidden image from the LSBs
                int decodedRed = (Color.red(pixel) & 0x07) << 5;
                int decodedGreen = (Color.green(pixel) & 0x03) << 6;
                int decodedBlue = (Color.blue(pixel) & 0x07) << 5;

                int decodedPixel = Color.rgb(decodedRed, decodedGreen, decodedBlue);
                decodedImage.setPixel(x, y, decodedPixel);
            }
        }

        decodedImagePreview.setImageBitmap(decodedImage);
        decodedImagePreview.setVisibility(ImageView.VISIBLE);
        decodedTextView.setText("Decoded Image");
        Toast.makeText(this, "Image decoding complete!", Toast.LENGTH_SHORT).show();
    }

    private void saveDecodedImage() {
        if (decodedImage == null) {
            Toast.makeText(this, "No decoded image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the decoded image to the gallery
        String fileName = "decoded_image.png";
        saveImageToGallery(decodedImage, fileName);
        Toast.makeText(this, "Decoded image saved to gallery", Toast.LENGTH_SHORT).show();
    }

    private void saveImageToGallery(Bitmap bitmap, String fileName) {
        // Implement this method to save the bitmap to the gallery
    }
}