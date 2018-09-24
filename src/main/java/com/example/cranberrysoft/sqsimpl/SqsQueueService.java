package com.example.cranberrysoft.sqsimpl;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.example.QueueService;
import com.example.cranberrysoft.model.QueueNotExistsException;
import com.example.cranberrysoft.model.QueueResponse;

import java.util.Optional;

/**
 * SQS adapter for QueueService
 *
 * @author Mariusz Dubielecki
 */
public class SqsQueueService implements QueueService {

    //
    // Task 4: Optionally implement parts of me.
    //
    // This file is a placeholder for an AWS-backed implementation of QueueService.  It is included
    // primarily so you can quickly assess your choices for method signatures in QueueService in
    // terms of how well they map to the implementation intended for a production environment.
    //
    private final AmazonSQSClient amazonSQSClient;

    public SqsQueueService(AmazonSQSClient amazonSQSClient) {
        this.amazonSQSClient = amazonSQSClient;
    }

    @Override
    public void push(String queueName, String message) {
        amazonSQSClient.sendMessage(mapToQueueURL(queueName, true), message);
    }

    @Override
    public QueueResponse pull(String queueName, Integer delaySeconds) {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
        receiveMessageRequest.setVisibilityTimeout(delaySeconds);
        receiveMessageRequest.setQueueUrl(mapToQueueURL(queueName, false));
        receiveMessageRequest.setMaxNumberOfMessages(1); //we can return only one message
        final Optional<Message> oneMessage = amazonSQSClient.receiveMessage(receiveMessageRequest).getMessages().stream().findFirst();
        return oneMessage.map(message -> new QueueResponse(message.getReceiptHandle(), message.getBody())).orElse(null);
    }

    @Override
    public void deleteMessage(String queueName, String receiptHandle) {
        amazonSQSClient.deleteMessage(mapToQueueURL(queueName, false), receiptHandle);
    }

    private String mapToQueueURL(String queueName, boolean create) {
        try {
            return amazonSQSClient.getQueueUrl(queueName).getQueueUrl();
        }catch (QueueDoesNotExistException doesNotExist){
            if(create){
                final CreateQueueResult result = amazonSQSClient.createQueue(queueName);
                return result.getQueueUrl();
            }else{
                throw new QueueNotExistsException(String.format("Queue %s does not exist !",queueName));
            }
        }
    }
}

