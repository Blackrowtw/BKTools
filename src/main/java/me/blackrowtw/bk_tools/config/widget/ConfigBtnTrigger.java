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

package me.blackrowtw.bk_tools.config.widget;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigOptionList;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.IStringRepresentable;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;

/**
 * 通用觸發按鈕 Config 類型
 * 在 MaliLib Config GUI 中顯示為按鈕，點擊時觸發 Runnable 回調
 * 不維持任何狀態，純粹一次性觸發
 *
 * 內建冷卻機制，預設 20 ticks（1 秒），可自訂
 *
 * 使用範例：
 * 
 * <pre>
 * // 預設 20 ticks 冷卻，預設顯示名稱 "執行"
 * public static final ConfigBtnTrigger MY_ACTION = new ConfigBtnTrigger(
 *         "my_action",
 *         "Do Something",
 *         () -> MyFeature.execute()).apply(KEY_PREFIX);
 *
 * // 自訂冷卻 60 ticks（3 秒）
 * public static final ConfigBtnTrigger SLOW_ACTION = new ConfigBtnTrigger(
 *         "slow_action",
 *         "Do Something Slow",
 *         () -> MyFeature.execute(),
 *         60).apply(KEY_PREFIX);
 *
 * // 自訂顯示名稱翻譯 key
 * public static final ConfigBtnTrigger RELOAD_ACTION = new ConfigBtnTrigger(
 *         "reload_action",
 *         "Reload configurations",
 *         () -> Configs.reload(),
 *         "bk_tools.config.btnTrigger.reload").apply(KEY_PREFIX);
 * </pre>
 */
public class ConfigBtnTrigger extends ConfigBase<ConfigBtnTrigger>
        implements IConfigOptionList, IStringRepresentable {

    /**
     * 預設顯示名稱翻譯 key
     */
    private static final String DEFAULT_TRANSLATION_KEY = "bk_tools.config.btnTrigger.execute";

    /**
     * 觸發按鈕的內部狀態
     * READY：可點擊
     * COOLDOWN：冷卻中，點擊無效
     */
    public enum BtnTriggerState {
        READY("ready"),
        COOLDOWN("cooldown");

        private final String name;

        BtnTriggerState(String name) {
            this.name = name;
        }

        public String getStringValue() {
            return this.name;
        }
    }

    private final Runnable action;
    private final int cooldownTicks;
    private final long cooldownMillis;
    private long lastTriggerTime;
    private final String translationKey;

    /**
     * 建立觸發按鈕，預設顯示名稱 + 預設 20 ticks 冷卻
     */
    public ConfigBtnTrigger(String name, String comment, Runnable action) {
        this(name, comment, action, 20);
    }

    /**
     * 建立觸發按鈕，預設顯示名稱 + 自訂冷卻時間
     *
     * @param cooldownTicks 冷卻刻數（20 ticks = 1 秒）
     */
    public ConfigBtnTrigger(String name, String comment, Runnable action, int cooldownTicks) {
        this(name, comment, action, DEFAULT_TRANSLATION_KEY, cooldownTicks);
    }

    /**
     * 建立觸發按鈕，自訂顯示名稱翻譯 key + 預設 20 ticks 冷卻
     *
     * @param translationKey 顯示名稱的翻譯 key（如 "bk_tools.config.btnTrigger.reload"）
     */
    public ConfigBtnTrigger(String name, String comment, Runnable action, String translationKey) {
        this(name, comment, action, translationKey, 20);
    }

    /**
     * 建立觸發按鈕，自訂顯示名稱翻譯 key + 自訂冷卻時間
     *
     * @param translationKey 顯示名稱的翻譯 key
     * @param cooldownTicks  冷卻刻數（20 ticks = 1 秒）
     */
    public ConfigBtnTrigger(String name, String comment, Runnable action, String translationKey, int cooldownTicks) {
        super(ConfigType.OPTION_LIST, name, comment);
        this.action = action;
        this.cooldownTicks = cooldownTicks;
        this.cooldownMillis = cooldownTicks * 50L;
        this.lastTriggerTime = 0;
        this.translationKey = translationKey;
    }

    /**
     * 取得當前狀態：冷卻中回傳 COOLDOWN，否則回傳 READY
     */
    private BtnTriggerState getCurrentState() {
        long now = System.currentTimeMillis();
        if (now - this.lastTriggerTime < this.cooldownMillis) {
            return BtnTriggerState.COOLDOWN;
        }
        return BtnTriggerState.READY;
    }

    /**
     * 建立顯示用 Entry，持有當前實例的 translationKey
     */
    private IConfigOptionListEntry createDisplayEntry(BtnTriggerState state) {
        return new IConfigOptionListEntry() {
            @Override
            public String getStringValue() {
                return state.getStringValue();
            }

            @Override
            public String getDisplayName() {
                return StringUtils.translate(ConfigBtnTrigger.this.translationKey);
            }

            @Override
            public IConfigOptionListEntry cycle(boolean forward) {
                return this;
            }

            @Override
            public IConfigOptionListEntry fromString(String value) {
                return createDisplayEntry(BtnTriggerState.READY);
            }
        };
    }

    @Override
    public IConfigOptionListEntry getOptionListValue() {
        return this.createDisplayEntry(this.getCurrentState());
    }

    @Override
    public IConfigOptionListEntry getDefaultOptionListValue() {
        return this.createDisplayEntry(BtnTriggerState.READY);
    }

    @Override
    public void setOptionListValue(IConfigOptionListEntry value) {
        if (this.getCurrentState() == BtnTriggerState.COOLDOWN) {
            int remaining = this.getRemainingCooldownTicks();
            String tickText = "§7§o(" + remaining + " tick)§r";
            InfoUtils.printActionbarMessage("bk_tools.message.actionBarMessage.cooldown_wait", tickText);
            return;
        }
        this.lastTriggerTime = System.currentTimeMillis();
        this.action.run();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isModified(String newValue) {
        return false;
    }

    @Override
    public void resetToDefault() {
    }

    @Override
    public String getStringValue() {
        return this.getCurrentState().getStringValue();
    }

    @Override
    public String getDefaultStringValue() {
        return BtnTriggerState.READY.getStringValue();
    }

    @Override
    public void setValueFromString(String value) {
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive("");
    }

    /**
     * 取得冷卻刻數
     */
    public int getCooldownTicks() {
        return this.cooldownTicks;
    }

    /**
     * 取得剩餘冷卻毫秒數（0 表示已結束）
     */
    public long getRemainingCooldownMillis() {
        long elapsed = System.currentTimeMillis() - this.lastTriggerTime;
        long remaining = this.cooldownMillis - elapsed;
        return remaining > 0 ? remaining : 0;
    }

    /**
     * 取得剩餘冷卻刻數（0 表示已結束）
     */
    public int getRemainingCooldownTicks() {
        return (int) (this.getRemainingCooldownMillis() / 50L);
    }
}
