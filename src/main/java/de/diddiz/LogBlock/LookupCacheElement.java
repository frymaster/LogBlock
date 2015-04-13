package de.diddiz.LogBlock;

import de.diddiz.LogBlock.LookupCacheElement.Style;
import mkremins.fanciful.FancyMessage;
import static org.bukkit.ChatColor.stripColor;
import org.bukkit.Location;

public interface LookupCacheElement {

    public enum Style {
        COLORED,PLAIN,JSON,DATA
    }

    public Location getLocation();

    public String getMessage();

    public String getMessage(Style style);

    public FancyMessage getJsonMessage();
}

abstract class AbstractLookupCacheElement implements LookupCacheElement{

    @Override
    public String getMessage(Style style) {
        switch (style) {
            case PLAIN:
                return stripColor(getMessage());
            case DATA:
                return getDataMessage();
            case JSON:
                return getJsonMessage().toJSONString();
            default:
                return getMessage();
        }
    }

    public abstract String getDataMessage();

}