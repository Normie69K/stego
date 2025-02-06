package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
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
        ImageView infoButton = findViewById(R.id.info_icon);

        // Find all TextViews
        TextView text1 = findViewById(R.id.textView);
        TextView text2 = findViewById(R.id.textView2);
        TextView text3 = findViewById(R.id.textView3);

        // Set Click Listeners for First Image and Text (Redirect to EncodeTextToImg)
        img1.setOnClickListener(v -> {
            playClickSound();
            startActivity(new Intent(MainActivity.this, EncodeTextToImg.class));
        });

        text1.setOnClickListener(v -> {
            playClickSound();
            startActivity(new Intent(MainActivity.this, EncodeTextToImg.class));
        });

        // Set Click Listeners for Second Image and Text (Redirect to EncodeActivity2)
        img2.setOnClickListener(v -> {
            playClickSound();
            startActivity(new Intent(MainActivity.this, EncodeActivity.class));
        });

        text2.setOnClickListener(v -> {
            playClickSound();
            startActivity(new Intent(MainActivity.this, EncodeActivity.class));
        });

        // Set Click Listeners for Third Image and Text (Redirect to DecodeActivity)
        img3.setOnClickListener(v -> {
            playClickSound();
            startActivity(new Intent(MainActivity.this, DecodeActivity.class));
        });

        text3.setOnClickListener(v -> {
            playClickSound();
            startActivity(new Intent(MainActivity.this, DecodeActivity.class));
        });

        // Info Button Click - Plays Sound & Opens InfoActivity
        infoButton.setOnClickListener(v -> {
            playClickSound();
            startActivity(new Intent(MainActivity.this, InfoActivity.class));
        });
    }

    private void playClickSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
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
