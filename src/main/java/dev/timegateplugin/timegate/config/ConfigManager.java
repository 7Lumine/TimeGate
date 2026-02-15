package dev.timegateplugin.timegate.config;

import dev.timegateplugin.timegate.schedule.ScheduleEntry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Logger;

/**
 * config.yml の読み込み・管理クラス
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private final Logger logger;

    private List<ScheduleEntry> scheduleEntries;
    private boolean kickOnClose;
    private String kickMessage;
    private String denyMessage;
    private String motdOpen;
    private String motdClosed;
    private List<Integer> warningIntervals;
    private String warningMessage;
    private ZoneId timezone;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        reload();
    }

    /**
     * config.yml を再読み込みする
     */
    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // スケジュール読み込み
        scheduleEntries = new ArrayList<>();
        List<?> scheduleList = config.getList("schedule");
        if (scheduleList != null) {
            for (Object obj : scheduleList) {
                if (obj instanceof Map<?, ?> map) {
                    try {
                        ScheduleEntry entry = parseScheduleEntry(map);
                        scheduleEntries.add(entry);
                    } catch (Exception e) {
                        logger.warning("スケジュールエントリの読み込みに失敗しました: " + e.getMessage());
                    }
                }
            }
        }

        // その他の設定
        kickOnClose = config.getBoolean("kick-on-close", true);
        kickMessage = config.getString("kick-message", "<red>サーバーは現在閉鎖中です。");
        denyMessage = config.getString("deny-message", "<red>サーバーは現在閉鎖中です。");
        motdOpen = config.getString("motd.open", "<green>Server is OPEN");
        motdClosed = config.getString("motd.closed", "<red>Server is CLOSED");

        // 閉鎖前告知設定
        List<Integer> defaultIntervals = List.of(30, 15, 5, 1);
        warningIntervals = config.getIntegerList("warning.intervals");
        if (warningIntervals.isEmpty()) {
            warningIntervals = defaultIntervals;
        }
        warningMessage = config.getString("warning.message",
                "<gold><bold>⚠ 告知</bold></gold> <yellow>サーバーは <red>{minutes}分後</red> に閉鎖されます。");

        // タイムゾーン
        String tz = config.getString("timezone", "");
        if (tz != null && !tz.isEmpty()) {
            try {
                timezone = ZoneId.of(tz);
            } catch (Exception e) {
                logger.warning("無効なタイムゾーン: " + tz + " — システムデフォルトを使用します。");
                timezone = ZoneId.systemDefault();
            }
        } else {
            timezone = ZoneId.systemDefault();
        }

        logger.info("設定を読み込みました。スケジュール数: " + scheduleEntries.size()
                + ", タイムゾーン: " + timezone.getId());
    }

    /**
     * "H:mm" 形式の時刻文字列を分数に変換する。
     * 24:00 を超える値（例: "25:00" = 翌日 01:00）にも対応。
     */
    private int parseTimeToMinutes(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    @SuppressWarnings("unchecked")
    private ScheduleEntry parseScheduleEntry(Map<?, ?> map) {
        List<String> dayStrings = (List<String>) map.get("days");
        String startStr = (String) map.get("start");
        String endStr = (String) map.get("end");

        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (String dayStr : dayStrings) {
            days.add(DayOfWeek.valueOf(dayStr.toUpperCase()));
        }

        int startMinutes = parseTimeToMinutes(startStr);
        int endMinutes = parseTimeToMinutes(endStr);

        return new ScheduleEntry(days, startMinutes, endMinutes);
    }

    public List<ScheduleEntry> getScheduleEntries() {
        return Collections.unmodifiableList(scheduleEntries);
    }

    public boolean isKickOnClose() {
        return kickOnClose;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public String getDenyMessage() {
        return denyMessage;
    }

    public String getMotdOpen() {
        return motdOpen;
    }

    public String getMotdClosed() {
        return motdClosed;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public List<Integer> getWarningIntervals() {
        return Collections.unmodifiableList(warningIntervals);
    }

    public String getWarningMessage() {
        return warningMessage;
    }
}
