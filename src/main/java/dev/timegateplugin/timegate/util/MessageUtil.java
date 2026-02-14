package dev.timegateplugin.timegate.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * MiniMessage パース・メッセージヘルパー
 */
public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MessageUtil() {
        // ユーティリティクラス
    }

    /**
     * MiniMessage 形式の文字列を Adventure Component にパースする
     *
     * @param miniMessageString MiniMessage 形式の文字列
     * @return パースされた Component
     */
    public static Component parse(String miniMessageString) {
        if (miniMessageString == null || miniMessageString.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(miniMessageString);
    }
}
