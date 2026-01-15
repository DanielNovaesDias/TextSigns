package com.leniad.textsigns.textvisualizer;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.leniad.textsigns.SignTextsRegistry;

import javax.annotation.Nonnull;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class CheckForSignTick extends EntityTickingSystem<EntityStore> {

    @Nonnull
    private final Query<EntityStore> query;


    public CheckForSignTick() {
        this.query = Query.and(new Query[]{Player.getComponentType()});
    }

    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,  @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Holder<EntityStore> holder = EntityUtils.toHolder(index, archetypeChunk);
        Player player = holder.getComponent(Player.getComponentType());
        PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        Ref<EntityStore> entStore = player.getReference();


        //if (entStore != null) {
        //   Vector3i selectedBlock = getSelectedBlockPos(entStore);
//
        //    String value = getSignMetaData(selectedBlock, store);
//
        //    if (value != null && !value.isEmpty()) {
        //        TextVisualizer hud = new TextVisualizer(playerRef);
        //
        //    }
        //}

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


    @Nonnull
    public Query<EntityStore> getQuery() {
        return this.query;
    }
}
