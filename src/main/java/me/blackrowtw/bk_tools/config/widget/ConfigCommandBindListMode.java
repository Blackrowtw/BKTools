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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

/**
 * 命令清單循環模式切換 Config
 * 包含循環模式選擇和模式切換快捷鍵
 * 持有對父 ConfigCommandBindList 的引用，共享狀態
 *
 * GUI 排版：[循環模式][Sequential ▼][模式切換快捷鍵][⚙][↺]
 */
public class ConfigCommandBindListMode extends ConfigBase<ConfigCommandBindListMode> implements IHotkey {

    private final ConfigCommandBindList parent;
    private final ConfigOptionList loopMode;
    private final ConfigHotkey modeHotkey;

    /**
     * 建立模式切換 Config
     *
     * @param name          Config 名稱
     * @param parent        父 ConfigCommandBindList 引用
     * @param defaultMode   預設循環模式
     * @param defaultHotkey 預設模式切換快捷鍵
     * @param comment       註解文字
     */
    public ConfigCommandBindListMode(String name, ConfigCommandBindList parent,
            LoopMode defaultMode, String defaultHotkey, String comment) {
        super(ConfigType.HOTKEY, name, comment);
        this.parent = parent;
        this.loopMode = new ConfigOptionList(name + "_loopMode", defaultMode, comment);
        this.modeHotkey = new ConfigHotkey(name + "_modeHotkey", defaultHotkey, KeybindSettings.PRESS_ALLOWEXTRA);

        // 設定模式切換快捷鍵回調：按下後循環切換模式
        this.modeHotkey.getKeybind().setCallback((action, key) -> {
            if (action == KeyAction.PRESS) {
                this.cycleMode();
            }
            return true;
        });
    }

    /**
     * 循環切換模式：Sequential → Random → Fixed → Sequential...
     */
    private void cycleMode() {
        LoopMode current = (LoopMode) this.loopMode.getOptionListValue();
        LoopMode next = (LoopMode) current.cycle(true);
        this.loopMode.setOptionListValue(next);
    }

    /**
     * 取得循環模式 Config（供父 config 讀取）
     */
    public ConfigOptionList getLoopModeConfig() {
        return this.loopMode;
    }

    /**
     * 取得模式切換快捷鍵 Config（供自訂 Widget 存取）
     */
    public ConfigHotkey getModeHotkeyConfig() {
        return this.modeHotkey;
    }

    // ── IHotkey 介面實作（代理到 modeHotkey）──

    @Override
    public IKeybind getKeybind() {
        return this.modeHotkey.getKeybind();
    }

    @Override
    public String getStringValue() {
        return this.modeHotkey.getStringValue();
    }

    @Override
    public String getDefaultStringValue() {
        return this.modeHotkey.getDefaultStringValue();
    }

    @Override
    public void setValueFromString(String value) {
        this.modeHotkey.setValueFromString(value);
    }

    @Override
    public boolean isModified() {
        return this.loopMode.isModified() || this.modeHotkey.isModified();
    }

    @Override
    public boolean isModified(String newValue) {
        return this.modeHotkey.isModified(newValue);
    }

    @Override
    public void resetToDefault() {
        this.loopMode.resetToDefault();
        this.modeHotkey.resetToDefault();
    }

    // ── Dirty tracking ──

    @Override
    public boolean isDirty() {
        return this.loopMode.isModified() || this.modeHotkey.isDirty() || super.isDirty();
    }

    @Override
    public void markDirty() {
        this.modeHotkey.markDirty();
        super.markDirty();
    }

    @Override
    public void markClean() {
        this.modeHotkey.markClean();
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
        try {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (obj.has("loopMode")) {
                    this.loopMode.setValueFromString(obj.get("loopMode").getAsString());
                }

                if (obj.has("modeHotkey")) {
                    this.modeHotkey.setValueFromJsonElement(obj.get("modeHotkey"));
                } else if (obj.has("modeHotkey") && obj.get("modeHotkey").isJsonPrimitive()) {
                    this.modeHotkey.setValueFromString(obj.get("modeHotkey").getAsString());
                }
            }
        } catch (Exception e) {
            fi.dy.masa.malilib.MaLiLib.LOGGER.warn(
                "Failed to set config value for '{}' from JSON: {}", this.getName(), element);
        }
    }

    @Override
    public JsonElement getAsJsonElement() {
        JsonObject obj = new JsonObject();
        obj.addProperty("loopMode", this.loopMode.getStringValue());
        obj.add("modeHotkey", this.modeHotkey.getAsJsonElement());
        return obj;
    }

    @Override
    public ConfigCommandBindListMode apply(String translationPrefix) {
        this.loopMode.apply(translationPrefix);
        this.modeHotkey.apply(translationPrefix);
        return super.apply(translationPrefix);
    }

    @Override
    public String getConfigGuiDisplayName() {
        return fi.dy.masa.malilib.util.StringUtils.translate(
                "bk_tools.config.commandBindListMode.name." + this.getName());
    }
}
