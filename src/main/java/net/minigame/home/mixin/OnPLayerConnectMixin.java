package net.minigame.home.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class OnPLayerConnectMixin {
	@Shadow @Final private MinecraftServer server;

	@Shadow public abstract void sendToAll(Packet<?> packet);

	private static String name;

	@Inject(at = @At("HEAD"), method = "onPlayerConnect")
	private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci){
		name = player.getEntityName();
	}

	@Inject(at = @At("HEAD"), method = "broadcastChatMessage", cancellable = true)
	private void broadcastChatMessage(Text message, MessageType type, UUID senderUuid, CallbackInfo ci) {
		if (message.getString().contains("tham gia") || message.getString().contains("joined")){
			message = new LiteralText("§e"+name+" đã tham gia §a§lPARTY GAMES!");
		}
		this.server.sendSystemMessage(message, senderUuid);
		this.sendToAll(new GameMessageS2CPacket(message, type, senderUuid));
		ci.cancel();
	}
}
