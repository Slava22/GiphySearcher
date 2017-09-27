package com.example.andrushk.giphysearcher;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.felipecsl.gifimageview.library.GifImageView;

public class GifActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif);

        Intent intent = getIntent();
        int width = intent.getIntExtra("width", 1);
        int height = intent.getIntExtra("height", 1);
        int screenWidth = intent.getIntExtra("screenWidth", 1);
        final String url = intent.getStringExtra("url");

        GifImageView gifImageView = (GifImageView) findViewById(R.id.gifImageView);
        TextView textView = (TextView) findViewById(R.id.urlTextView);
        Button btnCopyUrl = (Button) findViewById(R.id.copyUrl);

        Glide.with(this).load(intent.getStringExtra("url")).into(gifImageView);
        textView.setText(url);

        double koef = (double) width / screenWidth;
        gifImageView.getLayoutParams().height = (int) (height / koef);

        btnCopyUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "URL copied", Toast.LENGTH_LONG).show();
            }
        });
    }
}