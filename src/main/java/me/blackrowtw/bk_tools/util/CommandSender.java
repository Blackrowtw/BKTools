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

import fi.dy.masa.malilib.util.game.wrap.GameWrap;

/**
 * 命令發送工具
 * 使用 MaliLib GameWrap 作為底層，版本相容性由 MaliLib 維護
 *
 * 行為：
 * - 以 / 開頭的字串視為命令（自動去除 / 後發送）
 * - 非 / 開頭的字串視為聊天訊息
 */
public class CommandSender {

    private CommandSender() {
    }

    /**
     * 發送命令或聊天訊息
     *
     * @param input 命令字串，以 / 開頭視為命令，否則視為聊天訊息
     * @return 是否成功發送
     */
    public static boolean send(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        if (input.startsWith("/")) {
            return GameWrap.sendCommand(input.substring(1));
        } else {
            return GameWrap.sendChatMessage(input);
        }
    }
}
