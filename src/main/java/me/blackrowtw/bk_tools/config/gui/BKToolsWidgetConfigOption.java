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

package me.blackrowtw.bk_tools.config.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IStringRepresentable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.config.gui.ConfigOptionListenerResetConfig;
import fi.dy.masa.malilib.config.gui.ConfigOptionListenerResetConfig.ConfigResetterBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.util.StringUtils;
import me.blackrowtw.bk_tools.config.widget.ConfigCommandBind;
import me.blackrowtw.bk_tools.config.widget.ConfigCommandBindList;
import me.blackrowtw.bk_tools.config.widget.ConfigCommandBindListMode;
import me.blackrowtw.bk_tools.config.widget.ConfigBtnTrigger;
import me.blackrowtw.bk_tools.config.gui.custom_render.TriggerButton;
import me.blackrowtw.bk_tools.config.gui.custom_render.CommandListButton;

/**
 * BKTools 自訂 GUI Widget
 * 負責渲染自訂 Config 型別，MaliLib 原生型別交由父類別處理
 *
 * 處理的型別：
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ ConfigCommandBind → 單一命令 + 快捷鍵 │
 * │ 排版: [名稱][文字輸入框 65%][快捷鍵按鈕][⚙][↺] │
 * │ │
 * │ ConfigCommandBindList → 命令清單 + 執行快捷鍵 │
 * │ 排版: [名稱][ N commands ][執行快捷鍵][⚙][↺] │
 * │ 清單按鈕: 點擊開啟 GuiStringListEdit 編輯命令清單 │
 * │ 清單按鈕: 左右各內縮 2px，外圍間距與其他按鈕一致 │
 * │ │
 * │ ConfigCommandBindListMode → 循環模式 + 模式切換快捷鍵 │
 * │ 排版: [循環模式][Sequential ▼][模式切換快捷鍵][⚙][↺] │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * 各區塊說明：
 * - addConfigOption() → 型別分派入口，根據 config 型別呼叫對應渲染方法
 * - addCommandBindWidgets() → ConfigCommandBind 的完整渲染邏輯
 * - addCommandBindListWidgets() → ConfigCommandBindList 的完整渲染邏輯
 * - addCommandBindListModeWidgets()→ ConfigCommandBindListMode 的完整渲染邏輯
 * - wasConfigModified() → 檢測 config 是否被修改（用於儲存提示）
 * - applyNewValueToConfig() → 將 UI 變更套用回 config（文字框失焦時觸發）
 * - CommandBindResetter → ConfigCommandBind 重設按鈕行為
 * - CommandBindListResetter → ConfigCommandBindList 重設按鈕行為
 * - CommandBindListModeResetter → ConfigCommandBindListMode 重設按鈕行為
 */
public class BKToolsWidgetConfigOption extends WidgetConfigOption {

    // ── ConfigCommandBind 狀態追蹤 ──
    private ConfigCommandBind commandBindConfig;
    private String initialHotkeyValue;
    private fi.dy.masa.malilib.hotkeys.KeybindSettings initialKeybindSettings;
    private String initialCommandValue;

    // ── ConfigCommandBindList 狀態追蹤 ──
    private ConfigCommandBindList commandBindListConfig;
    private String initialListValue;

    // ── 共用 Widget 引用 ──
    private ConfigButtonKeybind keybindButton;
    private CommandListButton stringListButton;

    public BKToolsWidgetConfigOption(int x, int y, int width, int height, int labelWidth, int configWidth,
            fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper wrapper,
            int listIndex, IKeybindConfigGui host,
            WidgetListConfigOptionsBase<?, ?> parent) {
        super(x, y, width, height, labelWidth, configWidth, wrapper, listIndex, host, parent);
    }

    // =====================================================================
    // 型別分派入口 — 根據 config 型別委派到對應的渲染方法
    // =====================================================================

    @Override
    protected void addConfigOption(int x, int y, int labelWidth, int configWidth, IConfigBase config) {
        if (config instanceof ConfigCommandBind entry) {
            // ── ConfigCommandBind: 單一命令 + 快捷鍵 ──
            this.commandBindConfig = entry;
            this.initialHotkeyValue = entry.getKeybind().getStringValue();
            this.initialKeybindSettings = entry.getKeybind().getSettings();
            this.initialCommandValue = entry.getCommandConfig().getStringValue();
            this.initialStringValue = entry.getCommandConfig().getStringValue();
            this.lastAppliedValue = entry.getCommandConfig().getStringValue();
            this.addCommandBindWidgets(x, y, labelWidth, configWidth, entry);
            return;
        }
        // ── ConfigCommandBindList: 命令清單 + 執行快捷鍵 ──
        if (config instanceof ConfigCommandBindList listEntry) {
            this.commandBindListConfig = listEntry;
            this.initialHotkeyValue = listEntry.getKeybind().getStringValue();
            this.initialKeybindSettings = listEntry.getKeybind().getSettings();
            this.initialListValue = listEntry.getCommandsConfig().getStrings().toString();
            this.initialStringValue = listEntry.getCommandsConfig().getStrings().toString();
            this.lastAppliedValue = listEntry.getCommandsConfig().getStrings().toString();
            this.addCommandBindListWidgets(x, y, labelWidth, configWidth, listEntry);
            return;
        }
        // ── ConfigCommandBindListMode: 循環模式 + 模式切換快捷鍵 ──
        if (config instanceof ConfigCommandBindListMode modeEntry) {
            this.initialHotkeyValue = modeEntry.getKeybind().getStringValue();
            this.initialKeybindSettings = modeEntry.getKeybind().getSettings();
            this.addCommandBindListModeWidgets(x, y, labelWidth, configWidth, modeEntry);
            return;
        }
        // ── ConfigBtnTrigger: 觸發按鈕 ──
        if (config instanceof ConfigBtnTrigger entry) {
            this.addBtnTriggerWidgets(x, y, labelWidth, configWidth, entry);
            return;
        }
        // ── MaliLib 原生型別: 交由父類別處理 ──
        super.addConfigOption(x, y, labelWidth, configWidth, config);
    }

    // =====================================================================
    // ConfigCommandBind 渲染邏輯
    // 排版: [名稱標籤][文字輸入框 65%][快捷鍵按鈕][⚙][↺]
    // =====================================================================

    /**
     * 建立 ConfigCommandBind 的所有 UI 元件
     * 包含：名稱標籤、hover 註解、文字輸入框、快捷鍵按鈕、進階設定圖示、重設按鈕
     */
    protected void addCommandBindWidgets(int x, int y, int labelWidth, int configWidth, ConfigCommandBind entry) {
        int configHeight = 20;

        // 名稱標籤 + hover 註解
        String displayName = StringUtils.translate(
                "bk_tools.config.commandBind.name.command_" + entry.getIndex());
        this.addLabel(x, y + 7, labelWidth, 8, 0xFFFFFFFF, displayName);

        String comment = entry.getCommandConfig().getComment();
        if (comment != null) {
            this.addWidget(new WidgetHoverInfo(x, y + 5, labelWidth, 12, comment));
        }

        x += labelWidth + 10;

        // 命令文字輸入框（65%）
        int textFieldWidth = (int) ((configWidth - 22) * 0.65);
        GuiTextFieldGeneric field = this.createTextField(x, y + 1, textFieldWidth - 4, configHeight - 3);
        field.setValue(entry.getCommandConfig().getStringValue());
        field.setMaxLength(this.maxTextfieldTextLength);
        x += textFieldWidth;

        // 快捷鍵按鈕（填充剩餘部分）
        int keybindWidth = configWidth - textFieldWidth - 22;
        this.keybindButton = new ConfigButtonKeybind(
                x, y, keybindWidth, 20, entry.getKeybind(), this.host);
        x += keybindWidth + 2;

        // 進階按鍵設定圖示
        this.addWidget(new WidgetKeybindSettings(
                x, y, 20, 20, entry.getKeybind(), entry.getName(),
                this.parent, this.host.getDialogHandler()));
        x += 22;

        // 重設按鈕
        ButtonGeneric resetButton = this.createResetButton(x, y, entry);

        // 事件綁定
        ConfigOptionChangeListenerTextField listenerChange = new ConfigOptionChangeListenerTextField(
                entry.getCommandConfig(), field, resetButton);
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(
                entry, new CommandBindResetter(entry.getCommandConfig(), field, this.keybindButton), resetButton, null);

        this.host.addKeybindChangeListener(() -> {
            this.keybindButton.updateDisplayString();
            resetButton.setEnabled(entry.isModified());
        });

        this.addTextField(field, listenerChange);
        this.addButton(this.keybindButton, this.host.getButtonPressListener());
        this.addButton(resetButton, listenerReset);
    }

    // =====================================================================
    // ConfigCommandBindList 渲染邏輯
    // 排版: [名稱標籤][N commands 按鈕 65%][快捷鍵按鈕][⚙][↺]
    // 循環模式由 ConfigCommandBindList 內部的 ConfigOptionList 控制
    // =====================================================================

    /**
     * 建立 ConfigCommandBindList 的所有 UI 元件
     * 包含：名稱標籤、hover 註解、命令清單按鈕、快捷鍵按鈕、進階設定圖示、重設按鈕
     * 循環模式按鈕由 MaliLib 原生 ConfigOptionList 渲染處理（獨立一行）
     */
    protected void addCommandBindListWidgets(int x, int y, int labelWidth, int configWidth,
            ConfigCommandBindList entry) {
        int configHeight = 20;

        // 名稱標籤 + hover 註解
        String displayName = entry.getConfigGuiDisplayName();
        this.addLabel(x, y + 7, labelWidth, 8, 0xFFFFFFFF, displayName);

        String comment = entry.getCommandsConfig().getComment();
        if (comment != null) {
            this.addWidget(new WidgetHoverInfo(x, y + 5, labelWidth, 12, comment));
        }

        x += labelWidth + 10;

        // 命令清單按鈕（65%），顯示「N commands」，黑底白框樣式
        int listButtonWidth = (int) ((configWidth - 22) * 0.65);
        this.stringListButton = new CommandListButton(
                x + 1, y, listButtonWidth - 2, configHeight, entry.getCommandsConfig(), this.host,
                this.host.getDialogHandler());
        this.updateStringListButtonDisplay(entry);
        x += listButtonWidth;

        // 快捷鍵按鈕（填充剩餘部分）
        int keybindWidth = configWidth - listButtonWidth - 22;
        this.keybindButton = new ConfigButtonKeybind(
                x, y, keybindWidth, 20, entry.getKeybind(), this.host);
        x += keybindWidth + 2;

        // 進階按鍵設定圖示
        this.addWidget(new WidgetKeybindSettings(
                x, y, 20, 20, entry.getKeybind(), entry.getName(),
                this.parent, this.host.getDialogHandler()));
        x += 22;

        // 重設按鈕
        ButtonGeneric resetButton = this.createResetButton(x, y, entry);

        // 事件綁定
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(
                entry, new CommandBindListResetter(entry.getCommandsConfig(), this.stringListButton, this.keybindButton,
                        entry),
                resetButton, null);

        this.host.addKeybindChangeListener(() -> {
            this.keybindButton.updateDisplayString();
            this.updateStringListButtonDisplay(entry);
            resetButton.setEnabled(entry.isModified());
        });

        this.addButton(this.stringListButton, this.host.getButtonPressListener());
        this.addButton(this.keybindButton, this.host.getButtonPressListener());
        this.addButton(resetButton, listenerReset);
    }

    /**
     * 更新命令清單按鈕顯示文字
     * 顯示格式：「N commands」（例如：3 commands）
     */
    private void updateStringListButtonDisplay(ConfigCommandBindList entry) {
        int count = entry.getCommandsConfig().getStrings().size();
        this.stringListButton.setDisplayString(count + " commands");
    }

    // =====================================================================
    // ConfigCommandBindListMode 渲染邏輯
    // 排版: [循環模式][Sequential ▼][模式切換快捷鍵][⚙][↺]
    // =====================================================================

    /**
     * 建立 ConfigCommandBindListMode 的所有 UI 元件
     * 包含：循環模式按鈕（MaliLib 原生 ConfigOptionList 渲染）、模式切換快捷鍵、進階設定圖示、重設按鈕
     */
    protected void addCommandBindListModeWidgets(int x, int y, int labelWidth, int configWidth,
            ConfigCommandBindListMode entry) {
        int configHeight = 20;

        // 名稱標籤 + hover 註解
        String displayName = entry.getConfigGuiDisplayName();
        this.addLabel(x, y + 7, labelWidth, 8, 0xFFFFFFFF, displayName);

        String comment = entry.getLoopModeConfig().getComment();
        if (comment != null) {
            this.addWidget(new WidgetHoverInfo(x, y + 5, labelWidth, 12, comment));
        }

        x += labelWidth + 10;

        // 循環模式按鈕（65%），MaliLib 原生 ConfigOptionList 渲染
        int modeButtonWidth = (int) ((configWidth - 22) * 0.65);
        fi.dy.masa.malilib.gui.button.ConfigButtonOptionList modeButton = new fi.dy.masa.malilib.gui.button.ConfigButtonOptionList(
                x + 1, y, modeButtonWidth - 2, configHeight, entry.getLoopModeConfig());
        x += modeButtonWidth;

        // 模式切換快捷鍵按鈕（填充剩餘部分）
        int modeHotkeyWidth = configWidth - modeButtonWidth - 22;
        ConfigButtonKeybind modeHotkeyButton = new ConfigButtonKeybind(
                x, y, modeHotkeyWidth, 20, entry.getKeybind(), this.host);
        x += modeHotkeyWidth + 2;

        // 進階按鍵設定圖示
        this.addWidget(new WidgetKeybindSettings(
                x, y, 20, 20, entry.getKeybind(), entry.getName(),
                this.parent, this.host.getDialogHandler()));
        x += 22;

        // 重設按鈕
        ButtonGeneric resetButton = this.createResetButton(x, y, entry);

        // 事件綁定
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(
                entry, new CommandBindListModeResetter(modeButton, modeHotkeyButton), resetButton, null);

        this.host.addKeybindChangeListener(() -> {
            modeHotkeyButton.updateDisplayString();
            modeButton.updateDisplayString();
            resetButton.setEnabled(entry.isModified());
        });

        this.addButton(modeButton, this.host.getButtonPressListener());
        this.addButton(modeHotkeyButton, this.host.getButtonPressListener());
        this.addButton(resetButton, listenerReset);
    }

    // =====================================================================
    // ConfigBtnTrigger 渲染邏輯
    // 排版: [名稱標籤][ 執行 ][↺]
    // 灰色背景 + hover 淡藍色文字，左右各內縮 8px
    // =====================================================================

    /**
     * 建立 ConfigBtnTrigger 的所有 UI 元件
     * 包含：名稱標籤、hover 註解、自訂觸發按鈕（灰色背景 + hover 淡藍色）
     */
    protected void addBtnTriggerWidgets(int x, int y, int labelWidth, int configWidth, ConfigBtnTrigger entry) {
        int configHeight = 20;

        // 名稱標籤 + hover 註解
        String displayName = entry.getConfigGuiDisplayName();
        this.addLabel(x, y + 7, labelWidth, 8, 0xFFFFFFFF, displayName);

        String comment = entry.getComment();
        if (comment != null) {
            this.addWidget(new WidgetHoverInfo(x, y + 5, labelWidth, 12, comment));
        }

        x += labelWidth + 10;

        // 觸發按鈕 使用自訂灰色背景 + hover 淡藍色文字
        // 扣除重設按鈕空間（14px）以及設定按鈕空間（22px）
        int btnWidth = configWidth;
        TriggerButton triggerBtn = new TriggerButton(
                x + 4, y + 1, btnWidth + 22, configHeight, entry);
        triggerBtn.updateDisplayString();

        this.addButton(triggerBtn, this.host.getButtonPressListener());
    }

    // =====================================================================
    // 重設器 — 點擊重設按鈕時更新 UI 顯示
    // =====================================================================

    /**
     * ConfigCommandBind 重設器
     * 重設後同步更新：文字框內容 + 快捷鍵按鈕顯示
     */
    private static class CommandBindResetter extends ConfigResetterBase {
        private final IStringRepresentable commandConfig;
        private final GuiTextFieldGeneric textField;
        private final ConfigButtonKeybind keybindButton;

        CommandBindResetter(IStringRepresentable commandConfig, GuiTextFieldGeneric textField,
                ConfigButtonKeybind keybindButton) {
            this.commandConfig = commandConfig;
            this.textField = textField;
            this.keybindButton = keybindButton;
        }

        @Override
        public void resetConfigOption() {
            this.textField.setTextWrapper(this.commandConfig.getStringValue());
            this.keybindButton.updateDisplayString();
        }
    }

    /**
     * ConfigCommandBindList 重設器
     * 重設後同步更新：清單按鈕顯示 + 快捷鍵按鈕顯示
     */
    private static class CommandBindListResetter extends ConfigResetterBase {
        private final fi.dy.masa.malilib.config.options.ConfigStringList commandsConfig;
        private final CommandListButton listButton;
        private final ConfigButtonKeybind keybindButton;
        private final ConfigCommandBindList entry;

        CommandBindListResetter(fi.dy.masa.malilib.config.options.ConfigStringList commandsConfig,
                CommandListButton listButton, ConfigButtonKeybind keybindButton,
                ConfigCommandBindList entry) {
            this.commandsConfig = commandsConfig;
            this.listButton = listButton;
            this.keybindButton = keybindButton;
            this.entry = entry;
        }

        @Override
        public void resetConfigOption() {
            int count = this.commandsConfig.getStrings().size();
            this.listButton.setDisplayString(count + " commands");
            this.keybindButton.updateDisplayString();
        }
    }

    /**
     * ConfigCommandBindListMode 重設器
     * 重設後同步更新：模式按鈕顯示 + 模式切換快捷鍵按鈕顯示
     */
    private static class CommandBindListModeResetter extends ConfigResetterBase {
        private final fi.dy.masa.malilib.gui.button.ConfigButtonOptionList modeButton;
        private final ConfigButtonKeybind modeHotkeyButton;

        CommandBindListModeResetter(fi.dy.masa.malilib.gui.button.ConfigButtonOptionList modeButton,
                ConfigButtonKeybind modeHotkeyButton) {
            this.modeButton = modeButton;
            this.modeHotkeyButton = modeHotkeyButton;
        }

        @Override
        public void resetConfigOption() {
            this.modeButton.updateDisplayString();
            this.modeHotkeyButton.updateDisplayString();
        }
    }

    // =====================================================================
    // 變更檢測 — 用於判斷關閉 GUI 時是否需要儲存
    // =====================================================================

    @Override
    public boolean wasConfigModified() {
        // ── ConfigCommandBind 變更檢測 ──
        if (this.commandBindConfig != null) {
            boolean commandChanged = !this.initialCommandValue.equals(
                    this.commandBindConfig.getCommandConfig().getStringValue());
            boolean hotkeyChanged = !this.initialHotkeyValue.equals(
                    this.commandBindConfig.getKeybind().getStringValue());
            boolean settingsChanged = !this.initialKeybindSettings.equals(
                    this.commandBindConfig.getKeybind().getSettings());
            boolean textPending = this.textField != null &&
                    !this.initialCommandValue.equals(this.textField.textField().getTextWrapper());
            return commandChanged || hotkeyChanged || settingsChanged || textPending;
        }
        // ── ConfigCommandBindList 變更檢測 ──
        if (this.commandBindListConfig != null) {
            boolean listChanged = !this.initialListValue.equals(
                    this.commandBindListConfig.getCommandsConfig().getStrings().toString());
            boolean hotkeyChanged = !this.initialHotkeyValue.equals(
                    this.commandBindListConfig.getKeybind().getStringValue());
            boolean settingsChanged = !this.initialKeybindSettings.equals(
                    this.commandBindListConfig.getKeybind().getSettings());
            return listChanged || hotkeyChanged || settingsChanged;
        }
        return super.wasConfigModified();
    }

    // =====================================================================
    // 套用值 — 文字框失焦時將變更寫入 config
    // =====================================================================

    @Override
    public void applyNewValueToConfig() {
        // ── ConfigCommandBind: 將文字框內容寫入 command config ──
        if (this.commandBindConfig != null) {
            if (this.textField != null) {
                String newText = this.textField.textField().getTextWrapper();
                if (!newText.equals(this.lastAppliedValue)) {
                    this.commandBindConfig.getCommandConfig().setValueFromString(newText);
                    this.lastAppliedValue = newText;
                }
            }
            return;
        }
        // ── ConfigCommandBindList: 清單變更透過 GuiStringListEdit 直接寫入，不需額外處理 ──
        if (this.commandBindListConfig != null) {
            return;
        }
        super.applyNewValueToConfig();
    }
}
