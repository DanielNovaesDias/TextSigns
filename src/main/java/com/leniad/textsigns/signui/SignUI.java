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
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.leniad.textsigns.SignTextsRegistry;

import javax.annotation.Nonnull;


import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class SignUI extends InteractiveCustomUIPage<SignUI.PageData> {

    private String signText;

    public SignUI(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, PageData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/TextSigns/TextSign.ui");
        Vector3i blockPos = getSelectedBlockPos(ref);
        String currentValue = getSignMetaData(blockPos, store);
        uiCommandBuilder.set("#SignInput.Value", currentValue);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SignInput", EventData.of("@SignText", "#SignInput.Value"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull SignUI.PageData data) {
        super.handleDataEvent(ref, store, data);
        setSignMetaData(ref, store, data.signText, SignUI.PageData.CODEC, data);
    }



    public void setSignMetaData(Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, String field, BuilderCodec<SignUI.PageData> codec, SignUI.PageData data) {
        Vector3i targetBlockPos = this.getSelectedBlockPos(ref);

        SignTextsRegistry res = store.getResource(SignTextsRegistry.getResourceType());

        if (data.signText != null) {
            res.add(targetBlockPos, data.signText);
        } else {
            res.delete(targetBlockPos);
        }
    }

    public String getSignMetaData(Vector3i blockPos, @Nonnull Store<EntityStore> store) {
        SignTextsRegistry res = store.getResource(SignTextsRegistry.getResourceType());
        String text = res.get(blockPos);
        getLogger().atInfo().log(text);
        return text != null ? text : "";
    }



    private Vector3i getSelectedBlockPos(Ref<EntityStore> ref) {
        TransformComponent transform = ref.getStore().getComponent(ref, TransformComponent.getComponentType());
        assert transform != null;

        Vector3i targetBlockPos = TargetUtil.getTargetBlock(ref, 5.0, ref.getStore());
        assert  targetBlockPos != null;

        return targetBlockPos;
    }

    public static class PageData {
        static final String KEY_SIGN_TEXT = "@SignText";

        public static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(PageData.class, PageData::new)
                .addField(new KeyedCodec<>(KEY_SIGN_TEXT, Codec.STRING), (data, s) -> data.signText = s, data -> data.signText)
                .build();


        private String signText;

    }

}
