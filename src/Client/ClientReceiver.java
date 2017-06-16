package Client;

import CommonUtils.Stoppable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by pelgray on 06.06.2017.
 */
public class ClientReceiver implements Stoppable {
    private boolean isActive_;
    private final Client classClient_;
    private final DatagramSocket datagramSocket_;
    private boolean senderFinished_ = false;

    public ClientReceiver(Client classClient, DatagramSocket datagramSocket_) {
        this.isActive_ = true;
        this.classClient_ = classClient;
        this.datagramSocket_ = datagramSocket_;
    }

    @Override
    public void stop() {
        if(isActive_) {
            isActive_ = false;
        }
    }

    public void senderFinished(){
        senderFinished_ = true;
        datagramSocket_.close();
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[4];
            DatagramPacket packet;
            while (isActive_) {
                packet = new DatagramPacket(buffer, 4);
                try {
                    datagramSocket_.receive(packet);
                } catch (SocketException e) {
                    if (e.getMessage().equalsIgnoreCase("socket closed")){
                        isActive_ = false;
                        return;
                    }
                    else e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int index = java.nio.ByteBuffer.wrap(packet.getData()).getInt();
                classClient_.setDeliveredPacket(index);
                if (senderFinished_) isActive_ = false;
            }
        }
        finally {
            datagramSocket_.close();
            System.out.println("\tBye, ClientReceiver");
        }
    }
}
