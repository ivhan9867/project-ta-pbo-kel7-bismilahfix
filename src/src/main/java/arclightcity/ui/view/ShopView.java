package arclightcity.ui.view;
import arclightcity.engine.GameEngine;
import arclightcity.ui.controller.SceneRouter;
public class ShopView {
    private final ViewsBundle.ShopViewImpl impl;
    public ShopView(GameEngine engine, SceneRouter router) {
        impl = new ViewsBundle.ShopViewImpl(engine, router);
    }
    public javafx.scene.Parent build() { return impl.build(); }
}
