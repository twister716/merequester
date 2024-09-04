package com.almostreliable.merequester.requester.status;

import com.almostreliable.merequester.requester.RequesterBlockEntity;

import appeng.api.networking.ticking.TickRateModulation;

import org.jetbrains.annotations.Nullable;

public class MissingState implements StatusState {

    @Nullable
    private PlanState simulatedPlanState;

    MissingState() {}

    @Override
    public StatusState handle(RequesterBlockEntity host, int slot) {
        // re-run the crafting job plan to see if we are still missing ingredients
        if (simulatedPlanState != null) {
            var planSim = simulatedPlanState.handle(host, slot);
            simulatedPlanState = null;
            return planSim;
        }

        var idleSim = IDLE.handle(host, slot);
        if (idleSim == IDLE || idleSim == EXPORT) {
            // idle simulation returning idle means a request is no
            // longer required because we have enough items
            // idle state returning export should not be possible,
            // but just in case, we will return to idle to handle it
            return IDLE;
        }

        // idle sim returned that we can request
        var requestSim = REQUEST.handle(host, slot);
        if (requestSim == IDLE) {
            return IDLE;
        }

        // request sim returned that we can start planning
        simulatedPlanState = (PlanState) requestSim;
        return this;
    }

    @Override
    public RequestStatus type() {
        return RequestStatus.MISSING;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE;
    }
}
