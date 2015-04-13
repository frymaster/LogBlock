package de.diddiz.LogBlock;

import de.diddiz.LogBlock.config.Config;
import de.diddiz.util.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.ResultSet;
import java.sql.SQLException;

import static de.diddiz.util.LoggingUtil.checkText;
import static de.diddiz.util.MaterialName.materialName;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mkremins.fanciful.FancyMessage;
import net.amoebaman.util.Reflection;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class BlockChange extends AbstractLookupCacheElement implements LookupCacheElement {
    public final long id, date;
    public final Location loc;
    public final Actor actor;
    public final String playerName;
    public final int replaced, type;
    public final byte data;
    public final String signtext;
    public final ChestAccess ca;
    public int index = 0;

    public BlockChange(long date, Location loc, Actor actor, int replaced, int type, byte data, String signtext, ChestAccess ca) {
        id = 0;
        this.date = date;
        this.loc = loc;
        this.actor = actor;
        this.replaced = replaced;
        this.type = type;
        this.data = data;
        this.signtext = checkText(signtext);
        this.ca = ca;
        this.playerName = actor == null ? null : actor.getName();
    }

    public BlockChange(int index, long date, Location loc, Actor actor, int replaced, int type, byte data, String signtext, ChestAccess ca) {
        this(date, loc, actor, replaced, type, data, signtext, ca);
        this.index = index;
    }

    public BlockChange(ResultSet rs, QueryParams p) throws SQLException {
        id = p.needId ? rs.getInt("id") : 0;
        date = p.needDate ? rs.getTimestamp("date").getTime() : 0;
        loc = p.needCoords ? new Location(p.world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z")) : null;
        actor = p.needPlayer ? new Actor(rs) : null;
        playerName = p.needPlayer ? rs.getString("playername") : null;
        replaced = p.needType ? rs.getInt("replaced") : 0;
        type = p.needType ? rs.getInt("type") : 0;
        data = p.needData ? rs.getByte("data") : (byte) 0;
        signtext = p.needSignText ? rs.getString("signtext") : null;
        ca = p.needChestAccess && rs.getShort("itemtype") != 0 && rs.getShort("itemamount") != 0 ? new ChestAccess(rs.getShort("itemtype"), rs.getShort("itemamount"), rs.getShort("itemdata")) : null;
    }

    public BlockChange(int index, ResultSet rs, QueryParams p) throws SQLException {
        this(rs,p);
        this.index = index;
    }

    public String getVerb() {
            if (BukkitUtils.getContainerBlocks().contains(Material.getMaterial(type))) {
                return "opened ";
            } else if (type == 64 || type == 71)
            // This is a problem that will have to be addressed in LB 2,
            // there is no way to tell from the top half of the block if
            // the door is opened or closed.
            {
                return "moved ";
            }
            // Trapdoor
            else if (type == 96) {
                return(data < 8 || data > 11) ? "opened " : "closed ";
            }
            // Fence gate
            else if (type == 107) {
                return(data > 3 ? "opened " : "closed ");
            } else if (type == 69) {
                return "switched ";
            } else if (type == 77 || type == 143) {
                return "pressed ";
            } else if (type == 92) {
                return "ate a piece of ";
            } else if (type == 25 || type == 93 || type == 94 || type == 149 || type == 150) {
                return("changed ");
            } else if (type == 70 || type == 72 || type == 147 || type == 148) {
                return "stepped on ";
            } else if (type == 132) {
                return "ran into ";
            }
            return "interacted with ";
    }
    @Override
    public String toString() {
        final StringBuilder msg = new StringBuilder();
        if (date > 0) {
            msg.append(Config.formatter.format(date)).append(" ");
        }
        if (actor != null) {
            msg.append(actor.getName()).append(" ");
        }
        if (signtext != null) {
            final String action = type == 0 ? "destroyed " : "created ";
            if (!signtext.contains("\0")) {
                msg.append(action).append(signtext);
            } else {
                msg.append(action).append(materialName(type != 0 ? type : replaced)).append(" [").append(signtext.replace("\0", "] [")).append("]");
            }
        } else if (type == replaced) {
            if (type == 0) {
                msg.append("did an unspecified action");
            } else if (ca != null) {
                if (ca.itemType == 0 || ca.itemAmount == 0) {
                    msg.append("looked inside ").append(materialName(type));
                } else if (ca.itemAmount < 0) {
                    msg.append("took ").append(-ca.itemAmount).append("x ").append(materialName(ca.itemType, ca.itemData)).append(" from ").append(materialName(type));
                } else {
                    msg.append("put ").append(ca.itemAmount).append("x ").append(materialName(ca.itemType, ca.itemData)).append(" into ").append(materialName(type));
                }
            } else {
                msg.append(getVerb()).append(materialName(type));
            }
        } else if (type == 0) {
            msg.append("destroyed ").append(materialName(replaced, data));
        } else if (replaced == 0) {
            msg.append("created ").append(materialName(type, data));
        } else {
            msg.append("replaced ").append(materialName(replaced, (byte) 0)).append(" with ").append(materialName(type, data));
        }
        if (loc != null) {
            msg.append(" at ").append(loc.getBlockX()).append(":").append(loc.getBlockY()).append(":").append(loc.getBlockZ());
        }
        return msg.toString();
    }

    @Override
    public Location getLocation() {
        return loc;
    }

    @Override
    public String getMessage() {
        return toString();
    }

    @Override
    public String getDataMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FancyMessage getJsonMessage() {
        FancyMessage msg = new FancyMessage();
        if (date > 0) {
            msg.text(Config.formatter.format(date) + " ");
        }
        if (actor != null) {
            msg.then(actor.getName() + " ").formattedTooltip(new FancyMessage("UUID: ").then(actor.getUUID()).color(ChatColor.GREEN));
        }
        if (signtext != null) {
            final String action = type == 0 ? "destroyed " : "created ";
            msg.then(action);
            if (!signtext.contains("\0")) {
                msg.then(signtext);
            } else {
                msg.then(materialName(type != 0 ? type : replaced)).then(" [").then(signtext.replace("\0", "] [")).then("]");
            }
        } else if (type == replaced) {
            if (type == 0) {
                msg.then("did an unspecified action");
            } else if (ca != null) {
                if (ca.itemType == 0 || ca.itemAmount == 0) {
                    msg.then("looked inside ");
                    fancyMaterial(msg,type,data);
                } else if (ca.itemAmount < 0) {
                    msg.then("took ").then(Integer.toString(-ca.itemAmount)).then("x ");
                    fancyMaterial(msg,ca.itemType, (short) 0, ca.itemData, -ca.itemAmount);
                    msg.then(" from ");
                    fancyMaterial(msg,type,data);
                } else {
                    msg.then("put ").then(Integer.toString(ca.itemAmount)).then("x ");
                    fancyMaterial(msg,ca.itemType, (short) 0, ca.itemData, ca.itemAmount);
                    msg.then(" into ");
                    fancyMaterial(msg,type,data);
                }
            } else {
                msg.then(getVerb());
                fancyMaterial(msg,type,data);
            }
        } else if (type == 0) {
            msg.then("destroyed ");
            fancyMaterial(msg,replaced, data);
        } else if (replaced == 0) {
            msg.then("created ");
            fancyMaterial(msg,type, data);
        } else {
            msg.then("replaced ");
            fancyMaterial(msg,replaced, (short) 0);
            msg.then(" with ");
            fancyMaterial(msg,type, data);
        }
        if (loc != null) {
            msg.then(" at ").then(Integer.toString(loc.getBlockX())).then(":").then(Integer.toString(loc.getBlockY())).then(":").then(Integer.toString(loc.getBlockZ()));
        }
        return msg;
    }
    
    public FancyMessage fancyMaterial(FancyMessage msg, int type, short data) {
        return fancyMaterial(msg,type,data,(short) 0,1);
    }

    public FancyMessage fancyMaterial(FancyMessage msg, int type, short data, int amount) {
        return fancyMaterial(msg,type,data,(short) 0,amount);
    }

    public FancyMessage fancyMaterial(FancyMessage msg, int type, short data, short damage, int amount) {
        Byte dataByte = null;
        if (data > 0) dataByte = (byte) data;
        return msg.then(materialName(type, data)).itemTooltip(new ItemStack(type,amount, damage,dataByte)).then(Integer.toString(type) + ":" + Short.toString(data) + ":" + Short.toString(damage) + ":" + Integer.toString(amount));
//        try {
//            
//            
//            //Object nmsItem = Reflection.getMethod(Reflection.getOBCClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class).invoke(null, itemStack);
//            //return itemTooltip(Reflection.getMethod(Reflection.getNMSClass("ItemStack"), "save", Reflection.getNMSClass("NBTTagCompound")).invoke(nmsItem, Reflection.getNMSClass("NBTTagCompound").newInstance()).toString());
//            ItemStack itemStack = new ItemStack(type,amount, damage,(byte) data);
//            Object nmsItem = Reflection.getMethod(Reflection.getOBCClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class).invoke(null, itemStack);
//            String itemString = Reflection.getMethod(Reflection.getNMSClass("ItemStack"), "save", Reflection.getNMSClass("NBTTagCompound")).invoke(nmsItem, Reflection.getNMSClass("NBTTagCompound").newInstance()).toString();
//            
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(BlockChange.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalArgumentException ex) {
//            Logger.getLogger(BlockChange.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InvocationTargetException ex) {
//            Logger.getLogger(BlockChange.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            Logger.getLogger(BlockChange.class.getName()).log(Level.SEVERE, null, ex);
//        }
//         
    }
}
