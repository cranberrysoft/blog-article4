package com.example.cranberrysoft.memoryimpl;

import com.example.QueueService;
import com.example.cranberrysoft.model.QueueNotExistsException;
import com.example.cranberrysoft.util.AtomClock;
import com.example.cranberrysoft.model.QueueResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Thread safe in memory queue.
 *
 * @author Mariusz Dubielecki
 */
public class InMemoryQueueService implements QueueService {

    private final AtomClock atom;

    private final Map<String, Queue<SimpleMessage>> messages = new ConcurrentHashMap();

    private final Map<String, Queue<SimpleMessage>> inDeliveryMessages = new ConcurrentHashMap();

    public InMemoryQueueService(AtomClock atom) {
        this.atom = atom;
    }

    @Override
    public void push(String queueName, String messageBody) {
        //Create queue if not exist otherwise add new Message to the messages
        messages.merge(queueName, new ConcurrentLinkedQueue<>
                (Arrays.asList(SimpleMessage.create(0, 0, messageBody))), (current, coming) -> {
            current.addAll(coming);
            return current;
        });
    }

    @Override
    public QueueResponse pull(String queueName, Integer delaySeconds) {
        if(messages.get(queueName) == null){
            throw new QueueNotExistsException(String.format("Queue %s does not exist !", queueName));
        }

        //Try pull from inDelivery
        synchronized (inDeliveryMessages) {
            final Queue<SimpleMessage> inDeliveryQueue = inDeliveryMessages.get(queueName);
            final SimpleMessage oldestInDeliver = inDeliveryQueue == null ? null : inDeliveryQueue.peek();
            if (oldestInDeliver != null && (oldestInDeliver.getVisibleFrom() == 0 || atom.now() >= oldestInDeliver.getVisibleFrom())) {
                final SimpleMessage simpleMessage = inDeliveryQueue.poll();
                inDeliveryQueue.add(SimpleMessage.createAttemptMessage(simpleMessage, generateReceiptHandle(),  getVisibleFrom(delaySeconds)));
                return new QueueResponse(simpleMessage.getReceiptHandle(), simpleMessage.getBody());
            }
        }

        //Try pull from messages
        final Queue<SimpleMessage> messageQueue = messages.get(queueName);
        final SimpleMessage message = messageQueue.poll();

        if(message == null ){ //empty queue nothing to deliver
            return null;
        }
        //Add pulled message to inDelivery queue ordered by visibleFrom
        final SimpleMessage attemptMessage =  SimpleMessage.createAttemptMessage(message, generateReceiptHandle(), getVisibleFrom(delaySeconds));

        inDeliveryMessages.merge(queueName,
                new PriorityBlockingQueue<>(Arrays.asList(attemptMessage)), //new attempt to deliver
                (current, coming) -> {
                    current.addAll(coming);
                    return current;
                });

        return new QueueResponse(attemptMessage.getReceiptHandle(), attemptMessage.getBody());
    }


    private long getVisibleFrom(Integer delaySeconds){
        return (delaySeconds != null) ? atom.now() + TimeUnit.SECONDS.toMillis(delaySeconds) : 0L;
    }

    @Override
    public void deleteMessage(String queueName, String receiptHandle) {
        synchronized (inDeliveryMessages) {
            if(inDeliveryMessages.get(queueName) == null){
                throw new QueueNotExistsException(String.format("Queue %s does not exist !", queueName));
            }
            inDeliveryMessages.get(queueName).removeIf(m -> m.getReceiptHandle() == receiptHandle);
        }
    }

}