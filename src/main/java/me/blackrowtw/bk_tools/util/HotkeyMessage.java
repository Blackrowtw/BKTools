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

package me.blackrowtw.bk_tools.util;

import fi.dy.masa.malilib.util.InfoUtils;
import me.blackrowtw.bk_tools.config.widget.ButtonLoopMode;

/**
 * Hotkey 執行訊息工具類
 * 統一處理 hotkey 觸發後的 action bar 訊息顯示
 */
public class HotkeyMessage {

    private static final String PREFIX = "bk_tools.actionBarMessage.";

    /**
     * 通用 hotkey 訊息（無動態引數）
     * 例如：open_gui
     */
    public static void printHotkeyPress(String configName) {
        String key = PREFIX + "hotkey_press." + configName;
        InfoUtils.printActionbarMessage(key);
    }

    /**
     * CMB 執行命令訊息
     * 
     * @param configType "CMB" 或 "CMBL"
     * @param index      編號
     * @param command    執行的命令
     */
    public static void printExecuteCommand(String configType, int index, String command) {
        String key = PREFIX + "execute_command";
        String msgExecuteCommand = "§7§o" + configType + " #" + index + " → §r" + command;
        InfoUtils.printActionbarMessage(key, msgExecuteCommand);
    }

    /**
     * CMBL 模式切換訊息
     * 
     * @param listIndex 列表編號
     * @param newMode   新模式
     */
    public static void printCMBLModeSwitch(int listIndex, ButtonLoopMode newMode) {
        String key = PREFIX + "command_bind_list_mode_switch";
        // 透過 getDisplayName() 取得翻譯後的模式名稱（如："順序"、"固定"、"隨機"）
        String modeName = newMode.getDisplayName();
        String msgModeSwitch = "§7§oCMBL #" + listIndex + " → §r" + modeName;
        InfoUtils.printActionbarMessage(key, msgModeSwitch);
    }

    /**
     * Cooldown 等待訊息
     * 
     * @param ticks 剩餘 ticks
     */
    public static void printCooldownWait(int ticks) {
        String key = PREFIX + "cooldown_wait";
        String tickText = "§7§o(" + ticks + " tick)";
        InfoUtils.printActionbarMessage(key, tickText);
    }
}