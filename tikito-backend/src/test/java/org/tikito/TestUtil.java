package org.tikito;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public final class TestUtil {
    public static String randomString(final int min, final int max) {
        return RandomStringUtils.secure().nextAlphanumeric(min, max);
    }

    public static int randomInt(final int min, final int max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static float randomFloat(final float min, final float max) {
        final Random random = new Random();
        return random.nextFloat(max - min) + min;
    }

    public static boolean randomBool() {
        return randomInt(0, 1) == 1;
    }

    public static String randomIBAN() {
        return "NL" + randomInt(10, 99) + randomString(4, 4) + "000" + randomInt(1000000, 9999999);
    }
}
