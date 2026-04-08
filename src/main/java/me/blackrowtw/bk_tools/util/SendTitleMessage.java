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

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 標題訊息發送工具
 * 在螢幕正中央顯示大標題與副標題
 * 使用 builder 風格，與 SendChatMessage 保持一致
 *
 * 使用範例：
 * 
 * <pre>
 * // 最簡單用法
 * SendTitleMessage.of("Hello World!").send();
 *
 * // 帶副標題
 * SendTitleMessage.of("Hello World!", "BKTools is working!").send();
 *
 * // 完整格式
 * SendTitleMessage.of("Hello World!", "BKTools is working!")
 *         .time(20, 80, 30)
 *         .color(ChatFormatting.GOLD)
 *         .bold()
 *         .send();
 *
 * // 只顯示副標題
 * SendTitleMessage.subtitleOnly("任務完成！")
 *         .subtitleColor(ChatFormatting.GREEN)
 *         .subtitleBold()
 *         .send();
 *
 * // 清除標題
 * SendTitleMessage.clear();
 * </pre>
 */
public class SendTitleMessage {

    // 預設時間（tick，1 tick = 1/20 秒）
    private static final int DEFAULT_FADE_IN = 10;
    private static final int DEFAULT_STAY = 60;
    private static final int DEFAULT_FADE_OUT = 20;

    private final String title;
    private final String subtitle;
    private int fadeInTicks = DEFAULT_FADE_IN;
    private int stayTicks = DEFAULT_STAY;
    private int fadeOutTicks = DEFAULT_FADE_OUT;
    private ChatFormatting color = null;
    private boolean bold = false;
    private boolean italic = false;
    private ChatFormatting subtitleColor = null;
    private boolean subtitleBold = false;
    private boolean subtitleItalic = false;

    private SendTitleMessage(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    // ── 建構入口 ──────────────────────────────────────────

    /**
     * 建立標題 builder（無副標題）
     */
    public static SendTitleMessage of(String title) {
        return new SendTitleMessage(title, null);
    }

    /**
     * 建立標題 builder（帶副標題）
     */
    public static SendTitleMessage of(String title, String subtitle) {
        return new SendTitleMessage(title, subtitle);
    }

    /**
     * 只顯示副標題（title 為空）
     * 注意：subtitle 會顯示在螢幕偏上方（原本 title 下方），非正中央
     */
    public static SendTitleMessage subtitleOnly(String subtitle) {
        return new SendTitleMessage("", subtitle);
    }

    // ── 時間設定 ──────────────────────────────────────────

    /**
     * 設定淡入、停留、淡出時間（tick）
     */
    public SendTitleMessage time(int fadeIn, int stay, int fadeOut) {
        this.fadeInTicks = fadeIn;
        this.stayTicks = stay;
        this.fadeOutTicks = fadeOut;
        return this;
    }

    /**
     * 設定淡入時間（tick）
     */
    public SendTitleMessage fadeIn(int ticks) {
        this.fadeInTicks = ticks;
        return this;
    }

    /**
     * 設定停留時間（tick）
     */
    public SendTitleMessage stay(int ticks) {
        this.stayTicks = ticks;
        return this;
    }

    /**
     * 設定淡出時間（tick）
     */
    public SendTitleMessage fadeOut(int ticks) {
        this.fadeOutTicks = ticks;
        return this;
    }

    // ── 格式設定 ──────────────────────────────────────────

    /**
     * 設定標題顏色
     */
    public SendTitleMessage color(ChatFormatting color) {
        this.color = color;
        return this;
    }

    /**
     * 設定粗體
     */
    public SendTitleMessage bold() {
        this.bold = true;
        return this;
    }

    /**
     * 設定斜體
     */
    public SendTitleMessage italic() {
        this.italic = true;
        return this;
    }

    /**
     * 設定副標題顏色
     */
    public SendTitleMessage subtitleColor(ChatFormatting color) {
        this.subtitleColor = color;
        return this;
    }

    /**
     * 設定副標題粗體
     */
    public SendTitleMessage subtitleBold() {
        this.subtitleBold = true;
        return this;
    }

    /**
     * 設定副標題斜體
     */
    public SendTitleMessage subtitleItalic() {
        this.subtitleItalic = true;
        return this;
    }

    // ── 輸出 ──────────────────────────────────────────────

    /**
     * 發送標題到遊戲畫面
     */
    public void send() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null)
            return;

        mc.gui.setTimes(this.fadeInTicks, this.stayTicks, this.fadeOutTicks);

        MutableComponent titleComp = Component.literal(this.title);
        if (this.color != null)
            titleComp = titleComp.withStyle(this.color);
        if (this.bold)
            titleComp = titleComp.withStyle(ChatFormatting.BOLD);
        if (this.italic)
            titleComp = titleComp.withStyle(ChatFormatting.ITALIC);
        mc.gui.setTitle(titleComp);

        if (this.subtitle != null && !this.subtitle.isEmpty()) {
            MutableComponent subtitleComp = Component.literal(this.subtitle);
            if (this.subtitleColor != null)
                subtitleComp = subtitleComp.withStyle(this.subtitleColor);
            if (this.subtitleBold)
                subtitleComp = subtitleComp.withStyle(ChatFormatting.BOLD);
            if (this.subtitleItalic)
                subtitleComp = subtitleComp.withStyle(ChatFormatting.ITALIC);
            mc.gui.setSubtitle(subtitleComp);
        }
    }

    /**
     * 清除當前螢幕上的標題
     */
    public static void clear() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null) {
            mc.gui.clearTitles();
        }
    }
}
