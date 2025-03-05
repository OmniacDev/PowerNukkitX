package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Transform;
import cn.nukkit.level.Locator;
import cn.nukkit.level.particle.WaxOffParticle;
import cn.nukkit.level.particle.WaxOnParticle;
import cn.nukkit.math.BlockFace;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * @author joserobjr
 * @since 2021-06-14
 */
public interface Waxable {

    @NotNull Transform getLocation();

    default boolean onActivate(@NotNull Item item, @Nullable Player player, BlockFace blockFace, float fx, float fy, float fz) {
        boolean waxed = isWaxed();
        if ((item.getId() != ItemID.HONEYCOMB || waxed) && (!item.isAxe() || !waxed)) {
            return false;
        }

        waxed = !waxed;
        if (!setWaxed(waxed)) {
            return false;
        }

        Locator location = this instanceof Block ? (Locator) this : getLocation();
        if (player == null || !player.isCreative()) {
            if (waxed) {
                item.count--;
            } else {
                item.useOn(this instanceof Block? (Block) this : location.getLevelBlock());
            }
        }
        location.getValidLevel().addParticle(waxed? new WaxOnParticle(location) : new WaxOffParticle(location));
        return true;
    }

    boolean isWaxed();

    boolean setWaxed(boolean waxed);
}
