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

package me.blackrowtw.bk_tools.config;

import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;

import me.blackrowtw.bk_tools.Reference;

public class InputHandler implements IKeybindProvider {

    private static final InputHandler INSTANCE = new InputHandler();

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    private InputHandler() {
    }

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        // Configs.ALL_HOTKEYS 自動包含所有功能
        for (IHotkey hk : Configs.ALL_HOTKEYS)
            manager.addKeybindToMap(hk.getKeybind());
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        // 告訴 malilib 這個模組有哪些快捷鍵（用於衝突偵測）
        manager.addHotkeysForCategory(
                Reference.MOD_NAME,
                Reference.MOD_ID + ".config",
                Configs.ALL_HOTKEYS);
    }
}