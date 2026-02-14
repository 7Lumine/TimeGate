package dev.timegateplugin.timegate.schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * 1つの開放スケジュールを表すレコード
 * <p>
 * 時刻は「分」で管理し、24:00 を超える値は翌日を表す。
 * 例: 25:00 = 翌日の 01:00, 26:30 = 翌日の 02:30
 *
 * @param days         開放する曜日のセット
 * @param startMinutes 開放開始時刻（0時からの分数）
 * @param endMinutes   開放終了時刻（0時からの分数、1440 超で翌日）
 */
public record ScheduleEntry(Set<DayOfWeek> days, int startMinutes, int endMinutes) {

    private static final int MINUTES_PER_DAY = 24 * 60; // 1440

    /**
     * 指定された曜日・時刻がこのスケジュールの開放時間内かどうかを判定する
     *
     * @param day  判定する曜日
     * @param time 判定する時刻
     * @return 開放時間内であれば true
     */
    public boolean isWithin(DayOfWeek day, LocalTime time) {
        int currentMinutes = time.getHour() * 60 + time.getMinute();

        // 当日のスケジュールをチェック
        if (days.contains(day)) {
            if (endMinutes <= MINUTES_PER_DAY) {
                // 通常: 日をまたがない (例: 15:00〜22:00)
                return currentMinutes >= startMinutes && currentMinutes < endMinutes;
            } else {
                // 日またぎ: 開始日の start 以降は開放 (例: 15:00〜25:00 の 15:00〜23:59)
                return currentMinutes >= startMinutes;
            }
        }

        // 前日のスケジュールが翌日にまたがっているかチェック
        DayOfWeek previousDay = day.minus(1);
        if (days.contains(previousDay) && endMinutes > MINUTES_PER_DAY) {
            int overflowMinutes = endMinutes - MINUTES_PER_DAY;
            // 翌日の 00:00〜overflow の範囲内か (例: 25:00 → 0:00〜1:00)
            return currentMinutes < overflowMinutes;
        }

        return false;
    }

    /**
     * 分数を "HH:mm" 形式の文字列に変換する（表示用）
     */
    public String formatTime(int minutes) {
        return String.format("%d:%02d", minutes / 60, minutes % 60);
    }
}
