package com.example.team1ca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScoreboardActivity extends AppCompatActivity {

    public final static int[] textViewIds = {
            R.id.textView1,R.id.textView2,R.id.textView3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        Button playAgainBtn = findViewById(R.id.button);
        playAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(ScoreboardActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        });

        final String gameType = this.getIntent().getStringExtra("gameType");

        if(gameType.equals("dual")) {
            String winner = getIntent().getStringExtra("WINNER");
            TextView textView = findViewById(R.id.textView8);
            if (winner.equals("")) {
                textView.setText("Draw!!!");
            } else {
                textView.setText(winner + " win the game!!!");
            }
            return;
        }

        String score = getIntent().getStringExtra("SCORE");

        // update leader board data
        SharedPreferences settings = getSharedPreferences("HIGH_SCORES", Context.MODE_PRIVATE);
        Set<String> highScores = settings.getStringSet("HIGH_SCORES",new HashSet<String>());
        highScores.add(score);
        List<String> topScores = new ArrayList<>(highScores);
        Collections.sort(topScores);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("HIGH_SCORES",highScores);
        editor.commit();

        TextView resultTextView = findViewById(R.id.textView8);
        resultTextView.setText("Try again to reach top 3, your score (" + score + ") !!!");
        int i = 0;
        for(;i < textViewIds.length && i < topScores.size(); i++) {
            TextView textView = findViewById(textViewIds[i]);
            int pos = i+1;
            textView.setText("RANK " + pos + " " + topScores.get(i));
            textView.setTypeface(null, Typeface.NORMAL);
            if(score.equals(topScores.get(i))) {
                textView.setTypeface(null, Typeface.BOLD_ITALIC);
                if(i == 0) {
                    resultTextView.setText("Great! New Highscore!!");
                } else {
                    resultTextView.setText("Cheers! You ranked top " + pos + " !!");
                }
            }
        }
    }
}
