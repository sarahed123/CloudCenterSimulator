package ch.ethz.systems.netbench.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NonblockingBufferedReader {
	private int command;
    private volatile boolean closed = false;
    private Thread backgroundReaderThread = null;
    
    public NonblockingBufferedReader(InputStream input) {
        this(new InputStreamReader(input));
      }
    public NonblockingBufferedReader(final InputStreamReader reader) {
    	BufferedReader bufferedReader = new BufferedReader(reader);
        backgroundReaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        int c = bufferedReader.read();
                        if(c!='\n') {
                        	command = c;
                        }
                        
                       
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    closed = true;
                }
            }
        });
        backgroundReaderThread.setDaemon(true);
        backgroundReaderThread.start();
    }

    public int readChar()  {
        return command;
    }

    public void close() {
        if (backgroundReaderThread != null) {
            backgroundReaderThread.interrupt();
            backgroundReaderThread = null;
        }
    }
	public int resetCommand() {
		command = 0;
		return command;
		
	}
}
