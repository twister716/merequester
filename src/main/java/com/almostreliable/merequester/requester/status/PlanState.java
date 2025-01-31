package com.almostreliable.merequester.requester.status;

import com.almostreliable.merequester.requester.RequesterBlockEntity;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class PlanState implements StatusState {

    private final Future<? extends ICraftingPlan> future;

    PlanState(Future<? extends ICraftingPlan> future) {
        this.future = future;
    }

    @Override
    public StatusState handle(RequesterBlockEntity host, int index) {
        if (!future.isDone()) return this;
        if (future.isCancelled()) return IDLE;

        try {
            var plan = future.get();
            if (!plan.missingItems().isEmpty()) {
                return new MissingState();
            }

            var submitResult = host.getMainNodeGrid().getCraftingService().submitJob(plan, host, null, false, host.getActionSource());
            if (!submitResult.successful() || submitResult.link() == null) {
                return IDLE;
            }

            host.getStorageManager().get(index).setTotalAmount(plan.finalOutput().amount());
            return new LinkState(Objects.requireNonNull(submitResult.link()));
        } catch (InterruptedException | ExecutionException e) {
            return IDLE;
        }
    }

    @Override
    public RequestStatus type() {
        return RequestStatus.PLAN;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return future.isDone() && !future.isCancelled() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }
}
