# ⏰ TimeGate

Minecraft Paper 1.21.11 向けの**時間制限ホワイトリストプラグイン**。

指定したスケジュールに基づいてサーバーの開放/閉鎖を自動制御し、MOTD もリアルタイムで切り替わります。

## 機能

- 🕐 **スケジュール制御** — 曜日＋時間帯で開放/閉鎖を自動切替
- 🔒 **アクセス制御** — 閉鎖時はバイパス権限を持つプレイヤーのみログイン可能
- 📋 **MOTD 動的切替** — 開放/閉鎖で異なる MOTD を表示（MiniMessage 対応）
- 🔧 **手動オーバーライド** — コマンドで強制開放/閉鎖が可能
- 👢 **自動キック** — 閉鎖時に権限のないプレイヤーを自動キック（設定で ON/OFF）
- ⚠️ **閉鎖前告知** — 閉鎖の○分前に全プレイヤーへ自動告知（間隔設定可能）

## 導入方法

1. [Releases](https://github.com/7Lumine/TimeGate/releases) から `TimeGate-x.x.x.jar` をダウンロード
2. Paper サーバーの `plugins/` フォルダに配置
3. サーバーを再起動
4. `plugins/TimeGate/config.yml` を編集してスケジュールを設定

## コマンド

| コマンド             | 説明             |
| -------------------- | ---------------- |
| `/timegate status` | 現在の状態を表示 |
| `/timegate open`   | 強制開放モード   |
| `/timegate close`  | 強制閉鎖モード   |
| `/timegate auto`   | 自動モードに戻す |
| `/timegate reload` | 設定をリロード   |

エイリアス: `/tg`

## パーミッション

| パーミッション      | デフォルト | 説明                             |
| ------------------- | ---------- | -------------------------------- |
| `timegate.bypass` | OP         | 閉鎖時でもサーバーに入れる       |
| `timegate.admin`  | OP         | `/timegate` コマンドを使用可能 |

## 設定例 (`config.yml`)

```yaml
# 開放スケジュール
# ※ 日をまたぐ場合は 24:00 を超える値を使用（例: 25:00 = 翌日 01:00）
schedule:
  - days: [SATURDAY, SUNDAY]
    start: "10:00"
    end: "25:00"    # 翌日 01:00 まで
  - days: [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY]
    start: "18:00"
    end: "23:00"

# 閉鎖時に権限のないプレイヤーをキック
kick-on-close: true

# メッセージ (MiniMessage 形式)
kick-message: "<red>サーバーは現在閉鎖中です。\n<gray>次の開放時間をお待ちください。"
deny-message: "<red>サーバーは現在閉鎖中です。\n<gray>次の開放時間をお待ちください。"

# MOTD (MiniMessage 形式)
motd:
  open: "<gradient:green:aqua><bold>MyServer</bold></gradient> <gray>- 開放中！\n<green>✔ 誰でも参加できます"
  closed: "<gradient:red:gold><bold>MyServer</bold></gradient> <gray>- 閉鎖中\n<red>✖ 現在はホワイトリスト限定です"

# 閉鎖前の告知設定
warning:
  intervals: [30, 15, 5, 1]       # 残り30, 15, 5, 1分で告知
  message: "<gold><bold>⚠ 告知</bold></gold> <yellow>サーバーは <red>{minutes}分後</red> に閉鎖されます。"

# タイムゾーン
timezone: "Asia/Tokyo"
```

## ビルド

```bash
# JAVA_HOME を JDK 21+ に設定
./gradlew build
```

成果物: `build/libs/TimeGate-x.x.x.jar`

## 動作要件

- Minecraft Paper 1.21.11
- Java 21+

## ライセンス

MIT License
