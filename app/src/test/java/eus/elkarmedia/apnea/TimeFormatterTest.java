package eus.elkarmedia.apnea;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for TimeFormatter using Uncle Bob's Clean Code principles.
 * Tests edge cases and boundaries with clear, readable names.
 */
public class TimeFormatterTest {

    @Test
    public void givenZeroMilliseconds_whenFormatted_thenReturnsZeros() {
        // Given
        double millis = 0;

        // When
        String result = TimeFormatter.formatMillis(millis);

        // Then
        assertEquals("00:00:00", result);
    }

    @Test
    public void givenLessThanOneMinute_whenFormatted_thenShowsOnlySeconds() {
        // Given
        double millis = 45000; // 45 seconds

        // When
        String result = TimeFormatter.formatMillis(millis);

        // Then
        assertEquals("00:00:45", result);
    }

    @Test
    public void givenExactMinutes_whenFormatted_thenShowsMinutesAndZeros() {
        // Given
        double millis = 60000; // 1 minute exactly

        // When
        String result = TimeFormatter.formatMillis(millis);

        // Then
        assertEquals("00:01:00", result);
    }

    @Test
    public void givenMinutesAndSeconds_whenFormatted_thenShowsBothCorrectly() {
        // Given
        double millis = 3661000; // 1 hour, 1 minute, 1 second

        // When
        String result = TimeFormatter.formatMillis(millis);

        // Then
        assertEquals("01:01:01", result);
    }

    @Test
    public void givenLargeAmountOfHours_whenFormatted_thenShowsCorrectHours() {
        // Given
        double millis = 360000000; // 100 hours

        // When
        String result = TimeFormatter.formatMillis(millis);

        // Then
        assertEquals("100:00:00", result);
    }
}
