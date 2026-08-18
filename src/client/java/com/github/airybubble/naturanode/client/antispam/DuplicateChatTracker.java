package com.github.airybubble.naturanode.client.antispam;

import com.github.airybubble.naturanode.mixin.client.ChatComponentAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DuplicateChatTracker {

    public static final DuplicateChatTracker CHAT_TRACKER = new DuplicateChatTracker();
    public static final DuplicateChatTracker GAME_TRACKER = new DuplicateChatTracker();

    private static final int MAX_TRACKED_ENTRIES = 200;

    private final Map<String, StackState> entries = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, StackState> eldest) {
            return size() > MAX_TRACKED_ENTRIES;
        }
    };

    private DuplicateChatTracker() {
    }

    @FunctionalInterface
    public interface MessageAdder {
        void add(ChatComponent chatHud, Component text);
    }

    private static final class StackState {
        private Component originalMessage;
        private int count;
        private GuiMessage logicalRef;
        private final List<GuiMessage.Line> visibleLineRefs = new ArrayList<>();
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

        StackState entry = entries.get(dedupeKey);
        Component textToAdd;

        if (entry != null) {
            entry.count++;
            textToAdd = buildStackedText(entry.originalMessage, entry.count);

            removeByIdentity(logical, entry.logicalRef);
            for (GuiMessage.Line line : entry.visibleLineRefs) {
                removeByIdentity(visible, line);
            }
            entry.visibleLineRefs.clear();
        } else {
            entry = new StackState();
            entry.originalMessage = message;
            entry.count = 1;
            textToAdd = message;
            entries.put(dedupeKey, entry);
        }

        int visibleBefore = visible.size();
        adder.add(chatHud, textToAdd);
        int addedVisibleLines = Math.max(visible.size() - visibleBefore, 1);

        entry.logicalRef = logical.isEmpty() ? null : logical.get(0);
        for (int i = 0; i < addedVisibleLines && i < visible.size(); i++) {
            entry.visibleLineRefs.add(visible.get(i));
        }

        return false;
    }

    private Component buildStackedText(Component original, int count) {
        MutableComponent text = original.copy();
        text.append(Component.literal(" x" + count).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        return text;
    }

    private void removeByIdentity(List<?> list, Object ref) {
        if (ref == null) {
            return;
        }
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            if (it.next() == ref) {
                it.remove();
                return;
            }
        }
    }

    public synchronized void reset() {
        entries.clear();
    }
}
