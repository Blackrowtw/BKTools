/*
 * This file is part of the BKTools project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Fallen_Breath and contributors
 *
 * BKTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BKTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BKTools.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.blackrowtw.bk_tools.config.gui;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;

/**
 * 自訂列表 Widget，回傳 BKToolsWidgetConfigOption
 */
public class BKToolsWidgetListConfigOptions extends WidgetListConfigOptions {

    public BKToolsWidgetListConfigOptions(int x, int y, int width, int height,
                                           int configWidth, float zLevel,
                                           boolean useKeybindSearch, GuiConfigsBase parent) {
        super(x, y, width, height, configWidth, zLevel, useKeybindSearch, parent);
    }

    @Override
    protected WidgetConfigOption createListEntryWidget(int x, int y, int listIndex, boolean isOdd, ConfigOptionWrapper wrapper) {
        return new BKToolsWidgetConfigOption(
            x, y, this.browserEntryWidth, this.browserEntryHeight,
            this.maxLabelWidth, this.configWidth,
            wrapper, listIndex, this.parent, this);
    }
}
