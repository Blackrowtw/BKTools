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
import java.util.concurrent.atomic.AtomicReference;

import me.blackrowtw.bk_tools.Reference;
import me.blackrowtw.bk_tools.config.Configs;
import me.blackrowtw.bk_tools.util.DebugLogger;
import me.blackrowtw.bk_tools.util.SendChatMessage;

/**
 * CN2TW Fallback 管理器（單例）
 *
 * 翻譯覆蓋範圍：
 * zh_tw 有 → 不干涉
 * zh_tw 沒有、en_us 有（英文 fallback）→ 替換為 zh_cn
 * zh_tw 和 en_us 都沒有（顯示 key）→ 替換為 zh_cn
 *
 * 執行緒安全修正（崩潰修正）：
 * refreshAsync() 在呼叫時快照 ResourceManager 引用
 * prepare() 使用快照引用，不在背景執行緒中呼叫 Minecraft.getInstance()
 * F3+T 期間 Minecraft 的 ResourceManager 正在被替換，
 * 直接從 Minecraft 取得引用會拿到替換中的物件 → 崩潰
 *
 * masa 系模組兼容說明：
 * masa 系（litematica, tweakeroo 等）將語言資源打包在 assets/<modid>/lang/ 下
 * 這與標準模組相同，listResources("lang",...) 能正確找到
 * 如果 dump 中看不到 masa 系翻譯，原因是：
 * 1. 這些模組的 zh_tw.json 已存在（malilib 自身有完整 zh_tw）
 * 2. 或 jar 載入順序問題（較少見）
 *
 * carpet 系模組兼容說明：
 * carpet 的部分 GUI 文字透過 carpet 自己的 Component 系統顯示
 * 這些文字最終仍會呼叫 ClientLanguage.getOrDefault()
 * 若 carpet 的 zh_cn 翻譯在 dump 中存在但 Mixin 無法替換，
 * 最可能的原因是 carpet 對應的 en_us key 的翻譯值和原本顯示的不完全一致
 * 解決方式：放寬 isEnUsFallback 的判斷（見 Mixin 說明）
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

    // ── 語言對常數 ────────────────────────────────────────
    private static final String SOURCE_LANG = "zh_cn";
    private static final String TARGET_LANG = "zh_tw";
    private static final String FALLBACK_LANG = "en_us";

    // ── fallbackMap：zh_cn 有但 zh_tw 沒有的翻譯 ──────────
    private final ConcurrentHashMap<String, String> fallbackMap = new ConcurrentHashMap<>();

    // ── enUsSnapshot：en_us 完整翻譯（用於判斷 en_us fallback）──
    private final AtomicReference<Map<String, String>> enUsSnapshot = new AtomicReference<>(Collections.emptyMap());

    // ── 非同步刷新旗標 ────────────────────────────────────
    private final AtomicBoolean refreshPending = new AtomicBoolean(false);

    // ══════════════════════════════════════════════════════
    // 公開查詢 API
    // ══════════════════════════════════════════════════════

    public boolean isEnabled() {
        return Configs.Cn2Tw.CN2TW_ENABLE_FALLBACK.getBooleanValue();
    }

    public boolean contains(String key) {
        return fallbackMap.containsKey(key);
    }

    public String get(String key) {
        return fallbackMap.get(key);
    }

    public int size() {
        return fallbackMap.size();
    }

    /**
     * 判斷 getOrDefault 的回傳值是否為 en_us fallback
     *
     * 目的：讓 Mixin 能攔截「zh_tw 沒有翻譯、顯示 en_us 英文」的情況
     *
     * @param key         翻譯 key
     * @param returnValue ClientLanguage.getOrDefault 的回傳值
     */
    public boolean isEnUsFallback(String key, String returnValue) {
        Map<String, String> enUs = enUsSnapshot.get();
        if (enUs.isEmpty())
            return false;
        String enUsValue = enUs.get(key);
        return enUsValue != null && enUsValue.equals(returnValue);
    }

    // ══════════════════════════════════════════════════════
    // 資源重載流程（Cn2TwReloadListener 呼叫）
    // ══════════════════════════════════════════════════════

    /**
     * PREPARE 階段：背景執行緒，純讀取
     *
     * 使用由 Cn2TwReloadListener 傳入的 store（ResourceManager 快照）
     * store 是 Fabric 為本次重載建立的穩定引用，不受 F3+T 期間的替換影響
     */
    public PreparedData prepare(ResourceManager resourceManager) {
        DebugLogger.info("Cn2TwFallback", "prepare() 開始");

        Map<String, String> zhCn = loadLanguageFile(resourceManager, SOURCE_LANG);
        Map<String, String> zhTw = loadLanguageFile(resourceManager, TARGET_LANG);
        Map<String, String> enUs = loadLanguageFile(resourceManager, FALLBACK_LANG);

        DebugLogger.info("Cn2TwFallback", "%s=%d / %s=%d / %s=%d",
                SOURCE_LANG, zhCn.size(), TARGET_LANG, zhTw.size(), FALLBACK_LANG, enUs.size());

        Map<String, String> fallback = new HashMap<>();
        for (Map.Entry<String, String> entry : zhCn.entrySet()) {
            if (!zhTw.containsKey(entry.getKey())) {
                // 預留接口：在此處加入 OpenCC/opencc4j 繁簡轉換
                // 例如：String converted = ZhConverter.toTraditional(entry.getValue());
                // fallback.put(entry.getKey(), converted);
                fallback.put(entry.getKey(), entry.getValue());
            }
        }

        DebugLogger.info("Cn2TwFallback", "prepare() 完成，待補充 %d 條", fallback.size());
        return new PreparedData(
                Collections.unmodifiableMap(fallback),
                Collections.unmodifiableMap(enUs));
    }

    /** APPLY 階段：主執行緒 */
    public void apply(PreparedData data) {
        fallbackMap.clear();
        enUsSnapshot.set(Collections.emptyMap());

        if (!isEnabled()) {
            DebugLogger.always("Cn2TwFallback", "功能已關閉，跳過套用");
            return;
        }

        fallbackMap.putAll(data.fallback());
        enUsSnapshot.set(data.enUs());
        DebugLogger.always("Cn2TwFallback", "apply() 完成，補充了 %d 條翻譯", fallbackMap.size());
    }

    /** prepare() 的不可變資料容器 */
    public record PreparedData(
            Map<String, String> fallback,
            Map<String, String> enUs) {
    }

    // ══════════════════════════════════════════════════════
    // 開關切換即時生效
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
                // 在呼叫點快照 ResourceManager，傳給 refreshAsync
                // 此時不在資源重載中，引用是穩定的
                final ResourceManager rm = mc.getResourceManager();
                refreshAsync(rm, false);
            } else {
                DebugLogger.alwaysWarn("Cn2TwFallback", "onToggle(true): ResourceManager 尚未就緒");
                SendChatMessage.of("[BKTools] CN2TW 已開啟，重載資源後生效")
                        .color(ChatFormatting.YELLOW).send();
            }
        } else {
            // 主執行緒直接清空，ConcurrentHashMap.clear() 是執行緒安全的
            fallbackMap.clear();
            enUsSnapshot.set(Collections.emptyMap());
            SendChatMessage.of("[BKTools] CN2TW 已關閉").color(ChatFormatting.GRAY).send();
            DebugLogger.always("Cn2TwFallback", "onToggle(false): 資料已清空");
        }
    }

    // ══════════════════════════════════════════════════════
    // GUI 按鈕操作
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
        // 呼叫點快照
        final ResourceManager rm = mc.getResourceManager();
        refreshAsync(rm, false);
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
        if (!refreshPending.compareAndSet(false, true)) {
            SendChatMessage.of("[BKTools] 正在刷新中，請稍候...")
                    .color(ChatFormatting.YELLOW).send();
            return;
        }

        // 呼叫點快照
        final ResourceManager rm = mc.getResourceManager();

        CompletableFuture.supplyAsync(() -> prepare(rm))
                .thenAcceptAsync(data -> mc.execute(() -> {
                    try {
                        apply(data);
                        CompletableFuture.runAsync(() -> writeDumpFile(data.fallback()))
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
                    DebugLogger.alwaysWarn("Cn2TwFallback", "dumpLangFile 失敗: %s", ex.getMessage());
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
        mc.reloadResourcePacks().thenRunAsync(
                () -> SendChatMessage.of("[BKTools] 資源包重載完成，CN2TW 已自動更新")
                        .color(ChatFormatting.GREEN).send(),
                mc);
    }

    // ══════════════════════════════════════════════════════
    // 內部輔助方法
    // ══════════════════════════════════════════════════════

    /**
     * 非同步刷新（GUI 按鈕和 onToggle 使用）
     *
     * @param resourceManager 呼叫點快照的 ResourceManager（不在背景執行緒中取得）
     * @param silent          是否靜默（不顯示聊天訊息）
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

        // resourceManager 已在呼叫點快照，背景執行緒直接使用，不再呼叫 mc.getResourceManager()
        CompletableFuture.supplyAsync(() -> prepare(resourceManager))
                .thenAcceptAsync(data -> mc.execute(() -> {
                    try {
                        apply(data);
                        if (!silent) {
                            int count = fallbackMap.size();
                            if (count > 0) {
                                SendChatMessage.of(String.format(
                                        "[BKTools] CN2TW 已刷新，補充了 %d 條翻譯", count))
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
     * 讀取指定語言的所有翻譯，掃描所有已載入的 resource pack
     * 後載入的不覆蓋先載入的（與 Minecraft 行為一致）
     *
     * 注意：此方法可能在背景執行緒呼叫，resourceManager 必須是穩定快照 * 公開方法：未來供其他語言對模組複用
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
            mc.execute(() -> SendChatMessage.of(String.format("[BKTools] 已輸出 %d 條 fallback 翻譯：", count))
                    .withFolderLink(OUTPUT_DIR, OUTPUT_FILE.getFileName().toString())
                    .color(ChatFormatting.GRAY).send());

        } catch (Exception e) {
            DebugLogger.alwaysWarn("Cn2TwFallback", "writeDumpFile 失敗: %s", e.getMessage());
            mc.execute(() -> SendChatMessage.of("[BKTools] 輸出失敗：" + e.getMessage())
                    .color(ChatFormatting.RED).send());
        }
    }
}
