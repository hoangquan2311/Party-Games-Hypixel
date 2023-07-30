package net.minigame.home;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static net.minigame.home.Core.*;
import static net.minigame.home.SnowRain.CURRENT_WAVE;
import static net.minigame.home.SnowRain.SHELTER;
import static net.minigame.home.WoolClaim.*;

public class InGameScoreboard extends ScoreboardDisplay {
    public static void setLine(int n,String s){
        Scoreboard scoreboard = MCserver.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjective("ingameShow");
        if (objective != null) {
            for (String playerName : scoreboard.getKnownPlayers()) {
                if(scoreboard.playerHasObjective(playerName,objective)){
                    if (scoreboard.getPlayerScore(playerName,objective).getScore() == n && !playerName.equals(s)) {
                        scoreboard.resetPlayerScore(playerName,objective);
                    }
                }
            }
            setScore(s,"ingameShow",n);
        }
    }
    public static void inGameSCBinit(){
        CMDManager.execute(CMDSource,"/scoreboard players reset * ingameShow");
        setLine(14,"      ");
        setLine(11,"     ");
        setLine(7,"    ");
        setLine(5,"  ");
        setLine(4," ");
        setLine(2,"");
    }
    public static void inGameScoreboardTicks(){
        setLine(15, "§7"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/YY")));
        setLine(12,"§eNgười chơi: §a"+PLAYERS_ACTIVE);
        setLine(3,"§eGames: §a"+GAMES_PLAY_CURRENT+"/"+GAMES_PLAY);
        setLine(1, "§7"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        String str="";
        if(availableGames.size()==2)
            str=" - index "+availableGames.get(0)+","+availableGames.get(1);
        else if(availableGames.size()==1)
            str=" - index "+availableGames.get(0);
        setLine(4,"Games left: "+availableGames.size()+str);
        if(getScore("game","index")==1 || getScore("game","index")==-1 || getScore("game","index")==11){
            setLine(13,"§eGame: §d§lWOOL CLAIM");
            if(getScore("game","index")==-1 || getScore("game","index")==11){
                setLine(10,"§b"+ Core.top1+"§f: §a"+star1+"§e★");
                setLine(9,"§b"+Core.top2+"§f: §a"+star2+"§e★");
                setLine(8,"§b"+Core.top3+"§f: §a"+star3+"§e★");
                if(getScore("game","index")==-1){
                    setLine(6,"§eGame bắt đầu sau §c§l"+(getScore("cool1","timer")/20+1));
                }
            }
            else {
                setLine(10,"§b"+WoolClaim.top1 + "§f: §a"+wool1);
                setLine(9,"§b"+WoolClaim.top2 + "§f: §a"+wool2);
                setLine(8,"§b"+WoolClaim.top3 + "§f: §a"+wool3);
                setLine(6,"   ");
            }
        } else if (getScore("game","index")==2 || getScore("game","index")==-2 || getScore("game","index")==12) {
            setLine(13,"§eGame: §f§lSNOW RAIN");
            if(getScore("game","index")==-2 || getScore("game","index")==12){
                setLine(10,"§b"+ Core.top1+"§f: §a"+star1+"§e★");
                setLine(9,"§b"+Core.top2+"§f: §a"+star2+"§e★");
                setLine(8,"§b"+Core.top3+"§f: §a"+star3+"§e★");
                if(getScore("game","index")==-2){
                    setLine(6,"§eGame bắt đầu sau §c§l"+(getScore("cool2","timer")/20+1));
                }
            }
            else {
                setLine(10,"§eWave: §c§l"+CURRENT_WAVE);
                setLine(9,"§eAlive: §a"+PLAYERS_ALIVE+"/"+PLAYERS_ACTIVE);
                setLine(8,"§eMái che: §a"+SHELTER);
                int t = getScore("waveWait","timer");
                if(t>0) setLine(6,"§eNext rain in: §c§l"+(t/20+1));
                else setLine(6,"   ");
            }
        }

    }
}
