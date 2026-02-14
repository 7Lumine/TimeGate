package dev.timegateplugin.timegate.command;

import dev.timegateplugin.timegate.config.ConfigManager;
import dev.timegateplugin.timegate.schedule.GateState;
import dev.timegateplugin.timegate.schedule.ScheduleManager;
import dev.timegateplugin.timegate.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * /timegate コマンドの処理クラス
 */
public class TimeGateCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of("status", "open", "close", "auto", "reload");

    private final ScheduleManager scheduleManager;
    private final ConfigManager configManager;

    public TimeGateCommand(ScheduleManager scheduleManager, ConfigManager configManager) {
        this.scheduleManager = scheduleManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "status" -> handleStatus(sender);
            case "open" -> handleOpen(sender);
            case "close" -> handleClose(sender);
            case "auto" -> handleAuto(sender);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        return List.of();
    }

    private void handleStatus(CommandSender sender) {
        GateState state = scheduleManager.getCurrentState();
        ScheduleManager.OverrideMode override = scheduleManager.getOverrideMode();

        String stateStr = state == GateState.OPEN
                ? "<green>開放中 (OPEN)"
                : "<red>閉鎖中 (CLOSED)";

        String overrideStr = switch (override) {
            case NONE -> "<gray>自動 (AUTO)";
            case FORCE_OPEN -> "<yellow>強制開放 (FORCE_OPEN)";
            case FORCE_CLOSED -> "<yellow>強制閉鎖 (FORCE_CLOSED)";
        };

        sender.sendMessage(MessageUtil.parse("<gold><bold>TimeGate</bold></gold> <gray>ステータス"));
        sender.sendMessage(MessageUtil.parse("<gray>状態: " + stateStr));
        sender.sendMessage(MessageUtil.parse("<gray>モード: " + overrideStr));
    }

    private void handleOpen(CommandSender sender) {
        scheduleManager.setOverrideMode(ScheduleManager.OverrideMode.FORCE_OPEN);
        sender.sendMessage(MessageUtil.parse("<gold><bold>TimeGate</bold></gold> <green>強制開放モードに切り替えました。"));
    }

    private void handleClose(CommandSender sender) {
        scheduleManager.setOverrideMode(ScheduleManager.OverrideMode.FORCE_CLOSED);
        sender.sendMessage(MessageUtil.parse("<gold><bold>TimeGate</bold></gold> <red>強制閉鎖モードに切り替えました。"));
    }

    private void handleAuto(CommandSender sender) {
        scheduleManager.setOverrideMode(ScheduleManager.OverrideMode.NONE);
        sender.sendMessage(MessageUtil.parse("<gold><bold>TimeGate</bold></gold> <aqua>自動モードに切り替えました。スケジュールに従います。"));
    }

    private void handleReload(CommandSender sender) {
        configManager.reload();
        scheduleManager.reevaluate();
        sender.sendMessage(MessageUtil.parse("<gold><bold>TimeGate</bold></gold> <green>設定をリロードしました。"));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(MessageUtil.parse("<gold><bold>TimeGate</bold></gold> <gray>使い方:"));
        sender.sendMessage(MessageUtil.parse("<yellow>/timegate status <gray>- 現在の状態を表示"));
        sender.sendMessage(MessageUtil.parse("<yellow>/timegate open <gray>- 強制開放"));
        sender.sendMessage(MessageUtil.parse("<yellow>/timegate close <gray>- 強制閉鎖"));
        sender.sendMessage(MessageUtil.parse("<yellow>/timegate auto <gray>- 自動モードに戻す"));
        sender.sendMessage(MessageUtil.parse("<yellow>/timegate reload <gray>- 設定をリロード"));
    }
}
