package arclightcity.ui.view;
import arclightcity.engine.GameEngine;
import arclightcity.ui.controller.SceneRouter;
public class InventoryView {
    private final ViewsBundle.InventoryViewImpl impl;
    public InventoryView(GameEngine engine, SceneRouter router) {
        impl = new ViewsBundle.InventoryViewImpl(engine, router);
    }
    public javafx.scene.Parent build() { return impl.build(); }
}
