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

package me.blackrowtw.bk_tools.tools.cn2tw;

import net.minecraft.ChatFormatting;
import net.minecraft.server.packs.resources.ResourceManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import me.blackrowtw.bk_tools.Reference;
import me.blackrowtw.bk_tools.config.Configs;
import me.blackrowtw.bk_tools.util.DebugLogger;
import me.blackrowtw.bk_tools.util.SendChatMessage;

public class Cn2TwFallbackManager {

    // ── 路徑常數 ──────────────────────────────────────────
    private static final Path OUTPUT_DIR = Reference.CONFIG_DIR.resolve("BKTools_DEBUG");
    private static final Path OUTPUT_FILE = OUTPUT_DIR.resolve("cn2tw_fallback_dump.json");

    // ── 單例 ──────────────────────────────────────────────
    private static final Cn2TwFallbackManager INSTANCE = new Cn2TwFallbackManager();

    public static Cn2TwFallbackManager getInstance() {
        return INSTANCE;
    }

    private Cn2TwFallbackManager() {
    }

    // ── fallback 結果存放處 (執行緒安全) ─────────────────────
    // key = translation key（例如 "some_mod.item.foo"）
    // value = zh_cn 原文（未來接繁簡轉換器後替換）
    private final ConcurrentHashMap<String, String> fallbackMap = new ConcurrentHashMap<>();

    // ── 公開查詢（給 Mixin 呼叫）─────────────────────────
    public boolean contains(String key) {
        return fallbackMap.containsKey(key);
    }

    public String get(String key) {
        return fallbackMap.get(key);
    }

    // ── prepare（背景執行緒，只讀資源，不判斷開關）─────────
    public Map<String, String> prepare(ResourceManager resourceManager) {

        DebugLogger.info("Cn2TwFallback", "ResourceManager: %s",
                resourceManager.getClass().getName());

        Map<String, String> zhCnAll = loadLanguageFile(resourceManager, "zh_cn");
        Map<String, String> zhTwAll = loadLanguageFile(resourceManager, "zh_tw");

        DebugLogger.info("Cn2TwFallback", "zh_cn 共 %d 條，zh_tw 共 %d 條",
                zhCnAll.size(), zhTwAll.size());

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : zhCnAll.entrySet()) {
            if (!zhTwAll.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    // ── apply（主執行緒，此時 load() 已完成，開關值正確）────
    public void apply(Map<String, String> preparedData) {
        fallbackMap.clear();

        boolean enabled = Configs.Cn2Tw.CN2TW_ENABLE_FALLBACK.getBooleanValue();
        if (!enabled) {
            DebugLogger.always("Cn2TwFallback", "功能已關閉，跳過套用");
            return;
        }

        fallbackMap.putAll(preparedData);

        if (!fallbackMap.isEmpty()) {
            DebugLogger.always("Cn2TwFallback", "載入完成，補充了 %d 條翻譯", fallbackMap.size());
        } else if (Reference.isDebugMode()) {
            DebugLogger.info("Cn2TwFallback", "apply 完成，fallbackMap 為空");
        }
    }

    // ── 檢查功能是否啟用 ─────────────────────────────────────
    public boolean isEnabled() {
        return Configs.Cn2Tw.CN2TW_ENABLE_FALLBACK.getBooleanValue();
    }

    // ── dumpLangFile（供 GUI 按鈕呼叫）──────────────────────
    public void dumpLangFile() {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft == null || minecraft.getResourceManager() == null) {
            DebugLogger.alwaysWarn("Cn2TwFallback", "無法取得 ResourceManager");
            return;
        }

        // 先刷新資源
        refreshSync(minecraft.getResourceManager());

        // 根據刷新結果輸出提示
        boolean enabled = isEnabled();
        int count = fallbackMap.size();

        if (enabled && count > 0) {
            SendChatMessage.of(String.format("[BKTools] 語言資源已刷新，補充了 %d 條翻譯", count))
                    .color(ChatFormatting.GREEN)
                    .send();
        } else if (enabled) {
            SendChatMessage.of("[BKTools] 語言資源已刷新，但無需要補充的翻譯")
                    .color(ChatFormatting.YELLOW)
                    .send();
        } else {
            SendChatMessage.of("[BKTools] CN2TW 功能已關閉，資源未刷新")
                    .color(ChatFormatting.RED)
                    .send();
        }

        // 輸出 JSON 文件
        try {
            Files.createDirectories(OUTPUT_DIR);

            // 按 namespace 分組
            // key 格式：「namespace:path」或「modid.category.name」
            // 優先用冒號分割，沒有冒號則用第一段點號作為 namespace
            Map<String, Map<String, String>> grouped = new TreeMap<>();

            for (Map.Entry<String, String> entry : fallbackMap.entrySet()) {
                String key = entry.getKey();
                String namespace;

                if (key.contains(":")) {
                    namespace = key.substring(0, key.indexOf(':'));
                } else if (key.contains(".")) {
                    namespace = key.substring(0, key.indexOf('.'));
                } else {
                    namespace = "_other";
                }

                grouped
                        .computeIfAbsent(namespace, k -> new TreeMap<>())
                        .put(key, entry.getValue());
            }

            // 組裝輸出 JSON，每個 namespace 是一個區塊
            JsonObject root = new JsonObject();
            for (Map.Entry<String, Map<String, String>> group : grouped.entrySet()) {
                JsonObject section = new JsonObject();
                group.getValue().forEach(section::addProperty);
                root.add(group.getKey(), section);
            }

            try (var writer = Files.newBufferedWriter(OUTPUT_FILE, StandardCharsets.UTF_8)) {
                new GsonBuilder()
                        .setPrettyPrinting()
                        .disableHtmlEscaping()
                        .create()
                        .toJson(root, writer);
            }

            // 輸出到聊天欄，帶可點擊的資料夾連結
            SendChatMessage.of("[BKTools] 調試文件已輸出到：")
                    .withFolderLink(OUTPUT_DIR, OUTPUT_FILE.getFileName().toString())
                    .color(ChatFormatting.GRAY)
                    .send();

        } catch (Exception e) {
            DebugLogger.alwaysWarn("Cn2TwFallback", "輸出 language map 失敗: %s", e.getMessage());
        }
    }

    // ── 同步刷新（供 dumpToFile 內部呼叫）────────────────
    private void refreshSync(ResourceManager resourceManager) {
        Map<String, String> prepared = prepare(resourceManager);
        apply(prepared);
    }

    // ── loadLanguageFile（讀取指定語言的所有翻譯）─────────
    private Map<String, String> loadLanguageFile(ResourceManager resourceManager, String langCode) {

        Map<String, String> result = new HashMap<>();

        var resources = resourceManager.listResources(
                "lang",
                location -> location.getPath().endsWith(langCode + ".json"));

        for (var entry : resources.entrySet()) {
            try (InputStream stream = entry.getValue().open();
                    InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {

                JsonElement json = JsonParser.parseReader(reader);
                if (!json.isJsonObject())
                    continue;

                for (Map.Entry<String, JsonElement> kv : json.getAsJsonObject().entrySet()) {
                    if (kv.getValue().isJsonPrimitive()) {
                        result.put(kv.getKey(), kv.getValue().getAsString());
                    }
                }

            } catch (Exception e) {
                DebugLogger.alwaysWarn("Cn2TwFallback", "讀取語言檔案失敗：%s - %s",
                        entry.getKey(), e.getMessage());
            }
        }

        return result;
    }
}