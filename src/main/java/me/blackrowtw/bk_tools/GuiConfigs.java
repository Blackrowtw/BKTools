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

// 使用者的圖形設定介面
package me.blackrowtw.bk_tools;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GuiConfigs extends GuiConfigsBase {

    public GuiConfigs() {
        super(10, 50,
                Reference.MOD_ID, // 模組 ID
                null, // 父介面（null = 無）
                "bk_tools.gui.title", // 標題 translation key
                Reference.MOD_VERSION // 顯示版本號
        );
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        // 將所有設定項目包裝成 GUI 元素
        return ConfigOptionWrapper.createFor(Configs.HOTKEYS);
        // 未來加更多：return ConfigOptionWrapper.createFor(Configs.HOTKEYS, Configs.GENERIC);
    }

    @Override
    protected boolean useKeybindSearch() {
        // 有快捷鍵設定時，開啟搜尋框
        return true;
    }
}
