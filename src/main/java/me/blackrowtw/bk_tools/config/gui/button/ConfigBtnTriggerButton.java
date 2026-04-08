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

package me.blackrowtw.bk_tools.config.gui.button;

import fi.dy.masa.malilib.config.IConfigOptionList;
import fi.dy.masa.malilib.gui.button.ConfigButtonOptionList;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;

/**
 * 自訂觸發按鈕：灰色背景 + hover 淡藍色文字
 * 如果自訂渲染失效，回退到 MaliLib 原始渲染
 */
public class ConfigBtnTriggerButton extends ConfigButtonOptionList {

    // 低調灰色背景 (ARGB)
    private static final int BG_COLOR = 0x66404040;

    // Minecraft 超連結淡藍色 #55FFFF (ARGB)
    private static final int HOVER_TEXT_COLOR = 0xFF55FFFF;

    public ConfigBtnTriggerButton(int x, int y, int width, int height, IConfigOptionList config) {
        super(x, y, width, height, config);
    }

    @Override
    public void render(GuiContext ctx, int mouseX, int mouseY, boolean selected) {
        try {
            this.hovered = mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width && mouseY < this.y + this.height;

            // 繪製自訂灰色背景
            RenderUtils.drawRect(ctx, this.x, this.y, this.width, this.height, BG_COLOR);

            // 繪製文字（hover 時使用淡藍色）
            if (this.displayString != null && !this.displayString.isEmpty()) {
                int y = this.y + (this.height - 8) / 2;
                int color = this.enabled ? (this.hovered ? HOVER_TEXT_COLOR : 0xFFE0E0E0) : 0xFFA0A0A0;
                this.drawCenteredStringWithShadow(ctx, this.x + this.width / 2, y, color, this.displayString);
            }
        } catch (Exception e) {
            // 回退到 MaliLib 原始渲染
            super.render(ctx, mouseX, mouseY, selected);
        }
    }
}
