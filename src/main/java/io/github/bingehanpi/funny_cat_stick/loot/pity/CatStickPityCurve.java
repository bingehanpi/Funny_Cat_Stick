package io.github.bingehanpi.funny_cat_stick.loot.pity;

public final class CatStickPityCurve {
    public static final double BASE_CHANCE = 0.006D;
    public static final int GUARANTEE_AT = 100;

    private CatStickPityCurve() {
    }

    public static double chance(int attempt) {
        int n = Math.max(1, attempt);
        if (n >= GUARANTEE_AT) {
            return 1.0D;
        }
        if (n <= 80) {
            return BASE_CHANCE + 0.014D * Math.pow((n - 1) / 79.0D, 3);
        }
        return 0.02D + 0.98D * Math.pow((n - 80) / 20.0D, 2);
    }
}
