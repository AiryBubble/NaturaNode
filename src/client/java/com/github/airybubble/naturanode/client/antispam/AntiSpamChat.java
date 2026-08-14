package com.github.airybubble.naturanode.client.antispam;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;

public final class AntiSpamChat {

    private AntiSpamChat() {
    }

    public static void register() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String dedupeKey = buildChatKey(sender, message);
            return DuplicateChatTracker.CHAT_TRACKER.handle(message, dedupeKey,
                    (chatHud, text) -> chatHud.addPlayerMessage(text, null, null));
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay) {
                return true;
            }
            return DuplicateChatTracker.GAME_TRACKER.handle(message, message.getString(),
                    (chatHud, text) -> chatHud.addServerSystemMessage(text));
        });
    }

    private static String buildChatKey(GameProfile sender, Component message) {
        String senderName = sender != null ? sender.name() : "";
        return senderName + "\u0000" + message.getString();
    }
}
