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

// 設定的各項目定義，快捷鍵與分類等
package me.blackrowtw.bk_tools;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

import java.util.List;

public class Configs implements IConfigHandler {

    // ── 快捷鍵設定 ───────────────────────────────────────
    // 格式："KEY" 或 "MODIFIER+KEY"，例如 "Z,C" 代表 Z 和 C 同按
    public static final ConfigHotkey OpenGuiConfigs = new ConfigHotkey("openGuiConfigs", "B,C",
            KeybindSettings.PRESS_ALLOWEXTRA)
            .apply(Reference.MOD_ID + ".hotkeys");

    // ── 布林設定（範例：未來功能開關） ────────────────────
    // public static final ConfigBoolean MY_FEATURE =
    // new ConfigBoolean("myFeature", false, "bk_tools.config.my_feature");

    // ── 所有快捷鍵的清單（供 InputHandler 和 GuiConfigs 使用）──
    public static final List<ConfigHotkey> HOTKEYS = ImmutableList.of(
            OpenGuiConfigs);

    @Override
    public void load() {
    }

    @Override
    public void save() {
    }
}
