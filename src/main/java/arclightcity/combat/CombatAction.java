package arclightcity.combat;

import java.util.List;

/**
 * Representasi aksi yang dilakukan entity di turn-nya.
 * Dihasilkan oleh decideAction() dan dieksekusi oleh CombatManager.
 */
public class CombatAction {

    public enum ActionType {
        BASIC_ATTACK,
        USE_SKILL,
        USE_ITEM,
        DEFEND,
        PASS
    }

    private final ActionType   actionType;
    private final String       skillId;
    private final String       itemId;
    private final List<String> targetIds;

    // ── Factory Methods ──────────────────────────────────────

    public static CombatAction basicAttack(List<String> targetIds) {
        return new CombatAction(ActionType.BASIC_ATTACK, null, null, targetIds);
    }

    public static CombatAction useSkill(String skillId, List<String> targetIds) {
        return new CombatAction(ActionType.USE_SKILL, skillId, null, targetIds);
    }

    public static CombatAction useItem(String itemId, List<String> targetIds) {
        return new CombatAction(ActionType.USE_ITEM, null, itemId, targetIds);
    }

    public static CombatAction defend() {
        return new CombatAction(ActionType.DEFEND, null, null, List.of());
    }

    public static CombatAction pass() {
        return new CombatAction(ActionType.PASS, null, null, List.of());
    }

    // ── Constructor ─────────────────────────────────────────

    private CombatAction(ActionType actionType, String skillId,
                         String itemId, List<String> targetIds) {
        this.actionType = actionType;
        this.skillId    = skillId;
        this.itemId     = itemId;
        this.targetIds  = targetIds;
    }

    // ── Getters ─────────────────────────────────────────────

    public ActionType    getActionType() { return actionType; }
    public String        getSkillId()    { return skillId; }
    public String        getItemId()     { return itemId; }
    public List<String>  getTargetIds()  { return targetIds; }

    @Override
    public String toString() {
        return switch (actionType) {
            case BASIC_ATTACK -> "Basic Attack → " + targetIds;
            case USE_SKILL    -> "Skill [" + skillId + "] → " + targetIds;
            case USE_ITEM     -> "Item [" + itemId + "]";
            case DEFEND       -> "Defend";
            case PASS         -> "Pass (cannot act)";
        };
    }
}
