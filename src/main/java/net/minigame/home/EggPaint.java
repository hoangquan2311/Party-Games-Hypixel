package net.minigame.home;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minigame.home.Core.*;
import static net.minigame.home.PlayerColor.playerColors;
import static net.minigame.home.PlayerColor.randomColor;

public class EggPaint {
    public static String top1, top2, top3;
    public static int p1, p2, p3;
    private static int gameTimer;
    public static void eggPaintTicks(){
        if(getScore("game","index")==-3){
            if(getScore("cool3","timer")>0){
                if(getScore("cool3","timer")<=100 && getScore("cool3","timer")%20==0){
                    execute("/title @a title {\"text\":\""+getScore("cool3","timer")/20+"\",\"color\":\"yellow\",\"bold\":true}");
                    execute("/execute as @a at @s run playsound block.lever.click ambient @s ~ ~ ~ 1 .7");
                }
                addScore("cool3","timer",-1);
            }
            else {
                execute("/title @a title \"\"");
                execute("/execute as @a at @s run playsound entity.player.burp ambient @s");
                top1=top2=top3="";
                p1=p2=p3=0;
                randomColor();
                setScore("game","index",3);
                setScore("game3","timer",620);
            }
        }
        else if(getScore("game","index")==3){
            for (ServerPlayerEntity play : MCserver.getPlayerManager().getPlayerList()) {
                if(playerColors.containsKey(play.getUuid())){
                    play.sendMessage(color("§lBẠN LÀ "+playerColors.get(play.getUuid()).getColor()+"!",playerColors.get(play.getUuid()).getTextColor()), true);
                    ItemStack egg = new ItemStack(Items.EGG,64);
                    if(!play.inventory.getStack(0).equals(egg))
                        play.inventory.setStack(0, egg);

                }
            }
            if(getScore("game3","timer")>0){
                addScore("game3","timer",-1);
            }
            else {

            }
        }

    }
}
