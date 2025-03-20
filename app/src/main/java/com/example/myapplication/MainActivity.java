package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize MediaPlayer for click sound
        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);

        // Find all UI elements
        ImageView img1 = findViewById(R.id.imageView);
        ImageView img2 = findViewById(R.id.imageView2);
        ImageView img3 = findViewById(R.id.imageView3);
        ImageView img4 = findViewById(R.id.imageView4);
        ImageView infoButton = findViewById(R.id.info_icon);

        TextView text1 = findViewById(R.id.textView);
        TextView text2 = findViewById(R.id.textView2);
        TextView text3 = findViewById(R.id.textView3);
        TextView text4 = findViewById(R.id.textView4);

        // Unified click listener for all interactive elements
        View.OnClickListener navigateWithEffects = v -> {
            playClickSound();
            Log.d("MainActivity", "Navigation triggered by: " + v.getId());

            Class<?> targetActivity = null;

            if (v == img1 || v == text1) {
                targetActivity = EncodeTextToImg.class;
            } else if (v == img2 || v == text2) {
                targetActivity = EncodeActivity.class;
            } else if (v == img3 || v == text3) {
                targetActivity = Decode_img_Activity.class;
            } else if (v == img4 || v == text4) {
                targetActivity = Decode_text_activity.class;
            } else if (v == infoButton) {
                targetActivity = InfoActivity.class;
            }

            if (targetActivity != null) {
                startActivity(new Intent(MainActivity.this, targetActivity));
            }
        };

        // Set click listeners for all elements
        img1.setOnClickListener(navigateWithEffects);
        text1.setOnClickListener(navigateWithEffects);
        img2.setOnClickListener(navigateWithEffects);
        text2.setOnClickListener(navigateWithEffects);
        img3.setOnClickListener(navigateWithEffects);
        text3.setOnClickListener(navigateWithEffects);
        img4.setOnClickListener(navigateWithEffects);
        text4.setOnClickListener(navigateWithEffects);
        infoButton.setOnClickListener(navigateWithEffects);

        // Exit confirmation setup
        findViewById(R.id.main).setOnClickListener(v -> showExitDialog());
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

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
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