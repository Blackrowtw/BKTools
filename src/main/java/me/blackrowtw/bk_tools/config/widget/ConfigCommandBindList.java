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

package me.blackrowtw.bk_tools.config.widget;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.JsonUtils;

/**
 * 命令清單綁定複合型 Config（Row 1）
 * 將命令清單 (ConfigStringList) 和執行快捷鍵 (ConfigHotkey) 包裝為單一 Config 物件
 * 循環模式由 ConfigCommandBindListMode（Row 2）管理，透過 sibling 引用讀取
 *
 * 支援三種循環模式：
 * - SEQUENTIAL: 順序執行，每次觸發下一條，到末尾循環。索引儲存到 config
 * - RANDOM: 每次隨機選一條執行
 * - FIXED: 永遠執行 currentIndex 指向的命令，不輪調
 */
public class ConfigCommandBindList extends ConfigBase<ConfigCommandBindList> implements IHotkey {

    private final ConfigStringList commands;
    private final ConfigHotkey hotkey;
    private ConfigCommandBindListMode modeConfig;
    private int currentIndex;

    /**
     * 建立命令清單綁定 Config
     *
     * @param name            Config 名稱
     * @param defaultCommands  預設命令清單
     * @param defaultHotkey    預設快捷鍵字串（如 "B,H"）
     * @param comment          註解文字
     */
    public ConfigCommandBindList(String name, List<String> defaultCommands, String defaultHotkey, String comment) {
        super(ConfigType.HOTKEY, name, comment);
        this.commands = new ConfigStringList(
                name + "_commands",
                com.google.common.collect.ImmutableList.copyOf(defaultCommands),
                comment);
        this.hotkey = new ConfigHotkey(name + "_hotkey", defaultHotkey, KeybindSettings.PRESS_ALLOWEXTRA);
        this.currentIndex = 0;

        // 設定快捷鍵回調：按下快捷鍵時執行下一條命令
        this.hotkey.getKeybind().setCallback((action, key) -> {
            if (action == KeyAction.PRESS) {
                this.execute();
            }
            return true;
        });
    }

    /**
     * 設定 sibling 引用（由 Configs 在建構後呼叫）
     */
    public void setModeConfig(ConfigCommandBindListMode modeConfig) {
        this.modeConfig = modeConfig;
    }

    /**
     * 取得 sibling mode config（供外部設定引用）
     */
    public ConfigCommandBindListMode getModeConfig() {
        return this.modeConfig;
    }

    /**
     * 執行下一條命令
     * 根據 sibling modeConfig 的 loopMode 決定執行行為
     */
    public void execute() {
        List<String> list = this.commands.getStrings();
        if (list.isEmpty()) return;

        // 邊界保護：currentIndex 超出範圍時重置
        if (this.currentIndex >= list.size()) {
            this.currentIndex = 0;
        }

        LoopMode mode = (LoopMode) this.modeConfig.getLoopModeConfig().getOptionListValue();

        String cmd;
        switch (mode) {
            case RANDOM:
                cmd = list.get(ThreadLocalRandom.current().nextInt(list.size()));
                break;
            case FIXED:
                // 不輪調，永遠執行 currentIndex 指向的命令
                cmd = list.get(this.currentIndex);
                break;
            case SEQUENTIAL:
            default:
                cmd = list.get(this.currentIndex);
                this.currentIndex = (this.currentIndex + 1) % list.size();
                this.markDirty();
                break;
        }

        me.blackrowtw.bk_tools.util.CommandSender.send(cmd);
    }

    /**
     * 取得命令清單 Config（供自訂 Widget 存取）
     */
    public ConfigStringList getCommandsConfig() {
        return this.commands;
    }

    /**
     * 取得快捷鍵 Config（供自訂 Widget 存取）
     */
    public ConfigHotkey getHotkeyConfig() {
        return this.hotkey;
    }

    /**
     * 取得當前執行索引（供 UI 顯示或調試用）
     */
    public int getCurrentIndex() {
        return this.currentIndex;
    }

    // ── IHotkey 介面實作 ──

    @Override
    public IKeybind getKeybind() {
        return this.hotkey.getKeybind();
    }

    @Override
    public String getStringValue() {
        return this.hotkey.getStringValue();
    }

    @Override
    public String getDefaultStringValue() {
        return this.hotkey.getDefaultStringValue();
    }

    @Override
    public void setValueFromString(String value) {
        this.hotkey.setValueFromString(value);
    }

    @Override
    public boolean isModified() {
        return this.commands.isModified() || this.hotkey.isModified();
    }

    @Override
    public boolean isModified(String newValue) {
        return this.hotkey.isModified(newValue);
    }

    @Override
    public void resetToDefault() {
        this.commands.resetToDefault();
        this.hotkey.resetToDefault();
        this.currentIndex = 0;
    }

    // ── Dirty tracking ──

    @Override
    public boolean isDirty() {
        return this.commands.isModified() || this.hotkey.isDirty() || super.isDirty();
    }

    @Override
    public void markDirty() {
        this.hotkey.markDirty();
        super.markDirty();
    }

    @Override
    public void markClean() {
        this.hotkey.markClean();
        super.markClean();
    }

    @Override
    public void checkIfClean() {
        if (this.isDirty()) {
            this.markClean();
            this.onValueChanged();
        }
    }

    // ── JSON 序列化 ──

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        final List<String> oldCommands = List.copyOf(this.commands.getStrings());
        final String oldHotkey = this.hotkey.getStringValue();
        final int oldIndex = this.currentIndex;

        try {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (JsonUtils.hasArray(obj, "commands")) {
                    this.commands.setValueFromJsonElement(obj.get("commands"));
                }

                if (JsonUtils.hasObject(obj, "hotkey")) {
                    this.hotkey.setValueFromJsonElement(obj.get("hotkey"));
                } else if (JsonUtils.hasString(obj, "hotkey")) {
                    this.hotkey.setValueFromString(obj.get("hotkey").getAsString());
                }

                if (JsonUtils.hasInteger(obj, "currentIndex")) {
                    this.currentIndex = obj.get("currentIndex").getAsInt();
                } else {
                    this.currentIndex = 0;
                }
            } else if (element.isJsonPrimitive()) {
                this.hotkey.setValueFromString(element.getAsString());
                this.currentIndex = 0;
            }

            // 檢查是否有變更
            if (!oldCommands.equals(this.commands.getStrings()) ||
                !oldHotkey.equals(this.hotkey.getStringValue()) ||
                oldIndex != this.currentIndex ||
                this.isDirty()) {
                this.markClean();
                this.onValueChanged();
            }
        } catch (Exception e) {
            MaLiLib.LOGGER.warn(
                "Failed to set config value for '{}' from JSON: {}", this.getName(), element, e);
        }
    }

    @Override
    public JsonElement getAsJsonElement() {
        JsonObject obj = new JsonObject();
        obj.add("commands", this.commands.getAsJsonElement());
        obj.add("hotkey", this.hotkey.getAsJsonElement());
        obj.addProperty("currentIndex", this.currentIndex);
        return obj;
    }

    @Override
    public ConfigCommandBindList apply(String translationPrefix) {
        this.commands.apply(translationPrefix);
        this.hotkey.apply(translationPrefix);
        return super.apply(translationPrefix);
    }

    @Override
    public String getConfigGuiDisplayName() {
        return fi.dy.masa.malilib.util.StringUtils.translate(
                "bk_tools.config.commandBindList.name." + this.getName());
    }
}
