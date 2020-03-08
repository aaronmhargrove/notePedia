//
// FILE: SearchLogic.java
// INFO: Business logic for the SearchScreen class. Handles all search related
//

package com.example.ehacks.notepedia;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class SearchLogic {
    // Class ctor
    public SearchLogic(){}


    // Searches for wikipedia page of user's search  - google -> wiki -> summarize.
    // Takes a string, which is the user's search. Returns a string array, which is the result of their search
    public String[] searchWiki (String searchString) throws ExecutionException, InterruptedException {
        // Google [user input] + "wikipedia" -> acts as a spell check and gives most relevant page
        String searchData = searchString + " wikipedia";
        searchData = searchData.replaceAll(" ", "+");
        String searchUrl = "https://www.googleapis.com/customsearch/v1?key=AIzaSyBhX7tiQmxvjqWnTUJf3MOdMmt5EUI8RKE&cx=001142268581179224968:vh2e9kgipgr&q=" + searchData;

        // Get proper url of wiki page
        String url = new SearchLogic.searchTheWeb().execute(searchUrl).get();

        // Use SMMRY api to get data from wiki page (json)
        String apiUrl = "http://api.smmry.com/&SM_API_KEY=A47BAB4439&SM_LENGTH=40&SM_WITH_BREAK&SM_URL=" + url;
        String result = new SearchLogic.JSONRequest().execute(apiUrl).get();
        System.out.println(result);

        return getCards(result);
    }

    // Creates the "cards" (array with result strings)
    // Takes results of api call as string, returns string array
    public String[] getCards(String apiResults){
        String[] cardsText;
        try{
            // If there is no error with summarization
            if(apiResults != null && !apiResults.contains("sm_api_error" )){
                JSONObject jsonObj = new JSONObject(apiResults);
                String jsonString = jsonObj.getString("sm_api_content");
                String regex = "[BREAK]";
                cardsText = jsonString.split(Pattern.quote(regex));
            }
            else{
                cardsText = new String[] {"ERROR"};
            }
        }
        catch(JSONException e){
            e.printStackTrace();
            cardsText = new String[] {"ERROR"};
        }

        return cardsText;
    }

    // Uses getRandom() to get a random title, and then formats it with the wikipedia api.
    // Returns a string, which is the properly formatted title.
    public String getRandomSearchTitle() throws ExecutionException, InterruptedException {
        String titleApiUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&titles=";
        String formattedTitle;

        // Get a random wikipedia page title (unformatted)
        String unformattedTitle = new SearchLogic.getRandom().execute(titleApiUrl).get();

        // Ensure correct title format (use JSONRequest with wiki api)
        String wikiApiJsonString = new SearchLogic.JSONRequest().execute(titleApiUrl + unformattedTitle).get();

        if (wikiApiJsonString != null) {
            formattedTitle = parseFormattedTitle(wikiApiJsonString); // Get correct title
        }
        else {
            formattedTitle = "Sailboat"; // Gives user something instead of empty string
        }

        return formattedTitle;
    }

    // Parses out title from wiki API. Takes api response string and returns string title
    public String parseFormattedTitle(String apiResponse){
        String title;

        try {
            // Create json boj with returned string and isolate the "pages" element
            JSONObject allJson = new JSONObject(apiResponse);
            JSONObject queryJson = allJson.optJSONObject("query");
            JSONObject pagesJson = queryJson.optJSONObject("pages");

            // Get json key (different for every wikipedia page)
            Iterator<?> keys = pagesJson.keys();
            String key = (String) keys.next();

            // Isolate the page id (int) element
            JSONObject pageIdJson = pagesJson.getJSONObject(key);

            // Get correct title
            title = pageIdJson.getString("title");
        }
        catch(JSONException e){
            e.printStackTrace();
            title = "Sailboat"; // Gives user something instead of empty string
        }

        return title;
    }

    // Async Class. Random search function.
    // Returns the unformatted title of a random wikipedia page.
    public class getRandom extends AsyncTask<String, Void, String> {
        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 15000;
        private static final int CONNECTION_TIMEOUT = 15000;
        private String result, rawTitle;

        // Gets random wiki page and returns text to be loaded into search bar
        @Override
        protected String doInBackground(String... params){
            String randomUrl = "https://en.wikipedia.org/wiki/Special:Random";

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
                int index = rawTitle.indexOf("wiki/") + 5;
                result = rawTitle.substring(index);

                con.disconnect();
            }
            catch(IOException e){
                e.printStackTrace();
                result = "Sailboat"; // Gives user something instead of empty string
            }

            return result;
        }
        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }
    }

    // Async Class. Get proper wiki url for search.
    // Takes a string URL for the user's search. https://google.com/search?q=[user_search]_wikipedia
    // Returns a string, which is URL of the corresponding wikipedia page.
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
                String jsonString = "";
                for(String line; (line = in.readLine()) != null; jsonString += line);

                JSONObject respJson = new JSONObject(jsonString);
                result = respJson.getJSONArray("items").getJSONObject(0).getString("link");

                in.close();
                con.disconnect();
            }
            catch(Exception e){
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

    // Async Class. Get summarized data (json).
    // Takes a string url to get json from.
    // Returns the json string.
    public class JSONRequest extends AsyncTask<String, Void, String> {
        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 15000;
        private static final int CONNECTION_TIMEOUT = 15000;

        // Read through json response
        @Override
        protected String doInBackground(String... params){
            String stringUrl = params[0];
            String result;
            String inputLine;
            try {
                URL myUrl = new URL(stringUrl);
                HttpURLConnection connection =(HttpURLConnection)myUrl.openConnection();

                // Set method and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.connect();

                // Set up reader
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
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
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }
    }
}