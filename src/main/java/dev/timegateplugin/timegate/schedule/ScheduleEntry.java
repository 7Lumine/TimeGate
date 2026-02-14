package dev.timegateplugin.timegate.schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * 1つの開放スケジュールを表すレコード
 *
 * @param days  開放する曜日のセット
 * @param start 開放開始時刻
 * @param end   開放終了時刻
 */
public record ScheduleEntry(Set<DayOfWeek> days, LocalTime start, LocalTime end) {

    /**
     * 指定された曜日・時刻がこのスケジュールの開放時間内かどうかを判定する
     *
     * @param day  判定する曜日
     * @param time 判定する時刻
     * @return 開放時間内であれば true
     */
    public boolean isWithin(DayOfWeek day, LocalTime time) {
        if (!days.contains(day)) {
            return false;
        }

        if (start.isBefore(end)) {
            // 通常: start <= time < end
            return !time.isBefore(start) && time.isBefore(end);
        } else {
            // 日をまたぐ場合 (例: 22:00 ~ 02:00): time >= start || time < end
            return !time.isBefore(start) || time.isBefore(end);
        }
    }
}
