package eus.elkarmedia.apnea;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeFormatter {

    public static String formatMillis(double timeInMillis) {
        long millis = Double.valueOf(timeInMillis).longValue();
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
