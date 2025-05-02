package com.mellah.mediassist;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class FullScreenImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        ImageView iv = findViewById(R.id.fullscreenImageView);
        String uriStr = getIntent().getStringExtra("imageUri");
        if (uriStr != null) {
            Uri uri = Uri.parse(uriStr);
            Glide.with(this)
                    .load(uri)
                    .into(iv);
        }

        // tap to exit
        iv.setOnClickListener(v -> finish());
    }
}
