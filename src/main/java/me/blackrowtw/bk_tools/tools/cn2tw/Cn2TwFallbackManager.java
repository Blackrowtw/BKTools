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


package me.blackrowtw.bk_tools.tools.cn2tw;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import me.blackrowtw.bk_tools.Reference;
import me.blackrowtw.bk_tools.config.Configs;
import me.blackrowtw.bk_tools.util.DebugLogger;
import me.blackrowtw.bk_tools.util.SendChatMessage;

/**
 * CN2TW Fallback 管理器（單例）
 *
 * 職責：
 * 1. 在資源重載（F3+T / 進世界）時，背景執行緒 prepare，主執行緒 apply
 * 2. 開關切換時即時生效（開→補充 map，關→清空 map）
 * 3. 提供 GUI 按鈕觸發的非同步刷新（不阻塞主執行緒）
 * 4. 提供非同步重載資源的接口（等同 F3+T）
 * 5. dumpLangFile 以非同步方式在背景執行緒完成 I/O
 *
 * 語言對擴充接口：
 * - 目前固定 zh_cn → zh_tw
 * - loadLanguageFile() 是公開方法，未來可供其他語言對模組複用
 * - prepare() 的 sourceLang / targetLang 參數化後可支援 en2tw、en2cn 等
 */
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

    // ── 語言對（未來擴展為可設定的 Config 選項）──────────
    private static final String SOURCE_LANG = "zh_cn";
    private static final String TARGET_LANG = "zh_tw";

    // ── fallback 結果存放處（執行緒安全）──────────────────
    // key = translation key（例如 "some_mod.item.foo"）
    // value = 來源語言的翻譯文字（zh_cn 原文，未來可接 OPENCC 轉換）
    private final ConcurrentHashMap<String, String> fallbackMap = new ConcurrentHashMap<>();

    // ── 非同步刷新進行中旗標（避免重複觸發）──────────────
    private final AtomicBoolean refreshPending = new AtomicBoolean(false);

    // ══════════════════════════════════════════════════════
    // 公開查詢 API（供 ClientLanguageMixin 呼叫，主執行緒安全）
    // ══════════════════════════════════════════════════════

    /** 功能是否啟用（直接讀取 Config 開關，每次查詢時動態判斷） */
    public boolean isEnabled() {
        return Configs.Cn2Tw.CN2TW_ENABLE_FALLBACK.getBooleanValue();
    }

    /** 查詢 key 是否存在於 fallback map */
    public boolean contains(String key) {
        return fallbackMap.containsKey(key);
    }

    /** 取得 fallback 翻譯文字 */
    public String get(String key) {
        return fallbackMap.get(key);
    }

    /** 取得目前 fallback 條目數 */
    public int size() {
        return fallbackMap.size();
    }

    // ══════════════════════════════════════════════════════
    // 資源重載流程（Cn2TwReloadListener 呼叫，Fabric API 保證執行緒）
    // ══════════════════════════════════════════════════════

    /**
     * PREPARE 階段：在 prepareExecutor 背景執行緒執行
     * 純粹讀取資源，不做任何狀態變更，回傳不可變快照
     *
     * @param resourceManager 當次重載的 ResourceManager
     * @return 缺少翻譯的條目快照（source 有、target 沒有）
     */
    public Map<String, String> prepare(ResourceManager resourceManager) {
        DebugLogger.info("Cn2TwFallback", "prepare() 開始");

        Map<String, String> sourceAll = loadLanguageFile(resourceManager, SOURCE_LANG);
        Map<String, String> targetAll = loadLanguageFile(resourceManager, TARGET_LANG);

        DebugLogger.info("Cn2TwFallback", "%s %d 條 / %s %d 條",
                SOURCE_LANG, sourceAll.size(), TARGET_LANG, targetAll.size());

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : sourceAll.entrySet()) {
            if (!targetAll.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
                // 預留接口：這裡可以接 OPENCC 對 entry.getValue() 做繁簡轉換後再放入
            }
        }

        DebugLogger.info("Cn2TwFallback", "prepare() 完成，待補充 %d 條", result.size());
        return Collections.unmodifiableMap(result);
    }

    /**
     * APPLY 階段：在 applyExecutor（主執行緒）執行
     * 此時 Config 已 load 完成，開關值正確
     *
     * @param preparedData prepare() 產生的資料快照
     */
    public void apply(Map<String, String> preparedData) {
        fallbackMap.clear();

        if (!isEnabled()) {
            DebugLogger.always("Cn2TwFallback", "功能已關閉，跳過套用");
            return;
        }

        fallbackMap.putAll(preparedData);
        DebugLogger.always("Cn2TwFallback", "apply() 完成，補充了 %d 條翻譯", fallbackMap.size());
    }

    // ══════════════════════════════════════════════════════
    // 開關切換即時生效（KeyCallbackRegistry → onToggle）
    // ══════════════════════════════════════════════════════

    /**
     * 當 CN2TW_ENABLE_FALLBACK 開關切換時呼叫（主執行緒）
     *
     * 開啟 → 非同步刷新 fallbackMap（不阻塞主執行緒）
     * 關閉 → 立即清空 fallbackMap，Mixin 的 isEnabled() 也會短路，雙重保護
     *
     * @param enabled 新的開關狀態
     */
    public void onToggle(boolean enabled) {
        if (enabled) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getResourceManager() != null) {
                refreshAsync(mc.getResourceManager(), false);
            } else {
                // 極少數情況：遊戲尚未完全初始化
                DebugLogger.alwaysWarn("Cn2TwFallback", "onToggle(true): ResourceManager 尚未就緒，等資源重載後生效");
                SendChatMessage.of("[BKTools] CN2TW 已開啟，重載資源後生效")
                        .color(ChatFormatting.YELLOW).send();
            }
        } else {
            fallbackMap.clear();
            SendChatMessage.of("[BKTools] CN2TW 已關閉").color(ChatFormatting.GRAY).send();
            DebugLogger.always("Cn2TwFallback", "onToggle(false): fallbackMap 已清空");
        }
    }

    // ══════════════════════════════════════════════════════
    // GUI 按鈕觸發的三個操作
    // ══════════════════════════════════════════════════════

    /**
     * 【按鈕：刷新 Map】
     * 非同步重新讀取語言檔案，更新記憶體中的 fallbackMap
     * 不輸出任何檔案，也不重載整個資源包
     */
    public void refreshMap() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getResourceManager() == null) {
            SendChatMessage.of("[BKTools] 無法取得 ResourceManager，請進入世界後重試")
                    .color(ChatFormatting.RED).send();
            return;
        }
        if (!isEnabled()) {
            SendChatMessage.of("[BKTools] CN2TW 功能已關閉，無法刷新 Map")
                    .color(ChatFormatting.RED).send();
            return;
        }
        refreshAsync(mc.getResourceManager(), false);
    }

    /**
     * 【按鈕：輸出 Lang Map】
     * 先非同步刷新 fallbackMap，完成後在背景執行緒輸出 JSON 檔案
     * 輸出路徑：config/BKTools_DEBUG/cn2tw_fallback_dump.json
     */
    public void dumpLangFile() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getResourceManager() == null) {
            SendChatMessage.of("[BKTools] 無法取得 ResourceManager，請進入世界後重試")
                    .color(ChatFormatting.RED).send();
            return;
        }

        if (!isEnabled()) {
            SendChatMessage.of("[BKTools] CN2TW 功能已關閉，無法輸出 Map")
                    .color(ChatFormatting.YELLOW).send();
            return;
        }

        // 避免重複觸發
        if (!refreshPending.compareAndSet(false, true)) {
            SendChatMessage.of("[BKTools] 正在刷新中，請稍候...")
                    .color(ChatFormatting.YELLOW).send();
            return;
        }

        final ResourceManager rm = mc.getResourceManager();

        // Step1: 背景執行緒 prepare
        CompletableFuture.supplyAsync(() -> prepare(rm))
                // Step2: 主執行緒 apply
                .thenAcceptAsync(preparedData -> mc.execute(() -> {
                    try {
                        apply(preparedData);
                        // Step3: 背景執行緒輸出檔案（帶 apply 後的快照，確保一致）
                        CompletableFuture.runAsync(() -> writeDumpFile(preparedData))
                                .exceptionally(ex -> {
                                    DebugLogger.alwaysWarn("Cn2TwFallback", "writeDumpFile 失敗: %s", ex.getMessage());
                                    mc.execute(() -> SendChatMessage.of("[BKTools] 輸出失敗：" + ex.getMessage())
                                            .color(ChatFormatting.RED).send());
                                    return null;
                                });
                    } finally {
                        refreshPending.set(false);
                    }
                }))
                .exceptionally(ex -> {
                    refreshPending.set(false);
                    DebugLogger.alwaysWarn("Cn2TwFallback", "dumpLangFile prepare 失敗: %s", ex.getMessage());
                    mc.execute(() -> SendChatMessage.of("[BKTools] 刷新失敗：" + ex.getMessage())
                            .color(ChatFormatting.RED).send());
                    return null;
                });
    }

    /**
     * 【按鈕：重載資源包】
     * 等同 F3+T，觸發後 Cn2TwReloadListener 的 prepare/apply 會自動執行
     */
    public void reloadResources() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            SendChatMessage.of("[BKTools] 遊戲尚未初始化，無法重載資源")
                    .color(ChatFormatting.RED).send();
            return;
        }

        SendChatMessage.of("[BKTools] 正在重載資源包，請稍候...")
                .color(ChatFormatting.YELLOW).send();
        DebugLogger.always("Cn2TwFallback", "reloadResources() 觸發");

        // 使用 Minecraft 原生重載，完成後 Cn2TwReloadListener 自動執行 prepare/apply
        mc.reloadResourcePacks().thenRunAsync(
                () -> SendChatMessage.of("[BKTools] 資源包重載完成，CN2TW 已自動更新")
                        .color(ChatFormatting.GREEN).send(),
                mc // 以 Minecraft 作為 Executor，確保回調在主執行緒
        );
    }

    // ══════════════════════════════════════════════════════
    // 內部輔助方法
    // ══════════════════════════════════════════════════════

    /**
     * 非同步刷新 fallbackMap（不輸出檔案）
     * ForkJoinPool 背景執行緒 prepare → mc.execute 主執行緒 apply
     */
    private void refreshAsync(ResourceManager resourceManager, boolean silent) {
        if (!refreshPending.compareAndSet(false, true)) {
            if (!silent) {
                SendChatMessage.of("[BKTools] 正在刷新中，請稍候...")
                        .color(ChatFormatting.YELLOW).send();
            }
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        CompletableFuture.supplyAsync(() -> prepare(resourceManager))
                .thenAcceptAsync(preparedData -> mc.execute(() -> {
                    try {
                        apply(preparedData);
                        if (!silent) {
                            int count = fallbackMap.size();
                            if (count > 0) {
                                SendChatMessage.of(
                                        String.format("[BKTools] CN2TW 已刷新，補充了 %d 條翻譯", count))
                                        .color(ChatFormatting.GREEN).send();
                            } else {
                                SendChatMessage.of("[BKTools] CN2TW 已刷新，目前沒有需要補充的翻譯")
                                        .color(ChatFormatting.YELLOW).send();
                            }
                        }
                    } finally {
                        refreshPending.set(false);
                    }
                }))
                .exceptionally(ex -> {
                    refreshPending.set(false);
                    DebugLogger.alwaysWarn("Cn2TwFallback", "refreshAsync 失敗: %s", ex.getMessage());
                    if (!silent) {
                        mc.execute(() -> SendChatMessage.of("[BKTools] 刷新失敗：" + ex.getMessage())
                                .color(ChatFormatting.RED).send());
                    }
                    return null;
                });
    }

    /**
     * 讀取指定語言代碼的所有翻譯（掃描全部已載入的 resource pack）
     * 在背景執行緒呼叫，後載入的 resource pack 不覆蓋先載入的（與 Minecraft 一致）
     *
     * 公開方法：未來供其他語言對模組複用
     *
     * @param resourceManager 當前 ResourceManager
     * @param langCode        語言代碼（例如 "zh_cn"、"en_us"）
     * @return 不可變 key→翻譯 Map
     */
    public Map<String, String> loadLanguageFile(ResourceManager resourceManager, String langCode) {
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
                        // putIfAbsent：優先度高的 resource pack 先載入，後者不覆蓋
                        result.putIfAbsent(kv.getKey(), kv.getValue().getAsString());
                    }
                }

            } catch (Exception e) {
                DebugLogger.alwaysWarn("Cn2TwFallback", "讀取語言檔案失敗：%s - %s",
                        entry.getKey(), e.getMessage());
            }
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * 將資料按 namespace 分組後輸出為格式化 JSON
     * 在背景執行緒呼叫（不阻塞主執行緒）
     *
     * @param dataToWrite 要輸出的資料快照（來自 prepare 的不可變 Map）
     */
    private void writeDumpFile(Map<String, String> dataToWrite) {
        Minecraft mc = Minecraft.getInstance();
        try {
            Files.createDirectories(OUTPUT_DIR);

            // 按 namespace 分組
            // key 格式：「namespace:path」或「modid.category.name」
            // 優先用冒號分割，沒有冒號則用第一段點號作為 namespace
            Map<String, Map<String, String>> grouped = new TreeMap<>();
            for (Map.Entry<String, String> entry : dataToWrite.entrySet()) {
                String key = entry.getKey();
                String namespace;

                if (key.contains(":")) {
                    namespace = key.substring(0, key.indexOf(':'));
                } else if (key.contains(".")) {
                    namespace = key.substring(0, key.indexOf('.'));
                } else {
                    namespace = "_other";
                }

                grouped.computeIfAbsent(namespace, k -> new TreeMap<>())
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

            int count = dataToWrite.size();
            mc.execute(() ->
            // 輸出到聊天欄，帶可點擊的資料夾連結
            SendChatMessage.of(String.format("[BKTools] 已輸出 %d 條 fallback 翻譯：", count))
                    .withFolderLink(OUTPUT_DIR, OUTPUT_FILE.getFileName().toString())
                    .color(ChatFormatting.GRAY).send());

        } catch (Exception e) {
            DebugLogger.alwaysWarn("Cn2TwFallback", "writeDumpFile 失敗: %s", e.getMessage());
            mc.execute(() -> SendChatMessage.of("[BKTools] 輸出失敗：" + e.getMessage())
                    .color(ChatFormatting.RED).send());
        }
    }
}
