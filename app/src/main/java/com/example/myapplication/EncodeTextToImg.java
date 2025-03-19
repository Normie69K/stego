package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;

public class EncodeTextToImg extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
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

        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);

        // Set Click Listeners
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

        // Encode text length into the first pixel (using LSBs)
        int textLength = textToEncode.length();
        int firstPixel = encodedImage.getPixel(0, 0);
        int newFirstPixel = Color.rgb((Color.red(firstPixel) & 0xFE) | (textLength & 0x01),
                Color.green(firstPixel), Color.blue(firstPixel));
        encodedImage.setPixel(0, 0, newFirstPixel);

        // Encode text into the image (using LSBs)
        for (int i = 0; i < textToEncode.length(); i++) {
            char c = textToEncode.charAt(i);
            int x = (i + 1) % width; // Start from the second pixel
            int y = (i + 1) / width;
            int pixel = encodedImage.getPixel(x, y);

            // Encode the character into the LSBs of the pixel
            int newPixel = Color.rgb((Color.red(pixel) & 0xFE) | ((c >> 7) & 0x01),
                    (Color.green(pixel) & 0xFE) | ((c >> 6) & 0x01),
                    (Color.blue(pixel) & 0xFE) | ((c >> 5) & 0x01));
            encodedImage.setPixel(x, y, newPixel);
        }

        imageView.setImageBitmap(encodedImage);

        // Calculate and display PSNR and SSIM
        double psnr = calculatePSNR(selectedImage, encodedImage);
        double ssim = calculateSSIM(selectedImage, encodedImage);

        psnrTextView.setText("PSNR: " + psnr);
        ssimTextView.setText("SSIM: " + ssim);

        Toast.makeText(this, "Encoding complete!", Toast.LENGTH_SHORT).show();
    }

    private double calculatePSNR(Bitmap original, Bitmap encoded) {
        int width = original.getWidth();
        int height = original.getHeight();
        long mse = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = original.getPixel(x, y);
                int encodedPixel = encoded.getPixel(x, y);

                // Calculate squared difference for each channel (R, G, B)
                int diffRed = Color.red(originalPixel) - Color.red(encodedPixel);
                int diffGreen = Color.green(originalPixel) - Color.green(encodedPixel);
                int diffBlue = Color.blue(originalPixel) - Color.blue(encodedPixel);

                mse += (diffRed * diffRed) + (diffGreen * diffGreen) + (diffBlue * diffBlue);
            }
        }

        // Calculate MSE (Mean Squared Error)
        double mseValue = (double) mse / (width * height * 3); // Divide by 3 for R, G, B channels

        // Avoid division by zero
        if (mseValue == 0) {
            return 100; // Perfect match, PSNR is infinite
        }

        // Calculate PSNR
        double psnr = 10 * Math.log10((255 * 255) / mseValue);
        return psnr;
    }

    private double calculateSSIM(Bitmap original, Bitmap encoded) {
        int width = original.getWidth();
        int height = original.getHeight();

        double C1 = Math.pow(0.01 * 255, 2); // Constants for stability
        double C2 = Math.pow(0.03 * 255, 2);

        double meanOriginal = 0, meanEncoded = 0;
        double varOriginal = 0, varEncoded = 0, covar = 0;

        // Calculate means
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = original.getPixel(x, y);
                int encodedPixel = encoded.getPixel(x, y);

                meanOriginal += Color.red(originalPixel) + Color.green(originalPixel) + Color.blue(originalPixel);
                meanEncoded += Color.red(encodedPixel) + Color.green(encodedPixel) + Color.blue(encodedPixel);
            }
        }
        meanOriginal /= (width * height * 3);
        meanEncoded /= (width * height * 3);

        // Calculate variances and covariance
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = original.getPixel(x, y);
                int encodedPixel = encoded.getPixel(x, y);

                double originalLuminance = (Color.red(originalPixel) + Color.green(originalPixel) + Color.blue(originalPixel)) / 3.0;
                double encodedLuminance = (Color.red(encodedPixel) + Color.green(encodedPixel) + Color.blue(encodedPixel)) / 3.0;

                varOriginal += Math.pow(originalLuminance - meanOriginal, 2);
                varEncoded += Math.pow(encodedLuminance - meanEncoded, 2);
                covar += (originalLuminance - meanOriginal) * (encodedLuminance - meanEncoded);
            }
        }
        varOriginal /= (width * height);
        varEncoded /= (width * height);
        covar /= (width * height);

        // Calculate SSIM
        double numerator = (2 * meanOriginal * meanEncoded + C1) * (2 * covar + C2);
        double denominator = (Math.pow(meanOriginal, 2) + Math.pow(meanEncoded, 2) + C1) * (varOriginal + varEncoded + C2);

        return numerator / denominator;
    }

    private void saveImageToGallery() {
        if (encodedImage == null) {
            Toast.makeText(this, "No encoded image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "encoded_image.png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Stego/");

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