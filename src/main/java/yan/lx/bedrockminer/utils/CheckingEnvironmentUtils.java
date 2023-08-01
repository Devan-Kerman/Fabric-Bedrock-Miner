package yan.lx.bedrockminer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.minecraft.block.Block.sideCoversSmallSquare;

public class CheckingEnvironmentUtils {

    /*** 找到附近的平地放置红石火把
     * @param world 客户端世界
     * @param pistonBlockPos 活塞位置
     * @return 可以直接放置红石火把的位置
     */
    public static List<BlockPos> findNearbyFlatBlockToPlaceRedstoneTorch(ClientWorld world, BlockPos pistonBlockPos) {
        var list = new ArrayList<BlockPos>();
        for (Direction direction : List.of(Direction.DOWN, Direction.EAST, Direction.WEST)) {
            var blockPos = pistonBlockPos.offset(direction);
            var blockState = world.getBlockState(blockPos);
            if (!sideCoversSmallSquare(world, blockPos.down(), Direction.UP)) {
                continue;
            }
            if (blockState.isReplaceable() || blockState.isOf(Blocks.REDSTONE_TORCH)) {
                list.add(blockPos);
            }
        }
        return list;
    }

    /**
     * 查找可能放置粘液块的位置
     */
    public static List<BlockPos> findPossibleSlimeBlockPos(ClientWorld world, BlockPos pistonBlockPos) {
        var list = new ArrayList<BlockPos>();
        for (Direction direction : List.of(Direction.DOWN, Direction.EAST, Direction.WEST)) {
            BlockPos redTorchPos = pistonBlockPos.offset(direction);
            BlockPos BaseBlockPos = redTorchPos.down();
            if (!world.getBlockState(redTorchPos).isReplaceable() || !world.getBlockState(BaseBlockPos).isReplaceable()) {
                continue;
            }
            list.add(BaseBlockPos);
        }
        return list;
    }

    public static boolean has2BlocksOfPlaceToPlacePiston(ClientWorld world, BlockPos blockPos) {
        BlockPos pos1 = blockPos.north();          // 活塞位置
        BlockPos pos2 = blockPos.north().north();     // 活塞臂位置
        // 获取硬度, 打掉0硬度值的方块
        var blockState1 = world.getBlockState(pos1);
        if (!blockState1.isAir() && blockState1.getBlock().getHardness() < 45f) {
            BlockBreakerUtils.breakPistonBlock(pos1);
        }
        var blockState2 = world.getBlockState(pos1);
        if (!blockState2.isAir() && blockState2.getBlock().getHardness() < 45f) {
            BlockBreakerUtils.breakPistonBlock(pos2);
        }

        // 实体碰撞箱
        boolean b = true;
        var state = Blocks.PISTON.getDefaultState();
        var shape = state.getCollisionShape(world, pos1);
        if (!shape.isEmpty()) {
            for (var entity : world.getEntities()) {
                // 过滤掉落物实体
                if (entity instanceof ItemEntity) {
                    continue;
                }
                if (entity.collidesWithStateAtPos(pos1, state)) {
                    b = false;
                }
            }
        }

        // 判断活塞位置和活塞臂位置是否可以放置
        return world.getBlockState(pos1).isReplaceable() && world.getBlockState(pos2).isReplaceable() && b;
    }

    public static List<BlockPos> findNearbyRedstoneTorch(ClientWorld world, BlockPos pistonBlockPos) {
        List<BlockPos> list = new ArrayList<>();
        list.add(pistonBlockPos.east());
        list.add(pistonBlockPos.west());
        list.add(pistonBlockPos.down());

        Iterator<BlockPos> iterator = list.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            Block block = world.getBlockState(pos).getBlock();
            if (block != Blocks.REDSTONE_TORCH && block != Blocks.REDSTONE_WALL_TORCH) {
                iterator.remove();
            }
        }
        return list;
    }

    public static boolean canPlace(BlockPos blockPos, Block block, Direction direction) {
        var player = MinecraftClient.getInstance().player;
        var world = MinecraftClient.getInstance().world;
        if (player != null && world != null) {
            // 放置检测
            var item = block.asItem();
            var context = new ItemPlacementContext(player, Hand.MAIN_HAND, item.getDefaultStack(), new BlockHitResult(blockPos.toCenterPos(), direction, blockPos, false));
            // 实体碰撞箱
            boolean b = true;
            var state = Blocks.PISTON.getDefaultState();
            var shape = state.getCollisionShape(world, blockPos);
            if (!shape.isEmpty()) {
                for (var entity : world.getEntities()) {
                    // 过滤掉落物实体
                    if (entity instanceof ItemEntity) {
                        continue;
                    }
                    if (entity.collidesWithStateAtPos(blockPos, state)) {
                        b = false;
                    }
                }
            }
            return context.canPlace() && b;
        }
        return false;
    }
}
