package org.zuttomae.sigil.api.skill.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.zuttomae.sigil.api.skill.SkillContext;
import org.zuttomae.sigil.api.skill.SkillInstance;
import org.zuttomae.sigil.api.skill.SkillResponse;

public final class SkillCastEvents {
    public static final Event<Allow> ALLOW = EventFactory.createArrayBacked(Allow.class, listeners -> context -> {
        for (Allow listener : listeners) {
            if (listener.onAllow(context) instanceof SkillResponse.Failure failure) {
                return failure;
            }
        }

        return SkillResponse.success();
    });

    public static final Event<Started> STARTED = EventFactory.createArrayBacked(Started.class, listeners -> instance -> {
        for (Started listener : listeners) {
            listener.onStarted(instance);
        }
    });

    public static final Event<Ended> ENDED = EventFactory.createArrayBacked(Ended.class, listeners -> (instance, reason) -> {
        for (Ended listener : listeners) {
            listener.onEnded(instance, reason);
        }
    });

    private SkillCastEvents() {
    }

    @FunctionalInterface
    public interface Allow {
        SkillResponse onAllow(SkillContext<?> context);
    }

    @FunctionalInterface
    public interface Started {
        void onStarted(SkillInstance<?> instance);
    }

    @FunctionalInterface
    public interface Ended {
        void onEnded(SkillInstance<?> instance, SkillCastEndReason reason);
    }
}
