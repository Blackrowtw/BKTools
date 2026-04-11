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

package me.blackrowtw.bk_tools.mixins;

import me.blackrowtw.bk_tools.tools.cn2tw.Cn2TwFallbackManager;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {
    @Inject(method = "getOrDefault", at = @At("RETURN"), cancellable = true)
    private void onGetOrDefault(String key, String fallback, CallbackInfoReturnable<String> cir) {

        // 只在回傳值等於 key 本身（即真正找不到翻譯）才 fallback
        // 不用 equals(fallback) 是因為 fallback 參數本來就是 key，
        // 但未來 Mojang 可能改這個邏輯，直接比 key 更穩
        if (!cir.getReturnValue().equals(key))
            return;

        Cn2TwFallbackManager manager = Cn2TwFallbackManager.getInstance();

        // 先檢查功能是否啟用
        if (!manager.isEnabled())
            return;

        if (manager.contains(key)) {
            cir.setReturnValue(manager.get(key));
        }
    }
}