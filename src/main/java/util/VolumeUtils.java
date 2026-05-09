package util;

public class VolumeUtils {

    public static double calculateVolumeCm3(double widthCm, double heightCm, double depthCm) {
        return widthCm * heightCm * depthCm;
    }

    public static double cm3ToM3(double cm3) {
        return cm3 / 1_000_000.0;
    }

    public static boolean fitsPallet(double itemWidthCm, double itemDepthCm,
                                     double palletWidthCm, double palletLengthCm) {
        return (itemWidthCm <= palletWidthCm && itemDepthCm <= palletLengthCm)
                || (itemDepthCm <= palletWidthCm && itemWidthCm <= palletLengthCm);
    }

    public static int maxItemsPerPalletLayer(double itemWidthCm, double itemDepthCm,
                                              double palletWidthCm, double palletLengthCm) {
        int option1 = (int) (palletWidthCm / itemWidthCm) * (int) (palletLengthCm / itemDepthCm);
        int option2 = (int) (palletWidthCm / itemDepthCm) * (int) (palletLengthCm / itemWidthCm);
        return Math.max(option1, option2);
    }
}
