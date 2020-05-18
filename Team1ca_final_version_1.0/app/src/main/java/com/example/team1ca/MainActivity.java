package com.example.team1ca;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DisplayImageAsyncTask.ICallback {
    int pos = 0;

    public final static int[] imageViewId = {
            R.id.imageView1,R.id.imageView2,R.id.imageView3,R.id.imageView4,
            R.id.imageView5,R.id.imageView6,R.id.imageView7,R.id.imageView8,
            R.id.imageView9,R.id.imageView10,R.id.imageView11,R.id.imageView12,
            R.id.imageView13,R.id.imageView14,R.id.imageView15,R.id.imageView16,
            R.id.imageView17,R.id.imageView18,R.id.imageView19,R.id.imageView20
    };

    public final Map<Integer, byte[]> selectedImageMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        startService(new Intent(MainActivity.this, SoundService.class));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = new Intent(MainActivity.this, GameActivity.class);

        final Button button = findViewById(R.id.fetchBtn);
        final EditText editText = findViewById(R.id.urlInput);

        if(button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                DisplayImageAsyncTask displayImageAsyncTask;

                @Override
                public void onClick(View v) {
                    // cancel previous task
                    if(displayImageAsyncTask != null && !displayImageAsyncTask.isCancelled()) {
                        displayImageAsyncTask.cancel(true);
                    }

                    // hide keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    String[] urls = {editText.getText().toString()};

                    // start new task
                    pos = 0;
                    displayImageAsyncTask = new DisplayImageAsyncTask(MainActivity.this);
                    displayImageAsyncTask.execute(urls);
                }
            });
        }

        final Button soloButton = findViewById(R.id.button2);
        soloButton.setClickable(false);
        soloButton.setVisibility(View.GONE);
        soloButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                intent.putExtra("gameType", "solo");
                startActivity(intent);
            }
        });

        final Button dualButton = findViewById(R.id.button3);
        dualButton.setClickable(false);
        dualButton.setVisibility(View.GONE);
        dualButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                intent.putExtra("gameType", "dual");
                startActivity(intent);
            }
        });

        for (int i : imageViewId) {
            final ImageView imgView = findViewById(i);
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // toggle selection
                    if(imgView.isSelected()) {
                        imgView.setSelected(false);
                        imgView.setColorFilter(Color.argb(0, 0, 0, 0));
                        selectedImageMap.remove(imgView.getId());
                    } else {
                        imgView.setSelected(true);
                        imgView.setColorFilter(Color.argb(80, 0, 128, 255));
                        selectedImageMap.put(imgView.getId(), getBytesFromImageView(imgView));
                    }

                    // if 6 selected, go to next activity
                    if(selectedImageMap.size() == 6) {
                        // add image data to intent
                        String[] imageNames = new String[6];
                        int i = 0;
                        for(Map.Entry<Integer, byte[]> entry : selectedImageMap.entrySet()) {
                            String extraName = new StringBuilder()
                                    .append("image_")
                                    .append(entry.getKey())
                                    .toString();
                            imageNames[i] = extraName;
                            intent.putExtra(extraName, entry.getValue());
                            i++;
                        }
                        intent.putExtra("imageNames", imageNames);
                        dualButton.setClickable(true);
                        dualButton.setVisibility(View.VISIBLE);
                        soloButton.setClickable(true);
                        soloButton.setVisibility(View.VISIBLE);
                    } else {
                        dualButton.setClickable(false);
                        dualButton.setVisibility(View.GONE);
                        soloButton.setClickable(false);
                        soloButton.setVisibility(View.GONE);
                    }
                }
            });
        }
    }


    protected void onDestroy(){
        stopService(new Intent(MainActivity.this, SoundService.class));
        super.onDestroy();
    }

    @Override
    public void onCompleted(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // clear existing picture
                if(pos == 0) {
                    for(int i = 0; i < imageViewId.length; i++) {
                        ImageView img = findViewById(imageViewId[i]);
                        img.setImageResource(android.R.color.transparent);
                    }
                }

                // update progress bar
                ProgressBar progressBar = findViewById(R.id.progressBar);
                TextView progressLabel = findViewById(R.id.progressLabel);

                int curProgress = (pos+1) * 100 / imageViewId.length;

                String label = new StringBuilder()
                        .append("Downloading ")
                        .append(String.valueOf(pos + 1))
                        .append(" of ")
                        .append(String.valueOf(imageViewId.length))
                        .append(" images...")
                        .toString();

                progressBar.setProgress(curProgress);
                progressLabel.setText(label);

                progressBar.setVisibility(View.VISIBLE);
                progressLabel.setVisibility(View.VISIBLE);

                // show picture
                ImageView img = findViewById(imageViewId[pos]);
                img.setImageBitmap(bitmap);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                img.setClickable(true);

                pos++;

                // at the end, close progress bae
                if (curProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                    progressLabel.setVisibility(View.GONE);
                    return;
                }
            }
        });
    }

    public byte[] getBytesFromImageView(ImageView imgView) {
        Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

}