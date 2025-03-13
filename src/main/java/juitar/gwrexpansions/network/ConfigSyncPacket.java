package juitar.gwrexpansions.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import juitar.gwrexpansions.config.GWREConfig;
import java.util.function.Supplier;

public class ConfigSyncPacket {
    private String configPath;
    private double value;

    public static void handle(ConfigSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        // 服务端向客户端同步配置值
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            GWREConfig.updateCachedValue(msg.configPath, msg.value);
        }
    }
} 