package network.darkhelmet.prism.actions;

import io.github.rothes.prismcn.CNLocalization;
import network.darkhelmet.prism.Prism;
import network.darkhelmet.prism.api.ChangeResult;
import network.darkhelmet.prism.api.ChangeResultType;
import network.darkhelmet.prism.api.PrismParameters;
import network.darkhelmet.prism.appliers.ChangeResultImpl;
import network.darkhelmet.prism.utils.block.Utilities;
import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;

import java.util.Locale;

public class HangingItemAction extends GenericAction {

    private HangingItemActionData actionData;

    /**
     * Constructor.
     * @param hanging Hanging item
     */
    public void setHanging(Hanging hanging) {

        actionData = new HangingItemActionData();

        if (hanging != null) {
            this.actionData.type = hanging.getType().name().toLowerCase();
            this.actionData.direction = hanging.getAttachedFace().name().toLowerCase();
            if (hanging instanceof Painting) {
                this.actionData.art = ((Painting) hanging).getArt().name();
            }
            setLoc(hanging.getLocation().getBlock().getLocation());
        }
    }

    @Override
    public boolean hasExtraData() {
        return actionData != null;
    }

    @Override
    public String serialize() {
        return gson().toJson(actionData);
    }

    @Override
    public void deserialize(String data) {
        if (data != null && data.startsWith("{")) {
            actionData = gson().fromJson(data, HangingItemActionData.class);
        }
    }

    /**
     * Get Type.
     *
     * @return String
     */
    @SuppressWarnings("WeakerAccess")
    public String getHangingType() {
        return actionData.type;
    }

    public String getArt() {
        return actionData.art;
    }

    /**
     * Get Direction.
     *
     * @return BlockFace
     */
    @SuppressWarnings("WeakerAccess")
    public BlockFace getDirection() {
        if (actionData.direction != null) {
            return BlockFace.valueOf(actionData.direction.toUpperCase());
        }
        return null;
    }

    @Override
    public String getNiceName() {
        // CN Edition changed
        if (actionData.type == null) {
            return "未知";
        }
        Material material = Material.getMaterial(actionData.type.toUpperCase(Locale.ROOT));
        if (material == null) {
            return actionData.type;
        }
        return CNLocalization.getMaterialLocale(material);
    }

    @Override
    public ChangeResult applyRollback(Player player, PrismParameters parameters, boolean isPreview) {
        return hangItem(player, parameters, isPreview);
    }

    @Override
    public ChangeResult applyRestore(Player player, PrismParameters parameters, boolean isPreview) {
        return hangItem(player, parameters, isPreview);
    }

    /**
     * Get A change result.
     * @param player Player
     * @param parameters Query params
     * @param isPreview is preview.
     * @return ChangeResult
     */
    private ChangeResult hangItem(Player player, PrismParameters parameters, boolean isPreview) {
        if (actionData == null) {
            Prism.debug(parameters.getProcessType() + "已跳过 - 悬挂行为数据为 null");
            return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
        }

        final BlockFace attachedFace = getDirection();

        final Location loc = getLoc().getBlock().getRelative(getDirection())
                .getLocation();

        // Ensure there's a block at this location that accepts an attachment
        if (Utilities.materialMeansBlockDetachment(loc.getBlock().getType())) {
            Prism.debug(parameters.getProcessType() + "悬挂已跳过 - 方块会脱离: "
                    + loc.getBlock().getType());
            return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
        }
        try {
            if (isPreview) {
                return new ChangeResultImpl(ChangeResultType.PLANNED, null);
            }
            if (getHangingType().equals("item_frame") || getHangingType().equals("物品展示框")) {
                final Hanging hangingItem = getWorld().spawn(loc, ItemFrame.class);
                hangingItem.setFacingDirection(attachedFace, true);
                return new ChangeResultImpl(ChangeResultType.APPLIED, null); //no change recorded
            } else if (getHangingType().equals("glow_item_frame") || getHangingType().equals("发光的物品展示框")) {
                final GlowItemFrame hangingItem = getWorld().spawn(loc, GlowItemFrame.class);
                hangingItem.setFacingDirection(attachedFace, true);
                return new ChangeResultImpl(ChangeResultType.APPLIED, null); //no change recorded
            } else if (getHangingType().equals("painting") || getHangingType().equals("画")) {
                final Painting hangingItem = getWorld().spawn(loc, Painting.class);
                hangingItem.setFacingDirection(attachedFace, true);
                Art art = Art.getByName(getArt());
                if (art != null) {
                    hangingItem.setArt(art);
                }
                return new ChangeResultImpl(ChangeResultType.APPLIED, null); //no change recorded
            }
        } catch (final IllegalArgumentException e) {
            // Something interfered with being able to place the painting
        }
        return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
    }

    @SuppressWarnings("WeakerAccess")
    public static class HangingItemActionData {
        public String type;
        public String direction;
        public String art;
    }
}