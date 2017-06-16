package Client;

import CommonUtils.Channel;
import CommonUtils.InitPackage;
import CommonUtils.LogMessageErrorWriter;
import CommonUtils.Stoppable;

import java.io.*;

/**
 * Created by pelgray on 06.06.2017.
 */
public class FileReader implements Stoppable {
    private boolean isActive_;
    private final File file_;
    private final LogMessageErrorWriter e_;
    private final Channel<byte[]> channel_;
    private final int sizeArr_;
    private final Client classClient_;


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
        FileInputStream fis_;
        try {
            fis_ = new FileInputStream(file_);
        } catch (FileNotFoundException e) {
            e_.write("File not found.");
            return;
        }
        try {
            long countPack_ = file_.length() / sizeArr_ + ((file_.length() % sizeArr_ == 0) ? 0 : 1);
            classClient_.setCountPack(countPack_);
            long lastSize = file_.length() - sizeArr_* (countPack_ - ((file_.length()%sizeArr_ == 0)?0:1));
            ByteArrayOutputStream outs = new ByteArrayOutputStream();
            InitPackage initPack = new InitPackage(file_.getName(), countPack_, lastSize);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(outs);
                oos.writeObject(initPack);
                oos.flush();
                oos.close();
            } catch (IOException e) {
                e_.write("Can't did serialization: " + e.getMessage());
                return;
            }
            byte[] initBytes = outs.toByteArray();
            System.out.println("Will be created "+ countPack_ + " packets. InitPack has size = " + initBytes.length + ". LastSize = "+lastSize);
            channel_.put(initBytes);
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
