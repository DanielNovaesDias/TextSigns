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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.leniad.textsigns.interaction.SignState;

import javax.annotation.Nonnull;


import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class SignUI extends InteractiveCustomUIPage<SignUI.PageData> {

    private String signText;

    private Vector3i interactedBlock;

    public SignUI(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, Vector3i pos) {
        super(playerRef, lifetime, PageData.CODEC);

        this.interactedBlock = pos;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/TextSigns/TextSign.ui");
        Vector3i blockPos = getSelectedBlockPos(ref);
        String currentValue = getSignMetaData(store);
        uiCommandBuilder.set("#SignInput.Value", currentValue);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SignInput", EventData.of("@SignText", "#SignInput.Value"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull SignUI.PageData data) {
        super.handleDataEvent(ref, store, data);
        setSignMetaData(ref, store, data.signText, SignUI.PageData.CODEC, data);
    }



    public void setSignMetaData(Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, String field, BuilderCodec<SignUI.PageData> codec, SignUI.PageData data) {
        BlockState signState = store.getExternalData().getWorld().getState(this.interactedBlock.x, this.interactedBlock.y, this.interactedBlock.z, true);

        if (signState instanceof SignState) {
            ((SignState) signState).setSignText(data.signText);
        }

        //if (data.signText != null) {
        //    res.add(targetBlockPos, data.signText);
        //} else {
        //    res.delete(targetBlockPos);
        //}
    }

    public String getSignMetaData(@Nonnull Store<EntityStore> store) {
        BlockState signState = store.getExternalData().getWorld().getState(this.interactedBlock.x, this.interactedBlock.y, this.interactedBlock.z, true);

        if (signState instanceof SignState) {
            return ((SignState) signState).getSignText();
        }

        return "";
    }

    private Vector3i getSelectedBlockPos(Ref<EntityStore> ref) {
        return TargetUtil.getTargetBlock(ref, 5, ref.getStore());
    }

    public static class PageData {
        static final String KEY_SIGN_TEXT = "@SignText";

        public static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(PageData.class, PageData::new)
                .addField(new KeyedCodec<>(KEY_SIGN_TEXT, Codec.STRING), (data, s) -> data.signText = s, data -> data.signText)
                .build();


        private String signText;

    }

}
