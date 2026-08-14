package com.github.airybubble.naturanode.client.antispam;

import com.github.airybubble.naturanode.mixin.client.ChatComponentAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public final class DuplicateChatTracker {

    public static final DuplicateChatTracker CHAT_TRACKER = new DuplicateChatTracker();
    public static final DuplicateChatTracker GAME_TRACKER = new DuplicateChatTracker();

    private String lastKey = null;
    private Component lastOriginalMessage = null;
    private int count = 0;

    private int lastVisibleLineCount = 0;

    private DuplicateChatTracker() {
    }

    @FunctionalInterface
    public interface MessageAdder {
        void add(ChatComponent chatHud, Component text);
    }

    public synchronized boolean handle(Component message, String dedupeKey, MessageAdder adder) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return true;
        }

        ChatComponent chatHud = client.gui.hud.getChat();
        ChatComponentAccessor accessor = (ChatComponentAccessor) chatHud;
        List<GuiMessage> logical = accessor.naturanode$getAllMessages();
        List<GuiMessage.Line> visible = accessor.naturanode$getTrimmedMessages();

        Component textToAdd;
        if (dedupeKey.equals(lastKey)) {
            count++;
            textToAdd = buildStackedText(lastOriginalMessage, count);

            removeFront(logical, 1);
            removeFront(visible, lastVisibleLineCount);
        } else {
            lastKey = dedupeKey;
            lastOriginalMessage = message;
            count = 1;
            textToAdd = message;
        }

        int visibleBefore = visible.size();
        adder.add(chatHud, textToAdd);
        lastVisibleLineCount = Math.max(visible.size() - visibleBefore, 1);

        return false;
    }

    private Component buildStackedText(Component original, int count) {
        MutableComponent text = original.copy();
        text.append(Component.literal(" x" + count).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        return text;
    }

    private void removeFront(List<?> list, int n) {
        for (int i = 0; i < n && !list.isEmpty(); i++) {
            list.removeFirst();
        }
    }

    public synchronized void reset() {
        lastKey = null;
        lastOriginalMessage = null;
        count = 0;
        lastVisibleLineCount = 0;
    }
}
