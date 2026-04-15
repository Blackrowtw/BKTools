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

package me.blackrowtw.bk_tools;

import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import me.blackrowtw.bk_tools.init.InitHandler;
import me.blackrowtw.bk_tools.tools.cn2tw.Cn2TwReloadListener;

@Environment(EnvType.CLIENT)
public class BKToolsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 註冊 Cn2TwReloadListener 以確保在資源重新載入時能夠正確更新 fallback 資料
        Cn2TwReloadListener.register();
        // malilib 的註冊與初始化 InitHandler 以進行後續的模組初始化工作
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
    }
}
