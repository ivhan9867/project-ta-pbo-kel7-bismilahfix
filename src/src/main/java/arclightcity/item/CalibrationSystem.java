package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.Random;

public class CalibrationSystem {

    private static final Random RNG = new Random();

    private static double getSuccessChance(Item.Rarity rarity, int kitCount) {
        double base = switch (rarity) {
            case COMMON    -> 0.90;
            case UNCOMMON  -> 0.80;
            case RARE      -> 0.65;
            case EPIC      -> 0.50;
            case LEGENDARY -> 0.35;
        };
        return Math.min(0.95, base + (Math.min(kitCount - 1, 2) * 0.10));
    }

    public static CalibrationResult calibrate(Equipment equipment, int kitCount) {
        if (kitCount < 1) return CalibrationResult.fail("Need at least 1 Calibration Kit.");

        double  chance  = getSuccessChance(equipment.getRarity(), kitCount);
        boolean success = RNG.nextDouble() < chance;

        if (!success) {
            return CalibrationResult.fail(
                    "Calibration failed! Item unchanged. (" + kitCount + " kit(s) used)");
        }

        StatType affected = equipment.calibrate(RNG);
        double   newVal   = equipment.getBonusStats().get(affected);

        return CalibrationResult.success(affected, newVal, kitCount,
                equipment.getCalibrationCount());
    }

    public static class CalibrationResult {
        public final boolean  success;
        public final String   message;
        public final StatType affectedStat;
        public final double   newValue;
        public final int      kitsUsed, totalCalibrations;

        private CalibrationResult(boolean s, String m, StatType st,
                                   double v, int k, int tc) {
            success = s; message = m; affectedStat = st;
            newValue = v; kitsUsed = k; totalCalibrations = tc;
        }

        public static CalibrationResult success(StatType st, double v, int k, int tc) {
            return new CalibrationResult(true,
                    String.format("Calibration success! %s -> +%.1f", st.displayName, v),
                    st, v, k, tc);
        }

        public static CalibrationResult fail(String reason) {
            return new CalibrationResult(false, reason, null, 0, 0, 0);
        }
    }
}
