# NRF5340 LCD Display Project

NRF5340開発キットを使用したBLE制御対応LCD表示システムです。

## 機能

- SB1602 16x2 LCD I2C制御
- BLEによるリモートLCD制御機能
- Androidコンパニオンアプリ対応
- Zephyr RTOSベースのファームウェア

## プロジェクト構成

```
.
├── boards/              # ボード設定ファイル
├── central/             
│   └── android/        # Androidアプリ
├── src/
│   ├── app/           # アプリケーションロジック
│   ├── ble/          # BLEサービス実装 
│   └── drivers/      # ハードウェアドライバ
└── sysbuild/          # MCUboot設定
```

## 必要環境

- nRF Connect SDK (NCS)
- nRF5340 DK
- Android Studio (コンパニオンアプリ用)
- CMake 3.20.0以上

## ビルド方法

1. ファームウェアのビルド:
```sh
west build
```

2. Androidアプリのビルド:
```sh
cd central/android
./gradlew assembleDebug
```

## BLEサービス

ファームウェアは以下のカスタムLCDプリントサービス(LPS)を実装しています:

- サービスUUID: `a00c1710-74ff-4bd5-9e86-cf601d80c054`
- 表示特性: `a00c1711-74ff-4bd5-9e86-cf601d80c054`
- クリア特性: `a00c1712-74ff-4bd5-9e86-cf601d80c054`

## ライセンス

Apache License 2.0で提供しています。詳細は各ソースファイルを参照してください。
