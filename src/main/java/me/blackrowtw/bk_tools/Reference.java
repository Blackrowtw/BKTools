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


// 模組的身份識別中心
package me.blackrowtw.bk_tools;

import fi.dy.masa.malilib.util.StringUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

import java.nio.file.Path;

import me.blackrowtw.bk_tools.config.Configs;

public class Reference {

    // ── 模組身份 ──────────────────────────────────────────
    public static final String MOD_ID = "bk_tools";
    public static final String MOD_NAME = "BKTools";
    public static final String MOD_VERSION = StringUtils.getModVersionString(MOD_ID);
    public static final String MALILIB_VERSION = StringUtils.getModVersionString("malilib");
    public static final String MODMENU_VERSION = StringUtils.getModVersionString("modmenu");

    // ── 路徑 ──────────────────────────────────────────────
    public static final Path GAME_DIR = FabricLoader.getInstance().getGameDir();
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    // ── Minecraft 版本資訊 ────────────────────────────────
    public static final String MC_VERSION = SharedConstants.getCurrentVersion().id();
    public static final int MC_DATA_VERSION = SharedConstants.getCurrentVersion().dataVersion().version();

    // ── 開發模式 ──────────────────────────────────────────
    public static final boolean RUNNING_IN_IDE = FabricLoader.getInstance().isDevelopmentEnvironment();

    // 改為 method，每次呼叫時動態判斷（包含手動開關）
    public static boolean isDebugMode() {
        return RUNNING_IN_IDE || Configs.Generic.FORCE_DEBUG_MODE.getBooleanValue();
    }
}