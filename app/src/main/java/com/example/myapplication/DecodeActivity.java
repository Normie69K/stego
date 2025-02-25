package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import java.io.File;
import java.io.FileOutputStream;
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

        // Upload Image Button Click Listener
        uploadImageButton.setOnClickListener(v -> openGallery());

        // Decrypt Button Click Listener
        decryptButton.setOnClickListener(v -> decodeImageFromStego());

        // Download Decoded Image Button Click Listener
        downloadDecodedImageButton.setOnClickListener(v -> {
            if (decodedImage != null) {
                saveDecodedImage(decodedImage);
            } else {
                Toast.makeText(this, "No decoded image to save", Toast.LENGTH_SHORT).show();
            }
        });

        // Info Button Click Listener with Sound Effect
        findViewById(R.id.info_icon).setOnClickListener(v -> {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);
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
                imagePreview.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Convert Bitmap to File and Get Path
    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "selected_image.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Call Python Script to Decode Image
    private void decodeImageFromStego() {
        if (selectedImage == null) {
            Toast.makeText(this, "Please upload an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        String imagePath = saveBitmapToFile(selectedImage);
        if (imagePath == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Python py = Python.getInstance();
            PyObject pyModule = py.getModule("steganography");  // Ensure script name is correct
            PyObject result = pyModule.callAttr("decode_image", imagePath);

            if (result != null) {
                decodedTextView.setText("Decoded Image Path: " + result.toString());
                decodedImage = BitmapFactory.decodeFile(result.toString());
                decodedImagePreview.setImageBitmap(decodedImage);
                Toast.makeText(this, "Decoding successful!", Toast.LENGTH_SHORT).show();
            } else {
                decodedTextView.setText("No hidden image found.");
                Toast.makeText(this, "No hidden image found.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            decodedTextView.setText("Error during decoding: " + e.getMessage());
            Toast.makeText(this, "Decoding failed!", Toast.LENGTH_LONG).show();
        }
    }

    // Save Decoded Image as "stego_decode.png"
    private void saveDecodedImage(Bitmap bitmap) {
        try {
            File file = new File(getExternalFilesDir(null), "stego_decode.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "Image saved at: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}
