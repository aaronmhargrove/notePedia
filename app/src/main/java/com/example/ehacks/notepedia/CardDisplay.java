//
// FILE: CardDisplay.java
// INFO: logic for activity_card_display.xml - Sets up activity and handles "next" and "prev" taps
//

package com.example.ehacks.notepedia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// CardDisplay class
public class CardDisplay extends AppCompatActivity {
    protected int count = 0;
    protected TextView textView;
    protected TextView title;
    protected TextView countDisplay;
    protected int len = 0;
    protected String[] info;
    protected Button prev, next;

    // Called on creation. Sets up activity with search information
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set display (using xml file)
        setContentView(R.layout.activity_card_display);

        // Get extras from SearchScreen (user search string and results array)
        Bundle extras = getIntent().getExtras();
        String[] cardsText = extras.getStringArray("cardsText");
        String searchQuarry = extras.getString("searchQuarry");

        // Find views with ID and assign them to variables
        textView = (TextView) findViewById(R.id.displayCardValue);
        title = (TextView) findViewById(R.id.title);
        countDisplay = (TextView) findViewById(R.id.count_display);
        prev = (Button) findViewById(R.id.previousNote);
        next = (Button) findViewById(R.id.nextNote);

        // Assign passed values to class variables
        len = cardsText.length;
        info = cardsText;
        String cd = (count + 1) + "/" + len;

        // Display cards if no error
        if(!cardsText[0].equals("ERROR")){
            textView.setText(cardsText[count]);
            prev.setEnabled(true);
            next.setEnabled(true);
        }
        // Display error
        else{
            textView.setText("Sorry, no results!");
            prev.setEnabled(false);
            next.setEnabled(false);

        }
        // Display searched string and number of results
        title.setText(searchQuarry);
        countDisplay.setText(cd);
    }

    // Called when user selects the "next" button on activity_card_display
    // Displays next card in the array.
    public void viewNextCard(View view){
        changeCard(1);

        // Set text of info TextView
        textView.setText(info[count]);

        // Set current card number text
        String cd = (count + 1) + "/" + info.length;
        countDisplay.setText(cd);
    }

    // Called when user selects the "previous" button on activity_card_display
    // Displays previous card in the array.
    public void viewPreviousCard(View view){
        changeCard(-1);

        // Set text of info TextView
        textView.setText(info[count]);

        // Set current card number text
        String cd = (count + 1) + "/" + info.length;
        countDisplay.setText(cd);
    }

    // Changes card, handles boundary carousel. Receives int (positive or negative direction)
    public void changeCard(int direction){
        count += direction;

        if (direction == 1){
            // If at the end of the array, go to the beginning
            if (count > len - 1)
                count = 0;
        }
        else{
            // If at the beginning of the array, go to the end (length - 1)
            if (count < 0)
                count = len - 1;
        }
    }
}
