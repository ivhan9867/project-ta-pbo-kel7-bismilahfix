package arclightcity.ui.view;
import arclightcity.engine.GameEngine;
import arclightcity.ui.controller.SceneRouter;
public class GameOverView {
    private final ViewsBundle.GameOverViewImpl impl;
    public GameOverView(GameEngine engine, SceneRouter router) {
        impl = new ViewsBundle.GameOverViewImpl(engine, router);
    }
    public javafx.scene.Parent build() { return impl.build(); }
}
