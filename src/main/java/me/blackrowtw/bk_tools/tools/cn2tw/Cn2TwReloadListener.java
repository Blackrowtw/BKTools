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

import me.blackrowtw.bk_tools.Reference;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;

/**
 * CN2TW Fallback 資源重載監聽器
 *
 * registerReloader 的 lambda 在 Fabric 資源重載的 setup 階段由主執行緒呼叫。
 * 此時 Minecraft 的 ResourceManager 已完成建構並替換，
 * 在 lambda 入口處捕獲引用（effectively final），
 * 背景執行緒的 prepare() 使用此捕獲值，不再自行呼叫 Minecraft.getInstance()。
 *
 * lambda 參數說明（fabric-resource-loader-v1）：
 *   store           → PreparableReloadListener.SharedState（非 ResourceManager，勿直接傳入）
 *   prepareExecutor → 背景執行緒池（用於 prepare）
 *   synchronizer    → 同步點（prepare 完成後 apply 才能開始）
 *   applyExecutor   → 主執行緒（用於 apply）
 */
public class Cn2TwReloadListener {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(
            Reference.MOD_ID, "cn2tw_fallback_reloader");

    public static void register() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .registerReloader(ID, (store, prepareExecutor, synchronizer, applyExecutor) -> {

                    // 在 lambda 入口處（主執行緒、setup 階段）捕獲 ResourceManager。
                    // 此時 Fabric 已完成新 ResourceManager 的建構，引用穩定。
                    // 背景執行緒的 prepare() 使用此捕獲值，
                    // 不在背景執行緒中再次呼叫 Minecraft.getInstance().getResourceManager()。
                    final ResourceManager rm = Minecraft.getInstance().getResourceManager();

                    return CompletableFuture
                            .supplyAsync(
                                    () -> Cn2TwFallbackManager.getInstance().prepare(rm),
                                    prepareExecutor)
                            .thenCompose(synchronizer::wait)
                            .thenAcceptAsync(
                                    (Cn2TwFallbackManager.PreparedData data) ->
                                            Cn2TwFallbackManager.getInstance().apply(data),
                                    applyExecutor);
                });
    }
}
