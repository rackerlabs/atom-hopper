package net.jps.atom.hopper.abdera;

import org.apache.abdera.protocol.server.*;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class WorkspaceProviderTest {

    public static class WhenProcessingRequest extends TestParent {

        @Test
        public void shouldReturn404GivenNullTarget() {
            final WorkspaceProvider workspaceProvider = workspaceProvider();
            final RequestContext requestContext = requestContext(null);

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            assertEquals("Should respond with 404 not found", 404, responseContext.getStatus());
        }

        @Test
        public void shouldReturn404GivenUnknownTargetType() {
            final WorkspaceProvider workspaceProvider = workspaceProvider();
            final RequestContext requestContext = requestContext(target(TargetType.TYPE_NOT_FOUND));

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            assertEquals("Should respond with 404 not found", 404, responseContext.getStatus());
        }

        @Test
        public void shouldReturn404GivenTargetTypeWithNoMatchingRequestProcessor() {
            final WorkspaceProvider workspaceProvider = workspaceProvider();
            workspaceProvider.setRequestProcessors(new HashMap<TargetType, RequestProcessor>());
            final RequestContext requestContext = requestContext(target(TargetType.TYPE_CATEGORIES));

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            assertEquals("Should respond with 404 not found", 404, responseContext.getStatus());
        }

        @Test
        public void shouldReturn404GivenRequestWithNoMatchingCollectionAdapter() {
            final WorkspaceProvider workspaceProvider = workspaceProvider();
            final RequestContext requestContext = requestContext(target(TargetType.TYPE_CATEGORIES));

            final WorkspaceManager workspaceManagerMock = workspaceProvider.getWorkspaceManager();
            when(workspaceManagerMock.getCollectionAdapter(requestContext)).thenReturn(null);

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            verify(workspaceManagerMock).getCollectionAdapter(isA(RequestContext.class));
            assertEquals("Should respond with 404 not found", 404, responseContext.getStatus());
        }

        @Test
        public void shouldStartAndEndTransactionForTransactionalCollectionAdapter() throws ResponseContextException {
            final WorkspaceProvider workspaceProvider = workspaceProvider();
            final RequestContext requestContext = requestContext(target(TargetType.TYPE_CATEGORIES));
            final WorkspaceManager workspaceManagerMock = workspaceProvider.getWorkspaceManager();
            final AbstractCollectionAdapter collectionAdapterMock = mock(AbstractCollectionAdapter.class);
            final RequestProcessor requestProcessorMock = mock(RequestProcessor.class);
                      workspaceProvider.addRequestProcessors(new HashMap<TargetType, RequestProcessor>() {
                          {
                              put(TargetType.TYPE_CATEGORIES, requestProcessorMock);
                          } });

            when(workspaceManagerMock.getCollectionAdapter(requestContext)).thenReturn(collectionAdapterMock);

            final ResponseContext responseContextMock = mock(ResponseContext.class);
            when(requestProcessorMock.process(requestContext, workspaceManagerMock, collectionAdapterMock)).thenReturn(responseContextMock);

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            InOrder inOrder = inOrder(collectionAdapterMock);
            inOrder.verify(collectionAdapterMock).start(requestContext);
            inOrder.verify(collectionAdapterMock).end(requestContext, responseContext);
            inOrder.verify(collectionAdapterMock, never()).compensate(eq(requestContext), any(Throwable.class));
        }

        @Test
        public void shouldStartAndCompensateTransactionForTransactionalCollectionAdapter() throws ResponseContextException {
        final WorkspaceProvider workspaceProvider = workspaceProvider();
            final RequestContext requestContext = requestContext(target(TargetType.TYPE_CATEGORIES));
            final WorkspaceManager workspaceManagerMock = workspaceProvider.getWorkspaceManager();
            final AbstractCollectionAdapter collectionAdapterMock = mock(AbstractCollectionAdapter.class);
            final RequestProcessor requestProcessorMock = mock(RequestProcessor.class);
                      workspaceProvider.addRequestProcessors(new HashMap<TargetType, RequestProcessor>() {
                          {
                              put(TargetType.TYPE_CATEGORIES, requestProcessorMock);
                          } });

            when(workspaceManagerMock.getCollectionAdapter(requestContext)).thenReturn(collectionAdapterMock);

            final ResponseContext responseContextMock = mock(ResponseContext.class);
            when(requestProcessorMock.process(requestContext, workspaceManagerMock, collectionAdapterMock)).thenThrow(new RuntimeException());

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            InOrder inOrder = inOrder(collectionAdapterMock);
            inOrder.verify(collectionAdapterMock).start(requestContext);
            inOrder.verify(collectionAdapterMock).compensate(eq(requestContext), any(Throwable.class));
            inOrder.verify(collectionAdapterMock).end(requestContext, responseContext);
        }

    }

    public static class WhenRequestIsNotHandledByDefaultCollectionAdapterBehavior extends TestParent {

        @Test
        public void shouldProcessAsExtensionRequest() {
            final TestableWorkspaceProvider workspaceProvider = workspaceProvider();
            final RequestContext requestContext = requestContext(target(TargetType.TYPE_CATEGORIES));
            final WorkspaceManager workspaceManagerMock = workspaceProvider.getWorkspaceManager();
            final CollectionAdapter collectionAdapterMock = mock(CollectionAdapter.class);
            final RequestProcessor requestProcessorMock = mock(RequestProcessor.class);

            workspaceProvider.addRequestProcessors(new HashMap<TargetType, RequestProcessor>() {
                {
                    put(TargetType.TYPE_CATEGORIES, requestProcessorMock);
                } });

            when(workspaceManagerMock.getCollectionAdapter(requestContext)).thenReturn(collectionAdapterMock);
            when(requestProcessorMock.process(requestContext, workspaceManagerMock, collectionAdapterMock)).thenReturn(null);

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            verify(collectionAdapterMock).extensionRequest(requestContext);
        }
    }

    public static class WhenProcessingRequestThrowsAnException extends TestParent {
        TestableWorkspaceProvider workspaceProvider;
        RequestContext requestContext;
        WorkspaceManager workspaceManagerMock;
        CollectionAdapter collectionAdapterMock;
        RequestProcessor requestProcessorMock;

        @Before
        public void setup() {
            workspaceProvider = workspaceProvider();
            requestContext = requestContext(target(TargetType.TYPE_CATEGORIES));
            workspaceManagerMock = workspaceProvider.getWorkspaceManager();
            collectionAdapterMock = mock(CollectionAdapter.class);
            requestProcessorMock = mock(RequestProcessor.class);

            workspaceProvider.addRequestProcessors(new HashMap<TargetType, RequestProcessor>() {
                {
                    put(TargetType.TYPE_CATEGORIES, requestProcessorMock);
                } });

            when(workspaceManagerMock.getCollectionAdapter(requestContext)).thenReturn(collectionAdapterMock);
        }

        @Test
        public void shouldReturnServerError() {
            when(requestProcessorMock.process(requestContext, workspaceManagerMock, collectionAdapterMock)).thenThrow(new RuntimeException());
            ResponseContext responseContext = workspaceProvider.process(requestContext);
            assertEquals("Should respond with 500 server error", 500, responseContext.getStatus());
        }

    }


    @Ignore
    private static class TestParent {

        public TestableWorkspaceProvider workspaceProvider() {
            final TestableWorkspaceProvider target = new TestableWorkspaceProvider();
            return target;
        }

        public void processRequestReturnsValidResponse() {

        }

        public RequestContext requestContext(Target target) {
            RequestContext context = mock(RequestContext.class);
            when(context.getTarget()).thenReturn(target);
            return context;
        }

        public Target target(TargetType type) {
            Target target = mock(Target.class);
            when(target.getType()).thenReturn(type);
            return target;
        }

    }

    private static class TestableWorkspaceProvider extends WorkspaceProvider {

        final WorkspaceManager workspaceManagerMock = mock(WorkspaceManager.class);

        private TestableWorkspaceProvider() {
            super();
        }

        @Override
        public WorkspaceManager getWorkspaceManager() {
            return workspaceManagerMock;
        }
    }

}
