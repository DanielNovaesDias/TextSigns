package com.leniad.textsigns.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("removal")
public class SignState extends BlockState {
    public static final Codec<SignState> CODEC;

    protected String signText;
    protected boolean isLocked;

    public boolean initialize(@Nonnull BlockType blockType) {
        if (!super.initialize(blockType)) {
            return false;
        } else {

            StateData signState = blockType.getState();
            if (signState instanceof SignState.SignStateData) {
                signText = ((SignStateData) signState).getSignText();
                isLocked = ((SignStateData) signState).getIsLocked();
            }

            return true;
        }
    }


    public void setSignText(String signText) {
        this.signText = signText;
        this.markNeedsSave();
    }

    public void setIsLocked(boolean locked) {
        this.isLocked = locked;
        this.markNeedsSave();
    }

    public String getSignText() {
        return signText != null ? signText : "";
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public void markNeedsSave() {
        this.getChunk().markNeedsSaving();
    }

    static {
        CODEC =
                BuilderCodec.builder(
                                SignState.class, SignState::new, BlockState.BASE_CODEC)
                        .addField(
                                new KeyedCodec<>("TextSign", Codec.STRING),
                                (state, o) -> state.signText = o,
                                (state) -> state.signText)
                        .addField(new KeyedCodec<>("IsLocked", Codec.BOOLEAN),
                                (state, isLocked) -> state.isLocked = isLocked,
                                (state) -> state.isLocked)
                        .build();
    }

    public static class SignStateData extends StateData {
        public static final BuilderCodec<SignStateData> CODEC;
        private String signText = "";
        private boolean isLocked = false;

        protected SignStateData() {
        }

        public String getSignText() {
            return this.signText;
        }

        public boolean getIsLocked() {
            return this.isLocked;
        }

        @Nonnull
        public String toString() {
            return "SignStateData{signText=" + this.signText + ", isLocked=" + this.isLocked + "} " + super.toString();
        }

        static {
            CODEC = (BuilderCodec.builder(SignState.SignStateData.class,
                            SignState.SignStateData::new,
                            StateData.DEFAULT_CODEC)
                    .append(
                            new KeyedCodec<>("SignText", new MapCodec<>(Codec.STRING, HashMap::new, false)),
                            (state, o) -> {
                                state.signText = o.getOrDefault("Text", "");
                                state.isLocked = Boolean.parseBoolean(o.getOrDefault("IsLocked", "false"));
                            },
                            (state) -> {
                             Map<String, String> jsonMap = new HashMap<>();

                             jsonMap.put("IsLocked", String.valueOf(state.isLocked));
                             jsonMap.put("Text", state.signText);

                             return jsonMap;
                            }
                          )
                    .add()
            ).build();
        }
    }
}
