package com.leniad.textsigns.signui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class SignUISupplier implements OpenCustomUIInteraction.CustomPageSupplier {

    public static final BuilderCodec<SignUISupplier> CODEC = BuilderCodec.builder(SignUISupplier.class, SignUISupplier::new).build();

    @Nonnull
    @Override
    public CustomUIPage tryCreate(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor, @Nonnull PlayerRef playerRef, InteractionContext context) {
        return new SignUI(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, new Vector3i());
    }

    @Nonnull
    public CustomUIPage tryCreateWithPos(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor, @Nonnull PlayerRef playerRef, InteractionContext context, Vector3i interactedBlock) {
        return new SignUI(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, interactedBlock);
    }

}
