package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private boolean isDarkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Initialize MediaPlayer for click sound
        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);

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
