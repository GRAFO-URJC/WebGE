package com.gramevapp.web.service;

import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.Grammar;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.GrammarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecordAccessServiceTest {

    private static final Long OWNER_ID = 12L;
    private static final Long OTHER_OWNER_ID = 34L;

    private final UserService userService = mock(UserService.class);
    private final ExperimentService experimentService = mock(ExperimentService.class);
    private final RunService runService = mock(RunService.class);
    private final GrammarRepository grammarRepository = mock(GrammarRepository.class);
    private final RecordAccessService accessService = new RecordAccessService();
    private final User owner = new User();

    @BeforeEach
    void setUp() {
        owner.setId(OWNER_ID);
        when(userService.getLoggedInUser()).thenReturn(owner);
        ReflectionTestUtils.setField(accessService, "userService", userService);
        ReflectionTestUtils.setField(accessService, "experimentService", experimentService);
        ReflectionTestUtils.setField(accessService, "runService", runService);
        ReflectionTestUtils.setField(accessService, "grammarRepository", grammarRepository);
    }

    @Test
    void allowsAccessToOwnedDataset() {
        Dataset dataset = new Dataset();
        dataset.setUserIdUserId(OWNER_ID);
        when(experimentService.findExperimentDataTypeById(1L)).thenReturn(dataset);

        assertSame(dataset, accessService.requireOwnedDataset(1L));
    }

    @Test
    void rejectsDatasetOwnedByAnotherUser() {
        Dataset dataset = new Dataset();
        dataset.setUserIdUserId(OTHER_OWNER_ID);
        when(experimentService.findExperimentDataTypeById(1L)).thenReturn(dataset);

        assertNotFound(() -> accessService.requireOwnedDataset(1L));
    }

    @Test
    void allowsAccessToOwnedExperimentAndRejectsOtherUsersExperiment() {
        Experiment owned = new Experiment();
        owned.setUserId(owner);
        when(experimentService.findExperimentById(1L)).thenReturn(owned);
        assertSame(owned, accessService.requireOwnedExperiment(1L));

        User other = new User();
        other.setId(OTHER_OWNER_ID);
        Experiment notOwned = new Experiment();
        notOwned.setUserId(other);
        when(experimentService.findExperimentById(2L)).thenReturn(notOwned);
        assertNotFound(() -> accessService.requireOwnedExperiment(2L));
    }

    @Test
    void allowsAccessToOwnedRunAndRejectsDetachedOrOtherUsersRun() {
        Experiment ownedExperiment = new Experiment();
        ownedExperiment.setUserId(owner);
        Run ownedRun = new Run();
        ownedRun.setExperimentId(ownedExperiment);
        when(runService.findByRunId(1L)).thenReturn(ownedRun);
        assertSame(ownedRun, accessService.requireOwnedRun(1L));

        Run detachedRun = new Run();
        when(runService.findByRunId(2L)).thenReturn(detachedRun);
        assertNotFound(() -> accessService.requireOwnedRun(2L));

        User other = new User();
        other.setId(OTHER_OWNER_ID);
        Experiment otherExperiment = new Experiment();
        otherExperiment.setUserId(other);
        Run otherRun = new Run();
        otherRun.setExperimentId(otherExperiment);
        when(runService.findByRunId(3L)).thenReturn(otherRun);
        assertNotFound(() -> accessService.requireOwnedRun(3L));
    }

    @Test
    void allowsAccessToOwnedGrammarAndRejectsOtherUsersGrammar() {
        Grammar owned = new Grammar();
        owned.setUserId(OWNER_ID);
        when(grammarRepository.findGrammarByIdOrderByCreationDateDesc(1L)).thenReturn(owned);
        assertSame(owned, accessService.requireOwnedGrammar(1L));

        Grammar notOwned = new Grammar();
        notOwned.setUserId(OTHER_OWNER_ID);
        when(grammarRepository.findGrammarByIdOrderByCreationDateDesc(2L)).thenReturn(notOwned);
        assertNotFound(() -> accessService.requireOwnedGrammar(2L));
    }

    private void assertNotFound(Runnable operation) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, operation::run);
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
