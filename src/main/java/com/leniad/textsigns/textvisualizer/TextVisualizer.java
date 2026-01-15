package com.leniad.textsigns.textvisualizer;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public class TextVisualizer extends CustomUIHud {

    public TextVisualizer(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }


    protected void build(@Nonnull UICommandBuilder ui) {
        ui.append("Pages/TextSigns/TextVisualizer.ui");
        ui.set("#SignContent.Text", "Teste");
    }

}
