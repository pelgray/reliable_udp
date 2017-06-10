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

/**
 * Created by 1 on 06.06.2017.
 */
public class ClientSender implements CallBack {
    private boolean isActive_;
    private SlidingWindow window_;
    private LogMessageErrorWriter err_;
    private DatagramSocket datagramSocket_;
    private Channel<byte[]> byteChannel_;
    private ClientReceiver classReceiver_;
    private long countOfPack_ = 0;
    private int numOfPack_ = 0;
    private InetSocketAddress servAddr_;
    private int packSize_;
    private boolean isAllAdded_ = false;
    private boolean canPut_;
    private final Object lock_ = new Object();

    public ClientSender(int winSize_, LogMessageErrorWriter errorWriter, DatagramSocket datagramSocket, Channel<byte[]> channel, ClientReceiver receiver, InetSocketAddress servAddr, int packSize) {
        this.err_ = errorWriter;
        window_ = new SlidingWindow(winSize_, errorWriter);
        datagramSocket_ = datagramSocket;
        byteChannel_ = channel;
        classReceiver_ = receiver;
        packSize_ = packSize;
        servAddr_ = servAddr;
        isActive_ = true;
        canPut_ = true;
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
            DatagramPacket packet = new DatagramPacket(data.clone(), data.length, servAddr_);
            synchronized (lock_) {
                canPut_ = window_.put(packet);
            }
            try {
                datagramSocket_.send(packet);
            } catch (IOException e) {
                err_.write("Can't send a init packet: " + e.getMessage());
            }
            System.out.println("Create and send init pack. Size " + packet.getLength());
            numOfPack_++;
            while (isActive_) {
                while (canPut_ && !isAllAdded_) {
                    // создание пакетов
                    bytes = byteChannel_.take();
                    data = new byte[packSize_];
                    index = ByteBuffer.allocate(4).putInt(numOfPack_).array(); // номер пакета
                    System.arraycopy(index, 0, data, 0, 4);
                    System.arraycopy(bytes, 0, data, 4, packSize_-4);
                    packet = new DatagramPacket(data.clone(), data.length, servAddr_);
                    //if (numOfPack_%1000 == 0) System.out.println("Send packet #" + numOfPack_);
                    if (numOfPack_ == countOfPack_) {
                        isAllAdded_ = true;
                    }
                    numOfPack_++;
                    synchronized (lock_) {
                        canPut_ = window_.put(packet);
                        //System.out.println("\t\tAfterSEND: canPut = " + canPut_);
                    }
                    try {
                        datagramSocket_.send(packet);
                    } catch (IOException e) {
                        err_.write("Can't send a packet: " + e.getMessage());
                    }

//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DatagramPacket[] packs = window_.getNonDelivered();
                //System.out.println("FOR RESEND " + packs.length);
                for (DatagramPacket pack : packs) {
                    try {
                        datagramSocket_.send(pack);
                    } catch (IOException e) {
                        err_.write("Can't send a packet: " + e.getMessage());
                    }
                    //System.out.println("\tResend packet #" + ByteBuffer.wrap(pack.getData(), 0, 4).getInt());
                }
                synchronized (lock_) {
                    canPut_ = window_.checkPut();
                    //System.out.println("\t\tAfterRESEND: canPut = " + canPut_);
                }
                if (isAllAdded_ && packs.length == 0) {
                    isActive_ = false;
                    classReceiver_.senderFinished();
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
            //System.out.println("\t\tSetDelivered: canPut = " + canPut_);
        }
    }

    public void setCountOfPack(long num){
        countOfPack_ = num;
    }
}
