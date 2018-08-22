package com.example.ehacks.notepedia;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SearchLogicTest {

    @Test
    public void testGetCardsValid() {
        // Arrange
        String apiResults = "{\"sm_api_character_count\":\"5508\",\"sm_api_content_reduced\":\"69%\",\"sm_api_title\"" +
                ":\"Unit testing\",\"sm_api_content\":\" In computer programming, unit testing is a software testing" +
                " method by which individual units of source code, sets of one or more computer program modules together" +
                " with associated control data, usage procedures, and operating procedures, are tested to determine" +
                " whether they are fit for use.[BREAK] Substitutes such as method stubs, mock objects, fakes, and test " +
                "harnesses can be used to assist testing a module in isolation.[BREAK]\",\"sm_api_limitation\":\"Waited " +
                "0 extra seconds due to API Free mode, 96 requests left to make for today.\"}";
        String expected = " In computer programming, unit testing is a software testing" +
                " method by which individual units of source code, sets of one or more computer program modules together" +
                " with associated control data, usage procedures, and operating procedures, are tested to determine" +
                " whether they are fit for use.";
        SearchLogic logic = new SearchLogic();

        // Act
        String actual = logic.getCards(apiResults)[0];

        // Assert
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetCardsNull() {
        // Arrange
        String nullString = null;
        String expected = "ERROR";
        SearchLogic logic = new SearchLogic();

        // Act
        String actual = logic.getCards(nullString)[0];

        // Assert
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetCardsError() {
        // Arrange
        String errorString = "{\"sm_api_error\":3,\"sm_api_message\":\"SOURCE IS TOO SHORT\"}";
        String expected = "ERROR";
        SearchLogic logic = new SearchLogic();

        // Act
        String actual = logic.getCards(errorString)[0];

        // Assert
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testParseFormattedTitleValid() {
        // Arrange
        String apiResults = "{\"batchcomplete\":\"\",\"query\":{\"pages\":{\"47278217\":{\"pageid\":47278217,\"ns\":0,\"title\":\"Uliyampalayam\"}}}}";
        String expected = "Uliyampalayam";
        SearchLogic logic = new SearchLogic();

        // Act
        String actual = logic.parseFormattedTitle(apiResults);

        // Assert
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testParseFormattedTitleInvalid() {
        // Arrange
        String apiResults = "{\"batchcomplete\":\"\",\"query\":{\"pages\":{\"47278217\":{\"pageid\":47278217,\"ns\":0,\"notATitle\":\"Uliyampalayam\"}}}}";
        String expected = "Sailboat";
        SearchLogic logic = new SearchLogic();

        // Act
        String actual = logic.parseFormattedTitle(apiResults);

        // Assert
        Assert.assertEquals(expected, actual);
    }
}