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

package me.blackrowtw.bk_tools.config;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;

import net.minecraft.client.Minecraft;

import me.blackrowtw.bk_tools.tools.cn2tw.Cn2TwFallbackManager;
import me.blackrowtw.bk_tools.util.DebugLogger;
import me.blackrowtw.bk_tools.util.HotkeyMessage;

public class KeyCallbackRegistry {

    // ── 防止 FORCE_DEBUG_MODE 在 config load 時雙觸發 ──────
    // Config load 時 setValueFromJsonElement 會呼叫 onValueChanged；
    // malilib 某些版本的 init 流程中可能再觸發一次。
    // 記錄上次處理的值，相同則跳過。
    private static Boolean lastHandledDebugMode = null;

    // 統一登記所有快捷鍵的 callback，方便管理與維護
    public static void register() {
        Configs.Generic.OPEN_GUI.getKeybind().setCallback(new OpenGuiCallback());
        Configs.Generic.FORCE_DEBUG_MODE.setValueChangeCallback(new ForceDebugModeChangeCallback());
        Configs.Cn2Tw.CN2TW_ENABLE_FALLBACK.setValueChangeCallback(new Cn2TwEnableCallback());
    }

    // ── GUI 開啟 callback ─────────────────────────────────
    // 放在這裡，因為它屬於框架層而非任何特定 tool
    private static class OpenGuiCallback implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (action == KeyAction.PRESS) {
                GuiBase.openGui(new GuiConfigs());
                HotkeyMessage.printHotkeyPress("open_gui");
            }
            return true;
        }
    }

    // ── Debug mode 切換 callback ──────────────────────────
    private static class ForceDebugModeChangeCallback implements IValueChangeCallback<ConfigBoolean> {
        @Override
        public void onValueChanged(ConfigBoolean config) {
            boolean newValue = config.getBooleanValue();

            // 防止重複觸發：與上次相同則跳過
            if (lastHandledDebugMode != null && lastHandledDebugMode == newValue) {
                DebugLogger.info("KeyCallbackRegistry", "FORCE_DEBUG_MODE 重複觸發，跳過（值=%s）", newValue);
                return;
            }
            lastHandledDebugMode = newValue;

            DebugLogger.notifyModeChange(newValue);
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof GuiConfigs gui) {
                gui.initGui();
            }
        }
    }

    // ── CN2TW 開關切換 callback ───────────────────────────
    // 開啟 → 非同步刷新 fallbackMap（不阻塞主執行緒）
    // 關閉 → 立即清空 fallbackMap
    // 注意：Config load 時若初始值為 false，onToggle(false) 只 clear 空 map，無副作用
    private static class Cn2TwEnableCallback implements IValueChangeCallback<ConfigBoolean> {
        @Override
        public void onValueChanged(ConfigBoolean config) {
            Cn2TwFallbackManager.getInstance().onToggle(config.getBooleanValue());
        }
    }
}
