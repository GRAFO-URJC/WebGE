package com.engine.algorithm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelEvaluatorTest {

    @Test
    void relativeErrorHandlesZeroTargets() {
        assertEquals(0.0, ModelEvaluator.computeRelativeError(new double[]{0.0}, new double[]{0.0}));
        assertEquals(2.0, ModelEvaluator.computeRelativeError(new double[]{0.0}, new double[]{2.0}));
        assertEquals(0.25, ModelEvaluator.computeRelativeError(new double[]{0.0, 2.0}, new double[]{0.0, 1.0}));
    }

    @Test
    void relativeErrorRejectsMismatchedOrEmptyInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> ModelEvaluator.computeRelativeError(new double[0], new double[0]));
        assertThrows(IllegalArgumentException.class,
                () -> ModelEvaluator.computeRelativeError(new double[]{1.0}, new double[0]));
    }

    @Test
    void relativeErrorUsesMeanRelativeErrorForNonZeroTargets() {
        assertEquals(0.5, ModelEvaluator.computeRelativeError(
                new double[]{2.0, 4.0}, new double[]{1.0, 6.0}), 1e-12);
    }

    @Test
    void relativeErrorReturnsWorstFiniteFitnessForNonFiniteValues() {
        assertEquals(Double.MAX_VALUE, ModelEvaluator.computeRelativeError(
                new double[]{1.0}, new double[]{Double.NaN}));
        assertEquals(Double.MAX_VALUE, ModelEvaluator.computeRelativeError(
                new double[]{Double.POSITIVE_INFINITY}, new double[]{1.0}));
    }

    @Test
    void objectiveCalculationSupportsRelativeErrorWithZeroTarget() {
        String[][] target = {{"Y"}, {"0"}, {"2"}};
        String[] prediction = {"model", "1", "1"};

        assertEquals(0.75, ModelEvaluator.calculateObjective(target, prediction, "REL"), 1e-12);
    }
}
