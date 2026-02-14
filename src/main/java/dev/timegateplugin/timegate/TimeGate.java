package dev.timegateplugin.timegate;

import dev.timegateplugin.timegate.command.TimeGateCommand;
import dev.timegateplugin.timegate.config.ConfigManager;
import dev.timegateplugin.timegate.listener.LoginListener;
import dev.timegateplugin.timegate.listener.PingListener;
import dev.timegateplugin.timegate.schedule.ScheduleManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TimeGate — 時間制限ホワイトリストプラグイン
 * <p>
 * 指定したスケジュールに基づいてサーバーの開閉を制御し、
 * MOTD を動的に切り替えます。
 */
public class TimeGate extends JavaPlugin {

    private ConfigManager configManager;
    private ScheduleManager scheduleManager;

    @Override
    public void onEnable() {
        // 設定読み込み
        configManager = new ConfigManager(this);

        // スケジュールマネージャー初期化・開始
        scheduleManager = new ScheduleManager(this, configManager);
        scheduleManager.start();

        // イベントリスナー登録
        getServer().getPluginManager().registerEvents(
                new LoginListener(scheduleManager, configManager), this);
        getServer().getPluginManager().registerEvents(
                new PingListener(scheduleManager, configManager), this);

        // コマンド登録
        TimeGateCommand commandHandler = new TimeGateCommand(scheduleManager, configManager);
        PluginCommand command = getCommand("timegate");
        if (command != null) {
            command.setExecutor(commandHandler);
            command.setTabCompleter(commandHandler);
        }

        getLogger().info("TimeGate が有効化されました！");
    }

    @Override
    public void onDisable() {
        if (scheduleManager != null) {
            scheduleManager.stop();
        }
        getLogger().info("TimeGate が無効化されました。");
    }
}
