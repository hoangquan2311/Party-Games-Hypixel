package net.minigame.home;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.minigame.home.InGameScoreboard.inGameSCBinit;
import static net.minigame.home.InGameScoreboard.inGameScoreboardTicks;
import static net.minigame.home.PlayerColor.createColors;
import static net.minigame.home.ReadyScoreboard.*;
import static net.minigame.home.SnowRain.*;
import static net.minigame.home.WoolClaim.*;

public class Core implements ModInitializer {
	public static MinecraftServer MCserver;
	public static CommandManager CMDManager;
	public static ServerCommandSource CMDSource;
	public static ServerWorld world;
	public static int PLAYERS_READY;
	public static int PLAYERS_ACTIVE;
	public static int ALL_PLAYERS;
	public static int PLAYERS_ALIVE;
	private static final int GAMES_POOL = 2;
	public static final int GAMES_PLAY = 2;
	public static int GAMES_PLAY_CURRENT;
	private static final Map<UUID, Boolean> playerAttacking = new HashMap<>();
	private static final List<Integer> gamesList = new ArrayList<>();
	public static LinkedList<Integer> availableGames = new LinkedList<>();
	public static String top1, top2, top3;
	public static Integer star1, star2, star3;
	public static final long SEED = 4527916943051099714L;
	@Override
	public void onInitialize() {
		registerRClickOnReady();
		woolClaimEventsReg();
		// LOAD
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MCserver = server;
			world = server.getOverworld();
			CMDManager = MCserver.getCommandManager();
			CMDSource = MCserver.getCommandSource();
			initialDataWhenOpen();
			createColors();
			readySCBinit();
			inGameSCBinit();
		});
		// TICK
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			//
			ALL_PLAYERS = MCserver.getPlayerManager().getPlayerList().size();
			PLAYERS_ACTIVE = getActivePlayers();
			PLAYERS_ALIVE = getAlivePlayers();
			PLAYERS_READY = getPlayersReady();
			if(getScore("game","index")==0){
				for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()){
					if(player.handSwinging){
						if (!playerAttacking.containsKey(player.getUuid()) || !playerAttacking.get(player.getUuid())) {
							playerAttacking.put(player.getUuid(), true);
							if (player.getStackInHand(Hand.MAIN_HAND).getItem() == Items.LIME_CONCRETE) {
								setScore(player.getEntityName(), "clickGreen", 1);
							} else if (player.getStackInHand(Hand.MAIN_HAND).getItem() == Items.RED_CONCRETE) {
								setScore(player.getEntityName(), "clickRed", 1);
							}
						}
					}
					else {
						playerAttacking.put(player.getUuid(), false);
					}
				}
				if(PLAYERS_READY == PLAYERS_ACTIVE && PLAYERS_ACTIVE>=3) {
					//WHEN ALL PLAYERS ARE READY
					execute( "/team leave @a[team=!admin]");
					execute( "/scoreboard players set @a stars 0");
					top1 = top2 = top3 = "";
					star1 = star2 = star3 = 0;
					randomGame();
				}
				readyScoreboardTicks();
				for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()){
					if(!isAdmin(player) && player.getPos().getY()<70){
						execute("/tp @a[name="+player.getEntityName()+"] -68 96 -81 180 0");
					}
				}
			}
			else if(getScore("game","index")!=0){
				if(!playerAttacking.isEmpty())
					playerAttacking.clear();
				//place update top player above reset game data
				updateTopPlayer();
				if(PLAYERS_ACTIVE<3){
					//CANCLE WHEN PLAYERS ACTIVE BELOW 3
					sendToAll("§e§l[PARTY GAME] §cSố lượng người chơi hiện không đủ để tiếp tục (dưới 3 người), trò chơi đã bị hủy!");
					execute("/title @a title {\"text\":\"Oops!\",\"color\":\"red\",\"bold\":true}");
					resetCoreData();
					resetWoolData();
					resetSnowData();
					resetGamesList();
				}
				woolClaimTicks();
				snowRainTicks();
				inGameScoreboardTicks();
			}
		});
	}

	private void initialDataWhenOpen(){
		for(int i=1;i<=GAMES_POOL;i++){
			gamesList.add(i);
		}
		top1 = top2 = top3 = "";
		star1 = star2 = star3 = 0;
		resetGamesList();
		setScore("game","index",0);
		PLAYERS_READY = 0;
		PLAYERS_ACTIVE = 0;
	}
	private static void updateTopPlayer(){
		Scoreboard scoreboard = MCserver.getScoreboard();
		ScoreboardObjective objective = scoreboard.getObjective("stars");
		List<ScoreboardPlayerScore> sortedScores = scoreboard.getAllPlayerScores(objective)
				.stream()
				.sorted(Comparator.comparingInt(ScoreboardPlayerScore::getScore).reversed())
				.collect(Collectors.toList());
		if(sortedScores.size()>0){
			top1 = sortedScores.get(0).getPlayerName();
			top2 = sortedScores.get(1).getPlayerName();
			top3 = sortedScores.get(2).getPlayerName();
		}
		star1 = getScore(top1,"stars");
		star2 = getScore(top2,"stars");
		star3 = getScore(top3,"stars");
	}
	public static void resetGamesList(){
		availableGames.clear();
		availableGames.addAll(gamesList);
		GAMES_PLAY_CURRENT = 0;
	}
	private void registerRClickOnReady(){
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack heldItem = player.getStackInHand(hand);
			if(getScore("game","index") == 0){
				if (heldItem.getItem() == Items.LIME_CONCRETE){
					setScore(player.getEntityName(),"clickGreen",1);
					return TypedActionResult.success(heldItem);
				}
				else if (heldItem.getItem() == Items.RED_CONCRETE){
					setScore(player.getEntityName(),"clickRed",1);
					return TypedActionResult.success(heldItem);
				}
			}
			return TypedActionResult.pass(heldItem);
		});
	}

	public static void nextGame(int currentIndex,String top1Name){
		execute("/execute as @a at @s run playsound entity.player.burp ambient @s");
		setScore("game","index",10+currentIndex);
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		for(int i=0; i<=10; i++){
				executorService.schedule(() -> {
					execute("/execute at @a[name="+ top1Name +"] run particle minecraft:firework ~ ~2 ~ .1 .1 .1 .6 10 force @a");
					execute("/execute at @a[name="+ top1Name +"] run playsound minecraft:entity.cat.ambient ambient @a ~ ~ ~ 1 1.6");
				}, i, TimeUnit.SECONDS);
		}
		if(GAMES_PLAY_CURRENT < GAMES_PLAY){
			executorService.schedule(() -> {
				if(getScore("game","index")!=0){
					randomGame();
				}
			}, 10, TimeUnit.SECONDS);
		}
		else if(GAMES_PLAY_CURRENT == GAMES_PLAY){
			executorService.schedule(() -> {
				if(getScore("game","index")!=0){
					endGame();
				}
			},10,TimeUnit.SECONDS);
		}
		executorService.shutdown();
	}
	public static void randomGame(){
		execute("/clear @a[team=!admin]");
		execute("/gamemode adventure @a[team=!admin,team=!spec]");
		GAMES_PLAY_CURRENT++;

		Random random = new Random();
		int randomIndex = random.nextInt(availableGames.size());
		int randomGame = availableGames.get(randomIndex);
		availableGames.remove(randomIndex);
		sendToAll("§eGame tiếp theo sẽ bắt đầu trong §c10s§e! Ready!");
		switch (randomGame) {
			case 1:
				execute("/tp @a 17 25 17");
				for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()){
					player.giveItemStack(new ItemStack(Items.DIAMOND_SHOVEL).setCustomName(new LiteralText("§b§lRIGHT CLICK")));
				}
				reloadWoolArena();
				execute("/scoreboard players set @a woolCount 0");
				sendToAll("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§d§l\n                       WOOL CLAIM\n\n§eMục tiêu: Chiếm nhiều ô đất nhất bằng cây xẻng của bạn.\n\n§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				break;
			case 2:
				execute("/tp @a -55 23 36");
				execute("/fill -67 27 24 -43 27 48 air");
				sendToAll("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§f§l\n                       SNOW RAIN\n\n§eMục tiêu: Sống sót khỏi cơn mưa tuyết bằng cách núp dưới mái che!\n\n   §aBạn có thể đánh đối thủ từ §cRound 8 §atrở đi.\n\n§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				break;
		}
		execute("/execute as @a at @s run playsound minecraft:entity.player.levelup ambient @s ~ ~ ~ 1 1");
		setScore("cool"+randomGame,"timer",200);
		setScore("game","index",-randomGame);
	}
	private static void endGame(){
		//EXECUTE ONCE WHEN ALL GAMES ARE DONE
		setScore("game","index",0);
		execute("/clear @a[team=!admin]");
		execute("/gamemode adventure @a[team=!admin,team=!spec]");
		execute("/tp @a -68 96 -81 0 -15");
		//ADD STARS TO ALLSTAR FOR ALL PLAYER
		for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()){
			addScore(player.getEntityName(),"allStars",getScore(player.getEntityName(),"stars"));
		}
		execute("/scoreboard players reset * stars");
		execute("/tp @a[name="+top1+"] -68 99.5 -76 180 25");
		execute("/tp @a[name="+top2+"] -66 98.5 -76 180 25");
		execute("/tp @a[name="+top3+"] -70 97.5 -76 180 25");
		//TELEPORT TOP PLAYERS TO WINNER PODIUM
		ScheduledExecutorService executor1, executor2, executor3;
		executor1 = executor2 = executor3 = Executors.newSingleThreadScheduledExecutor();
		executor1.scheduleAtFixedRate(() ->{
			if(!MCserver.getPlayerManager().getPlayer(top1).getBlockPos().equals(new BlockPos(-68, 99, -76))){
				execute("/tp @a[name="+top1+"] -68 99.5 -76");
			}
			if(!MCserver.getPlayerManager().getPlayer(top2).getBlockPos().equals(new BlockPos(-66, 98, -76))){
				execute("/tp @a[name="+top2+"] -66 98.5 -76");
			}
			if(!MCserver.getPlayerManager().getPlayer(top3).getBlockPos().equals(new BlockPos(-70, 97, -76))){
				execute("/tp @a[name="+top3+"] -70 97.5 -76");
			}
		}, 0, 50,TimeUnit.MILLISECONDS);
		for(int i=0;i<5;i++){
			executor2.schedule(() -> {
				execute("/playsound minecraft:entity.firework_rocket.launch ambient @a -68 99 -76 2");
				executor2.schedule(() ->{
					execute("/playsound minecraft:entity.firework_rocket.blast ambient @a -68 99 -76 2");
				},500,TimeUnit.MILLISECONDS);
			},1000*i,TimeUnit.MILLISECONDS);
		}
		executor3.schedule(() ->{
			execute("/team join notready @a[team=!admin]");
			execute("/gamemode adventure @a");
			execute("/tp @a[team=spec] -68 96 -81 0 0");
			execute("/execute as @a at @s run playsound minecraft:block.beacon.deactivate ambient @s ~ ~ ~ 1");
			executor1.shutdown();
			executor2.shutdown();
			executor3.shutdown();
		},10,TimeUnit.SECONDS);
		resetGamesList();
	}
	public static void resetCoreData(){
		setScore("game","index",0);
		execute("/scoreboard players reset * stars");
		execute("/clear @a[team=!admin]");
		execute("/gamemode adventure @a[team=!admin,team=!spec]");
		execute("/tp @a -68 96 -81 0 -15");
		execute("/team join notready @a[team=!admin]");
		execute("/gamemode adventure @a");
		execute("/tp @a[team=spec] -68 96 -81 0 0");
		execute("/execute as @a at @s run playsound minecraft:block.beacon.deactivate ambient @s ~ ~ ~ 1");
	}
	public static void setScore(String name,String obj, int value){
		Scoreboard scoreboard = MCserver.getScoreboard();
		ScoreboardObjective objective = scoreboard.getObjective(obj);
		if(objective != null){
			scoreboard.getPlayerScore(name, objective).setScore(value);
		}
	}
	public static int getScore(String name, String obj){
		Scoreboard scoreboard = MCserver.getScoreboard();
		ScoreboardObjective objective = scoreboard.getObjective(obj);
		scoreboard.getKnownPlayers();
		if(objective != null && scoreboard.playerHasObjective(name,objective)) {
			return scoreboard.getPlayerScore(name, objective).getScore();
		}
		return 0;
	}
	public static void addScore(String name, String obj, Integer value){
		Scoreboard scoreboard = MCserver.getScoreboard();
		ScoreboardObjective objective = scoreboard.getObjective(obj);
		if(objective != null){
			scoreboard.getPlayerScore(name,objective).incrementScore(value);
		}
	}
	public static MutableText color(String msg, Formatting color){
		Style style = Style.EMPTY.withColor(color);
		return new LiteralText(msg).setStyle(style);
	}
	public static void sendToAll(String msg){
		for (ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()) {
			player.sendMessage(new LiteralText(msg), false);
		}
	}
	public static boolean isAdmin(ServerPlayerEntity player){
		return player.isTeamPlayer(MCserver.getScoreboard().getTeam("admin"));
	}
	public static boolean isSpec(ServerPlayerEntity player){
		return player.isTeamPlayer(MCserver.getScoreboard().getTeam("spec"));
	}
	private static int getActivePlayers(){
		int c=0;
		for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()){
			if(!isAdmin(player) && !isSpec(player))
				c++;
		}
		return c;
	}
	public static int getAlivePlayers(){
		int c=0;
		for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()){
			if(!isAdmin(player) && !player.isSpectator())
				c++;
		}
		return c;
	}
	public static int getPlayersReady(){
		int c=0;
		for(ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()){
			if(player.isTeamPlayer(MCserver.getScoreboard().getTeam("ready")))
				c++;
		}
		return c;
	}
	public static void execute(String cmd){
		CMDManager.execute(CMDSource,cmd);
	}
}
