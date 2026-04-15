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

// 設定的各項目定義，按功能分組
package me.blackrowtw.bk_tools.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.JsonUtils;

import me.blackrowtw.bk_tools.Reference;
import me.blackrowtw.bk_tools.config.widget.ConfigBtnTrigger;
import me.blackrowtw.bk_tools.config.widget.ConfigCommandBind;
import me.blackrowtw.bk_tools.config.widget.ConfigCommandBindList;
import me.blackrowtw.bk_tools.config.widget.ConfigCommandBindListMode;
import me.blackrowtw.bk_tools.config.widget.ButtonLoopMode;
import me.blackrowtw.bk_tools.tools.hello.ShowHelloActions;

public class Configs implements IConfigHandler {

        // translation key 前綴常數，集中定義方便維護
        private static final String KEY_GENERIC = Reference.MOD_ID + ".config.generic";
        private static final String KEY_CN2TW = Reference.MOD_ID + ".config.cn2tw";
        private static final String KEY_COMMAND_BIND = Reference.MOD_ID + ".config.commandBind";
        private static final String KEY_COMMAND_BIND_LIST = Reference.MOD_ID + ".config.commandBindList";
        private static final String KEY_TEST = Reference.MOD_ID + ".config.test";

        // ── Generic：框架層快捷鍵 ────────────────────────────────
        public static class Generic {
                public static final ConfigHotkey OPEN_GUI = new ConfigHotkey(
                                "openGuiConfigs", "B,C", KeybindSettings.PRESS_ALLOWEXTRA)
                                .apply(KEY_GENERIC);

                // 手動 debug 模式開關
                // IDE 環境自動開啟，正式環境可手動切換
                public static final ConfigBoolean FORCE_DEBUG_MODE = new ConfigBoolean(
                                "force_debug_mode", false)
                                .apply(KEY_GENERIC);

                // OPTIONS 包含所有型別，供 load/save 使用
                public static final List<IConfigBase> OPTIONS = ImmutableList.of(
                                OPEN_GUI,
                                FORCE_DEBUG_MODE);
        }

        // ── CommandBind：命令綁定快捷鍵功能 ─────────────────────────
        public static class CommandBind {

                public static final ConfigCommandBind COMMAND_BIND_1 = new ConfigCommandBind(
                                1, "/bk hello", "", "")
                                .apply(KEY_COMMAND_BIND);

                public static final ConfigCommandBind COMMAND_BIND_2 = new ConfigCommandBind(
                                2, "/gamemode survival", "", "")
                                .apply(KEY_COMMAND_BIND);

                public static final ConfigCommandBind COMMAND_BIND_3 = new ConfigCommandBind(
                                3, "/gamemode creative", "", "")
                                .apply(KEY_COMMAND_BIND);

                public static final ConfigCommandBind COMMAND_BIND_4 = new ConfigCommandBind(
                                4, "/gamemode spectator", "", "")
                                .apply(KEY_COMMAND_BIND);

                public static final ConfigCommandBind COMMAND_BIND_5 = new ConfigCommandBind(
                                5, "/time set day", "", "")
                                .apply(KEY_COMMAND_BIND);

                public static final ConfigCommandBind COMMAND_BIND_6 = new ConfigCommandBind(
                                6, "/time set midnight", "", "")
                                .apply(KEY_COMMAND_BIND);

                public static final ConfigCommandBind COMMAND_BIND_7 = new ConfigCommandBind(
                                7, "/weather clear 7200s", "", "")
                                .apply(KEY_COMMAND_BIND);

                public static final ConfigCommandBind COMMAND_BIND_8 = new ConfigCommandBind(
                                8, "/tick step 1", "", "")
                                .apply(KEY_COMMAND_BIND);

                public static final ConfigCommandBind COMMAND_BIND_9 = new ConfigCommandBind(
                                9, "/tick step 2", "", "")
                                .apply(KEY_COMMAND_BIND);

                // OPTIONS 供 GuiConfigs 顯示
                public static final List<IConfigBase> OPTIONS = ImmutableList.of(
                                COMMAND_BIND_1, COMMAND_BIND_2, COMMAND_BIND_3,
                                COMMAND_BIND_4, COMMAND_BIND_5, COMMAND_BIND_6,
                                COMMAND_BIND_7, COMMAND_BIND_8, COMMAND_BIND_9);
        }

        // ── commandBindList：命令清單循環功能 ─────────────────────
        public static class commandBindList {

                /**
                 * 命令清單綁定（List 1）：命令清單 + 執行快捷鍵
                 */
                public static final ConfigCommandBindList COMMAND_BIND_LIST_1 = new ConfigCommandBindList(
                                "list_1",
                                java.util.List.of("/tick freeze",
                                                "/tick unfreeze"),
                                "",
                                "")
                                .apply(KEY_COMMAND_BIND_LIST);

                /**
                 * 命令清單模式切換（List 1 Mode）：循環模式 + 模式切換快捷鍵
                 */
                public static final ConfigCommandBindListMode COMMAND_BIND_LIST_1_MODE = new ConfigCommandBindListMode(
                                "list_1_mode",
                                COMMAND_BIND_LIST_1,
                                ButtonLoopMode.SEQUENTIAL,
                                "",
                                "")
                                .apply(KEY_COMMAND_BIND_LIST);

                /**
                 * 命令清單綁定（List 2）：命令清單 + 執行快捷鍵
                 */
                public static final ConfigCommandBindList COMMAND_BIND_LIST_2 = new ConfigCommandBindList(
                                "list_2",
                                java.util.List.of("/me =⩌⩊⩌= < CMB List #2 - Fix mode"),
                                "",
                                "")
                                .apply(KEY_COMMAND_BIND_LIST);

                /**
                 * 命令清單模式切換（List 2 Mode）：循環模式 + 模式切換快捷鍵
                 */
                public static final ConfigCommandBindListMode COMMAND_BIND_LIST_2_MODE = new ConfigCommandBindListMode(
                                "list_2_mode",
                                COMMAND_BIND_LIST_2,
                                ButtonLoopMode.FIXED,
                                "",
                                "")
                                .apply(KEY_COMMAND_BIND_LIST);

                /**
                 * 命令清單綁定（List 3）：命令清單 + 執行快捷鍵
                 */
                public static final ConfigCommandBindList COMMAND_BIND_LIST_3 = new ConfigCommandBindList(
                                "list_3",
                                java.util.List.of("/me =⩌⩊⩌= < ⚀",
                                                "/me =⩌⩊⩌= < ⚁",
                                                "/me =⩌⩊⩌= < ⚂",
                                                "/me =⩌⩊⩌= < ⚃",
                                                "/me =⩌⩊⩌= < ⚄",
                                                "/me =⩌⩊⩌= < ⚅"),
                                "",
                                "")
                                .apply(KEY_COMMAND_BIND_LIST);

                /**
                 * 命令清單模式切換（List 3 Mode）：循環模式 + 模式切換快捷鍵
                 */
                public static final ConfigCommandBindListMode COMMAND_BIND_LIST_3_MODE = new ConfigCommandBindListMode(
                                "list_3_mode",
                                COMMAND_BIND_LIST_3,
                                ButtonLoopMode.RANDOM,
                                "",
                                "")
                                .apply(KEY_COMMAND_BIND_LIST);

                static {
                        COMMAND_BIND_LIST_1.setModeConfig(COMMAND_BIND_LIST_1_MODE);
                        COMMAND_BIND_LIST_2.setModeConfig(COMMAND_BIND_LIST_2_MODE);
                        COMMAND_BIND_LIST_3.setModeConfig(COMMAND_BIND_LIST_3_MODE);
                }

                public static final List<IConfigBase> OPTIONS = ImmutableList.of(
                                COMMAND_BIND_LIST_1,
                                COMMAND_BIND_LIST_1_MODE,
                                COMMAND_BIND_LIST_2,
                                COMMAND_BIND_LIST_2_MODE,
                                COMMAND_BIND_LIST_3,
                                COMMAND_BIND_LIST_3_MODE);
        }

        // ── Cn2Tw：簡繁 fallback 功能 ────────────────────────────
        public static class Cn2Tw {

                /**
                 * 開關：啟用 zh_cn → zh_tw fallback
                 * 開啟時立即非同步刷新 fallbackMap（由 KeyCallbackRegistry 監聽）
                 * 關閉時立即清空 fallbackMap
                 */
                public static final ConfigBoolean CN2TW_ENABLE_FALLBACK = new ConfigBoolean(
                                "cn2tw_enable_fallback", false)
                                .apply(KEY_CN2TW);

                /**
                 * 按鈕 1：刷新 Fallback Map
                 * 非同步重新讀取語言檔案，更新記憶體中的 fallbackMap
                 * 不輸出檔案，也不重載整包資源，是三者中最輕量的操作
                 * 冷卻：60 ticks（3 秒）
                 */
                public static final ConfigBtnTrigger CN2TW_REFRESH_MAP = new ConfigBtnTrigger(
                                "cn2tw_refresh_map",
                                "",
                                () -> me.blackrowtw.bk_tools.tools.cn2tw.Cn2TwFallbackManager.getInstance()
                                                .refreshMap(),
                                "bk_tools.config.btnTrigger.cn2tw_refresh_map",
                                60).apply(KEY_CN2TW);

                /**
                 * 按鈕 2：輸出 Lang Map 到 JSON 檔案
                 * 先刷新 fallbackMap，再輸出到 config/BKTools_DEBUG/cn2tw_fallback_dump.json
                 * 輸出格式：按 namespace 分組的巢狀 JSON
                 * 冷卻：60 ticks（3 秒）
                 */
                public static final ConfigBtnTrigger CN2TW_DUMP_LANG_FILE = new ConfigBtnTrigger(
                                "cn2tw_dump_lang_file",
                                "",
                                () -> me.blackrowtw.bk_tools.tools.cn2tw.Cn2TwFallbackManager.getInstance()
                                                .dumpLangFile(),
                                "bk_tools.config.btnTrigger.cn2tw_dump_lang_file",
                                60).apply(KEY_CN2TW);

                /**
                 * 按鈕 3：重載資源包（等同 F3+T）
                 * 觸發後 Cn2TwReloadListener 會自動執行 prepare + apply
                 * 重載完成後 CN2TW fallbackMap 會自動更新
                 * 冷卻：200 ticks（10 秒），避免頻繁重載
                 */
                public static final ConfigBtnTrigger CN2TW_RELOAD_RESOURCES = new ConfigBtnTrigger(
                                "cn2tw_reload_resources",
                                "",
                                () -> me.blackrowtw.bk_tools.tools.cn2tw.Cn2TwFallbackManager.getInstance()
                                                .reloadResources(),
                                "bk_tools.config.btnTrigger.cn2tw_reload_resources",
                                200).apply(KEY_CN2TW);

                // OPTIONS 供 GuiConfigs 顯示用（包含所有型別）
                public static final List<IConfigBase> OPTIONS = ImmutableList.of(
                                CN2TW_ENABLE_FALLBACK,
                                CN2TW_REFRESH_MAP,
                                CN2TW_DUMP_LANG_FILE,
                                CN2TW_RELOAD_RESOURCES);
        }

        // ── Test：測試功能 ────────────────────────────────────────
        public static class Test {
                // public static final ConfigHotkey BKTOOLS_SAY_HELLO = new ConfigHotkey(
                // "bktools_say_hello", "B,H", KeybindSettings.PRESS_ALLOWEXTRA)
                // .apply(KEY_TEST);

                /**
                 * 觸發式按鈕：點擊後執行 Hello World 測試功能
                 * 不需要綁定快捷鍵，直接在 GUI 中點擊
                 * 預設 20 ticks 冷卻
                 */
                public static final ConfigBtnTrigger BKTOOLS_SAY_HELLO_BUTTON = new ConfigBtnTrigger(
                                "bktools_say_hello_button",
                                "",
                                ShowHelloActions::executeHello,
                                "bk_tools.config.btnTrigger.hello").apply(KEY_TEST);

                public static final List<IConfigBase> OPTIONS = ImmutableList.of(
                                BKTOOLS_SAY_HELLO_BUTTON);
        }

        // ── 設定檔讀寫 ────────────────────────────────────────────
        private static final Path CONFIG_PATH = Reference.CONFIG_DIR.resolve(Reference.MOD_ID + ".json");

        // ── 快捷鍵匯總清單（供 InputHandler 使用）────────────────────────
        public static final List<IHotkey> ALL_HOTKEYS;

        static {
                List<IHotkey> hotkeys = new ArrayList<>();

                // 所有 OPTIONS 清單統一列在這裡，自動過濾出 IHotkey
                Stream.of(
                                Generic.OPTIONS,
                                Cn2Tw.OPTIONS,
                                CommandBind.OPTIONS,
                                commandBindList.OPTIONS,
                                Test.OPTIONS
                // 未來新增：_New_.OPTIONS
                ).flatMap(List::stream)
                                .filter(c -> c instanceof IHotkey)
                                .map(c -> (IHotkey) c)
                                .forEach(hotkeys::add);

                ALL_HOTKEYS = ImmutableList.copyOf(hotkeys);
        }

        @Override
        public void load() {
                JsonElement element = JsonUtils.parseJsonFile(CONFIG_PATH.toFile());
                if (element != null && element.isJsonObject()) {
                        JsonObject root = element.getAsJsonObject();
                        ConfigUtils.readConfigBase(root, "generic", Generic.OPTIONS);
                        ConfigUtils.readConfigBase(root, "cn2tw", Cn2Tw.OPTIONS);
                        ConfigUtils.readConfigBase(root, "test", Test.OPTIONS);
                        ConfigUtils.readConfigBase(root, "commandBind", CommandBind.OPTIONS);
                        ConfigUtils.readConfigBase(root, "commandBindList", commandBindList.OPTIONS);
                        // 未來新增：ConfigUtils.readXxxx(root, "_new_", _New_.OPTIONS);
                }
        }

        @Override
        public void save() {
                JsonObject root = new JsonObject();
                ConfigUtils.writeConfigBase(root, "generic", Generic.OPTIONS);
                ConfigUtils.writeConfigBase(root, "cn2tw", Cn2Tw.OPTIONS);
                ConfigUtils.writeConfigBase(root, "test", Test.OPTIONS);
                ConfigUtils.writeConfigBase(root, "commandBind", CommandBind.OPTIONS);
                ConfigUtils.writeConfigBase(root, "commandBindList", commandBindList.OPTIONS);
                // 未來新增：ConfigUtils.writeXxxx(root, "_new_", _New_.OPTIONS);
                JsonUtils.writeJsonToFile(root, CONFIG_PATH.toFile());
        }
}
