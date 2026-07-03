package juitar.gwrexpansions.network;

import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class GWRENetwork {
    private static final String PROTOCOL_VERSION = "3";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(GWRexpansions.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++, GunSkillPacket.class,
            GunSkillPacket::encode,
            GunSkillPacket::decode,
            GunSkillPacket::handle,
            Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(packetId++, MeatHookInputPacket.class,
            MeatHookInputPacket::encode,
            MeatHookInputPacket::decode,
            MeatHookInputPacket::handle,
            Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(packetId++, CoinHitFeedbackPacket.class,
            CoinHitFeedbackPacket::encode,
            CoinHitFeedbackPacket::decode,
            CoinHitFeedbackPacket::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(packetId++, MeatHookMarkPacket.class,
            MeatHookMarkPacket::encode,
            MeatHookMarkPacket::decode,
            MeatHookMarkPacket::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(packetId++, SuperShotgunFeedbackPacket.class,
            SuperShotgunFeedbackPacket::encode,
            SuperShotgunFeedbackPacket::decode,
            SuperShotgunFeedbackPacket::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
