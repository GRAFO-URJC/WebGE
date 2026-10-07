package com.gramevapp.web.service;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class SaveDBServiceTest {

    @Test
    void persistsQueuedObjectsBeforeShutdownReturns() {
        RunService runService = mock(RunService.class);
        DiagramDataService diagramDataService = mock(DiagramDataService.class);
        SaveDBService service = new SaveDBService();
        ReflectionTestUtils.setField(service, "runService", runService);
        ReflectionTestUtils.setField(service, "diagramDataService", diagramDataService);
        Run run = new Run();
        DiagramData diagram = new DiagramData();

        service.onInit();
        service.saveRunAsync(run);
        service.saveDiagramDataAsync(diagram);
        service.onEnd();

        verify(runService, timeout(1000)).saveRun(run);
        verify(diagramDataService, timeout(1000)).saveDiagram(diagram);
        assertThrows(IllegalStateException.class, () -> service.saveRunAsync(new Run()));
        assertThrows(IllegalStateException.class, () -> service.saveDiagramDataAsync(new DiagramData()));
    }
}
