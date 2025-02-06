package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize MediaPlayer for click sound
        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound);

        // Find all ImageViews
        ImageView img1 = findViewById(R.id.imageView);
        ImageView img2 = findViewById(R.id.imageView2);
        ImageView img3 = findViewById(R.id.imageView3);
        ImageView infoButton = findViewById(R.id.info_icon); // Toolbar info button

        // Find all TextViews
        TextView text1 = findViewById(R.id.textView);
        TextView text2 = findViewById(R.id.textView2);
        TextView text3 = findViewById(R.id.textView3);

        // Set Click Listeners for Images
        img1.setOnClickListener(v -> playClickSound());
        img2.setOnClickListener(v -> playClickSound());
        img3.setOnClickListener(v -> playClickSound());

        // Set Click Listeners for Texts
        text1.setOnClickListener(v -> playClickSound());
        text2.setOnClickListener(v -> playClickSound());
        text3.setOnClickListener(v -> playClickSound());

        // Info Button Click - Plays Sound & Opens InfoActivity
        infoButton.setOnClickListener(v -> {
            playClickSound();
            startActivity(new Intent(MainActivity.this, InfoActivity.class));
        });
    }

    private void playClickSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start(); // Play sound on click
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
