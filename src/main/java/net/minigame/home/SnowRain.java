package net.minigame.home;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.minigame.home.Core.*;

public class SnowRain {
    public static int CURRENT_WAVE;
    public static int SHELTER;
    public static int WAVE_SECONDS;
    public static String top1, top2, top3;
    private static int bossbarTime = 0;
    public static void snowRainTicks(){
        if(getScore("game","index")==-2 && getScore("cool2","timer")==0){
            setScore("game","index",2);
            initialGame();
            summonShelters();
            top1 = top2 = top3 = "";
        }
        else if(getScore("game","index")==2){
            gamePlay();
            changeBossColor();
        }
    }
    private static void changeBossColor(){
        if(bossbarTime>0){
            if(bossbarTime==8)
                execute("/bossbar set minecraft:snowrain name {\"text\":\"TAKE COVER!\",\"color\":\"yellow\",\"bold\":true}");
            bossbarTime--;
        }
        else if(bossbarTime==0){
            execute("/bossbar set minecraft:snowrain name {\"text\":\"TAKE COVER!\",\"color\":\"red\",\"bold\":true}");
            bossbarTime=16;
        }

    }

    private static void initialGame(){
        CURRENT_WAVE = 1;
        SHELTER = 4;
        WAVE_SECONDS = 8;
        MCserver.setPvpEnabled(false);
        setScore("waveWait","timer",(WAVE_SECONDS+1)*20);
        setScore("rainWait","timer",0);
    }
    private static void gamePlay(){
        if(getScore("waveWait","timer")>0){
            addScore("waveWait","timer",-1);
            if(getScore("waveWait","timer")%20==0){
                execute("/execute as @a at @s run playsound minecraft:block.lever.click ambient @s ~ ~ ~ 1 .8");
                execute("/execute as @a at @s run playsound minecraft:block.lever.click ambient @s ~ ~ ~ 1 .6");
            }
        }
        else if(getScore("waveWait","timer")==0 && getScore("rainWait","timer")==0){
            setScore("rainWait","timer",120);
            setScore("waveWait","timer",-1);
            summonRain();
        }
        if(getScore("rainWait","timer")>0){
            addScore("rainWait","timer",-1);
        }
        else if(getScore("rainWait","timer")==0 && getScore("waveWait","timer")==-1){
            CURRENT_WAVE++;
            if(WAVE_SECONDS>3)
                WAVE_SECONDS--;
            if(CURRENT_WAVE>6 && SHELTER>1){
                SHELTER--;
                if(CURRENT_WAVE==8)
                    MCserver.setPvpEnabled(true);
            }
            setScore("waveWait","timer",WAVE_SECONDS*20);
            summonShelters();
        }
    }
    private static void summonRain(){
        int drop = 1000;
        Random random = new Random();
        BlockPos start = new BlockPos(-72, 52, 19);
        BlockPos end = new BlockPos(-39, 52, 52);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        for(int i=0;i<drop;i++){
            int x = random.nextInt(end.getX() - start.getX() + 1) + start.getX();
            int y = random.nextInt(end.getY() - start.getY() + 1) + start.getY();
            int z = random.nextInt(end.getZ() - start.getZ() + 1) + start.getZ();
            SnowballEntity snowball = new SnowballEntity(EntityType.SNOWBALL, world);
            snowball.updatePosition(x+0.5, y, z+0.5);
            executorService.schedule(() -> {
                world.spawnEntity(snowball);
            }, 3*i, TimeUnit.MILLISECONDS);
        }
        executorService.shutdown();
    }
    private static void summonShelters(){
        execute("/fill -67 27 24 -43 27 48 air");
        sendToAll("§eWAVE §c"+CURRENT_WAVE+" §ebắt đầu trong §c"+WAVE_SECONDS+" §egiây với §c"+SHELTER+" §emái che! Hãy tìm chỗ núp!");
        if(CURRENT_WAVE>7){
            sendToAll("§aBạn có thể đánh người chơi từ round này!");
            MCserver.setPvpEnabled(true);
        }
        int quantity = SHELTER;
        Random random = new Random();
        BlockPos start = new BlockPos(-67, 27, 24);
        BlockPos end = new BlockPos(-43, 27, 48);
        while (quantity>0){
            int x = random.nextInt(end.getX() - start.getX() + 1) + start.getX();
            int y = random.nextInt(end.getY() - start.getY() + 1) + start.getY();
            int z = random.nextInt(end.getZ() - start.getZ() + 1) + start.getZ();
            if (world.isAir(new BlockPos(x, y, z))) {
                execute("/setblock "+x+" "+y+" "+z+" oak_slab");
                execute("/setblock "+x+" "+y+" "+z+" air destroy");
                execute("/kill @e[type=item]");
                execute("/setblock "+x+" "+y+" "+z+" oak_slab");
                quantity--;
            }
        }
    }

    public static void endGame(){
        addScore(top1,"stars",3);
        addScore(top2,"stars",2);
        addScore(top3,"stars",1);
        for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()) {
            String msg = "§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n§f§l                       SNOW RAIN\n\n  §e1ST   §7(§e★★★§7) - §e" + top1 + "\n   §62ND   §7(§e ★★§7) - " + top2 + "\n   §c3RD   §7(§e   ★§7) - " + top3 + "\n\n   §fBạn có "+getScore(player.getEntityName(),"stars")+" x §e★.\n\n§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";
            player.sendMessage(new LiteralText(msg), false);
        }
        nextGame(2,top1);
        resetSnowData();
    }
    public static void resetSnowData(){
        setScore("rainWait","timer",0);
        setScore("waveWait","timer",0);
        MCserver.setPvpEnabled(false);
    }
}
