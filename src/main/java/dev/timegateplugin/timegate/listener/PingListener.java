package dev.timegateplugin.timegate.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import dev.timegateplugin.timegate.config.ConfigManager;
import dev.timegateplugin.timegate.schedule.GateState;
import dev.timegateplugin.timegate.schedule.ScheduleManager;
import dev.timegateplugin.timegate.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * サーバーリスト表示時の MOTD 切替リスナー
 */
public class PingListener implements Listener {

    private final ScheduleManager scheduleManager;
    private final ConfigManager configManager;

    public PingListener(ScheduleManager scheduleManager, ConfigManager configManager) {
        this.scheduleManager = scheduleManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onServerListPing(PaperServerListPingEvent event) {
        String motdTemplate;
        if (scheduleManager.getCurrentState() == GateState.OPEN) {
            motdTemplate = configManager.getMotdOpen();
        } else {
            motdTemplate = configManager.getMotdClosed();
        }

        Component motd = MessageUtil.parse(motdTemplate);
        event.motd(motd);
    }
}
