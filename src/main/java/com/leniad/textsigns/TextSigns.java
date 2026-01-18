package com.leniad.textsigns;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.leniad.textsigns.deletesign.DeleteSign;
import com.leniad.textsigns.signui.SignUISupplier;
import com.leniad.textsigns.SignTextsRegistry;
import com.leniad.textsigns.textvisualizer.CheckForSignTick;



import javax.annotation.Nonnull;

/**
 * @author Daniel Novaes Dias
 * @version 1.0.0
 */
public class TextSigns extends JavaPlugin {

    private static TextSigns instance;
    private ResourceType<EntityStore, SignTextsRegistry> _SignTextsRegistryInstance;

    private boolean multipleHUDAvailable = false;

    public TextSigns(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        var logger = getLogger();
        logger.atInfo().log("[SignText] Loaded");
    }

    @Override
    protected void setup() {
        super.setup();

        this._SignTextsRegistryInstance = this.getEntityStoreRegistry().registerResource(
                SignTextsRegistry.class,
                "SignTextsRegistry",
                SignTextsRegistry.CODEC
        );

        this.getEntityStoreRegistry().registerSystem(new DeleteSign(BreakBlockEvent.class));

        PluginBase plugin = PluginManager.get().getPlugin(PluginIdentifier.fromString("Buuz135:MultipleHUD"));
        if (plugin != null) {
            this.multipleHUDAvailable = true;
        }

        this.getEntityStoreRegistry().registerSystem(new CheckForSignTick(this.multipleHUDAvailable));

        this.getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("TextSign_UI", SignUISupplier.class, SignUISupplier.CODEC);
    }

    /**
     * Get plugin instance.
     */
    public static TextSigns getInstance() {
        return instance;
    }

    public ResourceType<EntityStore, SignTextsRegistry> getSignTextsRegistry() {
        return _SignTextsRegistryInstance;
    }

}
