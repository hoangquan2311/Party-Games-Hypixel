package net.minigame.home;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.minigame.home.Core.*;
import static net.minigame.home.PlayerColor.playerColors;
import static net.minigame.home.PlayerColor.randomColorButWhite;

public class EggPaint {
    public static String top1, top2, top3;
    public static int p1, p2, p3;
    private static int gameTimer;
    public static void eggPaintTicks(){
        if(getScore("game","index")==-3){
            if(getScore("cool3","timer")>0){
                if(getScore("cool3","timer")<=100 && getScore("cool3","timer")%20==0){
                    execute("/title @a title {\"text\":\""+getScore("cool3","timer")/20+"\",\"color\":\"red\",\"bold\":true}");
                    execute("/execute as @a at @s run playsound block.lever.click ambient @s ~ ~ ~ 1 .7");
                }
                addScore("cool3","timer",-1);
            }
            else if(getScore("cool3","timer")==0){
                top1=top2=top3="";
                p1=p2=p3=0;
                randomColorButWhite();
                execute("/title @a title \"\"");
                execute("/title @a subtitle {\"text\":\"Start\",\"color\":\"green\"}");
                execute("/execute as @a at @s run playsound entity.player.burp ambient @s");
                setScore("game","index",3);
                gameTimer = 620;
            }
        }
        else if(getScore("game","index")==3){
            killItem(Items.EGG);
            execute("/bossbar set minecraft:eggpaint name \"§eThời gian còn lại: §c§l"+gameTimer/20+"\"");
            execute("/bossbar set minecraft:eggpaint value "+gameTimer);
            for (ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()) {
                if(playerColors.containsKey(player.getUuid())){
                    player.sendMessage(color("§lBẠN LÀ "+playerColors.get(player.getUuid()).getColor()+"!",playerColors.get(player.getUuid()).getTextColor()), true);
                    ItemStack egg = new ItemStack(Items.EGG,64);
                    if(!player.inventory.getStack(0).equals(egg))
                        player.inventory.setStack(0, egg);
                }
                if(getScore(player.getEntityName(),"egg")>0){
                    player.getItemCooldownManager().set(Items.EGG,4);
                    setScore(player.getEntityName(),"egg",0);
                }
            }
            updateTopPlayers();
            if(gameTimer>0){
                --gameTimer;
            }
            else if(gameTimer==0){
                endGame();
            }
        }
        else if(getScore("game","index")==13) killItem(Items.EGG);

    }
    private static int countBlocks(Block targetBlock) {
        int count = 0;
        BlockPos startPos = new BlockPos(-80, 18, -245);
        BlockPos endPos = new BlockPos(-50, 37, -245);

        for (BlockPos pos : BlockPos.iterate(startPos, endPos)) {
            Block block = world.getBlockState(pos).getBlock();
            if (block == targetBlock)
                count++;
        }
        return count;
    }
    private static void countAndSetScore(){
        for (ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()) {
            UUID uuid = player.getUuid();
            if (playerColors.containsKey(uuid)) {
                int count = countBlocks(playerColors.get(uuid).getBlock());
                setScore(player.getEntityName(),"paintCount",count);
            }
        }
    }
    private static void updateTopPlayers(){
        countAndSetScore();
        Scoreboard scoreboard = MCserver.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjective("paintCount");
        List<ScoreboardPlayerScore> sortedScores = scoreboard.getAllPlayerScores(objective)
                .stream()
                .sorted(Comparator.comparingInt(ScoreboardPlayerScore::getScore).reversed())
                .collect(Collectors.toList());
        top1 = sortedScores.get(0).getPlayerName();
        top2 = sortedScores.get(1).getPlayerName();
        top3 = sortedScores.get(2).getPlayerName();
        p1 = getScore(top1,"paintCount");
        p2 = getScore(top2,"paintCount");
        p3 = getScore(top3,"paintCount");
    }
    private static void endGame(){
        addScore(top1,"stars",3);
        addScore(top2,"stars",2);
        addScore(top3,"stars",1);
        for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()) {
            String msg = "§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n§6§l                       EGG PAINTING\n\n  §e1ST   §7(§e★★★§7) - §e" + top1 + "\n   §62ND   §7(§e ★★§7) - " + top2 + "\n   §c3RD   §7(§e   ★§7) - " + top3 + "\n\n   §fBạn có "+getScore(player.getEntityName(),"stars")+" x §e★.\n\n§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";
            player.sendMessage(new LiteralText(msg), false);
        }
        nextGame(3,top1);
        resetPaintData();
    }
    public static void resetPaintData(){
        execute("/scoreboard players reset * paintCount");
        execute("/scoreboard players reset * egg");
        eggCooldowns.clear();
    }
    public static void reloadPainting(){
        execute("/fill -80 18 -245 -50 37 -245 minecraft:white_wool");
    }
}
