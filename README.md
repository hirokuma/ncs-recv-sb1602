# nRF Connect SDK SB1602 LCD Driver

このプロジェクトは、nRF Connect SDKを使用してSB1602 LCD（16x2文字液晶）を制御するサンプルアプリケーションです。

## 必要要件

- nRF Connect SDK v2.4.0以上
- nRF52840 DK または互換ボード
- SB1602 LCD モジュール

## ハードウェア接続

SB1602 LCDとnRF52840 DKの接続：

| SB1602 PIN | nRF52840 DK PIN |
|------------|-----------------|
| SDA        | P0.26          |
| SCL        | P0.27          |
| VCC        | VDD (3.3V)     |
| GND        | GND            |

## ビルドとフラッシュ

1. プロジェクトのクローン:
```bash
git clone https://github.com/yourusername/ncs-recv-sb1602.git
cd ncs-recv-sb1602
```

2. ビルド:
```bash
west build -b nrf52840dk_nrf52840
```

3. フラッシュ:
```bash
west flash
```

## 機能

- I2C経由でのLCD制御
- 16x2文字表示
- カスタム文字の定義と表示
- バックライト制御

## 設定

`prj.conf`でI2C設定やその他のオプションを変更できます：

```conf
CONFIG_I2C=y
CONFIG_I2C_NRFX=y
CONFIG_I2C0=y
```

## ライセンス

Apache License 2.0
