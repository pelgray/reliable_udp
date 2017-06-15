package Server;

import CommonUtils.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Created by pelgray on 06.06.2017.
 */
public class ServerReceiver implements Stoppable {
    private boolean isActive_;
    private final DatagramSocket datagramSocket_;
    private final int packSize_;
    private final LogMessageErrorWriter err_;
    private final SlidingWindow window_;
    private final Server classServer_;
    private long countPack_;
    private long lastSize_;

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

            System.out.println("In waiting packets...");
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

                if (index != 0) {
                    if (index != countPack_){
                        data = new byte[packSize_ - 4];
                        System.arraycopy(datagramBuffer, 4, data, 0, packSize_ - 4);
                    }
                    else {
                        data = new byte[(int)lastSize_];
                        System.arraycopy(datagramBuffer, 4, data, 0, (int)lastSize_);
                    }
                    boolean check = window_.put(data, index);
                    if (check) { // если пакет успешно добавлен в окно, подтверждаем получение
                        //System.out.println("Receive #" + index);
                        classServer_.setDeliveredPacket(index);
                    }
                } else {
                    System.arraycopy(datagramBuffer, 4, data, 0, datagramBuffer.length-4);
                    ByteArrayInputStream in = new ByteArrayInputStream(data);
                    InitPackage initPack = null;
                    try {
                        ObjectInputStream ois = new ObjectInputStream(in);
                        initPack = (InitPackage) ois.readObject();
                        in.close();
                        ois.close();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    lastSize_ = initPack != null ? initPack.getSizeLastPack() : 0;
                    countPack_ = initPack.getCountPack();
                    classServer_.startFileWriter(initPack.getFilename(), countPack_);
                    System.out.println("Will be received " + countPack_ + " packets.");

                    classServer_.setDeliveredPacket(index);
                }
                if (!classServer_.isActive()) {
                    isActive_ = false;
                }
            }
        }
        finally {
            datagramSocket_.close();
            System.out.println("\tBye, ServerReceiver");
        }
    }
}
