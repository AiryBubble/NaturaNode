package com.github.airybubble.naturanode.mixin.client;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * ChatComponent が内部に保持している2種類のメッセージリストを取得するための Accessor Mixin。
 *
 * 2026-08-14 時点でユーザーが実際にデコンパイルして確認した
 * net.minecraft.client.gui.components.ChatComponent のソースにより、
 * 以下のフィールドが存在することを確認済み:
 *   private final List<GuiMessage> allMessages = Lists.newArrayList();
 *   private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
 *
 * allMessages が論理メッセージ、trimmedMessages が実際に画面へ描画される
 * チャット幅で折り返し済みのリスト。表示は trimmedMessages を元に行われる。
 */
@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {

    @Accessor("allMessages")
    List<GuiMessage> naturanode$getAllMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> naturanode$getTrimmedMessages();
}
