package net.minigame.home;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.*;

import static net.minigame.home.Core.*;

public class PlayerColor {
    public static final Map<UUID, Color> playerColors = new HashMap<>();
    private static final List<Color> availableColors = new ArrayList<>();
    public static void createColors() {
        availableColors.add(new Color(Blocks.WHITE_WOOL, "WHITE", Formatting.WHITE));
        availableColors.add(new Color(Blocks.BLACK_WOOL, "BLACK", Formatting.BLACK));
        availableColors.add(new Color(Blocks.BLUE_WOOL, "BLUE", Formatting.BLUE));
        availableColors.add(new Color(Blocks.GREEN_WOOL, "GREEN", Formatting.DARK_GREEN));
        availableColors.add(new Color(Blocks.RED_WOOL, "RED", Formatting.RED));
        availableColors.add(new Color(Blocks.YELLOW_WOOL, "YELLOW", Formatting.YELLOW));
        availableColors.add(new Color(Blocks.ORANGE_WOOL, "ORANGE", Formatting.GOLD));
        availableColors.add(new Color(Blocks.PURPLE_WOOL, "PURPLE", Formatting.LIGHT_PURPLE));
        availableColors.add(new Color(Blocks.LIGHT_BLUE_WOOL, "AQUA", Formatting.AQUA));
        availableColors.add(new Color(Blocks.LIME_WOOL, "LIME", Formatting.GREEN));
    }
    public static void randomColor(){
        Random random = new Random();
        LinkedList<Color> availableColorsCopy = new LinkedList<>(availableColors);
        playerColors.clear();
        for (ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()) {
            if(!isSpec(player) && !isAdmin(player)){
                int randomIndex = random.nextInt(availableColorsCopy.size());
                Color randomColor = availableColorsCopy.get(randomIndex);
                playerColors.put(player.getUuid(), randomColor);
                availableColorsCopy.remove(randomIndex);
            }
        }
    }
    public static void randomColorButWhite(){
        Random random = new Random();
        LinkedList<Color> availableColorsCopy = new LinkedList<>(availableColors);
        availableColorsCopy.remove(new Color(Blocks.WHITE_WOOL, "WHITE", Formatting.WHITE));
        playerColors.clear();
        for (ServerPlayerEntity player : MCserver.getPlayerManager().getPlayerList()) {
            if(!isSpec(player) && !isAdmin(player)){
                int randomIndex = random.nextInt(availableColorsCopy.size());
                Color randomColor = availableColorsCopy.get(randomIndex);
                playerColors.put(player.getUuid(), randomColor);
                availableColorsCopy.remove(randomIndex);
            }
        }
    }
    public static class Color {
        private final Block block;
        private final String color;
        private final Formatting textColor;

        public Color(Block block, String color, Formatting textColor){
            this.block = block;
            this.color = color;
            this.textColor = textColor;
        }

        public Block getBlock() {
            return block;
        }

        public String getColor() {
            return color;
        }

        public Formatting getTextColor(){
            return textColor;
        }
    }
}
