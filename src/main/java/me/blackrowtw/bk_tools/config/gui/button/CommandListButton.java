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

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.input.MouseButtonEvent;
import fi.dy.masa.malilib.config.IConfigStringList;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiStringListEdit;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IConfigGui;
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.StringUtils;

/**
 * 命令列表按鈕：黑底白框樣式（類似文字輸入框）
 * 點擊後打開 GuiStringListEdit 對話框
 */
public class CommandListButton extends ButtonGeneric {

    private final IConfigStringList config;
    private final IConfigGui configGui;
    @Nullable
    private final IDialogHandler dialogHandler;

    // 背景：純黑 RGB 0,0,0 #000000 (ARGB)
    private static final int BG_COLOR = 0xFF000000;
    // 文字默認：淺灰 RGB 224,224,224 #808080 (ARGB)
    private static final int TEXT_COLOR = 0xFF808080;
    // 邊框默認：灰色 RGB 160,160,160 #A0A0A0 (ARGB)
    private static final int BORDER_COLOR = 0xFFA0A0A0;
    // 文字 hover：淡藍色 RGB 85,255,255 #55FFFF (ARGB)
    private static final int HOVER_TEXT_COLOR = 0xFF55FFFF;
    // 邊框 hover：淡藍色 RGB 85,255,255 #55FFFF (ARGB)
    private static final int HOVER_BORDER_COLOR = 0xFF55FFFF;

    public CommandListButton(int x, int y, int width, int height,
            IConfigStringList config, IConfigGui configGui,
            @Nullable IDialogHandler dialogHandler) {
        super(x, y, width, height, "");

        this.config = config;
        this.configGui = configGui;
        this.dialogHandler = dialogHandler;

        this.updateDisplayString();
    }

    @Override
    protected boolean onMouseClickedImpl(MouseButtonEvent click, boolean doubleClick) {
        super.onMouseClickedImpl(click, doubleClick);

        // 點擊後打開對話框（保持原有邏輯）
        if (this.dialogHandler != null) {
            this.dialogHandler.openDialog(new GuiStringListEdit(this.config, this.configGui, this.dialogHandler, null));
        } else {
            GuiBase.openGui(new GuiStringListEdit(this.config, this.configGui, null, GuiUtils.getCurrentScreen()));
        }

        return true;
    }

    @Override
    public void updateDisplayString() {
        // 顯示 "N commands" 格式
        this.displayString = StringUtils.getClampedDisplayStringRenderlen(
                this.config.getStrings(), this.width - 10, "", "");
    }

    @Override
    public void render(GuiContext ctx, int mouseX, int mouseY, boolean selected) {
        try {
            this.hovered = mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width && mouseY < this.y + this.height;

            // 內縮 1px
            int inset = 1;
            int bgX = this.x + inset;
            int bgY = this.y + inset;
            int bgW = this.width - inset * 2;
            int bgH = this.height - inset * 2;

            // 選擇邊框顏色（hover 時變淡藍色）
            int borderColor = this.hovered ? HOVER_BORDER_COLOR : BORDER_COLOR;

            // 繪製邊框（1px）- 環繞內縮後的區域
            // 上邊框
            RenderUtils.drawRect(ctx, bgX, bgY, bgW, 1, borderColor);
            // 下邊框
            RenderUtils.drawRect(ctx, bgX, bgY + bgH - 1, bgW, 1, borderColor);
            // 左邊框
            RenderUtils.drawRect(ctx, bgX, bgY, 1, bgH, borderColor);
            // 右邊框
            RenderUtils.drawRect(ctx, bgX + bgW - 1, bgY, 1, bgH, borderColor);

            // 繪製黑色背景（內縮後的區域）
            RenderUtils.drawRect(ctx, bgX + 1, bgY + 1, bgW - 2, bgH - 2, BG_COLOR);

            // 繪製文字（居中對齊）
            if (this.displayString != null && !this.displayString.isEmpty()) {
                int textY = this.y + (this.height - 8) / 2;
                int color = this.enabled ? (this.hovered ? HOVER_TEXT_COLOR : TEXT_COLOR) : 0xFF808080;
                this.drawCenteredStringWithShadow(ctx, this.x + this.width / 2, textY, color, this.displayString);
            }
        } catch (Exception e) {
            // 回退到 MaliLib 原始渲染
            super.render(ctx, mouseX, mouseY, selected);
        }
    }
}
