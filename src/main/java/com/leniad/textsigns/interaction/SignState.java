package com.leniad.textsigns.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;

import javax.annotation.Nonnull;

public class SignState extends BlockState {
    public static final Codec<SignState> CODEC;

    protected String signText;

    public boolean initialize(@Nonnull BlockType blockType) {
        if (!super.initialize(blockType)) {
            return false;
        } else {

            StateData signState = blockType.getState();
            if (signState instanceof SignState.SignStateData) {
                signText = ((SignStateData) signState).getSignText();
            }

            return true;
        }
    }


    public void setSignText(String signText) {
        this.signText = signText;
        this.markNeedsSave();
    }

    public String getSignText() {
        return signText != null ? signText : "";
    }

    public void markNeedsSave() {
        this.getChunk().markNeedsSaving();
    }

    static {
        CODEC =
                BuilderCodec.builder(
                                SignState.class, SignState::new, BlockState.BASE_CODEC)
                        .addField(
                                new KeyedCodec("TextSign", Codec.STRING),
                                (state, o) -> state.signText = o.toString(),
                                (state) -> state.signText)
                .build();
    }

    public static class SignStateData extends StateData {
        public static final BuilderCodec<SignStateData> CODEC;
        private String signText = "";

        protected SignStateData() {
        }

        public String getSignText() {
            return this.signText;
        }

        @Nonnull
        public String toString() {
            String signText = this.signText;
            return "SignStateData{signText=" + signText + "} " + super.toString();
        }

        static {
            CODEC = (BuilderCodec.builder(SignState.SignStateData.class,
                    SignState.SignStateData::new,
                    StateData.DEFAULT_CODEC).append(new KeyedCodec("SignText", Codec.STRING),
                    (t, i) -> t.signText = i,
                    (t) -> t.getSignText()).add())
                    .build();
        }
    }
}
