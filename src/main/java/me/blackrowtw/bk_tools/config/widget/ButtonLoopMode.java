/*
 * This file is part of the BKTools project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026 BlacKrowtw (Template by Fallen_Breath, Uses malilib by masa & sakura-ryoko)
 *
 * Based on fabric-mod-template by Fallen_Breath
 * See: https://github.com/Fallen-Breath/fabric-mod-template
 *
 * This project uses the malilib library by masa and sakura-ryoko
 * See: https://github.com/sakura-ryoko/malilib
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

package me.blackrowtw.bk_tools.config.widget;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

/**
 * 命令清單循環模式
 * 用於 ConfigCommandBindList 的執行行為切換
 */
public enum ButtonLoopMode implements IConfigOptionListEntry {

    SEQUENTIAL("sequential", "bk_tools.button.loopMode.sequential"),
    RANDOM("random", "bk_tools.button.loopMode.random"),
    FIXED("fixed", "bk_tools.button.loopMode.fixed");

    private final String stringValue;
    private final String translationKey;

    ButtonLoopMode(String stringValue, String translationKey) {
        this.stringValue = stringValue;
        this.translationKey = translationKey;
    }

    @Override
    public String getStringValue() {
        return this.stringValue;
    }

    @Override
    public String getDisplayName() {
        return StringUtils.translate(this.translationKey);
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward) {
        ButtonLoopMode[] values = values();
        int next = forward ? (this.ordinal() + 1) : (this.ordinal() - 1 + values.length);
        return values[next % values.length];
    }

    @Override
    public IConfigOptionListEntry fromString(String value) {
        for (ButtonLoopMode mode : values()) {
            if (mode.stringValue.equals(value)) {
                return mode;
            }
        }
        return SEQUENTIAL;
    }
}
