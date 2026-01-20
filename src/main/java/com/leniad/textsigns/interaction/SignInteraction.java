package com.leniad.textsigns.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.leniad.textsigns.signui.SignUI;
import com.leniad.textsigns.signui.SignUISupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class SignInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<SignInteraction> CODEC;

    void SimpleBlockInteraction() {}

    @SuppressWarnings("deprecation")
    @Override
    protected void interactWithBlock(@NotNull World world, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull InteractionType interactionType, @NotNull InteractionContext interactionContext, @Nullable ItemStack itemStack, @NotNull Vector3i pos, @NotNull CooldownHandler cooldownHandler) {
        BlockState sign = world.getState(pos.x, pos.y, pos.z, true);

        if (sign instanceof SignState) {
            Ref<EntityStore> ref = interactionContext.getEntity();
            Store<EntityStore> store = ref.getStore();
            Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
            PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());


            if (playerRef != null && playerComponent != null) {
                SignUISupplier signPageSupplier = new SignUISupplier();
                Vector3i pos_sign = new Vector3i(pos.x, pos.y, pos.z);
                CustomUIPage page = signPageSupplier.tryCreateWithPos(ref, commandBuffer, playerRef, interactionContext, pos_sign);
                playerComponent.getPageManager().openCustomPage(ref, store, page);
            }
        }
    }

    @Override
    protected void simulateInteractWithBlock(@NotNull InteractionType interactionType, @NotNull InteractionContext interactionContext, @Nullable ItemStack itemStack, @NotNull World world, @NotNull Vector3i vector3i) {

    }

    static {
        CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(SignInteraction.class, SignInteraction::new, SimpleBlockInteraction.CODEC).documentation("Opens the Sign UI.")).build();
    }

}
