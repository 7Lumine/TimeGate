package dev.timegateplugin.timegate.schedule;

/**
 * サーバーの開閉状態を表す列挙型
 */
public enum GateState {
    /** 開放状態 — すべてのプレイヤーがログイン可能 */
    OPEN,
    /** 閉鎖状態 — バイパス権限持ちのみログイン可能 */
    CLOSED
}
