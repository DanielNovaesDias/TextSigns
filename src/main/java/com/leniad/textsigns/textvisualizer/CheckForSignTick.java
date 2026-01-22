package com.leniad.textsigns.textvisualizer;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.buuz135.mhud.MultipleHUD;
import com.leniad.textsigns.interaction.SignState;
import org.w3c.dom.Text;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CheckForSignTick extends EntityTickingSystem<EntityStore> {

    @Nonnull
    private final Query<EntityStore> query;

    private final int UPDATE_INTERVAL_TICK = 10;

    private Vector3i lastSeenBlock;
    private final ConcurrentHashMap<PlayerRef, Integer> tickCounters = new ConcurrentHashMap<>();

    private final Map<PlayerRef, TextVisualizer> huds = new ConcurrentHashMap<>();

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

        if (entStore == null || playerRef == null) return;

        Vector3i selectedBlock = getSelectedBlockPos(entStore);

        if (selectedBlock == null) {
            if (hud != null) {
                hud.setDisplay("", false);
            }
            return;
        }

        int currentTick = this.tickCounters.compute(playerRef, (ref, count) -> count == null ? 1 : count + 1);

        if (selectedBlock.equals(this.lastSeenBlock) && currentTick < UPDATE_INTERVAL_TICK){
            return;
        };

        this.lastSeenBlock = selectedBlock;

        entStore.getStore().getExternalData().getWorld().execute(() -> {
            BlockState signData = entStore.getStore().getExternalData().getWorld().getState(selectedBlock.x, selectedBlock.y, selectedBlock.z, true);

            if (signData instanceof SignState) {
                String value = ((SignState) signData).getSignText();

                if (value != null && !value.isEmpty()) {
                    if (hud == null) {
                        TextVisualizer newHud = new TextVisualizer(playerRef, value, true);
                        this.setupUI(newHud, playerRef, player);
                        huds.put(playerRef, newHud);
                        tickCounters.put(playerRef, 0);
                    } else {
                        hud.setDisplay(value, true);
                        tickCounters.put(playerRef, 0);
                    }
                }
            } else {
                if (hud != null) {
                    hud.setDisplay("", false);
                    tickCounters.put(playerRef, 0);
                }
            }
        });


    }


    private void setupUI(TextVisualizer hud, PlayerRef playerRef, Player player) {
        if (this.multipleHudAvailable) {
            MultipleHUD.getInstance().setCustomHud(player, playerRef, "TextSigns_HUD", hud);
        } else {
            hud.show();
        }
    }

    private Vector3i getSelectedBlockPos(Ref<EntityStore> ref) {
        return TargetUtil.getTargetBlock(ref, 5, ref.getStore());
    }

    @Nonnull
    public Query<EntityStore> getQuery() {
        return this.query;
    }
}
