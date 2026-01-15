package com.leniad.textsigns;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class SignTextsRegistry implements Resource<EntityStore> {

    public static final BuilderCodec<SignTextsRegistry> CODEC = BuilderCodec.builder(
                    SignTextsRegistry.class, SignTextsRegistry::new
            )
            .append(
                    new KeyedCodec<>(
                            "SignsSavedTexts",
                            new MapCodec<>(Codec.STRING, HashMap::new, false),
                            true
                    ),
                    // decode: Map<String, String> → Map<Vector3i, String>
                    (o, map) -> {
                        if (map != null) {
                            map.forEach((k, v) -> {
                                String[] p = k.split(",");
                                Vector3i pos = new Vector3i(
                                        Integer.parseInt(p[0]),
                                        Integer.parseInt(p[1]),
                                        Integer.parseInt(p[2])
                                );
                                o.SavedSigns.put(pos, v);
                            });
                        }
                    },
                    // encode: Map<Vector3i, String> → Map<String, String>
                    o -> {
                        Map<String, String> out = new HashMap<>();
                        o.SavedSigns.forEach((pos, v) -> {
                            out.put(
                                    pos.x + "," + pos.y + "," + pos.z,
                                    v
                            );
                        });
                        return out;
                    }
            )
            .add()
            .build();


    private Map<Vector3i, String> SavedSigns = new HashMap<>();

    public static ResourceType<EntityStore, SignTextsRegistry> getResourceType() {
        return TextSigns.getInstance().getSignTextsRegistry();
    }

    public SignTextsRegistry() {
    }

    public SignTextsRegistry(@Nonnull SignTextsRegistry other) {
        this.SavedSigns = other.SavedSigns;
    }

    public void add(Vector3i block, String text) {
        SavedSigns.put(block, text);
    }


    public void delete(Vector3i block) {
        SavedSigns.remove(block);
    }


    public String get(Vector3i block) {
        return SavedSigns.getOrDefault(block, "");
    }


    @Nonnull
    @Override
    public Resource<EntityStore> clone() {
        return new SignTextsRegistry(this);
    }

}
