package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize MediaPlayer for click sound
        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);

        // Check and request storage permissions
//        checkAndRequestPermissions();

        // Find ImageViews and TextViews
        ImageView img1 = findViewById(R.id.imageView);
        ImageView img2 = findViewById(R.id.imageView2);
        ImageView img3 = findViewById(R.id.imageView3);
        ImageView infoButton = findViewById(R.id.info_icon);

        TextView text1 = findViewById(R.id.textView);
        TextView text2 = findViewById(R.id.textView2);
        TextView text3 = findViewById(R.id.textView3);

        // Set Click Listeners for navigation with animations and logs
        View.OnClickListener navigateWithEffects = v -> {
            playClickSound();
            Log.d("MainActivity", "Navigating to " + v.getId());

            if (v == img1 || v == text1) {
                startActivity(new Intent(MainActivity.this, EncodeTextToImg.class));
            } else if (v == img2 || v == text2) {
                startActivity(new Intent(MainActivity.this, EncodeActivity.class));
            } else if (v == img3 || v == text3) {
                startActivity(new Intent(MainActivity.this, DecodeActivity.class));
            } else if (v == infoButton) {
                startActivity(new Intent(MainActivity.this, InfoActivity.class));
            }
        };

        img1.setOnClickListener(navigateWithEffects);
        text1.setOnClickListener(navigateWithEffects);
        img2.setOnClickListener(navigateWithEffects);
        text2.setOnClickListener(navigateWithEffects);
        img3.setOnClickListener(navigateWithEffects);
        text3.setOnClickListener(navigateWithEffects);
        infoButton.setOnClickListener(navigateWithEffects);

        // Exit Confirmation Dialog
        findViewById(R.id.main).setOnClickListener(v -> showExitDialog());
    }

//    private void checkAndRequestPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            // Request permissions
//            ActivityCompat.requestPermissions(this,
//                    new String[]{
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    },
//                    REQUEST_STORAGE_PERMISSION);
//        } else {
//            // Permissions already granted
//            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_STORAGE_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permissions granted
//                Toast.makeText(this, "Storage permissions granted", Toast.LENGTH_SHORT).show();
//            } else {
//                // Permissions denied
//                Toast.makeText(this, "Storage permissions are required to use this app", Toast.LENGTH_LONG).show();
//                showPermissionDeniedDialog();
//            }
//        }
//    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("This app needs storage permissions to function properly. Please grant the permissions in the app settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> openAppSettings())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void playClickSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
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