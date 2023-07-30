package net.minigame.home.mixin;

import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minigame.home.SnowRain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minigame.home.Core.*;
import static net.minigame.home.SnowRain.*;

@Mixin(SnowballEntity.class)
public class SnowBallMixin {
    @Inject(at = @At("TAIL"), method = "onEntityHit")
    protected void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci){
        if (getScore("game", "index") == 2) {
            if(entityHitResult.getEntity() instanceof ServerPlayerEntity){
                ServerPlayerEntity player = (ServerPlayerEntity) entityHitResult.getEntity();
                if(!isAdmin(player)){
                    sendToAll("§b"+player.getEntityName()+" §7đã bị bóng tuyết rơi lủng đầu!");
                    CMDManager.execute(CMDSource,"/execute at @a[name="+player.getEntityName()+"] run playsound minecraft:entity.player.death ambient @a ~ ~ ~ 2");
                    CMDManager.execute(CMDSource,"/tp @a[name="+player.getEntityName()+"] -55.5 29 37.5");
                    PLAYERS_ALIVE--;
                    if(PLAYERS_ALIVE==2){
                        SnowRain.top3 = player.getEntityName();
                    } else if (PLAYERS_ALIVE==1) {
                        SnowRain.top2 = player.getEntityName();
                        for(ServerPlayerEntity player1:MCserver.getPlayerManager().getPlayerList()){
                            if(!isAdmin(player)&&!isSpec(player)&&!player1.isSpectator()&&!player1.getEntityName().equals(SnowRain.top2))
                                SnowRain.top1=player1.getEntityName();
                        }
                        endGame();
                    }
                    CMDManager.execute(CMDSource,"/gamemode spectator @a[name="+player.getEntityName()+"]");
                }
            }
        }
    }
}
