package Client;

import CommonUtils.Channel;
import CommonUtils.LogMessageErrorWriter;
import CommonUtils.Stoppable;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by 1 on 06.06.2017.
 */
public class FileReader implements Stoppable {
    private boolean isActive_;
    private File file_;
    private FileInputStream fis_;
    private LogMessageErrorWriter e_;
    private Channel<byte[]> channel_;
    private final int sizeArr_;
    private Client classClient_;
    private long countPack_;


    public FileReader(Client client, File file, int size, Channel<byte[]> channel, LogMessageErrorWriter errorWriter) {
        file_ = file;
        sizeArr_ = size;
        channel_ = channel;
        e_ = errorWriter;
        classClient_ = client;
        isActive_ = true;
    }

    @Override
    public void stop() {
        if(isActive_){
            isActive_ = false;
        }
    }

    @Override
    public void run() {
        try {
            fis_ = new FileInputStream(file_);
        } catch (FileNotFoundException e) {
            e_.write("File not found.");
            return;
        }
        try {
            countPack_ = file_.length()/sizeArr_ + ((file_.length()%sizeArr_ == 0)?0:1);
            System.out.println("Will be created "+ countPack_ + " packets.");
            classClient_.setCountPack(countPack_);
            // количество пакетов
            channel_.put(ByteBuffer.allocate(Long.BYTES).putLong(countPack_).array());

            //int currArr = 1;
            while (isActive_) {
                try {
                    if (fis_.available() != 0) {
                        byte bytes[] = new byte[sizeArr_];
                        try {
                            fis_.read(bytes);
                        } catch (IOException e) {
                            e_.write("Can't read a file: " + e.getMessage());
                            return;
                        }
                        channel_.put(bytes);
                        //System.out.println("Was put in channel " + currArr + " part of file.");
                        //currArr++;
                    }
                    else {
                        isActive_ = false;
                    }
                } catch (IOException e) {
                    e_.write("Something wrong with FileInputStream: " + e.getMessage());
                    return;
                }
            }
        }finally {
            try {
                fis_.close();
            } catch (IOException e) {
                e_.write("Can't close FileInputStream: " + e.getMessage());
            }
            System.out.println("\tBye, FileReader");
        }
    }
}
