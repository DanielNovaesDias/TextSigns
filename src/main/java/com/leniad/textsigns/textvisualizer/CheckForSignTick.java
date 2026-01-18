package com.leniad.textsigns.textvisualizer;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.leniad.textsigns.SignTextsRegistry;
import com.buuz135.mhud.MultipleHUD;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;



public class CheckForSignTick extends EntityTickingSystem<EntityStore> {

    @Nonnull
    private final Query<EntityStore> query;

    //private Vector3i lastSeenBlock = new Vector3i(0,0,0);

    private final Map<PlayerRef, TextVisualizer> huds = new HashMap<>();

    private boolean multipleHudAvailable = false;


    public CheckForSignTick(boolean useMultipleHUD) {
        this.query = Query.and(new Query[]{Player.getComponentType()});

        this.multipleHudAvailable = useMultipleHUD;
    }

    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,  @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Holder<EntityStore> holder = EntityUtils.toHolder(index, archetypeChunk);
        Player player = holder.getComponent(Player.getComponentType());

        PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        Ref<EntityStore> entStore = player.getReference();


        TextVisualizer hud = this.huds.get(playerRef);

        if (entStore != null && playerRef != null) {
            Vector3i selectedBlock = getSelectedBlockPos(entStore);

            //if (java.util.Objects.equals(lastSeenBlock, selectedBlock)) {
            //    return;
            //}

            //lastSeenBlock = selectedBlock;

            if (selectedBlock == null) {

                if (hud != null) {
                    hud.setShouldDisplay(false);
                    this.showHideUI(hud, player, playerRef);
                }

                return;
            }

            String value = getSignMetaData(selectedBlock, store);

            if (value != null && !value.isEmpty()) {
                if (hud == null) {
                    TextVisualizer newHud = new TextVisualizer(playerRef);
                    newHud.setShouldDisplay(true);
                    newHud.setText(value);

                    this.showHideUI(newHud, player, playerRef);

                    huds.put(playerRef, newHud);
                } else {
                    hud.setShouldDisplay(true);
                    hud.setText(value);

                    this.showHideUI(hud, player, playerRef);

                }
            } else {
                if (hud != null) {
                    hud.setShouldDisplay(false);
                    this.showHideUI(hud, player, playerRef);
                }

            }
        }

    }

    public String getSignMetaData(Vector3i blockPos, @Nonnull Store<EntityStore> store) {
        SignTextsRegistry res = store.getResource(SignTextsRegistry.getResourceType());
        String text = res.get(blockPos);
        return text != null ? text : "";
    }

    private Vector3i getSelectedBlockPos(Ref<EntityStore> ref) {
        return TargetUtil.getTargetBlock(ref, 5, ref.getStore());
    }

    private void showHideUI(CustomUIHud hud, Player player, PlayerRef playerRef) {
        if (this.multipleHudAvailable) {
            MultipleHUD.getInstance().setCustomHud(player, playerRef, "TextSigns_HUD", hud);
        } else {
            hud.show();
        }
    }


    @Nonnull
    public Query<EntityStore> getQuery() {
        return this.query;
    }
}
