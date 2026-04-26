package arclightcity.ui.view;
import arclightcity.combat.CombatResult;
import arclightcity.engine.GameEngine;
import arclightcity.ui.controller.SceneRouter;
public class VictoryView {
    private final ViewsBundle.VictoryViewImpl impl;
    public VictoryView(GameEngine engine, SceneRouter router, CombatResult result) {
        impl = new ViewsBundle.VictoryViewImpl(engine, router, result);
    }
    public javafx.scene.Parent build() { return impl.build(); }
}
