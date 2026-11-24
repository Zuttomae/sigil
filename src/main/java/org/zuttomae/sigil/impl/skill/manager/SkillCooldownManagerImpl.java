package org.zuttomae.sigil.impl.skill.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.zuttomae.sigil.api.skill.Skill;
import org.zuttomae.sigil.api.skill.SkillContext;
import org.zuttomae.sigil.api.skill.event.SkillCooldownEvents;
import org.zuttomae.sigil.api.skill.manager.SkillCooldownManager;
import org.zuttomae.sigil.api.skill.registry.SkillRegistries;
import org.zuttomae.sigil.impl.skill.SkillContextImpl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillCooldownManagerImpl implements SkillCooldownManager {
    @SuppressWarnings("unchecked")
    public static final Codec<SkillCooldownManager> CODEC = RecordCodecBuilder.<SkillCooldownManagerImpl>create(instance ->
            instance.group(
                            Codec.unboundedMap(SkillRegistries.SKILL.holderByNameCodec(), Codec.INT)
                                    .fieldOf("cooldowns")
                                    .forGetter(cooldownManager ->
                                            Map.copyOf(
                                                    (Map<Holder<Skill<?>>, Integer>) (Map<?, ?>) cooldownManager.cooldowns
                                            )
                                    )
                    )
                    .apply(instance, SkillCooldownManagerImpl::new)
    ).xmap(
            cooldownManager -> cooldownManager,
            cooldownManager -> (SkillCooldownManagerImpl) cooldownManager
    );

    private final Map<Holder<? extends Skill<?>>, Integer> cooldowns;
    private @Nullable LivingEntity owner = null;

    private SkillCooldownManagerImpl(
            Map<? extends Holder<? extends Skill<?>>, ? extends Integer> cooldowns
    ) {
        this.cooldowns = new ConcurrentHashMap<>(Objects.requireNonNull(cooldowns));
    }

    public SkillCooldownManagerImpl() {
        this(Map.of());
    }

    @Override
    public boolean isCoolingDown(Holder<? extends Skill<?>> skill) {
        Objects.requireNonNull(skill);
        return getCooldown(skill) > 0;
    }

    @Override
    public int getCooldown(Holder<? extends Skill<?>> skill) {
        Objects.requireNonNull(skill);
        return cooldowns.getOrDefault(skill, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setCooldown(Holder<? extends Skill<?>> skill, int cooldown) {
        Objects.requireNonNull(skill);
        if (cooldown > 0) {
            if (cooldowns.put(skill, cooldown) == null) {
                handleCooldownStarted((Holder<? extends Skill<Object>>) skill, cooldown);
            }
        } else {
            if (cooldowns.remove(skill) != null) {
                handleCooldownEnded((Holder<? extends Skill<Object>>) skill);
            }
        }
    }

    @Override
    public void clearCooldowns() {
        for (Holder<? extends Skill<?>> skill : List.copyOf(cooldowns.keySet())) {
            setCooldown(skill, 0);
        }
    }

    public @Nullable LivingEntity getOwner() {
        return owner;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = Objects.requireNonNull(owner);
    }

    public void update() {
        for (Holder<? extends Skill<?>> skill : List.copyOf(cooldowns.keySet())) {
            setCooldown(skill, getCooldown(skill) - 1);
        }
    }

    private <S> void handleCooldownStarted(Holder<? extends Skill<S>> skill, int cooldownTicks) {
        SkillContext<S> context = createContext(skill);

        SkillCooldownEvents.STARTED.invoker().onStarted(context, cooldownTicks);
    }

    private <S> void handleCooldownEnded(Holder<? extends Skill<S>> skill) {
        SkillContext<S> context = createContext(skill);

        SkillCooldownEvents.ENDED.invoker().onEnded(context);
    }

    private <S> SkillContext<S> createContext(Holder<? extends Skill<S>> skill) {
        return new SkillContextImpl<>(skill, getOwnerOrThrow());
    }

    private LivingEntity getOwnerOrThrow() {
        return Optional.ofNullable(owner)
                .orElseThrow(() -> new IllegalStateException("Owner entity is not set"));
    }
}
