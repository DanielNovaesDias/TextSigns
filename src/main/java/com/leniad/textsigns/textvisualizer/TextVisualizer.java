package com.leniad.textsigns.textvisualizer;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public class TextVisualizer extends CustomUIHud {

    private boolean shouldDisplay = false;
    private String text = "";

    public TextVisualizer(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }


    protected void build(@Nonnull UICommandBuilder ui) {

        if (shouldDisplay) {
            ui.append("Pages/TextSigns/TextVisualizer.ui");
            ui.set("#SignContent.Text", text);
        }
    }


    public void setShouldDisplay(boolean shouldDisplay) {
        this.shouldDisplay = shouldDisplay;
    }

    public boolean getShouldDisplay() {
        return this.shouldDisplay;
    }

    public void setText(String text) {
        this.text = text;
    }
}
