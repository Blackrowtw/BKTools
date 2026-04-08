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

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;

import net.minecraft.client.Minecraft;

import me.blackrowtw.bk_tools.tools.hello.ShowHelloActions;
import me.blackrowtw.bk_tools.util.DebugLogger;

public class KeyCallbackRegistry {

    // 統一登記所有快捷鍵的 callback，方便管理與維護
    public static void register() {
        Configs.Generic.OPEN_GUI.getKeybind().setCallback(new OpenGuiCallback());
        Configs.Generic.FORCE_DEBUG_MODE.setValueChangeCallback(new ForceDebugModeChangeCallback());
        Configs.Test.BKTOOLS_SAY_HELLO.getKeybind().setCallback(new ShowHelloActions.Callback());
    }

    // GUI callback 放在這裡，因為它屬於框架層而非任何特定 tool
    private static class OpenGuiCallback implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (action == KeyAction.PRESS) {
                GuiBase.openGui(new GuiConfigs());
            }
            return true;
        }
    }

    // Debug mode 切換 callback
    private static class ForceDebugModeChangeCallback implements IValueChangeCallback<ConfigBoolean> {
        @Override
        public void onValueChanged(ConfigBoolean config) {
            DebugLogger.notifyModeChange(config.getBooleanValue());
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof GuiConfigs gui) {
                gui.initGui();
            }
        }
    }
}
