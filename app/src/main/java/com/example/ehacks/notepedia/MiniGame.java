//
// FILE: MiniGame.java
// INFO: logic for activity_mini_game.xml - small mini game - first to 100 wins
//

package com.example.ehacks.notepedia;

import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

// MiniGame class
public class MiniGame extends AppCompatActivity {
    protected static int scoreOne, scoreTwo;
    private TextView win1, win2;

    // Set up activity on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Zero Scores
        scoreOne = scoreTwo = 0;

        // Set up frame layout
        setContentView(R.layout.activity_mini_game);

        // Get views with id
        Button butt2 = findViewById(R.id.playerTwo);
        TextView txt1 = findViewById(R.id.editText);
        TextView txt2 = findViewById(R.id.editText2);
        win1 = findViewById(R.id.winner1);
        win2 = findViewById(R.id.winner2);


        // Set focusable / non-focusable
        txt1.setFocusable(false);
        txt2.setFocusable(false);
        win1.setFocusable(false);
        win2.setFocusable(false);
        butt2.setFocusable(true);
        butt2.requestFocus();

        // Display toast to turn on bluetooth clicker for mini game (given out at eHacks -> integrated into project)
        Toast.makeText(getApplicationContext(),"Please turn on your clicker if you have one!",Toast.LENGTH_LONG).show();
    }

    // Listener for bluetooth clicker
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Volume up == player one
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Button button = findViewById(R.id.playerOne);
            button.performClick();
            return true;
        }
        // Enter == player 2
        else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            Button button = findViewById(R.id.playerTwo);
            button.performClick();
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    // Add one to player one score and update GUI
    public void playerOne(View view) {
        //View v2 = new View(MiniGame.class);
        // Only allow the game to continue of nobody has won yet
        if(scoreOne < 100 && scoreTwo < 100){
            scoreOne++;
            TextView newtext = findViewById(R.id.editText2); newtext.setText("Score: "+scoreOne);

            // If player 1 reaches 100 - Display winner
            if (scoreOne >= 100)
                win1.setVisibility(View.VISIBLE);
        }
    }
    // Add one to player two score and update GUI
    public void playerTwo(View view) {
        // Only allow the game to continue of nobody has won yet
        if(scoreOne < 100 && scoreTwo < 100){
            scoreTwo++;
            TextView newtext = findViewById(R.id.editText); newtext.setText("Score: "+scoreTwo);

            // If player 2 reaches 100 - Display winner
            if (scoreTwo >= 100)
                win2.setVisibility(View.VISIBLE);
        }
    }
}