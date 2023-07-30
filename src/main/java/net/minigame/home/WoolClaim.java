package net.minigame.home;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

import static net.minigame.home.Core.*;
import static net.minigame.home.PlayerColor.playerColors;
import static net.minigame.home.PlayerColor.randomColor;

public class WoolClaim {

    public static final Map<UUID, Long> lastClickTimes = new HashMap<>();
    private static final long MIN_CLICK_INTERVAL = 300;
    public static String top1, top2, top3;
    public static Integer wool1, wool2, wool3;
    private static int gameTimer;
    public static void woolClaimEventsReg(){
        clickBlockRegister();
    }

    public static void woolClaimTicks(){
            if(getScore("game","index")==-1){
                if(getScore("cool1","timer")>0){
                    if(getScore("cool1","timer")<=100 && getScore("cool1","timer")%20==0){
                        execute("/title @a title {\"text\":\""+getScore("cool1","timer")/20+"\",\"color\":\"red\",\"bold\":true}");
                        execute("/execute as @a at @s run playsound block.lever.click ambient @s ~ ~ ~ 1 .7");
                    }
                    addScore("cool1","timer",-1);
                }
                else if(getScore("cool1","timer")==0) {
                    top1 = top2 = top3 = "";
                    wool1 = wool2 = wool3 = 0;
                    randomColor();
                    execute("/title @a title \"\"");
                    execute("/title @a subtitle {\"text\":\"Start\",\"color\":\"green\"}");
                    execute("/execute as @a at @s run playsound entity.player.burp ambient @s");
                    execute("/scoreboard players set @a woolCount 0");
                    setScore("game", "index", 1);
                    gameTimer = 1200;
                }
            }
            else if(getScore("game","index")==1){
                execute("/bossbar set minecraft:woolclaim name \"§eThời gian còn lại: §c§l"+gameTimer/20+"\"");
                execute("/bossbar set minecraft:woolclaim value "+gameTimer);
                for (ServerPlayerEntity play : MCserver.getPlayerManager().getPlayerList()) {
                    if(playerColors.containsKey(play.getUuid())){
                        play.sendMessage(color("§lBẠN LÀ "+playerColors.get(play.getUuid()).getColor()+"!",playerColors.get(play.getUuid()).getTextColor()), true);
                    }
                }
                //UPDATE TOP PLAYER
                updateTop();
                if(gameTimer>0){
                    --gameTimer;
                }
                else if(gameTimer==0){
                    endGame();
                }
            }
    }
    private static void updateTop(){
        Scoreboard scoreboard = MCserver.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjective("woolCount");
        List<ScoreboardPlayerScore> sortedScores = scoreboard.getAllPlayerScores(objective)
                .stream()
                .sorted(Comparator.comparingInt(ScoreboardPlayerScore::getScore).reversed())
                .collect(Collectors.toList());
        top1 = sortedScores.get(0).getPlayerName();
        top2 = sortedScores.get(1).getPlayerName();
        top3 = sortedScores.get(2).getPlayerName();
        wool1 = sortedScores.get(0).getScore();
        wool2 = sortedScores.get(1).getScore();
        wool3 = sortedScores.get(2).getScore();
    }
    private static void endGame(){
        addScore(top1,"stars",3);
        addScore(top2,"stars",2);
        addScore(top3,"stars",1);
        for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()) {
            String msg = "§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n§d§l                       WOOL CLAIM\n\n  §e1ST   §7(§e★★★§7) - §e" + top1 + "\n   §62ND   §7(§e ★★§7) - " + top2 + "\n   §c3RD   §7(§e   ★§7) - " + top3 + "\n\n   §fBạn có "+getScore(player.getEntityName(),"stars")+" x §e★.\n\n§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";
                player.sendMessage(new LiteralText(msg), false);
            }
        nextGame(1,top1);
        resetWoolData();
    }
    public static void resetWoolData(){
        execute("/scoreboard players reset * woolCount");
        lastClickTimes.clear();
    }
    private static void clickBlockRegister(){
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (getScore("game","index")==1 && hand == Hand.MAIN_HAND) {
                BlockPos pos = hitResult.getBlockPos();
                ItemStack heldItem = player.getMainHandStack();
                if (heldItem.getItem() == Items.DIAMOND_SHOVEL && (world.getBlockState(pos).isOf(Blocks.MYCELIUM) || world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK))) {
                    long currentClickTime = System.currentTimeMillis();
                    if(lastClickTimes.containsKey(player.getUuid())) {
                        long lastClickTime = lastClickTimes.get(player.getUuid());
                        long timeSinceLastClick = currentClickTime - lastClickTime;
                        if(timeSinceLastClick >= MIN_CLICK_INTERVAL) {
                            player.swingHand(Hand.MAIN_HAND);
                            setWoolBlock(currentClickTime, Core.world, pos, player);
                        }
                    }
                    else {
                        player.swingHand(Hand.MAIN_HAND);
                        setWoolBlock(currentClickTime, Core.world, pos, player);
                    }
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });
    }

    private static void setWoolBlock(long currentClickTime, ServerWorld serverWorld, BlockPos pos, PlayerEntity player){
            if (serverWorld.getBlockState(pos.add(1, 0, 0)).getBlock() == playerColors.get(player.getUuid()).getBlock()
                    || serverWorld.getBlockState(pos.add(-1, 0, 0)).getBlock() == playerColors.get(player.getUuid()).getBlock()
                    || serverWorld.getBlockState(pos.add(0, 0, 1)).getBlock() == playerColors.get(player.getUuid()).getBlock()
                    || serverWorld.getBlockState(pos.add(0, 0, -1)).getBlock() == playerColors.get(player.getUuid()).getBlock()
                    || getScore(player.getEntityName(), "woolCount") == 0
            ) {
                Block playerBlock = playerColors.get(player.getUuid()).getBlock();
                serverWorld.setBlockState(pos, playerBlock.getDefaultState());
                addScore(player.getEntityName(), "woolCount", 1);
                execute("/execute as @a[name=!"+player.getEntityName()+"] positioned "+pos.getX()+" "+pos.getY()+" "+pos.getZ()+" run playsound minecraft:block.lava.pop ambient @s ~ ~ ~ 1 0");
                serverWorld.spawnParticles(ParticleTypes.LAVA,pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5,2,.1, .1, .1,1);
            } else {
                MutableText errorLand = color("Bạn chỉ được chiếm ô đất nằm cạnh block ", Formatting.RED);
                MutableText a = color(playerColors.get(player.getUuid()).getColor() + " WOOL", playerColors.get(player.getUuid()).getTextColor());
                player.sendMessage(errorLand.append(a), false);
            }
            execute("/execute as "+player.getEntityName()+" positioned "+pos.getX()+" "+pos.getY()+" "+pos.getZ()+" run playsound minecraft:item.hoe.till ambient @s ~ ~ ~ 1");
            lastClickTimes.put(player.getUuid(), currentClickTime);
    }

    public static void reloadWoolArena(){
        for (int x = 1; x <= 33; x++){
            for (int z = 0; z <= 32; z++){
                BlockPos c = new BlockPos(x, 24, z);
                Block block = world.getBlockState(c).getBlock();
                if(block.equals(Blocks.MYCELIUM) || block.equals(Blocks.WHITE_WOOL) || block.equals(Blocks.BLACK_WOOL) || block.equals(Blocks.BLUE_WOOL)
                        || block.equals(Blocks.GREEN_WOOL) || block.equals(Blocks.RED_WOOL) || block.equals(Blocks.YELLOW_WOOL)
                        || block.equals(Blocks.ORANGE_WOOL) || block.equals(Blocks.PURPLE_WOOL) || block.equals(Blocks.LIGHT_BLUE_WOOL)
                        || block.equals(Blocks.LIME_WOOL)){
                    world.setBlockState(c, Blocks.GRASS_BLOCK.getDefaultState(), 3);
                }
            }
        }
        Random random = new Random();
        int myce = 200;
        while (myce>0) {
            int x = random.nextInt(33) + 1;
            int z = random.nextInt(33);
            BlockPos r = new BlockPos(x, 24, z);
            Block block = world.getBlockState(r).getBlock();
            if (block.equals(Blocks.GRASS_BLOCK)) {
                world.setBlockState(r, Blocks.MYCELIUM.getDefaultState(), 3);
                myce--;
            }
        }
    }

}

