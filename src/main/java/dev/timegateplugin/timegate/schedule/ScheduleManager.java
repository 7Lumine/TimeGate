package dev.timegateplugin.timegate.schedule;

import dev.timegateplugin.timegate.config.ConfigManager;
import dev.timegateplugin.timegate.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * スケジュールに基づく開閉状態の管理クラス
 */
public class ScheduleManager {

    /**
     * 手動オーバーライドモード
     */
    public enum OverrideMode {
        /** 自動（スケジュール準拠） */
        NONE,
        /** 強制開放 */
        FORCE_OPEN,
        /** 強制閉鎖 */
        FORCE_CLOSED
    }

    private static final String BYPASS_PERMISSION = "timegate.bypass";
    private static final long CHECK_INTERVAL_TICKS = 20L * 60; // 60秒

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Logger logger;

    private OverrideMode overrideMode = OverrideMode.NONE;
    private GateState currentState;
    private BukkitTask checkTask;

    /** すでに送信済みの告知（分数）を記録し、重複送信を防ぐ */
    private final Set<Integer> sentWarnings = new HashSet<>();

    public ScheduleManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.logger = plugin.getLogger();
        // 初期状態を判定
        this.currentState = evaluateState();
    }

    /**
     * 定期チェックタイマーを開始する
     */
    public void start() {
        logger.info("スケジュールマネージャーを開始します。現在の状態: " + currentState);
        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
    }

    /**
     * タイマーを停止する
     */
    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
    }

    /**
     * 定期チェック処理
     */
    private void tick() {
        GateState newState = evaluateState();
        if (newState != currentState) {
            GateState oldState = currentState;
            currentState = newState;
            onStateChanged(oldState, newState);
        }

        // 開放中の場合、閉鎖前の告知をチェック
        if (currentState == GateState.OPEN && overrideMode == OverrideMode.NONE) {
            checkWarnings();
        }
    }

    /**
     * 閉鎖前の告知チェック
     */
    private void checkWarnings() {
        ZonedDateTime now = ZonedDateTime.now(configManager.getTimezone());
        DayOfWeek day = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        // 現在のスケジュールの残り分数を取得
        int minutesRemaining = -1;
        List<ScheduleEntry> entries = configManager.getScheduleEntries();
        for (ScheduleEntry entry : entries) {
            int remaining = entry.getMinutesUntilEnd(day, time);
            if (remaining >= 0) {
                minutesRemaining = remaining;
                break;
            }
        }

        if (minutesRemaining < 0) {
            return;
        }

        // 設定された告知間隔とマッチするか確認
        List<Integer> intervals = configManager.getWarningIntervals();
        for (int interval : intervals) {
            if (minutesRemaining <= interval && !sentWarnings.contains(interval)) {
                sentWarnings.add(interval);
                broadcastWarning(minutesRemaining);
                break; // 1 tick で 1 つだけ告知
            }
        }
    }

    /**
     * 全プレイヤーに閉鎖前告知を送信する
     */
    private void broadcastWarning(int minutesRemaining) {
        String messageTemplate = configManager.getWarningMessage();
        String message = messageTemplate.replace("{minutes}", String.valueOf(minutesRemaining));
        Component component = MessageUtil.parse(message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }

        logger.info("閉鎖前告知を送信しました: 残り " + minutesRemaining + " 分");
    }

    /**
     * 状態が変化した際の処理
     */
    private void onStateChanged(GateState oldState, GateState newState) {
        logger.info("ゲート状態が変化しました: " + oldState + " -> " + newState);

        // 状態が変わったら告知済みセットをリセット
        sentWarnings.clear();

        if (newState == GateState.CLOSED && configManager.isKickOnClose()) {
            kickNonBypassPlayers();
        }
    }

    /**
     * バイパス権限のないオンラインプレイヤーをキックする
     */
    private void kickNonBypassPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(BYPASS_PERMISSION)) {
                player.kick(MessageUtil.parse(configManager.getKickMessage()));
            }
        }
    }

    /**
     * 現在の状態を評価する（オーバーライド考慮）
     */
    private GateState evaluateState() {
        return switch (overrideMode) {
            case FORCE_OPEN -> GateState.OPEN;
            case FORCE_CLOSED -> GateState.CLOSED;
            case NONE -> evaluateSchedule();
        };
    }

    /**
     * スケジュールに基づき現在の状態を判定する
     */
    private GateState evaluateSchedule() {
        ZonedDateTime now = ZonedDateTime.now(configManager.getTimezone());
        DayOfWeek day = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        List<ScheduleEntry> entries = configManager.getScheduleEntries();
        for (ScheduleEntry entry : entries) {
            if (entry.isWithin(day, time)) {
                return GateState.OPEN;
            }
        }
        return GateState.CLOSED;
    }

    /**
     * 現在のゲート状態を取得する
     */
    public GateState getCurrentState() {
        return currentState;
    }

    /**
     * オーバーライドモードを取得する
     */
    public OverrideMode getOverrideMode() {
        return overrideMode;
    }

    /**
     * オーバーライドモードを設定する
     * 設定後すぐに状態を再評価し、変化があればイベント処理を行う
     */
    public void setOverrideMode(OverrideMode mode) {
        this.overrideMode = mode;
        GateState newState = evaluateState();
        if (newState != currentState) {
            GateState oldState = currentState;
            currentState = newState;
            onStateChanged(oldState, newState);
        }
    }

    /**
     * 現在の状態を即座に再評価する（reload 後などに使用）
     */
    public void reevaluate() {
        sentWarnings.clear();
        GateState newState = evaluateState();
        if (newState != currentState) {
            GateState oldState = currentState;
            currentState = newState;
            onStateChanged(oldState, newState);
        }
    }
}
