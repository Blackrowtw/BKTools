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

import fi.dy.masa.malilib.util.game.wrap.GameWrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

/**
 * 命令發送工具
 * 使用 Minecraft.player.networkHandler 發送命令到服務器
 *
 * 行為：
 * - 以 / 開頭的字串視為命令
 * - 非 / 開頭的字串視為聊天訊息
 *
 * 發送流程：
 * 1. sendToChatScreen() - 優先使用，會添加到聊天歷史記錄，malilib 可正確攔截本地命令
 * 2. sendToServer() - 備選方案，直接發送到服務端
 */

public class CommandSender {
    public static boolean send(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        var handler = GameWrap.getNetworkConnection();
        if (handler == null) {
            return false;
        }

        boolean success = false;

        try {
            success = sendToChatScreen(input); // 方法一：ChatScreen
        } catch (Exception e) {
            try {
                success = sendToServer(input); // 方法二：Server fallback
            } catch (Exception e2) {
                DebugLogger.always("CommandSender", "Both methods failed, input: %s", input);
            }
        }
        return success;
    }

    /**
     * 方法一：透過 ChatScreen 發送
     * - 模擬玩家輸入，發送到聊天框
     * - 添加到聊天歷史記錄（按上下箭頭可回顧）
     * - malilib 可正確攔截本地命令（如 /bk hello）
     */
    private static boolean sendToChatScreen(String input) {
        Minecraft mc = Minecraft.getInstance();

        ChatScreen chatScreen;
        if (!(mc.screen instanceof ChatScreen)) {
            chatScreen = new ChatScreen("", false);
            mc.setScreen(chatScreen);
        } else {
            chatScreen = (ChatScreen) mc.screen;
        }
        chatScreen.handleChatInput(input, true);
        mc.setScreen(null); // 關閉聊天框，避免干擾玩家操作
        return true;
    }

    /**
     * 方法二：直接發送到服務端
     * - 命令發送到服務器
     * - 無法添加聊天歷史記錄
     * - malilib 自定義命令可能無法攔截
     */
    private static boolean sendToServer(String input) {
        var handler = GameWrap.getNetworkConnection();
        if (handler == null) {
            return false;
        }

        String command = input.startsWith("/") ? input.substring(1) : input;

        if (input.startsWith("/")) {
            handler.sendCommand(command);
        } else {
            handler.sendChat(command);
        }

        return true;
    }
}