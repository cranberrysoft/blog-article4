package com.example.cranberrysoft.fileimpl;

import com.example.QueueService;
import com.example.cranberrysoft.model.QueueNotExistsException;
import com.example.cranberrysoft.util.AtomClock;
import com.example.cranberrysoft.model.QueueResponse;
import com.google.common.io.Files;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * This implementation is base on the article  https://product.canva.com/hermeticity/
 * Use mkdir as a lock in critical sections
 *
 * @author Mariusz Dubielecki
 */
public class FileQueueService implements QueueService {

    private final AtomClock atom;
    private final String filesystemPath;

    public FileQueueService(AtomClock atom, String filesystemPath) {
        this.atom = atom;
        this.filesystemPath = filesystemPath;
    }

    @Override
    public void push(String queueName, String message)
            throws InterruptedException, IOException {
        final File messagesFile = getMessagesFile(queueName, true);
        final File lock = getLockFile(queueName);

        lock(lock);
        try (PrintWriter pw = new PrintWriter(new FileWriter(messagesFile, true))) {  // append
            pw.println(Record.create(0, message));
        } finally {
            unlock(lock);
        }
    }

    @Override
    public QueueResponse pull(String queueName, Integer delaySeconds) throws InterruptedException, IOException {
        QueueResponse response = null;

        final File messagesFile = getMessagesFile(queueName, false);
        final File lock = getLockFile(queueName);
        final long visibleFrom = (delaySeconds != null) ? atom.now() + TimeUnit.SECONDS.toMillis(delaySeconds) : 0L;

        lock(lock);
        final File tmpMessagesFile = getTmpMessagesFile(queueName);

        try (BufferedReader br = new BufferedReader(new FileReader(messagesFile))) {

            try (PrintWriter pw = new PrintWriter(new FileWriter(tmpMessagesFile, true))) {  // append
                String line;
                while ((line = br.readLine()) != null) {
                    final Record record = Record.read(line);
                    if (response == null && (record.getVisibleFrom() == 0 || atom.now() >= record.getVisibleFrom())) { //make visible
                        final String receiptHandle = generateReceiptHandle();
                        response = new QueueResponse(receiptHandle, record.getMessage());
                        pw.println(Record.createAttempt(record, receiptHandle,visibleFrom));
                    } else {
                        pw.println(Record.create(record.getVisibleFrom(), record.getMessage()));
                    }
                }
            }
        } finally {
            try {
                messagesFile.delete();
                Files.move(tmpMessagesFile, messagesFile);
            }finally {
                unlock(lock);
            }
        }

        return response;
    }

    @Override
    public void deleteMessage(String queueName, String receiptHandle) throws InterruptedException, IOException {
        final File messagesFile = getMessagesFile(queueName, false);

        final File lock = getLockFile(queueName);

        lock(lock);
        final File tmpMessagesFile = getTmpMessagesFile(queueName);

        try (BufferedReader br = new BufferedReader(new FileReader(messagesFile))) {

            try (PrintWriter pw = new PrintWriter(new FileWriter(tmpMessagesFile, true))) {  // append
                String line;
                while ((line = br.readLine()) != null) {
                    final Record record = Record.read(line);
                    if (!record.getReceiptHandle().equals(receiptHandle)) {
                        pw.println(line);
                    }
                }
            }
        } finally {
            try {
                Files.move(tmpMessagesFile, messagesFile);
            }finally {
                unlock(lock);
            }
        }

    }

    private void lock(File lock) throws InterruptedException {
        while (!lock.mkdir()) {
            Thread.sleep(50);
        }
    }

    private void unlock(File lock) {
        lock.delete();
    }

    private File getLockFile(String queue) {
        return new File(String.format("%s\\lock\\", filesystemPath));
    }

    private File getMessagesFile(String queueName, boolean create) throws IOException {
        if (create) {
            final File queueFileSystem = new File(String.format("%s\\%s\\", filesystemPath, queueName));
            queueFileSystem.mkdir();
        }

        try {
            final File msgFileSystem = new File(String.format("%s\\%s\\messages", filesystemPath, queueName));
            msgFileSystem.createNewFile();
            return msgFileSystem;
        } catch (IOException io) {
            throw new QueueNotExistsException(String.format("Queue %s does not exist !", queueName));
        }

    }

    private File getTmpMessagesFile(String queue) throws IOException {
        final File file = new File(String.format("%s\\%s\\messages_tmp", filesystemPath, queue));
        file.createNewFile();
        return file;
    }
}
