package com.example.team1ca;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    public final static int[] imageViewId = {
            R.id.imageView1,R.id.imageView2,R.id.imageView3,R.id.imageView4,
            R.id.imageView5,R.id.imageView6,R.id.imageView7,R.id.imageView8,
            R.id.imageView9,R.id.imageView10,R.id.imageView11,R.id.imageView12
    };

    LottieAnimationView animationView;


    private Drawable defaultDrawable;
    private Drawable[] drawables = new Drawable[6];
    private int currentOpenImagePos = -1;
    private int[] imageDataId = {0,0,1,1,2,2,3,3,4,4,5,5};

    private int pOneScore = 0;
    private boolean pOnePlaying = true;
    private int pTwoScore = 0;


    //animation
    public void Animation(){

        Toast.makeText(this,"Bingo!!!",Toast.LENGTH_SHORT).show();

        new CountDownTimer(2000,2000){
            @Override
            public void onTick(long millisUntilFinished){
                // sleep for 0.3 seconds for demo
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                animationView.setVisibility(View.VISIBLE);
                animationView.playAnimation();

            }
            @Override
            public void onFinish(){
                animationView.setVisibility(View.GONE);
            }

        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        defaultDrawable = getResources().getDrawable(R.drawable.question_mark);
        //animation
        animationView = findViewById(R.id.animation_item);
        animationView.setVisibility(View.GONE);
        animationView.pauseAnimation();



        final String gameType = this.getIntent().getStringExtra("gameType");

        final String[] imageNames = this.getIntent().getStringArrayExtra("imageNames");

        // get image data from intent
        for (int i = 0; i < imageNames.length; i++) {
            byte[] bytes = this.getIntent().getByteArrayExtra(imageNames[i]);
            drawables[i] = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }

        RandomizeArray(imageDataId);

        // initialize image with empty image
        for (int i = 0; i < imageViewId.length; i++) {
            ImageView img = findViewById(imageViewId[i]);
            img.setImageDrawable(defaultDrawable);
            img.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        // add listener for each image
        for (int i = 0; i < imageViewId.length; i++) {
            final ImageView imgView = findViewById(imageViewId[i]);
            final int pos = i;
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickImage(imgView, pos);
                }
            });
        }

        Chronometer c = findViewById(R.id.chronometer);
        EditText editText = findViewById(R.id.matchText);
        TextView textView = findViewById(R.id.textView9);

        if(gameType.equals("solo")) {
            c.setText("00:00:00");

            c.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                public void onChronometerTick(Chronometer cArg) {
                    long t = SystemClock.elapsedRealtime() - cArg.getBase();
                    cArg.setText(DateFormat.format("kk:mm:ss", t));
                }
            });

            editText.setText("0/6 matches");

            textView.setVisibility(View.GONE);
        } else {
            c.setVisibility(View.GONE);
            editText.setText("P1: 0 P2: 0");
            textView.setTypeface(null, Typeface.BOLD);
        }
    }

    public void clickImage(final ImageView imgView, final int pos) {
        EditText editText = findViewById(R.id.matchText);

        Chronometer c = findViewById(R.id.chronometer);

        final String gameType = this.getIntent().getStringExtra("gameType");

        // start stopwatch
        if(!c.isActivated()) {
            c.setActivated(true);
            c.start();
        }

        // open the image temporarily for matching
        imgView.setImageDrawable(drawables[imageDataId[pos]]);

        if(currentOpenImagePos != -1){
            final ImageView openImgView = findViewById(imageViewId[currentOpenImagePos]);

            if(imageDataId[pos] == imageDataId[currentOpenImagePos]) {
                imgView.setClickable(false);

                if(pOnePlaying) {
                    pOneScore++;
                } else {
                    pTwoScore++;
                }

                //Animation

                Toast.makeText(this,"Match",Toast.LENGTH_SHORT).show();
                Animation();

                // when paired, update match text
                String matchText;
                if(gameType.equals("solo")) {
                    matchText = new StringBuilder()
                            .append(pOneScore+pTwoScore)
                            .append("/6 matches")
                            .toString();
                }else{
                    matchText = new StringBuilder()
                            .append("P1: ")
                            .append(pOneScore)
                            .append(" P2: ")
                            .append(pTwoScore)
                            .toString();
                }

                editText.setText(matchText);

                // if 6 matches, win
                if(pOneScore+pTwoScore == 6) {

                    // stop watch
                    if(c.isActivated()) {
                        c.setActivated(false);
                        c.stop();
                    }

                    // go to leader board
                    Intent intent = new Intent(GameActivity.this, ScoreboardActivity.class);
                    intent.putExtra("gameType", gameType);
                    if(gameType.equals("solo")) {
                        intent.putExtra("SCORE", c.getText());
                        startActivity(intent);
                        return;
                    } else {
                        String winner = "";
                        if(pOneScore > pTwoScore) {
                            winner = "P1";
                        } else if(pOneScore < pTwoScore){
                            winner = "P2";
                        };

                        intent.putExtra("WINNER", winner);
                        startActivity(intent);
                        return;
                    }
                }

                currentOpenImagePos = -1;
                return;
            } else {
                pOnePlaying = !pOnePlaying;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // sleep for 1 seconds before closing image
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    return;
                                }

                                // if not matching, close both the previous opened image
                                // as well as the newly opened image
                                // and make them clickable again
                                imgView.setImageDrawable(defaultDrawable);
                                imgView.setClickable(true);
                                openImgView.setImageDrawable(defaultDrawable);
                                openImgView.setClickable(true);

                                if(gameType.equals("dual")) {
                                    TextView textView = findViewById(R.id.textView9);
                                    if (pOnePlaying) {
                                        textView.setText("P1's turn!");
                                    } else {
                                        textView.setText("P2's turn!");
                                    }
                                }
                            }
                        });
                    }
                }).start();

                currentOpenImagePos = -1;

                return;
            }
        }

        // if no image open for matching
        // keep the current image open and non-clickable
        if(currentOpenImagePos == -1) {
            currentOpenImagePos = pos;
            imgView.setClickable(false);
        }
    }

    // create randomized array
    private int[] RandomizeArray(int[] array){
        Random rgen = new Random();

        for (int i=0; i<array.length; i++) {
            int randomPosition = rgen.nextInt(array.length);
            int temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }
        return array;
    }
}