/*
 * This file is part of the BKTools project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  BlacKrowtw and contributors
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

import me.blackrowtw.bk_tools.Reference;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * CN2TW Fallback 資源重載監聽器
 *
 * 在 Fabric 資源重載管線中（F3+T、進入世界、切換資源包）自動執行：
 * prepareExecutor 背景執行緒 → prepare()
 * applyExecutor 主執行緒 → apply()
 *
 * 由 BKToolsClient.onInitializeClient() 呼叫 register() 一次性註冊
 */
public class Cn2TwReloadListener {

        public static final Identifier ID = Identifier.fromNamespaceAndPath(
                        Reference.MOD_ID, "cn2tw_fallback_reloader");

        public static void register() {
                ResourceLoader.get(PackType.CLIENT_RESOURCES)
                                .registerReloader(ID, (store, prepareExecutor, synchronizer, applyExecutor) -> {

                                        // Fabric 保證 prepareExecutor 為背景執行緒，applyExecutor 為主執行緒
                                        // prepare() 純讀取資源，不修改任何狀態，執行緒安全
                                        // apply() 修改 fallbackMap，在主執行緒執行，不需要額外同步

                                        return CompletableFuture
                                                        .supplyAsync(
                                                                        () -> Cn2TwFallbackManager.getInstance()
                                                                                        .prepare(Minecraft.getInstance()
                                                                                                        .getResourceManager()),
                                                                        prepareExecutor)
                                                        .thenCompose(synchronizer::wait)
                                                        .thenAcceptAsync(
                                                                        (Map<String, String> preparedData) -> Cn2TwFallbackManager
                                                                                        .getInstance()
                                                                                        .apply(preparedData),
                                                                        applyExecutor);
                                });
        }
}
