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

    private ImageView imagePreview;
    private Button uploadImageButton, decryptButton;
    private TextView decodedTextView;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_text);

        imagePreview = findViewById(R.id.imagePreview);
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
            Toast.makeText(this, "No text encoded in image", Toast.LENGTH_SHORT).show();
        }
    }

    private void decodeText() {
        int width = selectedImage.getWidth();
        int height = selectedImage.getHeight();

        // Extract text length from first pixel (bits 1-7 of red channel)
        int firstPixel = selectedImage.getPixel(0, 0);
        int textLength = (Color.red(firstPixel) >> 1);

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
                int redBit = (Color.red(pixel) & 0x01);
                int greenBit = (Color.green(pixel) & 0x01);
                int blueBit = (Color.blue(pixel) & 0x01);

                if (bitPos == 7 || bitPos == 4) {
                    // Process 3 bits for positions 7-5 and 4-2
                    c |= (redBit << (bitPos - 0));
                    c |= (greenBit << (bitPos - 1));
                    c |= (blueBit << (bitPos - 2));
                } else if (bitPos == 1) {
                    // Process last 2 bits (positions 1-0); blue bit ignored
                    c |= (redBit << 1);
                    c |= (greenBit << 0);
                }
            }
            decodedText.append(c);
        }

        decodedTextView.setText("Decoded Text: " + decodedText.toString());
        Toast.makeText(this, "Text decoded!", Toast.LENGTH_SHORT).show();
    }
}
