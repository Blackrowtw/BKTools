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
 * CN2TW 翻譯補充 Mixin — 攔截 ClientLanguage.getOrDefault()
 *
 * 攔截時機與判斷邏輯：
 *
 * ClientLanguage.loadFrom() 同時載入 en_us（底層）和 zh_tw（覆蓋層），
 * storage 中已包含所有 en_us key 作為 zh_tw 的 fallback。
 *
 * 三種情況，我們只替換後兩種：
 *   ① zh_tw 有翻譯   → key 不在 fallbackMap → manager.contains() 短路，不干涉
 *   ② zh_tw 無、en_us 有 → 回傳英文；isEnUsFallback() 確認後替換為 zh_cn
 *   ③ zh_tw 和 en_us 都沒有 → 回傳 key 本身；returnValue.equals(key) 確認後替換為 zh_cn
 *
 * carpet 兼容說明：
 *   carpet 系模組部分翻譯 key 查詢時，回傳的值可能包含 Minecraft 的格式化字符
 *   或 carpet 自己的 Component 可能先調用過處理，導致 isEnUsFallback 比對失敗。
 *   目前的判斷是「回傳值與 en_us 完全相等」，carpet 如果對翻譯有額外處理就比對不到。
 *   待進一步確認 carpet 的具體行為後，可在此處加入 carpet 專用的兼容邏輯。
 *
 * masa 系說明：
 *   masa 系（litematica 等）若有 zh_cn 但無 zh_tw，在 dump 中會存在且可被替換。
 *   若 dump 中找不到其 key，表示這些模組的 zh_tw.json 已存在（例如 malilib 本身），
 *   即這些 key 不在 fallbackMap 中，Mixin 正確跳過，無需干涉。
 */
@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Inject(method = "getOrDefault", at = @At("RETURN"), cancellable = true)
    private void onGetOrDefault(String key, String fallback, CallbackInfoReturnable<String> cir) {
        Cn2TwFallbackManager manager = Cn2TwFallbackManager.getInstance();

        // 快速短路 1：功能未開啟
        if (!manager.isEnabled()) return;

        // 快速短路 2：此 key 不在 fallbackMap（zh_tw 已有翻譯，或 zh_cn 也沒有此 key）
        // 這是最常見的情況，O(1) 查詢，幾乎無效能影響
        if (!manager.contains(key)) return;

        final String returnValue = cir.getReturnValue();

        // 情況 ③：zh_tw 和 en_us 都沒有此 key，Minecraft 回傳 key 本身
        if (returnValue.equals(key)) {
            cir.setReturnValue(manager.get(key));
            return;
        }

        // 情況 ②：zh_tw 沒有，en_us 有，Minecraft 回傳 en_us 英文翻譯
        // isEnUsFallback() 透過比對 enUsSnapshot 確認回傳值確實來自 en_us
        if (manager.isEnUsFallback(key, returnValue)) {
            cir.setReturnValue(manager.get(key));
        }

        // 未匹配以上兩種情況：不干涉（可能是 zh_tw 確實有翻譯，或其他特殊情況）
    }
}
