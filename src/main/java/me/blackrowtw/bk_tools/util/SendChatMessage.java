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

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

//#if MC >= 11900
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import java.nio.file.Path;
//#endif

public class SendChatMessage {

    private final String text;
    private ChatFormatting color = null;
    private boolean bold = false;
    private boolean italic = false;
    private boolean strikethrough = false;
    private boolean obfuscated = false;

    // #if MC >= 11900
    private Path folderLinkPath = null;
    private String folderLinkName = null;
    // #endif

    // ── 建構入口 ──────────────────────────────────────────
    private SendChatMessage(String text) {
        this.text = text;
    }

    public static SendChatMessage of(String text) {
        return new SendChatMessage(text);
    }

    // ── 格式設定 ──────────────────────────────────────────
    public SendChatMessage color(ChatFormatting color) {
        this.color = color;
        return this;
    }

    public SendChatMessage bold() {
        this.bold = true;
        return this;
    }

    public SendChatMessage italic() {
        this.italic = true;
        return this;
    }

    public SendChatMessage strikethrough() {
        this.strikethrough = true;
        return this;
    }

    public SendChatMessage obfuscated() {
        this.obfuscated = true;
        return this;
    }

    // ── 子功能：資料夾連結 ────────────────────────────────
    // #if MC >= 11900
    public SendChatMessage withFolderLink(Path folderPath, String displayName) {
        this.folderLinkPath = folderPath;
        this.folderLinkName = displayName;
        return this;
    }
    // #endif

    // ── 輸出 ──────────────────────────────────────────────
    public void send() {
        MutableComponent component = Component.literal(text);

        if (color != null)
            component = component.withStyle(color);
        if (bold)
            component = component.withStyle(ChatFormatting.BOLD);
        if (italic)
            component = component.withStyle(ChatFormatting.ITALIC);
        if (strikethrough)
            component = component.withStyle(ChatFormatting.STRIKETHROUGH);
        if (obfuscated)
            component = component.withStyle(ChatFormatting.OBFUSCATED);

        // #if MC >= 11900
        if (folderLinkPath != null && folderLinkName != null) {
            Component linkComponent = Component.literal(folderLinkName)
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent.OpenFile(
                                    folderLinkPath.toAbsolutePath()))
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Component.literal("開啟檔案位置"))));
            component = component.append(linkComponent);
        }
        // #else
        // if (folderLinkPath != null && folderLinkName != null) {
        // component = component.append(Component.literal(folderLinkName));
        // }
        // #endif

        Minecraft.getInstance().gui.getChat().addMessage(component);
    }
}