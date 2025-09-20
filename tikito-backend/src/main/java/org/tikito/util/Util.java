package org.tikito.util;

public final class Util {
    private Util() {

    }

    // todo, make method without default and replace Double.parseDouble
    public static double getDoubleOrDefault(final String value) {
        try {
            if (value.contains(",") && value.contains(".")) {
                return Double.parseDouble(value.replaceAll("\\.", "").replace(',', '.'));
            }
            return Double.parseDouble(value.replace(',', '.'));
        } catch (final Exception e) {
            return 0;
        }
    }

    public static String getFileExtension(final String filename) {
        if (filename == null) {
            return null;
        }
        final int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
