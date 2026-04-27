package arclightcity.ui.view;
import arclightcity.engine.GameEngine;
import arclightcity.ui.controller.SceneRouter;
public class MercenaryView {
    private final ViewsBundle.MercenaryViewImpl impl;
    public MercenaryView(GameEngine engine, SceneRouter router) {
        impl = new ViewsBundle.MercenaryViewImpl(engine, router);
    }
    public javafx.scene.Parent build() { return impl.build(); }
}
