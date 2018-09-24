package com.example;

import com.example.cranberrysoft.model.QueueResponse;

import java.io.IOException;
import java.util.UUID;

/**
 *
 *
 * @author Mariusz Dubielecki
 */
public interface QueueService {
  //
  // Task 1: Define me.
  //
  // This interface should include the following methods.  You should choose appropriate
  // signatures for these methods that prioritise simplicity of implementation for the range of
  // intended implementations (in-memory, file, and SQS).  You may include additional methods if
  // you choose.
  //
  // - push
  //   pushes a message onto a queue.
  // - pull
  //   retrieves a single message from a queue.
  // - delete
  //   deletes a message from the queue that was received by pull().
  //

  /**
   * Pushes a message onto a queue. If the specified queue does not exists create one.
   * @param queueName
   * @param message
   * @throws InterruptedException
   * @throws IOException
   */
    void push(String queueName, String message) throws InterruptedException, IOException;

  /**
   *  Retrieves a single message from a queue. If queue does not exist throws {@link com.example.cranberrysoft.model.QueueNotExistsException}
   * @param queueName
   * @param delaySeconds delay after which the message will be visible again if not removed from queue
   * @return QueueResponse or null if no messages in a queue.
   * @throws InterruptedException
   * @throws IOException
   */
    QueueResponse pull(String queueName, Integer delaySeconds) throws InterruptedException, IOException;

  /**
   * Deletes a message from the queue that was received by pull(). If queue does not exist throws {@link com.example.cranberrysoft.model.QueueNotExistsException}
   * @param queueName
   * @param receiptHandle id which is used to remove pulled message.
   * @throws InterruptedException
   * @throws IOException
   */
    void deleteMessage(String queueName, String receiptHandle) throws InterruptedException, IOException;

  /**
   * Generate unique id for receiptHandle
   * @return
   */
  default String generateReceiptHandle() {
      return UUID.randomUUID().toString();
    }
}
