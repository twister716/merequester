package com.almostreliable.merequester.requester.abstraction;

import net.minecraft.network.chat.Component;

import com.almostreliable.merequester.requester.Requests;

public interface RequestHost {

    void saveChanges();

    void requestChanged(int index);

    boolean isClientSide();

    Requests getRequests();

    Component getTerminalName();
}
