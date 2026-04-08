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

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.JsonUtils;

/**
 * 命令綁定複合型 Config
 * 將命令文字 (ConfigString) 和快捷鍵 (ConfigHotkey) 包裝為單一 Config 物件
 * 在 MaliLib GUI 中佔用一行，顯示為 [文字輸入框][快捷鍵按鈕][⚙][↺]
 *
 * 命名遵循 MaliLib 的 ConfigXxx 慣例（如 ConfigHotkey, ConfigBoolean）
 */
public class ConfigCommandBind extends ConfigBase<ConfigCommandBind> implements IHotkey {

    private final ConfigString command;
    private final ConfigHotkey hotkey;
    private final int index;
    private String lastCommand;
    private String lastHotkey;

    /**
     * 建立命令綁定 Config
     *
     * @param index          序號（1-10），用於生成 name 和翻譯 key
     * @param defaultCommand 預設命令文字
     * @param defaultHotkey  預設快捷鍵字串（如 "B,H"）
     * @param comment        註解文字
     */
    public ConfigCommandBind(int index, String defaultCommand, String defaultHotkey, String comment) {
        super(ConfigType.HOTKEY, "command_bind_" + index, comment);
        this.index = index;
        this.command = new ConfigString("command_" + index, defaultCommand);
        this.hotkey = new ConfigHotkey("hotkey_" + index, defaultHotkey, KeybindSettings.PRESS_ALLOWEXTRA);

        // 設定快捷鍵回調：按下快捷鍵時執行命令
        this.hotkey.getKeybind().setCallback((action, key) -> {
            if (action == KeyAction.PRESS) {
                this.execute();
            }
            return true;
        });

        this.updateLastValues();
    }

    /**
     * 執行綁定的命令
     */
    public void execute() {
        me.blackrowtw.bk_tools.util.CommandSender.send(this.command.getStringValue());
    }

    /**
     * 取得命令文字 Config（供自訂 Widget 存取）
     */
    public ConfigString getCommandConfig() {
        return this.command;
    }

    /**
     * 取得快捷鍵 Config（供自訂 Widget 存取）
     */
    public ConfigHotkey getHotkeyConfig() {
        return this.hotkey;
    }

    /**
     * 取得序號
     */
    public int getIndex() {
        return this.index;
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
        this.updateLastValues();
        this.hotkey.setValueFromString(value);
    }

    @Override
    public boolean isModified() {
        return this.command.isModified() || this.hotkey.isModified();
    }

    @Override
    public boolean isModified(String newValue) {
        return this.hotkey.isModified(newValue);
    }

    @Override
    public void resetToDefault() {
        this.updateLastValues();
        this.command.resetToDefault();
        this.hotkey.resetToDefault();
    }

    // ── Dirty tracking ──

    @Override
    public boolean isDirty() {
        return this.command.isDirty() || this.hotkey.isDirty() || super.isDirty();
    }

    @Override
    public void markDirty() {
        this.command.markDirty();
        this.hotkey.markDirty();
        super.markDirty();
    }

    @Override
    public void markClean() {
        this.command.markClean();
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

    private void updateLastValues() {
        this.lastCommand = this.command.getStringValue();
        this.lastHotkey = this.hotkey.getStringValue();
    }

    // ── JSON 序列化 ──

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        final String oldCommand = this.command.getStringValue();
        final String oldHotkey = this.hotkey.getStringValue();

        try {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (JsonUtils.hasString(obj, "command")) {
                    this.command.setValueFromString(obj.get("command").getAsString());
                }

                if (JsonUtils.hasObject(obj, "hotkey")) {
                    this.hotkey.setValueFromJsonElement(obj.get("hotkey"));
                } else if (JsonUtils.hasString(obj, "hotkey")) {
                    // 相容舊格式：hotkey 為字串而非物件
                    this.hotkey.setValueFromString(obj.get("hotkey").getAsString());
                }
            } else if (element.isJsonPrimitive()) {
                // 向後相容：如果只有快捷鍵字串
                this.hotkey.setValueFromString(element.getAsString());
            }

            // 檢查是否有變更
            if (!oldCommand.equals(this.command.getStringValue()) ||
                    !oldHotkey.equals(this.hotkey.getStringValue()) ||
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
        obj.addProperty("command", this.command.getStringValue());
        obj.add("hotkey", this.hotkey.getAsJsonElement());
        return obj;
    }

    @Override
    public ConfigCommandBind apply(String translationPrefix) {
        // 套用翻譯前綴到內部的 command 和 hotkey
        this.command.apply(translationPrefix);
        this.hotkey.apply(translationPrefix);
        return super.apply(translationPrefix);
    }

    @Override
    public String getConfigGuiDisplayName() {
        return fi.dy.masa.malilib.util.StringUtils.translate(
                "bk_tools.config.commandBind.name.command_" + this.index);
    }
}
