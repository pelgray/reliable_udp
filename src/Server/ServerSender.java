package Server;

import CommonUtils.CallBack;
import CommonUtils.Channel;
import CommonUtils.LogMessageErrorWriter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by 1 on 06.06.2017.
 */
public class ServerSender implements CallBack {
    private boolean isActive_;
    private long countPack_;
    private Channel<Integer> channelIndex_;
    private DatagramSocket datagramSocket_;
    private LogMessageErrorWriter err_;
    InetSocketAddress clientAddr_;

    public ServerSender(DatagramSocket datagramSocket_, LogMessageErrorWriter err_, int channelSize, InetSocketAddress clientAddr) {
        this.datagramSocket_ = datagramSocket_;
        this.err_ = err_;
        this.isActive_ = true;
        this.channelIndex_ = new Channel<>(channelSize, err_);
        this.clientAddr_ = clientAddr;
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
        System.out.println("In waiting numbers of packets...");
        try {
            while (isActive_) {
                if (channelIndex_.getSize() != 0) {
                    int index = channelIndex_.take();
                    byte[] bytesInd = ByteBuffer.allocate(4).putInt(index).array();
                    DatagramPacket packet_ = new DatagramPacket(bytesInd, 4, clientAddr_);
                    try {
                        datagramSocket_.send(packet_);
                    } catch (IOException e) {
                        err_.write("Can't send a number of received packet: " + e.getMessage());
                    }
                    //System.out.println("Confirmed receipt of packet #" + index);
                }
            }
        }
        finally {
            datagramSocket_.close();
            System.out.println("\tBye, ServerSender");
        }
    }

    @Override
    public void setDeliveredPacket(int index) {
        channelIndex_.put(index);
    }
}
