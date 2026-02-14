package dev.timegateplugin.timegate.listener;

import dev.timegateplugin.timegate.config.ConfigManager;
import dev.timegateplugin.timegate.schedule.GateState;
import dev.timegateplugin.timegate.schedule.ScheduleManager;
import dev.timegateplugin.timegate.util.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * プレイヤーログイン時のアクセス制御リスナー
 */
public class LoginListener implements Listener {

    private static final String BYPASS_PERMISSION = "timegate.bypass";

    private final ScheduleManager scheduleManager;
    private final ConfigManager configManager;

    public LoginListener(ScheduleManager scheduleManager, ConfigManager configManager) {
        this.scheduleManager = scheduleManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        // 開放状態なら何もしない
        if (scheduleManager.getCurrentState() == GateState.OPEN) {
            return;
        }

        // 閉鎖状態: バイパス権限チェック
        if (event.getPlayer().hasPermission(BYPASS_PERMISSION)) {
            return;
        }

        // 権限なし → ログイン拒否
        event.disallow(
                PlayerLoginEvent.Result.KICK_WHITELIST,
                MessageUtil.parse(configManager.getDenyMessage()));
    }
}
