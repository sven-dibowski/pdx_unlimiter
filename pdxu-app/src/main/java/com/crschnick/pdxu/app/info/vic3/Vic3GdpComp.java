package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import javafx.scene.image.Image;

import java.util.Collections;
import java.util.List;

public class Vic3GdpComp extends Vic3ChannelComp {

    @Override
    protected String getDisplayValue() {
        return Math.round((value / 2.0 / 10000.0)) + "M";
    }

    @Override
    protected List<String> getNames() {
        return Collections.singletonList("gdp");
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_GDP;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("GDP");
    }
}
