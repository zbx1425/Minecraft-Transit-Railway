package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.data.TrainVirtualDrive;
import cn.zbx1425.mtrsteamloco.gui.ConfigScreen;
import cn.zbx1425.mtrsteamloco.network.PacketVirtualDrive;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.function.Function;

public class NTEClientCommand {

    public static <T> void register(CommandDispatcher<T> dispatcher, Function<String, LiteralArgumentBuilder<T>> literal) {
        dispatcher.register(literal.apply("mtrnte")
                .then(literal.apply("config")
                        .executes(context -> {
                            Minecraft.getInstance().tell(() -> {
                                Minecraft.getInstance().setScreen(ConfigScreen.createScreen(Minecraft.getInstance().screen));
                            });
                            return 1;
                        }))
                .then(literal.apply("hideriding")
                        .executes(context -> {
                            ClientConfig.hideRidingTrain = !ClientConfig.hideRidingTrain;
                            return 1;
                        }))
                .then(literal.apply("virtdrive")
                        .executes(context -> {
                            TrainVirtualDrive.startDrivingRidingTrain();
                            return 1;
                        })
                        .then(literal.apply("stop")
                                .executes(context -> {
                                    TrainVirtualDrive.stopDriving();
                                    return 1;
                                }))
                )
                .then(literal.apply("stat")
                        .executes(context -> {
                            Minecraft.getInstance().tell(() -> {
                                String info = RenderUtil.getRenderStatusMessage();
                                Minecraft.getInstance().player.sendSystemMessage(Text.literal(info));
                            });
                            return 1;
                        }))
        );
    }
}
