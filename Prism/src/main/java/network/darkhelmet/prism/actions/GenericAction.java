package network.darkhelmet.prism.actions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import network.darkhelmet.prism.Prism;
import network.darkhelmet.prism.actionlibs.ActionTypeImpl;
import network.darkhelmet.prism.api.ChangeResult;
import network.darkhelmet.prism.api.PrismParameters;
import network.darkhelmet.prism.api.actions.ActionType;
import network.darkhelmet.prism.api.actions.Handler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.UUID;

public abstract class GenericAction implements Handler {
    private static final SimpleDateFormat date = new SimpleDateFormat("yy/MM/dd");
    private static final SimpleDateFormat time = new SimpleDateFormat("hh:mm:ssa");
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private boolean canceled = false;
    private ActionType type;

    private long id;

    private long epoch;

    private String sourceName;

    private UUID playerUuid;

    private Location location;

    private Material material = Material.AIR;

    private BlockData blockData;

    private Material oldBlock = Material.AIR;

    private BlockData oldBlockData;

    private boolean rollbacked;

    private int aggregateCount = 0;

    public GenericAction() {
        epoch = System.currentTimeMillis() / 1000;
    }

    protected static Gson gson() {
        return GenericAction.gson;
    }

    @Override
    public String getCustomDesc() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#getId()
     */
    @Override
    public long getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#setId(int)
     */
    @Override
    public void setId(long id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#getActionTime()
     */
    @Override
    public long getUnixEpoch() {
        return epoch;
    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#setDisplayDate(java.lang.String)
     */
    @Override
    public void setUnixEpoch(long epoch) {
        this.epoch = epoch;
    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#getDisplayDate()
     */
    @Override
    public String getDisplayDate() {
        return date.format(epoch * 1000);
    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#getDisplayTime()
     */
    @Override
    public String getDisplayTime() {
        return time.format(epoch * 1000);
    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#getTimeSince()
     */
    @Override
    public String getTimeSince() {
        long diffInSeconds = System.currentTimeMillis() / 1000 - epoch;

        if (diffInSeconds < 60) {
            return "就在刚刚";
        }

        long period = 24 * 60 * 60;

        final long[] diff = {
              diffInSeconds / period,
              (diffInSeconds / (period /= 24)) % 24,
              (diffInSeconds / (period /= 60)) % 60
        };

        StringBuilder timeAgo = new StringBuilder();

        if (diff[0] > 0) {
            timeAgo.append(diff[0]).append('天');
        }

        if (diff[1] > 0) {
            timeAgo.append(diff[1]).append('时');
        }

        if (diff[2] > 0) {
            timeAgo.append(diff[2]).append('分');
        }

        // 'time_ago' will have something at this point, because if all 'diff's
        // were 0, the first if check would have caught and returned "just now"
        return timeAgo.append("前").toString();

    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#getType()
     */
    @Override
    public ActionType getActionType() {
        return type;
    }

    /**
     * Set Action Type from a String.
     *
     * @param actionType String
     */
    public void setActionType(String actionType) {
        if (actionType != null) {
            setActionType(Prism.getActionRegistry().getAction(actionType));
        }
    }

    /**
     * Set the Action Type.
     *
     * @param type {@link ActionTypeImpl}
     */
    @Override
    public void setActionType(ActionType type) {
        this.type = type;
    }

    private void createWorldIfNull() {
        if (location == null) {
            location = Bukkit.getWorlds().get(0).getSpawnLocation();
        }
    }

    /**
     * Set the player.
     *
     * @param player OfflinePlayer
     */
    public void setPlayer(AnimalTamer player) {
        if (player != null) {
            setUuid(player.getUniqueId());
            this.sourceName = player.getName();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable String getSourceName() {
        if (sourceName != null) {
            return sourceName;
        }
        return Bukkit.getOfflinePlayer(playerUuid).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
        this.playerUuid = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getUuid() {
        return playerUuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUuid(UUID uuid) {
        this.playerUuid = uuid;
        this.sourceName = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setX(double x) {
        createWorldIfNull();
        location.setX(x);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setY(double y) {
        createWorldIfNull();
        location.setY(y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setZ(double z) {
        createWorldIfNull();
        location.setZ(z);
    }

    /**
     * Get World.
     *
     * @return World
     */
    public World getWorld() {
        if (location != null) {
            return location.getWorld();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#setWorld(org.bukkit.World)
     */
    @Override
    public void setWorld(World world) {
        createWorldIfNull();
        location.setWorld(world);
    }

    /**
     * Get Location.
     *
     * @return Location
     */
    public Location getLoc() {
        return location;
    }

    /**
     * Set Location.
     *
     * @param loc Location
     */
    public void setLoc(Location loc) {
        if (loc != null) {
            location = loc.clone();
        } else {
            location = null;
        }
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public void setMaterial(Material material) {
        this.material = material;
    }

    /*
     * (non-Javadoc)
     *
     * @see network.darkhelmet.prism.actions.Handler#getBlockSubId()
     */
    @Override
    public BlockData getBlockData() {
        return blockData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlockData(BlockData data) {
        this.blockData = data;
    }

    @Override
    public short getDurability() {
        return 0;
    }

    @Override
    public void setDurability(short durability) {
    }

    @Override
    public Material getOldMaterial() {
        return oldBlock;
    }

    @Override
    public void setOldMaterial(Material material) {
        this.oldBlock = material;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockData getOldBlockData() {
        return oldBlockData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOldBlockData(BlockData data) {
        this.oldBlockData = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getOldDurability() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOldDurability(short durability) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAggregateCount() {
        return aggregateCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAggregateCount(int aggregateCount) {
        this.aggregateCount = aggregateCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCanceled() {
        return canceled;
    }


    @Override
    public void setCanceled(boolean cancel) {
        this.canceled = cancel;
    }

    @Override
    public boolean isRollbacked() {
        return rollbacked;
    }

    @Override
    public void setRollbacked(boolean rollbacked) {
        this.rollbacked = rollbacked;
    }

    /**
     * Currently these methods are not made available in the api.  As they perform world
     * changes.  This can be reviewed later.
     *
     * @param player Player
     * @param parameters PrismParameters
     * @param isPreview boolean
     * @return ChangeResult
     */
    public ChangeResult applyRollback(Player player, PrismParameters parameters, boolean isPreview) {
        return null;
    }

    /**
     * See above.
     *
     * @see GenericAction#applyRollback(Player, PrismParameters, boolean)
     * @param player Player
     * @param parameters PrismParameters
     * @param isPreview boolean
     * @return ChangeResult
     */
    public ChangeResult applyRestore(Player player, PrismParameters parameters, boolean isPreview) {
        return null;
    }

    /**
     * See above.
     *
     * @see GenericAction#applyRollback(Player, PrismParameters, boolean)
     * @param player Player
     * @param parameters PrismParameters
     * @param isPreview boolean
     * @return ChangeResult
     */
    public ChangeResult applyUndo(Player player, PrismParameters parameters, boolean isPreview) {
        return null;
    }

    /**
     * See above.
     *
     * @see GenericAction#applyRollback(Player, PrismParameters, boolean)
     * @param player Player
     * @param parameters PrismParameters
     * @param isPreview boolean
     * @return ChangeResult
     */
    public ChangeResult applyDeferred(Player player, PrismParameters parameters, boolean isPreview) {
        return null;
    }
}
