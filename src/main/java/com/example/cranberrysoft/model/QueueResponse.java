package com.example.cranberrysoft.model;

/**
 * Contains details about  pulled message
 *
 * @author Mariusz Dubielecki
 */
public class QueueResponse {

    final String receiptHandle;
    final String messageBody;

    public QueueResponse(String receiptHandle, String messageBody) {
        this.receiptHandle = receiptHandle;
        this.messageBody = messageBody;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public String getMessageBody() {
        return messageBody;
    }

    @Override
    public String toString() {
        return "QueueResponse{" +
                "receiptHandle='" + receiptHandle + '\'' +
                ", messageBody='" + messageBody + '\'' +
                '}';
    }
}
