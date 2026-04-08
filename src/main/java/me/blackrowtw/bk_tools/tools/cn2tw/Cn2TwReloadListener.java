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

import me.blackrowtw.bk_tools.Reference;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import java.util.concurrent.CompletableFuture;

public class Cn2TwReloadListener {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "cn2tw_fallback_reloader");

    public static void register() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .registerReloader(ID, (store, prepareExecutor, synchronizer, applyExecutor) -> CompletableFuture
                        .supplyAsync(
                                () -> Cn2TwFallbackManager.getInstance()
                                        .prepare(Minecraft.getInstance().getResourceManager()),
                                prepareExecutor)
                        .thenCompose(synchronizer::wait)
                        .thenAcceptAsync(
                                preparedData -> Cn2TwFallbackManager.getInstance().apply(preparedData),
                                applyExecutor));
    }
}