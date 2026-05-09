package util;

public class TimeUtils {

    public static int estimateDrivingMinutes(double distanceKm, double avgSpeedKmh) {
        if (avgSpeedKmh <= 0) avgSpeedKmh = 40.0;
        return Math.max((int) Math.ceil(distanceKm / avgSpeedKmh * 60), 5);
    }

    public static String formatMinutes(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%dh %02dmin", hours, minutes);
    }

    public static int estimateDeliveryTimeMinutes(int itemCount) {
        return 10 + itemCount * 2;
    }
}
