package net.minigame.home;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static net.minigame.home.Core.*;

public class ReadyScoreboard extends ScoreboardDisplay {
    public static void setLine(int n,String s){
        Scoreboard scoreboard = MCserver.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjective("readyShow");
        if (objective != null) {
            for (String playerName : scoreboard.getKnownPlayers()) {
                if(scoreboard.playerHasObjective(playerName,objective)){
                    if (scoreboard.getPlayerScore(playerName,objective).getScore() == n && !playerName.equals(s)) {
                        scoreboard.resetPlayerScore(playerName,objective);
                    }
                }
            }
            setScore(s,"readyShow",n);
        }
    }
    public static void readySCBinit(){
        CMDManager.execute(CMDSource,"/scoreboard players reset * readyShow");
        setLine(8,"   ");
        setLine(5,"  ");
        setLine(2," ");
    }
    public static void readyScoreboardTicks(){
        setLine(9, "§7"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/YY")));
        setLine(7,"§eGame: §aLobby");
        setLine(6,"§eSẵn sàng: §a"+PLAYERS_READY+"/"+PLAYERS_ACTIVE);
        if(PLAYERS_ACTIVE<3){
            setLine(4,("§eCần thêm §c"+(3-PLAYERS_ACTIVE)+"§e người"));
            setLine(3,("§echơi nữa!"));
        }
        else{
            setLine(4,("§eCòn §c"+(PLAYERS_ACTIVE-PLAYERS_READY)+" §engười chơi"));
            setLine(3,("§echưa sẵn sàng!"));
        }
        setLine(1, "§7"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
    }
}
