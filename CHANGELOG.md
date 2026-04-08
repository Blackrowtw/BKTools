# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2026-04-09

### Added

#### 新增配置功能
- **ConfigCommandBindList**：可配置的命令綁定功能
  - 支援三種循環模式：SEQUENTIAL（順序）、RANDOM（隨機）、FIXED（固定）
  - 兩行 GUI 顯示：命令列表 + 循環模式選擇
  - Mode Hotkey：切換循環模式
- **ConfigBtnTrigger**：按鈕觸發器配置
- **LoopMode**：循環模式枚舉類別
- **ConfigCommandBind**：單一命令綁定配置
- **ConfigCommandBindListMode**：配置界面顯示模式

#### 新增 GUI 元件
- **BKToolsWidgetConfigOption**：自定義配置選項 Widget
- **BKToolsWidgetListConfigOptions**：配置選項列表 Widget
- **ConfigBtnTriggerButton**：自定義樣式按鈕
  - 灰色背景 (0x66404040)
  - 預設文字顏色 (0xFFE0E0E0)
  - 懸停時文字變為青色 (0xFF55FFFF)
  - 8px 內邊距
  - Try-catch 回退機制

#### 新增工具類別
- **CommandSender**：執行玩家命令的工具類別
- **SendChatMessage**：顯示聊天訊息
- **SendTitleMessage**：顯示標題訊息（Title + Subtitle）
- **DebugLogger**：除錯日誌工具
- **KeyCallbackRegistry**：按鍵回調註冊管理器

#### 新增翻譯工具
- **Cn2TwActions**：簡體轉繁體轉換功能
- **Cn2TwFallbackManager**：備用轉換管理器
- **Cn2TwReloadListener**：配置重新載入監聽器

#### GitHub Actions Workflow
- **git-tag-release.yml**：正式發布流程（push tag release-v*）
  - 從 CHANGELOG.md 提取變更日誌
  - 若無則自動從 git commit 生成
  - 建立 GitHub Release 並觸發 release.yml
- **git-tag-dev.yml**：Dev 測試發布流程（push tag dev-v*）
  - 自動從 git commit 生成 Release Notes
  - 建立 pre-release 類型的 GitHub Release

### Changed

- **Reference.java**：新增執行期資訊（版本、路徑、偵錯模式）
- **Configs.java**：新增 Generic 與 BIND 類別配置選項
- **GuiConfigs.java**：新增 GUI 配置選項
- **InputHandler.java**：整合 KeyCallbackRegistry 回調機制
- **InitHandler.java**：更新初始化處理邏輯
- **BKToolsClient.java**：整合新功能與 MaliLib 介面

### Fixed

- 翻譯檔案移除了底線格式 (§n, §r)，使用標準樣式

### Updated

- 版本號從 1.0.1 更新至 1.1.0
- Maven Group 從 me.fallenbreath 改為 me.blackrowtw
- README.md 重寫為 BKTools 專案說明
- fabric.mod.json 更新描述與 GitHub 連結
- 版權聲明更新為三方共同作者格式：
  - BlacKrowtw（主要作者）
  - Fallen_Breath（模板作者）
  - masa & sakura-ryoko（malilib 庫）

---

## [1.0.1] - 2026-03-23

### Added

- **modMenu 整合**：成功引入 ModMenu API
- **設定標籤頁面切換基礎功能**：建立設定頁面的基本架構
- Git 忽略設定：新增 zip 檔案排除

### Changed

- 成功構建並讀取到 ModMenu 介面
- 模組資訊正確顯示在 MaliLib 設定介面中

---

## [1.0.0] - 2026-03-21

### Added

- **初始專案建立**：基於 Fallen-Breath/fabric-mod-template
- **多版本支援**：完成多版本 Minecraft (1.21.11) 的模組建構
- **Malilib 依賴設定**：
  - 成功設定 malilib 依賴
  - 模組可在遊戲中正確被 MaliLib 識別
- **基礎模組結構**：
  - 建立 BKTools 主類別
  - 建立 Reference 類別
  - 建立基本配置文件結構

---

## [版本號] - YYYY-MM-DD

### Added
- 新功能 1
- 新功能 2

### Changed
- 變更 1
- 變更 2

### Fixed
- 修復 1
- 修復 2

### Removed
- 移除功能 1

### Updated
- 更新內容 1

---

## 發布備註

- 使用 Git Tag 發布：
  - 正式發布：`git tag -a release-vX.X.X && git push origin release-vX.X.X`
  - Dev 發布：`git tag -a dev-vX.X.X && git push origin dev-vX.X.X`
- CHANGELOG.md 用於發布時自動提取變更日誌

---

## 相關連結

- GitHub: https://github.com/Blackrowtw/BKTools
- 基於模板: https://github.com/Fallen-Breath/fabric-mod-template
- 使用函式庫: https://github.com/sakura-ryoko/malilib

