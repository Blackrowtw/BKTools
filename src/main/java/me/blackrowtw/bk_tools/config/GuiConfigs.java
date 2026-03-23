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

// 使用者的圖形設定介面
package me.blackrowtw.bk_tools.config;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// import java.util.Collections;
import java.util.List;
import java.util.Objects;

import me.blackrowtw.bk_tools.Reference;

@Environment(EnvType.CLIENT)
public class GuiConfigs extends GuiConfigsBase {

    // 目前顯示哪個分頁
    private static Tab currentTab = Tab.ALL;

    public GuiConfigs() {
        super(10, 50,
                Reference.MOD_ID,
                null,
                "bk_tools.gui.title",
                Reference.MOD_VERSION);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;

        // 分頁按鈕
        for (Tab tab : Tab.values()) {
            ButtonGeneric btn = new ButtonGeneric(x, y, tab.label.length() * 8 + 16, 20, tab.label);
            btn.setEnabled(currentTab != tab);
            this.addButton(btn, new TabButtonListener(tab, this));
            x += btn.getWidth() + 4;
        }

        // // CMB 頁面額外顯示「+ 新增」按鈕
        // if (currentTab == Tab.CMB) {
        // ButtonGeneric addBtn = new ButtonGeneric(x + 20, y, 80, 20, "+ Add");
        // this.addButton(addBtn, new AddButtonListener(this));
        // }
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        return switch (currentTab) {
            case ALL -> ConfigOptionWrapper.createFor(Configs.HOTKEYS);
            case CMB -> ConfigOptionWrapper.createFor(Configs.HOTKEYS);
        };
    }

    @Override
    protected boolean useKeybindSearch() {
        return currentTab == Tab.ALL;
    }

    // ── 分頁定義 ─────────────────────────────────────────
    private enum Tab {
        ALL("ALL"),
        CMB("CMB");

        final String label;

        Tab(String label) {
            this.label = label;
        }
    }

    // ── 分頁切換按鈕 listener ─────────────────────────────
    private static class TabButtonListener implements IButtonActionListener {
        private final Tab tab;
        private final GuiConfigs gui;

        TabButtonListener(Tab tab, GuiConfigs gui) {
            this.tab = tab;
            this.gui = gui;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            currentTab = tab;
            gui.reCreateListWidget();
            Objects.requireNonNull(gui.getListWidget()).getScrollbar().setValue(0);
            gui.initGui();
        }
    }

    // ── 新增命令綁定按鈕 listener ─────────────────────────
    // private static class AddButtonListener implements IButtonActionListener {
    // private final GuiConfigs gui;

    // AddButtonListener(GuiConfigs gui) {
    // this.gui = gui;
    // }

    // @Override
    // public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
    // }
    // }
}
