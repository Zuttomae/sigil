package org.zuttomae.sigil.api.skill.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.zuttomae.sigil.api.skill.SkillContext;

public final class SkillContainerEvents {
    public static final Event<Added> ADDED = EventFactory.createArrayBacked(Added.class, listeners -> context -> {
        for (Added listener : listeners) {
            listener.onAdded(context);
        }
    });

    public static final Event<Removed> REMOVED = EventFactory.createArrayBacked(Removed.class, listeners -> context -> {
        for (Removed listener : listeners) {
            listener.onRemoved(context);
        }
    });

    public static final Event<Loaded> LOADED = EventFactory.createArrayBacked(Loaded.class, listeners -> context -> {
        for (Loaded listener : listeners) {
            listener.onLoaded(context);
        }
    });

    private SkillContainerEvents() {
    }

    @FunctionalInterface
    public interface Added {
        void onAdded(SkillContext<?> context);
    }

    @FunctionalInterface
    public interface Removed {
        void onRemoved(SkillContext<?> context);
    }

    @FunctionalInterface
    public interface Loaded {
        void onLoaded(SkillContext<?> context);
    }
}
