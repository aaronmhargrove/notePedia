//
// FILE: SearchScreen.java
// INFO: Listener for activity_search_screen.xml - uses SearchLogic.java for searching
//

package com.example.ehacks.notepedia;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.concurrent.ExecutionException;

// SearchScreen class
public class SearchScreen extends AppCompatActivity {
    SearchLogic logic = new SearchLogic();

    // Set up GUI on create (frame and load random search string)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up frame with xml
        setContentView(R.layout.activity_search_screen);
        String random = null;

        // Get a random wikipedia page
        try {
            random = logic.getRandomSearchTitle();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Get title of random wiki page
        if (random.contains("("))
            random = random.substring(0,random.indexOf("(")-1);

        // Set search bar text to random wiki page
        TextView displayRandom = findViewById(R.id.searchText);
        displayRandom.setText(random);
    }

    // Listener for "Search" button.
    // Takes user search from UI and passes it to SearchLogic class to handle business logic.
    // Starts the CardDisplay activity when it gets returned data from SearchLogic.
    public void initiateSearch(View view) throws ExecutionException, InterruptedException {
        TextView searchQuarry = findViewById(R.id.searchText);
        String searchString = searchQuarry.getText().toString();

        // If user didn't search minigame -> search for what they enter
        if (!(searchString.toLowerCase().equals("minigame"))){
            // Search Wikipedia
            String[] cardsText = logic.searchWiki(searchString);

            // Prepare CardDisplay intent and add extras (search string and cards array)
            Intent intent = new Intent(this, CardDisplay.class);
            intent.putExtra("searchQuarry", searchQuarry.getText().toString());
            intent.putExtra("cardsText", cardsText);

            startActivity(intent);
        }
        // If the user searches 'minigame' take them to the "easter egg" mini game
        else{
            Intent intent2 = new Intent(this, MiniGame.class);
            startActivity(intent2);
        }
    }
}