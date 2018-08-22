package com.example.ehacks.notepedia;

import org.junit.Test;
import junit.framework.Assert;

public class CardDisplayTest {
    @Test
    public void changeCardNextAtBeginning() {
        // Arrange
        CardDisplay cd = new CardDisplay();
        int actual, expected = 1;
        cd.count = 0;
        cd.len = 3;

        // Act
        cd.changeCard(1);
        actual = cd.count;

        // Assert

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void changeCardNextAtEnd() {
        // Arrange
        CardDisplay cd = new CardDisplay();
        int actual, expected = 0;
        cd.count = 2;
        cd.len = 3;

        // Act
        cd.changeCard(1);
        actual = cd.count;

        // Assert
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void changeCardPrevAtBeginning() {
        // Arrange
        CardDisplay cd = new CardDisplay();
        int actual, expected = 2;
        cd.count = 0;
        cd.len = 3;

        // Act
        cd.changeCard(-1);
        actual = cd.count;

        // Assert
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void changeCardPrevAtEnd() {
        // Arrange
        CardDisplay cd = new CardDisplay();
        int actual, expected = 1;
        cd.count = 2;
        cd.len = 3;

        // Act
        cd.changeCard(-1);
        actual = cd.count;

        // Assert
        Assert.assertEquals(expected, actual);
    }
}
