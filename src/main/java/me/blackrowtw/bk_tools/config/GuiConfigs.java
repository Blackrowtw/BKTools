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

// 使用者的圖形設定介面
package me.blackrowtw.bk_tools.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.util.StringUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

import me.blackrowtw.bk_tools.Reference;
import me.blackrowtw.bk_tools.config.gui.BKToolsWidgetListConfigOptions;

@Environment(EnvType.CLIENT)
public class GuiConfigs extends GuiConfigsBase {

    // 目前顯示哪個分頁
    private static Tab currentTab = Tab.ALL;

    // 建立 GUI 時的標題顯示模組版本
    public GuiConfigs() {
        // 搜尋列以及模組功能的位置
        super(12, 40, Reference.MOD_ID, null, "bk_tools.gui.title", buildVersionInfo());
    }

    private static String buildVersionInfo() {
        return String.format("%s | MC %s", Reference.MOD_VERSION, Reference.MALILIB_VERSION);
    }

    // ── GUI 初始化和配置選項管理 ───────────────────────────────────────────────
    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        int x = 24;
        int y = 24;

        for (Tab tab : Tab.values()) {
            // debugOnly 的 tab 只在 debug 模式下顯示
            if (tab.debugOnly && !Reference.isDebugMode())
                continue;
            x += this.createTabButton(x, y, tab) + 4;
        }
    }

    private int createTabButton(int x, int y, Tab tab) {
        ButtonGeneric btn = new ButtonGeneric(
                x, y, this.getStringWidth(tab.getDisplayName()) + 16, 16, tab.getDisplayName());
        btn.setEnabled(currentTab != tab);
        this.addButton(btn, new TabButtonListener(tab, this));
        return btn.getWidth();
    }

    // @Override
    // // 按鈕區域寬度
    // protected int getConfigWidth() {
    // return 160;
    // }

    @Override
    protected WidgetListConfigOptions createListWidget(int listX, int listY) {
        return new BKToolsWidgetListConfigOptions(listX, listY,
                this.getBrowserWidth(), this.getBrowserHeight(),
                this.getConfigWidth(), 0.f, this.useKeybindSearch(), this);
    }

    // ── 分頁定義 ─────────────────────────────────────────
    // 分頁標籤與其中的設定選項
    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        return switch (currentTab) {

            case ALL -> {
                List<IConfigBase> all = new ArrayList<>();
                all.addAll(Configs.Generic.OPTIONS);
                all.addAll(Configs.CommandBind.OPTIONS);
                all.addAll(Configs.commandBindList.OPTIONS);
                // all.addAll(Configs.Cn2Tw.OPTIONS);
                // 未來新增：all.addAll(_New_.OPTIONS);
                yield ConfigOptionWrapper.createFor(all);
            }
            case COMMAND_BIND -> {
                List<IConfigBase> cmb = new ArrayList<>();
                cmb.addAll(Configs.CommandBind.OPTIONS);
                cmb.addAll(Configs.commandBindList.OPTIONS);
                yield ConfigOptionWrapper.createFor(cmb);
            }
            // case COMMAND_BIND_LIST ->
            // ConfigOptionWrapper.createFor(Configs.commandBindList.OPTIONS);
            // case CN2TW -> ConfigOptionWrapper.createFor(Configs.Cn2Tw.OPTIONS);
            // case TEST -> ConfigOptionWrapper.createFor(Configs.Test.HOTKEYS);
            case TEST -> {
                List<IConfigBase> test = new ArrayList<>();
                test.addAll(Configs.Test.OPTIONS);
                test.addAll(Configs.Cn2Tw.OPTIONS);
                // 未來新增：test.addAll(_New_.OPTIONS);
                yield ConfigOptionWrapper.createFor(test);
            }
        };
    }

    @Override
    protected boolean useKeybindSearch() {
        return true; // 有快捷鍵的分頁都開啟搜尋欄
    }

    // ── 分頁翻譯 key值 ─────────────────────────────────────────
    private enum Tab {
        ALL("bk_tools.gui.tab.all", false),
        COMMAND_BIND("bk_tools.gui.tab.commandBind", false),
        TEST("bk_tools.gui.tab.test", true);

        final String translationKey;
        final boolean debugOnly;

        Tab(String translationKey, boolean debugOnly) {
            this.translationKey = translationKey;
            this.debugOnly = debugOnly;
        }

        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
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
            if (gui.getListWidget() != null) {
                gui.getListWidget().resetScrollbarPosition();
            }
            gui.initGui();
        }
    }
}
