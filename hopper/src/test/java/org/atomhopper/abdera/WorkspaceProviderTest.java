package org.atomhopper.abdera;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.RequestProcessor;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class WorkspaceProviderTest {

    private static TestableWorkspaceProvider workspaceProvider;

    public static class WhenProcessingRequest {

        @Before
        public void setup() {
            workspaceProvider = new TestableWorkspaceProvider();
        }

        @Test
        public void shouldReturn404GivenNullTarget() {
            ResponseContext responseContext = workspaceProvider.process(REQUEST_TARGET_ISNULL);
            assertEquals("Should respond with 404 not found", 404, responseContext.getStatus());
        }

        @Test
        public void shouldReturn404GivenUnknownTargetType() {
            ResponseContext responseContext = workspaceProvider.process(REQUEST_TARGET_TYPE_UNKNOWN);
            assertEquals("Should respond with 404 not found", 404, responseContext.getStatus());
        }

        @Test
        public void shouldReturn404GivenTargetTypeWithNoMatchingRequestProcessor() {
            workspaceProvider.setRequestProcessors(new HashMap<TargetType, RequestProcessor>());
            ResponseContext responseContext = workspaceProvider.process(REQUEST_TARGET_TYPE_CATEGORIES);
            assertEquals("Should respond with 404 not found", 404, responseContext.getStatus());
        }

        @Test
        public void shouldReturn404GivenRequestWithNoMatchingCollectionAdapter() {
            final ThreadSafeWorkspaceManager workspaceManagerMock = workspaceProvider.getWorkspaceManager();
            when(workspaceManagerMock.getCollectionAdapter(REQUEST_TARGET_TYPE_CATEGORIES)).thenReturn(null);

            ResponseContext responseContext = workspaceProvider.process(REQUEST_TARGET_TYPE_CATEGORIES);

            verify(workspaceManagerMock).getCollectionAdapter(REQUEST_TARGET_TYPE_CATEGORIES);
            assertEquals("Should respond with 404 not found", 404, responseContext.getStatus());
        }

        @Test
        public void shouldReturnServerErrorWhenProcessingExceptionOccurs() {
            final ThreadSafeWorkspaceManager workspaceManagerMock = workspaceProvider.getWorkspaceManager();
            final CollectionAdapter collectionAdapterMock = mock(CollectionAdapter.class);
            final RequestProcessor requestProcessorMock = mock(RequestProcessor.class);

            workspaceProvider.addRequestProcessors(new HashMap<TargetType, RequestProcessor>() {

                {
                    put(TargetType.TYPE_CATEGORIES, requestProcessorMock);
                }
            });

            when(workspaceManagerMock.getCollectionAdapter(REQUEST_TARGET_TYPE_CATEGORIES)).thenReturn(collectionAdapterMock);
            when(requestProcessorMock.process(any(RequestContext.class), any(ThreadSafeWorkspaceManager.class), any(CollectionAdapter.class))).thenThrow(new RuntimeException());
            ResponseContext responseContext = workspaceProvider.process(REQUEST_TARGET_TYPE_CATEGORIES);
            assertEquals("Should respond with 500 server error", 500, responseContext.getStatus());
        }
    }

    public static class WhenProcessingRequestWithTransactionalCollectionAdapter {

        RequestContext requestContext;
        ThreadSafeWorkspaceManager workspaceManagerMock;
        AbstractCollectionAdapter collectionAdapterMock;
        RequestProcessor categoriesRequestProcessorMock;

        @Before
        public void setup() {
            workspaceProvider = new TestableWorkspaceProvider();
            requestContext = requestContext(target(TargetType.TYPE_CATEGORIES));
            workspaceManagerMock = workspaceProvider.getWorkspaceManager();
            collectionAdapterMock = mock(AbstractCollectionAdapter.class);
            categoriesRequestProcessorMock = mock(RequestProcessor.class);

            workspaceProvider.addRequestProcessors(new HashMap<TargetType, RequestProcessor>() {

                {
                    put(TargetType.TYPE_CATEGORIES, categoriesRequestProcessorMock);
                }
            });

            when(workspaceManagerMock.getCollectionAdapter(requestContext)).thenReturn(collectionAdapterMock);
        }

        @Test
        public void shouldStartAndEndTransaction() throws ResponseContextException {
            final ResponseContext processorResponse = mock(ResponseContext.class);
            when(categoriesRequestProcessorMock.process(any(RequestContext.class), any(ThreadSafeWorkspaceManager.class), any(CollectionAdapter.class))).thenReturn(processorResponse);

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            InOrder inOrder = inOrder(collectionAdapterMock);
            inOrder.verify(collectionAdapterMock).start(requestContext);
            inOrder.verify(collectionAdapterMock).end(requestContext, responseContext);
            inOrder.verify(collectionAdapterMock, never()).compensate(eq(requestContext), any(Throwable.class));
        }

        @Test
        public void shouldCompensateTransactionWhenExceptionOccurs() throws ResponseContextException {
            when(categoriesRequestProcessorMock.process(any(RequestContext.class), any(ThreadSafeWorkspaceManager.class), any(CollectionAdapter.class))).thenThrow(new RuntimeException());

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            InOrder inOrder = inOrder(collectionAdapterMock);
            inOrder.verify(collectionAdapterMock).start(requestContext);
            inOrder.verify(collectionAdapterMock).compensate(eq(requestContext), any(Throwable.class));
            inOrder.verify(collectionAdapterMock).end(requestContext, responseContext);
        }
    }

    private static final RequestContext REQUEST_TARGET_ISNULL = requestContext(null);
    private static final RequestContext REQUEST_TARGET_TYPE_UNKNOWN = requestContext(target(TargetType.TYPE_NOT_FOUND));
    private static final RequestContext REQUEST_TARGET_TYPE_CATEGORIES = requestContext(target(TargetType.TYPE_CATEGORIES));

    public static class WhenRequestIsNotHandledByDefaultCollectionAdapterBehavior {

        @Test
        public void shouldProcessAsExtensionRequest() {
            final WorkspaceProvider workspaceProvider = new TestableWorkspaceProvider();
            final RequestContext requestContext = requestContext(target(TargetType.TYPE_CATEGORIES));
            final ThreadSafeWorkspaceManager workspaceManagerMock = workspaceProvider.getWorkspaceManager();
            final CollectionAdapter collectionAdapterMock = mock(CollectionAdapter.class);
            final RequestProcessor requestProcessorMock = mock(RequestProcessor.class);

            workspaceProvider.addRequestProcessors(new HashMap<TargetType, RequestProcessor>() {

                {
                    put(TargetType.TYPE_CATEGORIES, requestProcessorMock);
                }
            });

            when(workspaceManagerMock.getCollectionAdapter(requestContext)).thenReturn(collectionAdapterMock);
            when(requestProcessorMock.process(requestContext, workspaceManagerMock, collectionAdapterMock)).thenReturn(null);

            ResponseContext responseContext = workspaceProvider.process(requestContext);

            verify(collectionAdapterMock).extensionRequest(requestContext);
        }
    }

    public static RequestContext requestContext(Target target) {
        RequestContext context = mock(RequestContext.class);
        when(context.getTarget()).thenReturn(target);
        return context;
    }

    public static Target target(TargetType type) {
        Target target = mock(Target.class);
        when(target.getType()).thenReturn(type);
        return target;
    }

    private static class TestableWorkspaceProvider extends WorkspaceProvider {

        private TestableWorkspaceProvider() {
            super(mock(ThreadSafeWorkspaceManager.class));
        }
    }
}
