package com.leniad.textsigns;

import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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

        this.getEntityStoreRegistry().registerSystem(new CheckForSignTick());

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
