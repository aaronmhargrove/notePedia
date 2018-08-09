//
// FILE: SearchScreen.java
// INFO: logic for activity_search_screen.xml - handles searching wiki for info
//

package com.example.ehacks.notepedia;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.*;

// SearchScreen class
// TODO: separate out business logic and place into its own class, leaving this class as listeners
public class SearchScreen extends AppCompatActivity {
    // Protected members used for unit tests
    protected String wikiUrlUnitTest, smmryApiUnitTest;

    // Set up GUI on create (frame and load random search string)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up frame with xml
        setContentView(R.layout.activity_search_screen);
        String random = null;

        // Get a random wikipedia page
        try {
            random = new getRandom().execute().get();
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

    // Takes user search string and uses it to find information - google -> wiki -> summarize
    public void initiateSearch(View view) throws ExecutionException, InterruptedException, JSONException {
        TextView searchQuarry = findViewById(R.id.searchText);
        String searchString = searchQuarry.getText().toString();

        // If user didn't search minigame -> search for what they enter
        if (!(searchString.toLowerCase().equals("minigame"))){
            // Search Wikipedia
            String[] cardsText = searchWiki(searchString);

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

    public String[] searchWiki (String searchString) throws ExecutionException, InterruptedException, JSONException {
        // Google [user input] + "wikipedia" -> acts as a spell check and gives most relevant page
        String searchData = searchString + " wikipedia";
        searchData = searchData.replaceAll(" ", "_");
        String searchUrl = "https://google.com/search?q=" + searchData;

        // Get proper url of wiki page
        String url = wikiUrlUnitTest = new searchTheWeb().execute(searchUrl).get();

        // Use SMMRY api to get data from wiki page (json)
        String apiUrl = "http://api.smmry.com/&SM_API_KEY=A47BAB4439&SM_LENGTH=40&SM_WITH_BREAK&SM_URL=" + url;
        JSONRequest request = new JSONRequest();
        String result = smmryApiUnitTest = request.execute(apiUrl).get();

        // Create json object with result string
        JSONObject jsonObj = new JSONObject(result);
        String[] cardsText;

        // If there is no error with summarization
        if(!result.contains("sm_api_error")){
            String jsonString = jsonObj.getString("sm_api_content");
            String regex = "[BREAK]";
            cardsText = jsonString.split(Pattern.quote(regex));
        }
        else{
            cardsText = new String[] {"ERROR"};
        }

        return cardsText;
    }


    // Async Class. Random search function
    public class getRandom extends AsyncTask<String, Void, String> {
        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 15000;
        private static final int CONNECTION_TIMEOUT = 15000;
        private String result, rawTitle;

        // Gets random wiki page and returns text to be loaded into search bar
        @Override
        protected String doInBackground(String... params){
            String randomUrl = "https://en.wikipedia.org/wiki/Special:Random";
            String titleApiUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&titles=";

            // Get random title
            try {
                URL url = new URL(randomUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Set method and timeouts
                con.setRequestMethod(REQUEST_METHOD);
                con.setReadTimeout(READ_TIMEOUT);
                con.setConnectTimeout(CONNECTION_TIMEOUT);

                // Get status
                int status = con.getResponseCode();

                // Check for move or redirect and update url
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM) {
                    String location = con.getHeaderField("Location");
                    URL newUrl = new URL(location);
                    con = (HttpURLConnection) newUrl.openConnection();
                }

                // Get name of page
                rawTitle = con.toString();
                int temp = rawTitle.indexOf("wiki/") + 5;
                rawTitle = rawTitle.substring(temp);

                con.disconnect();
            }
            catch(IOException e){
                e.printStackTrace();
                rawTitle = "Sailboat"; // Gives user something instead of empty string
            }

            // Ensure correct title format (use getFormattedTitle)
            try{
                // Get json from wiki api
                String wikiApiJsonString = getFormattedTitle(titleApiUrl + rawTitle);

                // Create json boj with returned string and isolate the "pages" element
                JSONObject allJson = new JSONObject(wikiApiJsonString);
                JSONObject queryJson = allJson.optJSONObject("query");
                JSONObject pagesJson = queryJson.optJSONObject("pages");

                // Get json key (different for every wikipedia page)
                Iterator<?> keys = pagesJson.keys();
                String key = (String) keys.next();

                // Isolate the page id (int) element
                JSONObject pageIdJson = pagesJson.getJSONObject(key);

                // Get correct title
                result = pageIdJson.getString("title");
            }
            catch(JSONException e){
                e.printStackTrace();
                result = "Sailboat"; // Gives user something instead of empty string
            }

            return result;
        }
        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }

        // Returns formatted correctly formatted title from wiki API.
        // Same as JSONRequest because an AsyncTask cannot call another AsyncTask unless it is
        //      ran on the UI thread (takes away benefit of async call) TEMPORARY FOR EHACKS
        // TODO: create method that runs each getRandom and JsonRequest separately to cut out duplicate code
        private String getFormattedTitle(String apiUrl){
            String title;
            String inputLine;
            try {
                // Create a URL object holding our url
                URL myUrl = new URL(apiUrl);

                // Create a connection
                HttpURLConnection connection =(HttpURLConnection)myUrl.openConnection();

                // Set method and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                // Connect to url
                connection.connect();

                // Create a new InputStreamReader
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());

                // Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();

                // Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                // Set our result equal to our stringBuilder
                title = stringBuilder.toString();
            }
            catch(IOException e){
                e.printStackTrace();
                title = null;
            }
            return title;
        }
    }


    // Async Class. Get proper wiki url for search
    public class searchTheWeb extends AsyncTask<String, Void, String> {
        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 15000;
        private static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(String... params){
            String stringUrl = params[0];
            String result = "";
            String inputLine = "";

            try {
                URL url = new URL(stringUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Set method and timeouts
                con.setRequestMethod(REQUEST_METHOD);
                con.setReadTimeout(READ_TIMEOUT);
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                //con.setRequestMethod("GET");

                // Check status
                int status = con.getResponseCode();

                // Check for move or redirect and update url
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM) {
                    String location = con.getHeaderField("Location");
                    URL newUrl = new URL(location);
                    con = (HttpURLConnection) newUrl.openConnection();
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                // Search through google results page to get to wiki page
                while ((inputLine = in.readLine()) != null) {
                    // If first match found -> update result and exit loop
                    if (inputLine.toLowerCase().matches(".*href=\"https://en.wikipedia.org/wiki/.*")) {
                        result = inputLine;
                        int index = result.indexOf("https://en.wikipedia.org/wiki/");
                        result = result.substring(index, index+500);
                        result = result.substring(0,result.indexOf("\""));
                        result = result.replace("https://en.wikipedia.org/wiki/","https://en.m.wikipedia.org/wiki/");
                        break;
                    }
                }

                in.close();
                con.disconnect();
            }
            catch(IOException e){
                e.printStackTrace();
                result = null;
            }

            return result;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }
    }


    // Async Class. Get summarized data (json)
    public class JSONRequest extends AsyncTask<String, Void, String> {
        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 15000;
        private static final int CONNECTION_TIMEOUT = 15000;

        // Read through JSON file
        @Override
        protected String doInBackground(String... params){
            String stringUrl = params[0];
            String result;
            String inputLine;
            try {
                // Create a URL object holding our url
                URL myUrl = new URL(stringUrl);

                // Create a connection
                HttpURLConnection connection =(HttpURLConnection)myUrl.openConnection();

                // Set method and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                // Connect to url
                connection.connect();

                // Create a new InputStreamReader
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());

                // Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();

                // Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                // Set our result equal to our stringBuilder
                result = stringBuilder.toString();
            }
            catch(IOException e){
                e.printStackTrace();
                result = null;
            }
            return result;
        }
        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }
    }
}


