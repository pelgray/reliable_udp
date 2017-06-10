package Server;

import CommonUtils.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

/**
 * Created by 1 on 06.06.2017.
 */
public class ServerReceiver implements Stoppable {
    private boolean isActive_;
    private DatagramSocket datagramSocket_;
    private int packSize_;
    private LogMessageErrorWriter err_;
    private SlidingWindow window_;
    private Server classServer_;
    private long countPack_;
    long start;

    public ServerReceiver(DatagramSocket datagramSocket_, int packSize_, LogMessageErrorWriter err_, Server classServer_, SlidingWindow win) {
        this.datagramSocket_ = datagramSocket_;
        this.packSize_ = packSize_;
        this.err_ = err_;
        this.classServer_ = classServer_;
        isActive_ = true;
        window_ = win;
    }

    @Override
    public void stop() {
        if (isActive_) {
            isActive_ = false;
            datagramSocket_.close();
        }
    }

    @Override
    public void run() {
        try {
            byte[] byteIndex = new byte[4];
            byte[] data = new byte[packSize_ - 4];
            byte[] datagramBuffer = new byte[packSize_];
            DatagramPacket packet = new DatagramPacket(datagramBuffer, packSize_);

            start = System.currentTimeMillis();
            System.out.println("In waiting packets...\nTIME: " + LocalDateTime.now());
            while (isActive_) {
                try {
                    datagramSocket_.receive(packet);
                } catch (SocketException e) {
                    if (e.getMessage().equalsIgnoreCase("socket closed")) {
                        isActive_ = false;
                        return;
                    }
                    else e.printStackTrace();
                } catch (IOException e) {
                    err_.write("An error while receive packet: " + e.getMessage());
                }
                datagramBuffer = packet.getData();
                System.arraycopy(datagramBuffer, 0, byteIndex, 0, 4);
                int index = ByteBuffer.wrap(byteIndex).getInt();

                if (index == 0) {
                    System.arraycopy(datagramBuffer, 4, data, 0, 8);
                    countPack_ = ByteBuffer.wrap(datagramBuffer, 4, 8).getLong();
                    classServer_.setCountPack(countPack_);
                    System.out.println("Will be received " + countPack_ + " packets.");
                } else {
                    data = new byte[packSize_ - 4];
                    System.arraycopy(datagramBuffer, 4, data, 0, packSize_ - 4);
                    window_.put(data, index);
                    //if (index%1000 == 0) System.out.println("Receive packet #" + index);
                }
                classServer_.setDeliveredPacket(index);
                if (!classServer_.isActive()) {
                    isActive_ = false;

                }
            }
        }
        finally {
            long finish = System.currentTimeMillis();
            System.out.println("END TIME: " + LocalDateTime.now());
            System.out.println("ALL TIME: " + (finish - start));
            datagramSocket_.close();
            System.out.println("\tBye, ServerReceiver");
        }
    }
}
