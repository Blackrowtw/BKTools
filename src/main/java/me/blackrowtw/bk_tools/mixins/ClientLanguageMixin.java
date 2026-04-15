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


package me.blackrowtw.bk_tools.mixins;

import me.blackrowtw.bk_tools.tools.cn2tw.Cn2TwFallbackManager;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 攔截 ClientLanguage.getOrDefault()
 *
 * 當繁體中文（zh_tw）找不到翻譯時（回傳值 == key），
 * 從 Cn2TwFallbackManager 補充對應的 zh_cn 翻譯
 *
 * 判斷「真正找不到」的條件：
 * 回傳值與 key 相同 → Minecraft 找不到翻譯，回傳 key 本身作為 fallback
 *
 * 不用與 fallback 參數比較的原因：
 * fallback 參數本身就是 key，但 Mojang 未來可能改變此行為
 * 直接比較 cir.getReturnValue() 與 key 更穩定可靠
 */
@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Inject(method = "getOrDefault", at = @At("RETURN"), cancellable = true)
    private void onGetOrDefault(String key, String fallback, CallbackInfoReturnable<String> cir) {

        // 快速短路：回傳值不等於 key，表示 zh_tw 已有翻譯，不需 fallback
        if (!cir.getReturnValue().equals(key))
            return;

        Cn2TwFallbackManager manager = Cn2TwFallbackManager.getInstance();

        // 快速短路：功能未開啟，直接返回
        if (!manager.isEnabled())
            return;

        // 有 fallback 翻譯才替換
        if (manager.contains(key)) {
            cir.setReturnValue(manager.get(key));
        }
    }
}
