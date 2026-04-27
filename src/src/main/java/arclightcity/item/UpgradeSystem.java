package arclightcity.item;

import java.util.Map;

public class UpgradeSystem {

    private static final Map<Item.Rarity, int[]> MATERIAL_COST = Map.of(
        Item.Rarity.COMMON,    new int[]{2, 0, 0},
        Item.Rarity.UNCOMMON,  new int[]{4, 1, 0},
        Item.Rarity.RARE,      new int[]{6, 2, 1},
        Item.Rarity.EPIC,      new int[]{8, 4, 2},
        Item.Rarity.LEGENDARY, new int[]{10, 6, 4}
    );

    public static UpgradeResult upgrade(Equipment equipment,
                                        int scrap, int chips, int crystals) {
        if (!equipment.canUpgrade()) {
            return UpgradeResult.fail("Item already at max upgrade level.");
        }

        int[] cost       = MATERIAL_COST.get(equipment.getRarity());
        int   lvl        = equipment.getUpgradeLevel();
        int   needScrap  = cost[0] * (lvl + 1);
        int   needChips  = cost[1] * (lvl + 1);
        int   needCrystal= cost[2] * (lvl + 1);

        if (scrap < needScrap || chips < needChips || crystals < needCrystal) {
            return UpgradeResult.fail(String.format(
                "Not enough materials. Need: %d Scrap, %d Chips, %d Crystals",
                needScrap, needChips, needCrystal));
        }

        equipment.applyUpgrade();
        return UpgradeResult.success(needScrap, needChips, needCrystal,
                equipment.getUpgradeLevel());
    }

    public static class UpgradeResult {
        public final boolean success;
        public final String  message;
        public final int     scrapUsed, chipsUsed, crystalsUsed, newLevel;

        private UpgradeResult(boolean s, String m, int sc, int ch, int cr, int nl) {
            success = s; message = m; scrapUsed = sc;
            chipsUsed = ch; crystalsUsed = cr; newLevel = nl;
        }

        public static UpgradeResult success(int sc, int ch, int cr, int nl) {
            return new UpgradeResult(true,
                    "Upgrade success! Item is now +" + nl, sc, ch, cr, nl);
        }

        public static UpgradeResult fail(String reason) {
            return new UpgradeResult(false, reason, 0, 0, 0, 0);
        }
    }
}
