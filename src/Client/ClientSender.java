package Client;

import CommonUtils.Channel;
import CommonUtils.CallBack;
import CommonUtils.LogMessageErrorWriter;
import CommonUtils.SlidingWindow;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pelgray on 06.06.2017.
 */
public class ClientSender implements CallBack {
    private boolean isActive_;
    private boolean isActiveTimer_;
    private final SlidingWindow window_;
    private final LogMessageErrorWriter err_;
    private final DatagramSocket datagramSocket_;
    private final Channel<byte[]> byteChannel_;
    private final ClientReceiver classReceiver_;
    private long countOfPack_;
    private int numOfPack_ = 0;
    private final InetSocketAddress servAddr_;
    private final int packSize_;
    private boolean isAllAdded_;
    private boolean canPut_;
    private final Object lock_ = new Object();
    private final Timer timer = new Timer();
    private final long TIME_OUT = 1000;

    public ClientSender(int winSize_, LogMessageErrorWriter errorWriter, DatagramSocket datagramSocket, Channel<byte[]> channel, ClientReceiver receiver, InetSocketAddress servAddr, int packSize) {
        this.err_ = errorWriter;
        window_ = new SlidingWindow(winSize_, errorWriter);
        datagramSocket_ = datagramSocket;
        byteChannel_ = channel;
        classReceiver_ = receiver;
        packSize_ = packSize;
        servAddr_ = servAddr;
        isActive_ = true;
        isActiveTimer_ = true;
        canPut_ = true;
        isAllAdded_ = false;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                resend();
            }
        }, 2*TIME_OUT, TIME_OUT);
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
            byte[] bytes = byteChannel_.take();
            byte[] data = new byte[bytes.length + 4];
            byte[] index = ByteBuffer.allocate(4).putInt(numOfPack_).array(); // номер пакета
            System.arraycopy(index, 0, data, 0, 4);
            System.arraycopy(bytes, 0, data, 4, bytes.length);
            DatagramPacket packet = new DatagramPacket(data, data.length, servAddr_);

            canPut_ = window_.put(packet);
            try {
                datagramSocket_.send(packet);
            } catch (IOException e) {
                err_.write("Can't send a init packet: " + e.getMessage());
            }
            System.out.println("Create and send an init packet.");
            numOfPack_++;
            while (isActive_) {
                while (canPut_ && !isAllAdded_) {
                    // создание пакетов
                    bytes = byteChannel_.take();
                    data = new byte[packSize_];
                    index = ByteBuffer.allocate(4).putInt(numOfPack_).array(); // номер пакета
                    System.arraycopy(index, 0, data, 0, 4);
                    System.arraycopy(bytes, 0, data, 4, packSize_-4);
                    packet = new DatagramPacket(data, packSize_, servAddr_);
                    if (numOfPack_ != countOfPack_) {
                        numOfPack_++;
                    }
                    else {
                        isAllAdded_ = true;
                    }
                    synchronized (lock_) {
                        canPut_ = window_.put(packet);
                    }
                    try {
                        datagramSocket_.send(packet);
                    } catch (IOException e) {
                        err_.write("Can't send a packet: " + e.getMessage());
                    }
                }
                synchronized (lock_) {
                    if (!isActiveTimer_) {
                        isActive_ = false;
                    }
                }
            }
        }finally {
            datagramSocket_.close();
            System.out.println("\tBye, ClientSender");
        }
    }

    @Override
    public void setDeliveredPacket(int index) {
        synchronized (lock_) {
            canPut_ = window_.setDelivered(index);
        }
    }

    public void setCountOfPack(long num){
        countOfPack_ = num;
    }

    private void resend(){
        DatagramPacket[] packs;
        synchronized (lock_) {
            packs = window_.getNonDelivered();
        }
        for (DatagramPacket pack : packs) {
            try {
                datagramSocket_.send(pack);
            } catch (IOException e) {
                err_.write("Can't send a packet: " + e.getMessage());
            }
        }
        if (isAllAdded_ && packs.length == 0) {
            synchronized (lock_) {
                isActiveTimer_ = false;
            }
            System.out.println("\tTimer is off.");
            classReceiver_.senderFinished();
            timer.cancel();
        }
    }
}
