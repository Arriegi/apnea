package eus.elkarmedia.apnea;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for PostureCalculator, written in "Clean Code" style by Uncle Bob.
 * Test names clearly express the behavior being tested (Given-When-Then
 * concept).
 */
public class PostureCalculatorTest {

    private final double HIGH_VALUE = 9.81;
    private final double LOW_VALUE = 2.0;

    @Test
    public void givenDefaultOrientation_whenYSensorIsHighAndPositive_thenPositionIsRight() {
        // Given
        String orientation = "Left";
        double x = LOW_VALUE;
        double y = HIGH_VALUE;
        double z = LOW_VALUE;

        // When
        int result = PostureCalculator.getPosition(x, y, z, orientation);

        // Then
        assertEquals(PostureCalculator.RIGHT, result);
    }

    @Test
    public void givenDefaultOrientation_whenYSensorIsHighAndNegative_thenPositionIsLeft() {
        // Given
        String orientation = "Left";
        double x = LOW_VALUE;
        double y = -HIGH_VALUE;
        double z = LOW_VALUE;

        // When
        int result = PostureCalculator.getPosition(x, y, z, orientation);

        // Then
        assertEquals(PostureCalculator.LEFT, result);
    }

    @Test
    public void givenDefaultOrientation_whenZSensorIsHighAndPositive_thenPositionIsBack() {
        // Given
        String orientation = "Left";
        double x = LOW_VALUE;
        double y = LOW_VALUE;
        double z = HIGH_VALUE;

        // When
        int result = PostureCalculator.getPosition(x, y, z, orientation);

        // Then
        assertEquals(PostureCalculator.BACK, result);
    }

    @Test
    public void givenDefaultOrientation_whenZSensorIsHighAndNegative_thenPositionIsStomach() {
        // Given
        String orientation = "Left";
        double x = LOW_VALUE;
        double y = LOW_VALUE;
        double z = -HIGH_VALUE;

        // When
        int result = PostureCalculator.getPosition(x, y, z, orientation);

        // Then
        assertEquals(PostureCalculator.STOMACH, result);
    }

    @Test
    public void givenDefaultOrientation_whenXSensorIsHigh_thenPositionIsUp() {
        // Given
        String orientation = "Left";
        double x = HIGH_VALUE;
        double y = LOW_VALUE;
        double z = LOW_VALUE;

        // When
        int result = PostureCalculator.getPosition(x, y, z, orientation);

        // Then
        assertEquals(PostureCalculator.UP, result);
    }

    @Test
    public void givenRightOrientation_whenYSensorIsHighAndPositive_thenPositionIsLeft() {
        // Given
        String orientation = "Right";
        double x = LOW_VALUE;
        double y = HIGH_VALUE; // When orientation is "Right", Y is inverted, making it effectively negative
        double z = LOW_VALUE;

        // When
        int result = PostureCalculator.getPosition(x, y, z, orientation);

        // Then
        assertEquals(PostureCalculator.LEFT, result);
    }

    @Test
    public void givenUpOrientation_whenXSensorIsHigh_thenPositionIsRightOrLeft() {
        // Given
        String orientation = "Up";
        double x = HIGH_VALUE; // X gets swapped with Y. Original X > 0, so new Y > 0 => RIGHT
        double y = LOW_VALUE;
        double z = LOW_VALUE;

        // When
        int result = PostureCalculator.getPosition(x, y, z, orientation);

        // Then
        assertEquals(PostureCalculator.RIGHT, result);
    }

    @Test
    public void givenDownOrientation_whenXSensorIsHigh_thenPositionIsLeft() {
        // Given
        String orientation = "Down";
        double x = HIGH_VALUE; // Swapped with -Y => new Y = -x => negative => LEFT
        double y = LOW_VALUE;
        double z = LOW_VALUE;

        // When
        int result = PostureCalculator.getPosition(x, y, z, orientation);

        // Then
        assertEquals(PostureCalculator.LEFT, result);
    }
}
