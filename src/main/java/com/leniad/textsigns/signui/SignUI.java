package com.leniad.textsigns.signui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.components.PlacedByInteractionComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.leniad.textsigns.interaction.SignState;
import javax.annotation.Nonnull;
import java.util.UUID;

@SuppressWarnings("removal")
public class SignUI extends InteractiveCustomUIPage<SignUI.PageData> {

    private String signText;
    private boolean isLocked;

    private Vector3i interactedBlock;

    public SignUI(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, Vector3i pos) {
        super(playerRef, lifetime, PageData.CODEC);

        this.interactedBlock = pos;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/TextSigns/TextSign.ui");
        SignState currentState = getSignMetaData(store);

        if (currentState == null) {
            return;
        }
        WorldChunk chunk = currentState.getChunk();
        assert chunk != null;
        Ref<ChunkStore> blockChunkRef = chunk.getBlockComponentEntity(interactedBlock.x, interactedBlock.y, interactedBlock.z);
        assert blockChunkRef != null;
        PlacedByInteractionComponent placedByComp =  blockChunkRef.getStore().getComponent(blockChunkRef, PlacedByInteractionComponent.getComponentType());

        if (placedByComp != null) {
            UUID PlayerOwnerID = placedByComp.getWhoPlacedUuid();

            if (PlayerOwnerID != null) {
                PlayerRef currentPlayer = store.getComponent(ref, PlayerRef.getComponentType());
                assert currentPlayer != null;

                if (!PlayerOwnerID.equals(currentPlayer.getUuid())) {
                    uiCommandBuilder.set("#TXTSCheckBox.Disabled", true);
                }
            }
        }


        uiCommandBuilder.set("#TXTSSignInput.Value", currentState.getSignText());
        uiCommandBuilder.set("#TXTSCheckBox.Value", currentState.getIsLocked());

        if (currentState.getIsLocked()) {
            uiCommandBuilder.set("#TXTSSignInput.IsReadOnly", true);
        }

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TXTSSignInput", EventData.of("@SignText", "#TXTSSignInput.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TXTSCheckBox", EventData.of("@SignLocked", "#TXTSCheckBox.Value"), false);

    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull SignUI.PageData data) {
        super.handleDataEvent(ref, store, data);
        setSignMetaData(ref, store, SignUI.PageData.CODEC, data);
    }



    public void setSignMetaData(Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, BuilderCodec<SignUI.PageData> codec, SignUI.PageData data) {
        BlockState signState = store.getExternalData().getWorld().getState(this.interactedBlock.x, this.interactedBlock.y, this.interactedBlock.z, true);

        if (signState instanceof SignState signStateCast) {

            if (data.signText != null) {
                if (!signStateCast.getIsLocked()) {
                    signStateCast.setSignText(data.signText);
                }
                return;
            }

            if (data.isLocked != signStateCast.getIsLocked()) {
                signStateCast.setIsLocked(data.isLocked);
                UICommandBuilder update = new UICommandBuilder();
                update.set("#TXTSSignInput.IsReadOnly", data.isLocked);
                this.sendUpdate(update, false);
            }
        }
    }

    public SignState getSignMetaData(@Nonnull Store<EntityStore> store) {
        BlockState signState = store.getExternalData().getWorld().getState(this.interactedBlock.x, this.interactedBlock.y, this.interactedBlock.z, true);

        if (signState instanceof SignState signStateCast) {
            return signStateCast;
        }

        return null;
    }

    private Vector3i getSelectedBlockPos(Ref<EntityStore> ref) {
        return TargetUtil.getTargetBlock(ref, 5, ref.getStore());
    }

    public static class PageData {
        static final String KEY_SIGN_TEXT = "@SignText";
        static final String KEY_LOCKED_TEXT = "@SignLocked";

        public static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(PageData.class, PageData::new)
                .addField(new KeyedCodec<>(KEY_SIGN_TEXT, Codec.STRING), (data, s) -> data.signText = s, data -> data.signText)
                .addField(new KeyedCodec<>(KEY_LOCKED_TEXT, Codec.BOOLEAN), (data, b) -> data.isLocked = b, data -> data.isLocked)
                .build();


        private String signText;
        private boolean isLocked;
    }

}
