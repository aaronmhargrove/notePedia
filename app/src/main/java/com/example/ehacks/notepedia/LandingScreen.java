//
// FILE: LandingScreen.java
// INFO: logic for activity_landing_screen.xml - Takes user to search screen when button selected
//

package com.example.ehacks.notepedia;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

// LandingScreen class
public class LandingScreen extends AppCompatActivity {
    private FrameLayout frameLayout;
    private AnimationDrawable animationDrawable;

    // Sets up activity on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up activity / frame
        setContentView(R.layout.activity_landing_screen);
        frameLayout = (FrameLayout) findViewById(R.id.myFrame);

        // Set background color changing animation
        animationDrawable=(AnimationDrawable)frameLayout.getBackground();
        animationDrawable.setEnterFadeDuration(5000);
        animationDrawable.setExitFadeDuration(4000);

        // Start animation
        animationDrawable.start();
    }

    // Listener for the "Begin Learning" button. Starts SearchScreen activity
    public void knowledge(View view) {
        Intent intent = new Intent(this, SearchScreen.class);
        startActivity(intent);
    }
}
