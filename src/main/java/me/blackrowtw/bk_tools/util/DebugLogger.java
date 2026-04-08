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

package me.blackrowtw.bk_tools.util;

import me.blackrowtw.bk_tools.BKTools;
import me.blackrowtw.bk_tools.Reference;
import net.minecraft.ChatFormatting;

public class DebugLogger {

    // ── LOG 輸出（只在 debug 模式下輸出）────────────────────
    public static void info(String tag, String message, Object... args) {
        if (!Reference.isDebugMode())
            return;
        BKTools.LOGGER.info("[{}] {}", tag, String.format(message, args));
    }

    public static void warn(String tag, String message, Object... args) {
        if (!Reference.isDebugMode())
            return;
        BKTools.LOGGER.warn("[{}] {}", tag, String.format(message, args));
    }

    // ── 無論是否 debug 模式，都輸出（重要狀態用）────────────
    public static void always(String tag, String message, Object... args) {
        BKTools.LOGGER.info("[{}] {}", tag, String.format(message, args));
    }

    public static void alwaysWarn(String tag, String message, Object... args) {
        BKTools.LOGGER.warn("[{}] {}", tag, String.format(message, args));
    }

    // ── 聊天欄 debug 訊息（只在 debug 模式下顯示）──────────
    public static void chat(String message, Object... args) {
        if (!Reference.isDebugMode())
            return;
        SendChatMessage.of(String.format("[DEBUG] " + message, args))
                .color(ChatFormatting.GRAY)
                .italic()
                .send();
    }

    // ── 狀態切換提示（切換 debug 模式時顯示）────────────────
    public static void notifyModeChange(boolean enabled) {
        if (enabled) {
            SendChatMessage.of("[BKTools] Debug 模式已開啟")
                    .color(ChatFormatting.YELLOW)
                    .bold()
                    .send();
        } else {
            SendChatMessage.of("[BKTools] Debug 模式已關閉")
                    .color(ChatFormatting.GRAY)
                    .send();
        }
    }
}