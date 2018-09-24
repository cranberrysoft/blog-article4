package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.example.cranberrysoft.sqsimpl.SqsQueueService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SqsQueueServiceTest {

    SqsQueueService sqsQueueService;
    AmazonSQSClient amazonSQSClientMock;

    @Before
    public void setUp() {
        amazonSQSClientMock = mock(AmazonSQSClient.class);
        sqsQueueService = new SqsQueueService(amazonSQSClientMock);
    }

    @Test
    public void testPushOneMessageToExistingQueue() {
        GetQueueUrlResult urlResult = new GetQueueUrlResult();
        urlResult.setQueueUrl("Some amazon URL to queue");
        when(amazonSQSClientMock.getQueueUrl("testQueue")).thenReturn(urlResult);

        //when
        sqsQueueService.push("testQueue", "testMessage");

        //then
        //message with correct url and body was sent to SQS
        verify(amazonSQSClientMock).sendMessage("Some amazon URL to queue","testMessage");
    }

    @Test
    public void testPushOneMessageToNonExistingQueue() {
        when(amazonSQSClientMock.getQueueUrl("testQueue2")).thenThrow(QueueDoesNotExistException.class);
        CreateQueueResult createQueueResult = new CreateQueueResult();
        createQueueResult.setQueueUrl("Some amazon URL to queue 2");
        when(amazonSQSClientMock.createQueue("testQueue2")).thenReturn(createQueueResult);

        //when
        sqsQueueService.push("testQueue2", "testMessage");

        //then
        //message with correct url and body was sent to SQS
        verify(amazonSQSClientMock).createQueue("testQueue2");
        verify(amazonSQSClientMock).sendMessage("Some amazon URL to queue 2","testMessage");
    }

   //and so on...

}