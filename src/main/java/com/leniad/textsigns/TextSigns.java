package com.leniad.textsigns;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenPageInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.leniad.textsigns.signui.SignUI;
import com.leniad.textsigns.signui.SignUISupplier;

import javax.annotation.Nonnull;

/**
 * @author Daniel Novaes Dias
 * @version 1.0.0
 */
public class TextSigns extends JavaPlugin {

    private static TextSigns instance;


    public TextSigns(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        var logger = getLogger();
        logger.atInfo().log("[SignText] Loaded");
    }

    @Override
    protected void setup() {
        super.setup();
        this.getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("TextSign_UI", SignUISupplier.class, SignUISupplier.CODEC);
    }

    /**
     * Get plugin instance.
     */
    public static TextSigns getInstance() {
        return instance;
    }
}
