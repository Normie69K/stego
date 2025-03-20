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

public class Decode_text_activity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imagePreview, decodedImagePreview;
    private Button uploadImageButton, decryptButton;
    private TextView decodedTextView;
    private Bitmap selectedImage, decodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        imagePreview = findViewById(R.id.imagePreview);
        decodedImagePreview = findViewById(R.id.decodedImagePreview);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        decryptButton = findViewById(R.id.decryptButton);
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

        int firstPixel = selectedImage.getPixel(0, 0);
        boolean isTextEncoded = (Color.red(firstPixel) & 0x01) == 1;

        if (isTextEncoded) {
            decodeText();
        } else {
            decodeHiddenImage();
        }
    }

    private void decodeText() {
        int width = selectedImage.getWidth();
        int height = selectedImage.getHeight();

        // Extract text length from first pixel (bits 1-7 of red channel)
        int textLength = (Color.red(selectedImage.getPixel(0, 0)) >> 1);

        if (textLength == 0) {
            decodedTextView.setText("No text found");
            return;
        }

        StringBuilder decodedText = new StringBuilder();
        for (int i = 0; i < textLength; i++) {
            char c = 0;
            for (int bitPos = 7; bitPos >= 0; bitPos -= 3) {
                int pixelIndex = i * 3 + (7 - bitPos) / 3 + 1;
                int x = pixelIndex % width;
                int y = pixelIndex / width;
                if (x >= width || y >= height) break;

                int pixel = selectedImage.getPixel(x, y);
                c |= ((Color.red(pixel) & 0x01) << (bitPos - 0));
                c |= ((Color.green(pixel) & 0x01) << (bitPos - 1));
                c |= ((Color.blue(pixel) & 0x01) << (bitPos - 2));
            }
            decodedText.append(c);
        }

        decodedTextView.setText("Decoded Text: " + decodedText.toString());
        decodedImagePreview.setVisibility(ImageView.GONE);
        Toast.makeText(this, "Text decoded!", Toast.LENGTH_SHORT).show();
    }

    private void decodeHiddenImage() {
        int width = selectedImage.getWidth();
        int height = selectedImage.getHeight();

        decodedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = selectedImage.getPixel(x, y);
                int decodedRed = (Color.red(pixel) & 0x07) << 5;
                int decodedGreen = (Color.green(pixel) & 0x03) << 6;
                int decodedBlue = (Color.blue(pixel) & 0x07) << 5;
                decodedImage.setPixel(x, y, Color.rgb(decodedRed, decodedGreen, decodedBlue));
            }
        }

        decodedImagePreview.setImageBitmap(decodedImage);
        decodedImagePreview.setVisibility(ImageView.VISIBLE);
        decodedTextView.setText("Decoded Image");
        Toast.makeText(this, "Image decoded!", Toast.LENGTH_SHORT).show();
    }
}