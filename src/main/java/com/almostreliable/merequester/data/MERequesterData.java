package com.almostreliable.merequester.data;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

import com.almostreliable.merequester.BuildConfig;

import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Consumer;

public final class MERequesterData {

    private MERequesterData() {}

    public static final DeferredRegister.DataComponents DR = DeferredRegister
        .createDataComponents(BuildConfig.MOD_ID);

    public static final DataComponentType<List<MERequesterRequest>> EXPORTED_REQUESTER_REQUESTS = register("exported_requests", builder -> {
        builder.persistent(MERequesterRequest.CODEC.listOf())
            .networkSynchronized(MERequesterRequest.STREAM_CODEC.apply(ByteBufCodecs.list()));
    });

    private static <T> DataComponentType<T> register(String name, Consumer<DataComponentType.Builder<T>> customizer) {
        var builder = DataComponentType.<T> builder();
        customizer.accept(builder);
        var componentType = builder.build();
        DR.register(name, () -> componentType);
        return componentType;
    }
}
