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


package me.blackrowtw.bk_tools.config.gui.custom_render;

import fi.dy.masa.malilib.config.IConfigOptionList;
import fi.dy.masa.malilib.gui.button.ConfigButtonOptionList;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;

/**
 * 自訂觸發按鈕：黑底白框樣式（類似文字輸入框）+ hover 淡藍色效果
 * 如果自訂渲染失效，回退到 MaliLib 原始渲染
 */
public class TriggerButton extends ConfigButtonOptionList {

    // 背景：純黑 RGB 0,0,0 #000000 (ARGB)
    private static final int BG_COLOR = 0xFF000000;
    // 文字默認：淺灰 RGB 224,224,224 #E0E0E0 (ARGB)
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    // 邊框默認：灰色 RGB 160,160,160 #A0A0A0 (ARGB)
    private static final int BORDER_COLOR = 0xFFA0A0A0;
    // 邊框 hover：淡藍色 RGB 85,255,255 #55FFFF (ARGB)
    private static final int HOVER_BORDER_COLOR = 0xFF55FFFF;
    // 文字 hover：淡藍色 RGB 85,255,255 #55FFFF (ARGB)
    private static final int HOVER_TEXT_COLOR = 0xFF55FFFF;

    public TriggerButton(int x, int y, int width, int height, IConfigOptionList config) {
        super(x, y, width, height, config);
    }

    @Override
    public void render(GuiContext ctx, int mouseX, int mouseY, boolean selected) {
        try {
            this.hovered = mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width && mouseY < this.y + this.height;

            // 選擇邊框顏色（hover 時變淡藍色）
            int borderColor = this.hovered ? HOVER_BORDER_COLOR : BORDER_COLOR;

            // 繪製邊框（1px）- 環繞整個按鈕區域
            // 上邊框
            RenderUtils.drawRect(ctx, this.x, this.y, this.width, 1, borderColor);
            // 下邊框
            RenderUtils.drawRect(ctx, this.x, this.y + this.height - 1, this.width, 1, borderColor);
            // 左邊框
            RenderUtils.drawRect(ctx, this.x, this.y, 1, this.height, borderColor);
            // 右邊框
            RenderUtils.drawRect(ctx, this.x + this.width - 1, this.y, 1, this.height, borderColor);

            // 繪製黑色背景（在邊框內部）
            RenderUtils.drawRect(ctx, this.x + 1, this.y + 1, this.width - 2, this.height - 2, BG_COLOR);

            // 繪製文字（hover 時使用淡藍色）
            if (this.displayString != null && !this.displayString.isEmpty()) {
                int y = this.y + (this.height - 8) / 2;
                int color = this.enabled ? (this.hovered ? HOVER_TEXT_COLOR : TEXT_COLOR) : 0xFF808080;
                this.drawCenteredStringWithShadow(ctx, this.x + this.width / 2, y, color, this.displayString);
            }
        } catch (Exception e) {
            // 回退到 MaliLib 原始渲染
            super.render(ctx, mouseX, mouseY, selected);
        }
    }
}
