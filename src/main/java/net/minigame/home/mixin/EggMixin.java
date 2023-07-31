package net.minigame.home.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static net.minigame.home.Core.getScore;
import static net.minigame.home.PlayerColor.playerColors;

@Mixin(EggEntity.class)
public class EggMixin extends ThrownItemEntity {
    public EggMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public EggMixin(EntityType<? extends ThrownItemEntity> entityType, double d, double e, double f, World world) {
        super(entityType, d, e, f, world);
    }

    public EggMixin(EntityType<? extends ThrownItemEntity> entityType, LivingEntity livingEntity, World world) {
        super(entityType, livingEntity, world);
    }

    @Inject(at = @At("HEAD"), method = "onCollision",cancellable = true)
    protected void onCollision(HitResult hitResult, CallbackInfo ci){
        if (getScore("game", "index") == 3 && getOwner()!=null) {
            UUID uuid = getOwner().getUuid();
            if(playerColors.containsKey(uuid)){
                BlockPos pos = new BlockPos(hitResult.getPos().add(0,0,-1));
                BlockState bstate = world.getBlockState(pos);
                if(bstate.isOf(Blocks.WHITE_WOOL) || bstate.isOf(Blocks.BLACK_WOOL) || bstate.isOf(Blocks.BLUE_WOOL) || bstate.isOf(Blocks.GREEN_WOOL) || bstate.isOf(Blocks.RED_WOOL) || bstate.isOf(Blocks.YELLOW_WOOL) || bstate.isOf(Blocks.ORANGE_WOOL) || bstate.isOf(Blocks.PURPLE_WOOL) || bstate.isOf(Blocks.LIGHT_BLUE_WOOL) || bstate.isOf(Blocks.LIME_WOOL)){
                    BlockState b = playerColors.get(uuid).getBlock().getDefaultState();
                    world.setBlockState(pos, b,3);
                    world.setBlockState(pos.add(1,0,0), b,3);
                    world.setBlockState(pos.add(1,1,0), b,3);
                    world.setBlockState(pos.add(0,1,0), b,3);
                    world.setBlockState(pos.add(-1,1,0), b,3);
                    world.setBlockState(pos.add(-1,0,0), b,3);
                    world.setBlockState(pos.add(-1,-1,0), b,3);
                    world.setBlockState(pos.add(0,-1,0), b,3);
                    world.setBlockState(pos.add(1,-1,0), b,3);
                }
            }
        }
        super.onCollision(hitResult);
        if (!this.world.isClient) {
            this.remove();
        }
        ci.cancel();
    }

    @Override
    public Item getDefaultItem() {
        return null;
    }
}
