package com.example.cranberrysoft.fileimpl;

/**
 * Represents a message in a filesystem
 *
 * @author Mariusz Dubielecki
 */
class Record {
    private long attempt;
    private String message;
    private long visibleFrom;
    private String receiptHandle;

    private Record(){}

    public long getAttempt() {
        return attempt;
    }

    public String getMessage() {
        return message;
    }

    public long getVisibleFrom() {
        return visibleFrom;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public static String createAttempt(Record record, String receiptHandle, long visibleFrom) {
        return String.format("%d:%d:%s:%s", record.attempt+1, visibleFrom, receiptHandle , record.message);
    }

    public static String create(long visibleFrom, String message) {
        return String.format("0:%d::%s", visibleFrom, message);
    }

    public static Record read(String line) {
        final Record record = new Record();
        final String[] data = line.split(":");
        record.attempt = Long.parseLong(data[0]);
        record.visibleFrom = Long.parseLong(data[1]);
        record.receiptHandle = data[2];
        record.message = data[3];
        return record;
    }

    @Override
    public String toString() {
        return "Record{" +
                "attempt=" + attempt +
                ", message='" + message + '\'' +
                ", visibleFrom=" + visibleFrom +
                ", receiptHandle='" + receiptHandle + '\'' +
                '}';
    }
}
