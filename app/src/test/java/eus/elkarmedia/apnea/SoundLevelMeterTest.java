package eus.elkarmedia.apnea;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for SoundLevelMeter's pure decibel conversion logic.
 *
 * Tests follow Given-When-Then naming convention (Uncle Bob style).
 * Only the pure static method is tested — no Android dependencies needed.
 */
public class SoundLevelMeterTest {

    private static final double TOLERANCE = 0.1;

    @Test
    public void givenZeroAmplitude_whenConverted_thenReturnsZeroDecibels() {
        double result = SoundLevelMeter.amplitudeToDecibels(0);
        assertEquals(0.0, result, TOLERANCE);
    }

    @Test
    public void givenNegativeAmplitude_whenConverted_thenReturnsZeroDecibels() {
        double result = SoundLevelMeter.amplitudeToDecibels(-100);
        assertEquals(0.0, result, TOLERANCE);
    }

    @Test
    public void givenReferenceAmplitude_whenConverted_thenReturnsZeroDecibels() {
        double result = SoundLevelMeter.amplitudeToDecibels(1.0);
        assertEquals(0.0, result, TOLERANCE);
    }

    @Test
    public void givenAmplitude10_whenConverted_thenReturns20Decibels() {
        double result = SoundLevelMeter.amplitudeToDecibels(10);
        assertEquals(20.0, result, TOLERANCE);
    }

    @Test
    public void givenAmplitude100_whenConverted_thenReturns40Decibels() {
        double result = SoundLevelMeter.amplitudeToDecibels(100);
        assertEquals(40.0, result, TOLERANCE);
    }

    @Test
    public void givenMaxAmplitude_whenConverted_thenReturnsApprox90Decibels() {
        // MediaRecorder.getMaxAmplitude() typically returns values up to 32767
        double result = SoundLevelMeter.amplitudeToDecibels(32767);
        assertEquals(90.3, result, TOLERANCE);
    }

    @Test
    public void givenTypicalSnoringAmplitude_whenConverted_thenReturnsReasonableDecibels() {
        // Typical snoring is around 40-60 dB
        double result = SoundLevelMeter.amplitudeToDecibels(1000);
        assertEquals(60.0, result, TOLERANCE);
    }
}
