package net.jps.atom.hopper.adapter.request.impl;

import java.io.IOException;
import org.apache.abdera.protocol.server.RequestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
//import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class PostEntryRequestImplTest {

    public static class WhenParsingAdberaRequestContexts {

        private RequestContext requestContextMock;
        
        @Before
        public void standUp() throws Exception {
            requestContextMock = mock(RequestContext.class);
            
            when(requestContextMock.getDocument()).thenThrow(new IOException("Unable to read stream"));
        }
        
        @Test (expected = RequestParsingException.class)
        public void shouldWrapExceptionCasesGracefully() {
            new PostEntryRequestImpl(requestContextMock);
        }
    }
}
