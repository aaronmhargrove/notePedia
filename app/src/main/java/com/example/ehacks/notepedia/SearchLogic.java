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
    // Takes a string, which is the user's search.
    // Returns a string array, which is the result of their search (to be displayed on "cards".)
    public String[] searchWiki (String searchString) throws ExecutionException, InterruptedException, JSONException {
        // Google [user input] + "wikipedia" -> acts as a spell check and gives most relevant page
        String searchData = searchString + " wikipedia";
        searchData = searchData.replaceAll(" ", "_");
        String searchUrl = "https://google.com/search?q=" + searchData;

        // Get proper url of wiki page
        String url = new SearchLogic.searchTheWeb().execute(searchUrl).get();

        // Use SMMRY api to get data from wiki page (json)
        String apiUrl = "http://api.smmry.com/&SM_API_KEY=A47BAB4439&SM_LENGTH=40&SM_WITH_BREAK&SM_URL=" + url;
        String result = new SearchLogic.JSONRequest().execute(apiUrl).get();

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

    // Uses getRandom() to get a random title, and then formats it with the wikipedia api.
    // Returns a string, which is the properly formatted title.
    public String getRandomSearchTitle() throws ExecutionException, InterruptedException {
        String titleApiUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&titles=";
        String formattedTitle;

        // Get a random wikipedia page title (unformatted)
        String unformattedTitle = new SearchLogic.getRandom().execute(titleApiUrl).get();

        // Ensure correct title format (use JSONRequest with wiki api)
        try{
            // Get json from wiki api
            String wikiApiJsonString = new SearchLogic.JSONRequest().execute(titleApiUrl + unformattedTitle).get();

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
            formattedTitle = pageIdJson.getString("title");
        }
        catch(JSONException e){
            e.printStackTrace();
            formattedTitle = "Sailboat"; // Gives user something instead of empty string
        }

        return formattedTitle;
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
                int temp = rawTitle.indexOf("wiki/") + 5;
                result = rawTitle.substring(temp);

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

                // Search through google results page to get to wiki page
                while ((inputLine = in.readLine()) != null) {
                    // If first match found -> update result and exit loop
                    if (inputLine.toLowerCase().matches(".*href=\"https://en.wikipedia.org/wiki/.*")) {
                        result = inputLine;
                        int index = result.indexOf("https://en.wikipedia.org/wiki/");
                        result= result.substring(index, index+500);
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

    // Async Class. Get summarized data (json).
    // Takes a string url to get json from.
    // Returns the json string.
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