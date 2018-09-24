package com.example;

import com.example.cranberrysoft.util.AtomClock;
import com.example.cranberrysoft.memoryimpl.InMemoryQueueService;
import com.example.cranberrysoft.util.JavaClock;

import static org.mockito.Mockito.mock;

public class InMemoryQueueTest extends  QueueTest {

    AtomClock atom = null;

    @Override
    public QueueService queueService() {
        return  new InMemoryQueueService(atom());
    }

    @Override
    public AtomClock atom() {
        if(atom == null){
            atom = mock(JavaClock.class);
        }
        return atom;
    }
}
