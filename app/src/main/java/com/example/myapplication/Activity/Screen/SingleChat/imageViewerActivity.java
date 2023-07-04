package com.example.myapplication.Activity.Screen.SingleChat;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

public class imageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.image_viewer);
        imageUrl = getIntent().getStringExtra("url");

        Picasso.get().load(imageUrl).into(imageView);

    }
}
