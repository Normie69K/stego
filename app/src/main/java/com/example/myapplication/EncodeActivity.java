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
    private TextView psnrTextView, ssimTextView;
    private Bitmap baseImage, hiddenImage, encodedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        baseImageView = findViewById(R.id.baseImagePreview);
        hiddenImageView = findViewById(R.id.hiddenImagePreview);
        encodedImageView = findViewById(R.id.encodedImagePreview);
        selectBaseImageButton = findViewById(R.id.selectBaseImageButton);
        selectHiddenImageButton = findViewById(R.id.selectHiddenImageButton);
        encodeButton = findViewById(R.id.encodeButton);
        downloadButton = findViewById(R.id.downloadButton);
        psnrTextView = findViewById(R.id.psnrTextView);
        ssimTextView = findViewById(R.id.ssimTextView);

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

        int width = baseImage.getWidth();
        int height = baseImage.getHeight();

        if (hiddenImage.getWidth() > width || hiddenImage.getHeight() > height) {
            Toast.makeText(this, "Hidden image must be smaller than base image", Toast.LENGTH_SHORT).show();
            return;
        }

        encodedBitmap = baseImage.copy(baseImage.getConfig(), true);

        for (int x = 0; x < hiddenImage.getWidth(); x++) {
            for (int y = 0; y < hiddenImage.getHeight(); y++) {
                int basePixel = baseImage.getPixel(x, y);
                int hiddenPixel = hiddenImage.getPixel(x, y);

                int newRed = (Color.red(basePixel) & 0xF8) | (Color.red(hiddenPixel) >> 5);
                int newGreen = (Color.green(basePixel) & 0xFC) | (Color.green(hiddenPixel) >> 6);
                int newBlue = (Color.blue(basePixel) & 0xF8) | (Color.blue(hiddenPixel) >> 5);

                int newPixel = Color.rgb(newRed, newGreen, newBlue);
                encodedBitmap.setPixel(x, y, newPixel);
            }
        }

        encodedImageView.setImageBitmap(encodedBitmap);
        encodedImageView.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Encoding successful!", Toast.LENGTH_SHORT).show();
    }

    private void saveEncodedImage() {
        if (encodedBitmap == null) {
            Toast.makeText(this, "No encoded image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "encoded_image.png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Stego/");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                encodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Toast.makeText(this, "Image saved to Pictures/Stego/", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
