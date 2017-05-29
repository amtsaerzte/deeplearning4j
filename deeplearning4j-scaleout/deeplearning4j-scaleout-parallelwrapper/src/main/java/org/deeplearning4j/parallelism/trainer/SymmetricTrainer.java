package org.deeplearning4j.parallelism.trainer;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.SharedGradient;
import org.deeplearning4j.optimize.solvers.accumulation.GradientsAccumulator;
import org.deeplearning4j.parallelism.ParallelWrapper;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.MultiDataSet;

/**
 * This trainer implementation does parallel training via gradients broadcasts.
 * After each iteration, gradients from this trainer will be propagated & applied to all other trainers
 *
 * @author raver119@gmail.com
 */
@Slf4j
public class SymmetricTrainer extends DefaultTrainer implements CommunicativeTrainer {
    protected GradientsAccumulator accumulator;

    public SymmetricTrainer(@NonNull Model originalModel, int threadIdx, @NonNull WorkspaceMode mode, @NonNull ParallelWrapper wrapper) {
        super();
        this.originalModel = originalModel;
        this.threadId = threadIdx;
        this.workspaceMode = mode;
        this.parallelWrapper = wrapper;
        this.accumulator = wrapper.getGradientsAccumulator();
    }

    // FIXME: delete this method, it's not needed anymore
    @Deprecated
    public void enqueueGradient(SharedGradient gradient) {
        //log.info("Gradient attached: {}", gradient.getGradient().isAttached());
        //extractor.enqueueGradient(gradient);
    }


    @Override
    public boolean averagingRequired() {
        return false;
    }

    // FIXME: delete this method, it's not needed anymore
    @Override
    protected void fit(DataSet dataSet) {
        super.fit(dataSet);

        // gradients should be extracted here
        // and broadcasted to all trainers
/*
        while (!extractor.getOwnGradients().isEmpty()) {
            // TODO: ensure gradients array is detached!!!

            parallelWrapper.broadcastGradients(extractor.getOwnGradients().poll());
        }
        */
    }

    // FIXME: delete this method, it's not needed anymore
    @Override
    protected void fit(MultiDataSet dataSet) {
        super.fit(dataSet);

        // gradients should be extracted here
    }

    @Override
    protected void postInit() {
        super.postInit();

        if (accumulator == null) {
            log.warn("GradientsAccumulator is undefined, gradients sharing will be skipped");
            return;
        }

        // just pass accumulator down the hill
        if (replicatedModel instanceof ComputationGraph) {
            ((ComputationGraph) replicatedModel).setGradientsAccumulator(accumulator);
        } else if (replicatedModel instanceof MultiLayerNetwork) {
            ((MultiLayerNetwork) replicatedModel).setGradientsAccumulator(accumulator);
        }
    }




}
