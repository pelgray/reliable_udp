package Server;

import CommonUtils.LogMessageErrorWriter;
import CommonUtils.SlidingWindow;
import CommonUtils.Sound;
import CommonUtils.Stoppable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by pelgray on 06.06.2017.
 */
public class FileWriter implements Stoppable {
    private boolean isActive_;
    private final long countPack_;
    private long currCountPack_;
    private final SlidingWindow window_;
    private FileOutputStream fos_;
    private final LogMessageErrorWriter err_;
    private final Server classServer_;
    private final File file_;

    private long start;

    public FileWriter(SlidingWindow window_, File file, LogMessageErrorWriter errorWriter, Server server, long countPack) {
        this.isActive_ = true;
        this.window_ = window_;
        this.err_ = errorWriter;
        classServer_ = server;
        this.countPack_ = countPack;
        currCountPack_ = 1;
        file_ = file;
        try {
            this.fos_ = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            err_.write("File not found.");
        }
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
            start = System.currentTimeMillis();
            while (isActive_) {
                byte[] bytes = (byte[]) window_.take();
                try {
                    fos_.write(bytes);
                } catch (IOException e) {
                    err_.write("Can't write in file: " + e.getMessage());
                }
                //System.out.println("\t\tWrote #" + currCountPack_);
                if (currCountPack_ == countPack_) {
                    isActive_ = false;
                }
                currCountPack_++;
            }
        }finally {
            long finish = System.currentTimeMillis();
            System.out.println("ALL TIME: " + (finish - start)*0.001);
            try {
                fos_.close();
            } catch (IOException e) {
                err_.write("Can't close file output stream: "+e.getMessage());
            }
            System.out.printf("A '%s' file of size ", file_.getName());
            long size = file_.length();
            if (size<1000) System.out.printf("[%d B] was received.%n", size);
            else {
                if (size < 1000000) System.out.printf("[%,.2f KB] was received.%n", size * 0.001);
                else System.out.printf("[%,.2f MB] was received.%n", size * 1e-6);
            }
            System.out.println("\tBye, FileWriter");
            classServer_.stop();
            Sound.playSound("C:\\Users\\1\\IdeaProjects\\UDP_lab\\src" + File.separator + "SP0000.WAV").join();
        }
    }
}
