package arclightcity.ui.view;
import arclightcity.dungeon.DungeonEvent;
import arclightcity.engine.GameEngine;
import arclightcity.ui.controller.SceneRouter;
public class EventView {
    private final ViewsBundle.EventViewImpl impl;
    public EventView(GameEngine engine, SceneRouter router, DungeonEvent event) {
        impl = new ViewsBundle.EventViewImpl(engine, router, event);
    }
    public javafx.scene.Parent build() { return impl.build(); }
}
