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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.buuz135.mhud.MultipleHUD;
import com.leniad.textsigns.TextSigns;
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
        if (player == null) return;

        PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        Ref<EntityStore> entStore = player.getReference();

        TextVisualizer hud = this.huds.get(playerRef);

        if (entStore == null || playerRef == null) return;

        Vector3i selectedBlock = getSelectedBlockPos(entStore);

        if (selectedBlock == null) {
            this.lastSeenBlock = null;

            if (hud != null) {
                if (hud.getShouldDisplay()) {
                    this.updateUI(hud, playerRef, "", false, false);
                }
            }

            return;
        }

        int currentTick = this.tickCounters.compute(playerRef, (ref, count) -> count == null ? 1 : count + 1);


        if (selectedBlock.equals(this.lastSeenBlock) && currentTick < UPDATE_INTERVAL_TICK){
            return;
        };

        this.lastSeenBlock = selectedBlock;

        World world = commandBuffer.getExternalData().getWorld();

        world.execute(() -> {
            BlockState signData = world.getState(selectedBlock.x, selectedBlock.y, selectedBlock.z, true);

            if (signData instanceof SignState signStateCast) {
                String value = signStateCast.getSignText();

                if (value != null && !value.isEmpty()) {
                    if (hud == null) {
                        this.setupUI(playerRef, player, value);
                    } else {
                        this.updateUI(hud, playerRef, value, true, true);
                    }
                }
            } else {
                if (hud != null) {
                    this.updateUI(hud, playerRef, "", false, true);
                }
            }
        });
    }


    private void updateUI(TextVisualizer hud, PlayerRef playerRef, String text, boolean active, boolean resetTicks) {
            hud.setDisplay(text, active);
            if (resetTicks) {
                this.tickCounters.put(playerRef, 0);
            }
    }

    private void setupUI(PlayerRef playerRef, Player player, String text) {
        TextVisualizer newHud = new TextVisualizer(playerRef, text, true);
        this.setCustomUI(newHud, playerRef, player);
        huds.put(playerRef, newHud);
        this.tickCounters.put(playerRef, 0);
    }

    private void setCustomUI(TextVisualizer hud, PlayerRef playerRef, Player player) {
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
