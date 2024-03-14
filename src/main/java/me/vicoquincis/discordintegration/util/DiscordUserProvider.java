package me.vicoquincis.discordintegration.util;

import io.socket.socketio.server.SocketIoSocket;
import me.vicoquincis.discordintegration.DiscordIntegration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DiscordUserProvider {
    private final HashMap<String, String> userList = new HashMap<>();
    DiscordIntegration plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);

    public DiscordUserProvider() {
        requestList();
    }

    public void requestList() {
        SocketIoSocket.ReceivedByRemoteAcknowledgementCallback callback = data -> {
            try {
                JSONObject res = (JSONObject) data[0];
                if (res.getString("status").equals("ok")) {
                    JSONArray users = res.getJSONArray("users");
                    plugin.getLogger().info("Received " + users.length() + " discord user entries");
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        userList.put(user.getString("id"), user.getString("tag"));
                    }
                } else {
                    plugin.getLogger().warning("There was an error fetching user list. Callback: " + res.getString("status"));
                }
            } catch (JSONException e) {
                plugin.getLogger().warning("There was an error decoding the callback payload: " + e.getMessage());
                e.printStackTrace();
            }
        };
        plugin.socketSend("user list plz", new Object[]{}, callback);
    }

    public HashMap<String, String> getList() {
        if (userList.isEmpty()) {
            plugin.getLogger().info("Fetching discord user list");
            requestList();
            return new HashMap<>();
        }
        return userList;
    }

    @Nullable
    public String getUserTag(String id) {
        return userList.get(id);
    }

    @Nullable
    public String getUserId(String tag) {
        Set matches = userList.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), tag))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (matches.size() == 0) return null;
        return (String)matches.toArray()[0];
    }

    public void addUser(String id, String tag) {
        userList.put(id, tag);
    }
}
