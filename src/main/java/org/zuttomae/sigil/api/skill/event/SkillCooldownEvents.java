package org.zuttomae.sigil.api.skill.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.zuttomae.sigil.api.skill.SkillContext;

public final class SkillCooldownEvents {
    public static final Event<Started> STARTED = EventFactory.createArrayBacked(Started.class, listeners -> (context, cooldownTicks) -> {
        for (Started listener : listeners) {
            listener.onStarted(context, cooldownTicks);
        }
    });

    public static final Event<Ended> ENDED = EventFactory.createArrayBacked(Ended.class, listeners -> context -> {
        for (Ended listener : listeners) {
            listener.onEnded(context);
        }
    });

    private SkillCooldownEvents() {
    }

    @FunctionalInterface
    public interface Started {
        void onStarted(SkillContext<?> context, int cooldownTicks);
    }

    @FunctionalInterface
    public interface Ended {
        void onEnded(SkillContext<?> context);
    }
}
