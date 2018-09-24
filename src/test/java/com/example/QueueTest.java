package com.example;

import com.example.cranberrysoft.model.QueueNotExistsException;
import com.example.cranberrysoft.util.AtomClock;
import com.example.cranberrysoft.util.JavaClock;
import com.example.cranberrysoft.model.QueueResponse;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static java.util.concurrent.TimeUnit.*;

public abstract class QueueTest {

    public abstract QueueService queueService();

    public abstract AtomClock atom();

    public static final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Test
    public void shouldPushAndReceiveOneMessage() throws IOException, InterruptedException {
        //given
        long now = new JavaClock().now();
        QueueService queueService = queueService();

        //when
        queueService.push("testQueue", "someMessage");

        //then
        //return pushed message
        when(atom().now()).thenReturn(now);
        QueueResponse response = queueService.pull("testQueue", 5);
        assertNotNull("Response should not be empty", response);
        assertNotNull("Receipt handle should not be empty", response.getReceiptHandle());
        assertEquals("someMessage", response.getMessageBody());

        //no more messages in the queue
        when(atom().now()).thenReturn(now + 1);
        assertEquals(null, queueService.pull("testQueue", 5));
    }

    @Test
    public void shouldPushAndReceiveTwoMessagesInCorrectOrder() throws IOException, InterruptedException {
        //given
        long now = new JavaClock().now();

        QueueService queueService = queueService();

        //when
        queueService.push("testQueue", "someMessage");
        queueService.push("testQueue", "someMessage2");

        //then
        when(atom().now()).thenReturn(now);
        QueueResponse response = queueService.pull("testQueue", 5);
        when(atom().now()).thenReturn(now + 1);
        QueueResponse response2 = queueService.pull("testQueue", 5);
        assertNotNull("Response should not be null", response);
        assertNotNull("Receipt handle should not be null", response.getReceiptHandle());
        assertNotNull("Second response should not be null", response2);
        assertNotNull("Receipt handle for second response should not be null", response2.getReceiptHandle());
        assertEquals("someMessage", response.getMessageBody());
        assertEquals("someMessage2", response2.getMessageBody());

        //no more messages in the queue
        when(atom().now()).thenReturn(now + 2);
        assertEquals(null, queueService.pull("testQueue", 5));
    }

    @Test
    public void shouldGetTheSameMessageIfNotRemovedBeforeVisibilityTime() throws InterruptedException, IOException {
        //given
        QueueService queueService = queueService();
        long now = new JavaClock().now();

        //when
        queueService.push("testQueue", "someMessage");

        when(atom().now()).thenReturn(now);
        QueueResponse response = queueService.pull("testQueue", 1);
        assertNotNull("Response should not be empty", response);
        assertNotNull("Receipt handle should not be empty", response.getReceiptHandle());
        assertEquals("someMessage", response.getMessageBody());

        //Expiring invisibility
        when(atom().now()).thenReturn(now + SECONDS.toMillis(1));
        response = queueService.pull("testQueue", 1);
        assertNotNull("Response should not be empty", response);
        assertNotNull("Receipt handle should not be null", response.getReceiptHandle());
        assertEquals("someMessage", response.getMessageBody());
    }

    @Test
    public void shouldNotGetTheSameMessageIfPulledBeforeVisibilityTime() throws InterruptedException, IOException {
        //given
        QueueService queueService = queueService();
        long now = new JavaClock().now();

        //when
        queueService.push("testQueue", "someMessage");

        when(atom().now()).thenReturn(now);
        QueueResponse response = queueService.pull("testQueue", 1);
        assertNotNull("Response should not be empty", response);
        assertNotNull("Receipt handle should not be null", response.getReceiptHandle());
        assertEquals("someMessage", response.getMessageBody());

        //Message stays invisible for consumer
        when(atom().now()).thenReturn(now + 1);
        response = queueService.pull("testQueue", 1);
        assertEquals(null, response);
    }


    @Test
    public void shouldNotGetTheSameMessageIfRemovedBeforeVisibilityTime() throws InterruptedException, IOException {
        //given
        QueueService queueService = queueService();
        long now = new JavaClock().now();

        //when
        queueService.push("testQueue", "someMessage");
        queueService.push("testQueue", "someMessage2");

        when(atom().now()).thenReturn(now);
        QueueResponse response = queueService.pull("testQueue", 1);
        assertNotNull("Response should not be empty", response);
        assertNotNull("Receipt handle should not be null", response.getReceiptHandle());
        assertEquals("someMessage", response.getMessageBody());

        queueService.deleteMessage("testQueue", response.getReceiptHandle());

        when(atom().now()).thenReturn(now + SECONDS.toMillis(1));
        response = queueService.pull("testQueue", 1);
        assertNotNull("Response should not be empty", response);
        assertNotNull("Receipt handle should not be null", response.getReceiptHandle());
        assertEquals("someMessage2", response.getMessageBody());
    }


    @Test
    public void shouldGetTheSameMessageIfNotRemovedBeforeVisibilityTimeForTheSecondPull() throws InterruptedException, IOException {
        //given
        QueueService queueService = queueService();
        long now = new JavaClock().now();

        //when
        queueService.push("testQueue", "someMessage");

        //then
        when(atom().now()).thenReturn(now);
        QueueResponse response = queueService.pull("testQueue", 5);
        assertNotNull("Response should not be empty", response);
        assertNotNull("Receipt handle should not be null", response.getReceiptHandle());
        assertEquals("someMessage", response.getMessageBody());

        when(atom().now()).thenReturn(now + SECONDS.toMillis(5));
        response = queueService.pull("testQueue", 1);
        assertNotNull("Response should not be empty", response);
        assertNotNull("Receipt handle should not be null", response.getReceiptHandle());
        assertEquals("someMessage", response.getMessageBody());

        when(atom().now()).thenReturn(now + SECONDS.toMillis(6));
        response = queueService.pull("testQueue", 1);
        assertNotNull("Response should not be empty", response);
        assertNotNull("Receipt handle should not be null", response.getReceiptHandle());
        assertEquals("someMessage", response.getMessageBody());
    }

    @Test
    public void shouldThrownAnExceptionWhenPullFromNonExistingQueue() throws IOException, InterruptedException {
        //given
        QueueService queueService = queueService();

        //when
        try {
            queueService.pull("testQueue", 1);
            fail("Should throw an exception");
        } catch (QueueNotExistsException ex) {
        }
    }

    @Test
    public void shouldThrownAnExceptionWhenDeleteFromNonExistingQueue() throws IOException, InterruptedException {
        //given
        QueueService queueService = queueService();

        //when
        try {
            queueService.deleteMessage("testQueue", "someHandlerID");
            fail("Should throw an exception");
        } catch (QueueNotExistsException ex) {
        }
    }

    @Test
    public void testPushingMessagesByManyProducersAndReadingByOneConsumer() {
        //given
        QueueService queueService = queueService();
        when(atom().now()).thenCallRealMethod();

        List<CompletableFuture<Void>> send100MessagesBy100tasks =
                IntStream.range(0, 100).mapToObj(number -> "message " + number)
                        .map(message -> CompletableFuture.runAsync(
                                () -> produce(queueService, "testQueue", message)
                                )
                        )
                        .collect(toList());


        send100MessagesBy100tasks.stream().map(CompletableFuture::join).collect(toList());

        List<QueueResponse> messages = IntStream.range(0, 100)
                .mapToObj(it -> consume(queueService, "testQueue", 10))
                .collect(toList());
        assertEquals(100, messages.size());
        assertEquals(100, messages.stream().map(QueueResponse::getMessageBody).distinct().count());
        //no more messages in the queue
        assertEquals(null, consume(queueService, "testQueue", 10));
    }

    @Test
    public void testPullingMessagesByManyConsumers() {
        //given
        QueueService queueService = queueService();
        when(atom().now()).thenCallRealMethod();

        //send 100 messages
        IntStream.range(0, 100).mapToObj(number -> "message " + number)
                .forEach(message -> produce(queueService, "testQueue", message)
                );

        List<CompletableFuture<QueueResponse>> consume100MessagesBy100tasks =
                IntStream.range(0, 100).mapToObj(number -> "message " + number)
                        .map(message -> CompletableFuture.supplyAsync(
                                () -> consume(queueService, "testQueue",5)
                                )
                        )
                        .collect(toList());

        final List<QueueResponse> responses = consume100MessagesBy100tasks.stream().map(CompletableFuture::join).collect(toList());

        assertEquals(100, responses.size());
        assertEquals(100, responses.stream().map(QueueResponse::getMessageBody).distinct().count());
        //not more messages in the queue
        assertEquals(null, consume(queueService, "testQueue", 10));
    }


    @AfterClass
    public static void afterAll() {
        executor.shutdown();
    }

    //Utils
    public static void produce(QueueService queueService, String queueName, String message) {
        try {
            queueService.push(queueName, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static QueueResponse consume(QueueService queueService, String queueName, Integer delaySeconds) {
        try {
            return queueService.pull(queueName, delaySeconds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
