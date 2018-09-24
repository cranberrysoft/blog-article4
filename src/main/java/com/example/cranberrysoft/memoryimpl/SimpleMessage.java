package com.example.cranberrysoft.memoryimpl;

/**
 * Represents message in the queue for in memory implementation
 *
 * @author Mariusz Dubielecki
 */
class SimpleMessage implements Comparable<SimpleMessage> {

    private long attempt;

    private String receiptHandle;

    //Currently not used
    private String messageId;

    private String body;

    private long visibleFrom;

    private SimpleMessage() {
    }

    public long getVisibleFrom() {
        return visibleFrom;
    }

    public long getAttempt() {
        return attempt;
    }

    public String getBody() {
        return body;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public static SimpleMessage create(long attempt, long visibleFrom, String message) {
        final SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.attempt = attempt;
        simpleMessage.visibleFrom = visibleFrom;
        simpleMessage.body = message;
        return simpleMessage;
    }

    /**
     * Create copy of message with incremented attempt by 1 and generated UUID for receiptHandle
     *
     * @param simpleMessage
     * @return
     */
    public static SimpleMessage createAttemptMessage(SimpleMessage simpleMessage, String receiptHandle, long visibleFrom) {
        final SimpleMessage copyOfMessage = new SimpleMessage();
        copyOfMessage.attempt = simpleMessage.attempt + 1;
        copyOfMessage.visibleFrom = visibleFrom;
        copyOfMessage.body = simpleMessage.body;
        copyOfMessage.receiptHandle = receiptHandle;
        return copyOfMessage;
    }

    //For priority queue
    @Override
    public int compareTo(SimpleMessage o) {
        return Long.compare(visibleFrom, o.visibleFrom);
    }

    @Override
    public String toString() {
        return "SimpleMessage{" +
                "attempt=" + attempt +
                ", receiptHandle='" + receiptHandle + '\'' +
                ", messageId='" + messageId + '\'' +
                ", body='" + body + '\'' +
                ", visibleFrom=" + visibleFrom +
                '}';
    }
}
