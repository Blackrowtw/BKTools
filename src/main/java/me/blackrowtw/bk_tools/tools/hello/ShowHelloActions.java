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

package me.blackrowtw.bk_tools.tools.hello;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.InfoUtils;
import me.blackrowtw.bk_tools.util.DebugLogger;
import me.blackrowtw.bk_tools.util.SendChatMessage;
import me.blackrowtw.bk_tools.util.SendTitleMessage;
import net.minecraft.ChatFormatting;

public class ShowHelloActions {

    /**
     * 執行 Hello World 測試功能
     * 可被熱鍵回調或 ConfigAction 按鈕呼叫
     */
    public static void executeHello() {
        try {
            SendTitleMessage.of("Hello World!", "BKTools is working!")
                    .time(20, 80, 30)
                    .color(ChatFormatting.GOLD)
                    .bold()
                    .send();

            // // 只顯示副標題
            // SendTitleMessage.subtitleOnly("There's only a subtitle here—does it look
            // cool?")
            // .subtitleColor(ChatFormatting.GREEN)
            // .subtitleBold()
            // .time(20, 60, 20)
            // .send();
            SendChatMessage.of("Hello World! BKTools is working!")
                    .color(ChatFormatting.GREEN)
                    .send();
        } catch (Exception e) {
            InfoUtils.printActionbarMessage("Hello World! BKTools is working!");
            DebugLogger.warn("ShowHello", "SendTitleMessage 失敗: %s", e.getMessage());
        }
    }

    public static class Callback implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (action == KeyAction.PRESS) {
                executeHello();
                return true;
            }
            return false;
        }
    }
}