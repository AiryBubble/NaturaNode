package com.github.airybubble.naturanode.client;

import com.github.airybubble.naturanode.client.antispam.AntiSpamChat;
import net.fabricmc.api.ClientModInitializer;

public class NaturanodeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AntiSpamChat.register();
    }
}
