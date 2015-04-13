package de.diddiz.LogBlock;

import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

import static de.diddiz.util.LoggingUtil.checkText;
import mkremins.fanciful.FancyMessage;

public class ChatMessage extends AbstractLookupCacheElement implements LookupCacheElement {
    final long id, date;
    final String playerName, message;
    final Actor player;

    public ChatMessage(Actor player, String message) {
        id = 0;
        date = System.currentTimeMillis() / 1000;
        this.player = player;
        this.message = checkText(message);
        this.playerName = player == null ? null : player.getName();
    }

    public ChatMessage(ResultSet rs, QueryParams p) throws SQLException {
        id = p.needId ? rs.getInt("id") : 0;
        date = p.needDate ? rs.getTimestamp("date").getTime() : 0;
        player = p.needPlayer ? new Actor(rs) : null;
        playerName = p.needPlayer ? rs.getString("playername") : null;
        message = p.needMessage ? rs.getString("message") : null;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public String getMessage() {
        return (player != null ? "<" + player.getName() + "> " : "") + (message != null ? message : "");
    }

    @Override
    public String getDataMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FancyMessage getJsonMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
