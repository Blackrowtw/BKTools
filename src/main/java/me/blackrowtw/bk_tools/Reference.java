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

// 模組的身份識別中心，用於取得 MOD_ID
package me.blackrowtw.bk_tools;

import fi.dy.masa.malilib.util.StringUtils;

public class Reference {
    public static final String MOD_ID = "bk_tools";
    public static final String MOD_NAME = "BKTools";
    public static final String MOD_VERSION = StringUtils.getModVersionString(MOD_ID);
}