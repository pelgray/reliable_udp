package Server;

import CommonUtils.LogMessageErrorWriter;
import CommonUtils.SlidingWindow;
import CommonUtils.Stoppable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 1 on 06.06.2017.
 */
public class FileWriter implements Stoppable {
    private boolean isActive_;
    private long countPack_;
    private long currCountPack_;
    private SlidingWindow window_;
    private FileOutputStream fos_;
    private LogMessageErrorWriter err_;
    private Server classServer_;

    public FileWriter(SlidingWindow window_, File file_, LogMessageErrorWriter errorWriter, Server server) {
        this.isActive_ = true;
        this.window_ = window_;
        this.err_ = errorWriter;
        classServer_ = server;
        currCountPack_ = 1;
        try {
            this.fos_ = new FileOutputStream(file_);
        } catch (FileNotFoundException e) {
            err_.write("File not found.");
        }
    }

    public void setCountPack(long countPack_) {
        this.countPack_ = countPack_;
    }

    @Override
    public void stop() {
        if (isActive_) {
            isActive_ = false;
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("In waiting parts of file...");
            while (isActive_) {
                byte[] bytes = (byte[]) window_.take();
                try {
                    fos_.write(bytes);
                } catch (IOException e) {
                    err_.write("Can't write in file: " + e.getMessage());
                }
                //System.out.println("Wrote " + currCountPack_ + " part of file");
                if (currCountPack_ == countPack_) {
                    isActive_ = false;
                }
                currCountPack_++;
            }
        }finally {
            try {
                fos_.close();
            } catch (IOException e) {
                err_.write("Can't close file output stream: "+e.getMessage());
            }
            System.out.println("\tBye, FileWriter");
            classServer_.stop();
        }
    }
}
