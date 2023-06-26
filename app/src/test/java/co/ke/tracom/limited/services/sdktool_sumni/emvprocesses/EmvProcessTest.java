package co.ke.tracom.limited.services.sdktool_sumni.emvprocesses;

import android.content.Context;
import android.content.Intent;

import org.junit.Test;

import ke.co.tracom.libsunmi.api.EmvConfig;
import ke.co.tracom.libsunmi.card.EmvResult;
import ke.co.tracom.libsunmi.emv.EMVAction;
import ke.co.tracom.libsunmi.interfaces.CardStateEmitter;
import ke.co.tracom.libsunmi.interfaces.EMVListener;

public class EmvProcessTest {

    @Mock
    private Context contextMock;

    @Mock
    private EMVAction emvActionMock;

    @Mock
    private EMVListener emvListenerMock;

    @Mock
    private CardStateEmitter cardStateEmitterMock;

    private String payload = "{\"amount\":\"100\"}";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Should finish the adjust process and return the EMV result
     */
    @Test
    public void finishAdjustReturnsEmvResult() {
        EmvProcess emvProcess = new EmvProcess(contextMock, emvActionMock, payload);
        Intent intentMock = mock(Intent.class);
        EmvResult emvResultMock = mock(EmvResult.class);
        when(intentMock.putExtra(eq("resp"), anyString())).thenReturn(intentMock);
        when(emvListenerMock.onEmvResult(emvResultMock)).thenReturn(intentMock);
        when(emvActionMock.start(eq(contextMock), eq(emvListenerMock), eq(cardStateEmitterMock), any(EmvConfig.class))).thenReturn(intentMock);

        emvProcess.finishAdjust();

        verify(emvActionMock, times(1)).start(eq(contextMock), eq(emvListenerMock), eq(cardStateEmitterMock), any(EmvConfig.class));
        verify(intentMock, times(1)).putExtra(eq("resp"), anyString());
        verify(contextMock, times(1)).setResult(eq(RESULT_OK), eq(intentMock));
        verify(contextMock, times(1)).finish();
    }

}