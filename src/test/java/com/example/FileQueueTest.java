package com.example;

import com.example.cranberrysoft.util.AtomClock;
import com.example.cranberrysoft.fileimpl.FileQueueService;
import com.example.cranberrysoft.util.JavaClock;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;

public class FileQueueTest extends  QueueTest {

    AtomClock atom = null;
    final String  filesystemPath = System.getProperty("java.io.tmpdir") + "\\sqs";
    @Override
    public AtomClock atom() {
        if(atom == null){
            atom = mock(JavaClock.class);
        }
        return atom;
    }

    @Override
    public QueueService queueService() {
        return new FileQueueService(atom(), filesystemPath);
    }

    @Before
    public void setUp(){
        new File(filesystemPath).mkdir();
    }

    @After
    public void cleanUp() throws IOException {
        // Please notice that I could easily delete important files from your local machine ;)
        // I hope that you will appreciate that I don't do this by mistake and you give me a chance in the next step
       MoreFiles.deleteRecursively(Paths.get(filesystemPath), RecursiveDeleteOption.ALLOW_INSECURE);
    }
}
