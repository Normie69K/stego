package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.OutputStream;
import java.io.IOException;

public class EncodeTextToImg extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView, infoButton;
    private EditText editText;
    private Button selectImageButton, encodeButton, downloadButton;
    private TextView psnrTextView, ssimTextView;
    private Bitmap selectedImage, encodedImage;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encode_text_to_img);

        // Initialize UI Components
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editTextText);
        selectImageButton = findViewById(R.id.selectImageButton);
        encodeButton = findViewById(R.id.encodeButton);
        downloadButton = findViewById(R.id.downloadButton);
        psnrTextView = findViewById(R.id.psnrTextView);
        ssimTextView = findViewById(R.id.ssimTextView);
        infoButton = findViewById(R.id.info_icon);

        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);

        infoButton.setOnClickListener(v -> {
            playClickSound();
            startActivity(new Intent(EncodeTextToImg.this, InfoActivity.class));
        });

        selectImageButton.setOnClickListener(v -> {
            playClickSound();
            openGallery();
        });

        encodeButton.setOnClickListener(v -> {
            playClickSound();
            encodeTextIntoImage();
        });

        downloadButton.setOnClickListener(v -> {
            playClickSound();
            saveImageToGallery();
        });
    }

    private void playClickSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
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
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

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

        encodedImage = selectedImage.copy(Bitmap.Config.ARGB_8888, true);
        int width = encodedImage.getWidth();
        int height = encodedImage.getHeight();

        for (int i = 0; i < textToEncode.length() && i < width * height; i++) {
            char c = textToEncode.charAt(i);
            int x = i % width;
            int y = i / width;
            int pixel = encodedImage.getPixel(x, y);
            int newPixel = Color.argb(Color.alpha(pixel), c, Color.green(pixel), Color.blue(pixel));
            encodedImage.setPixel(x, y, newPixel);
        }

        imageView.setImageBitmap(encodedImage);

        double psnr = calculatePSNR(selectedImage, encodedImage);
        double ssim = 1.0; // Placeholder for SSIM calculation

        psnrTextView.setText("PSNR: " + psnr);
        ssimTextView.setText("SSIM: " + ssim);

        Toast.makeText(this, "Encoding complete!", Toast.LENGTH_SHORT).show();
    }

    private double calculatePSNR(Bitmap original, Bitmap encoded) {
        long mse = 0;
        int width = original.getWidth();
        int height = original.getHeight();
        int totalPixels = width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = original.getPixel(x, y);
                int encodedPixel = encoded.getPixel(x, y);
                int diff = Color.red(originalPixel) - Color.red(encodedPixel);
                mse += diff * diff;
            }
        }

        mse /= totalPixels;
        if (mse == 0) return 100;
        return 10 * Math.log10(255 * 255 / (double) mse);
    }

    private void saveImageToGallery() {
        if (encodedImage == null) {
            Toast.makeText(this, "No encoded image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "encoded_image.png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Stego");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                encodedImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
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
