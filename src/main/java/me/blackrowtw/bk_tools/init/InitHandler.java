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

// 初始化並串接所有功能的地方，註冊設定處理器、設定介面工廠、快捷鍵提供者等
package me.blackrowtw.bk_tools.init;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;

import me.blackrowtw.bk_tools.config.Configs;
import me.blackrowtw.bk_tools.config.GuiConfigs;
import me.blackrowtw.bk_tools.config.InputHandler;
import me.blackrowtw.bk_tools.Reference;

public class InitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        // 1. 登錄設定處理器
        ConfigManager.getInstance().registerConfigHandler(
                Reference.MOD_ID, new Configs());
        // 2. 登錄設定介面工廠
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(Reference.MOD_ID, Reference.MOD_NAME, GuiConfigs::new));
        // 3. 登錄快捷鍵提供者（這行之前被註解掉了，是快捷鍵無效的原因）
        InputEventHandler.getKeybindManager()
                .registerKeybindProvider(InputHandler.getInstance());

        // 4. 設定快捷鍵 callback
        Configs.OPEN_GUI_CONFIGS.getKeybind().setCallback(new OpenGuiCallback());
    }

    // ── 開啟設定介面的快捷鍵 callback ──────────────────────
    private static class OpenGuiCallback implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (action == KeyAction.PRESS) {
                // GuiBase 是 malilib 自己的工具，不需要 import MinecraftClient
                GuiBase.openGui(new GuiConfigs());
            }
            return true;
        }
    }
}
