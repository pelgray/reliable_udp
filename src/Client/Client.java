package Client;

import CommonUtils.Channel;
import CommonUtils.CallBack;
import CommonUtils.LogMessageErrorWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by 1 on 06.06.2017.
 */
public class Client implements CallBack {
    // из аргументов:
    // порт для передачи файла
    // адрес, куда
    // порт для приема сообщений


    // путь к файлу - спрашивается при запуске (точнее, название файла в отведенной папке)

    private boolean isActive_;
    int port_; //порт для приема сообщений
    String host_; // адрес, куда
    final int packSize_ = 5000; //размер одного пакета
    int winSize_ = 5; //размер окна, а также циклического буфера 5
    int serverPort_; // порт для передачи данных
    BufferedReader bR_;
    LogMessageErrorWriter e_;
    int channelSize_ = 5000;
    private long countPack = 0;

    public void setCountPack(long num) {
        this.countPack = num;
        classSender_.setCountOfPack(num);
    }

    private File file_;
    private File folder_;
    private String filename_;
    private DatagramSocket sendSocket_;
    private DatagramSocket receiveSocket_;

    // запускаемые в отдельных потоках классы
    FileReader classFileReader_;
    ClientSender classSender_;
    ClientReceiver classReceiver_;


    public Client(int port, String host, int servPort, LogMessageErrorWriter errorWriter) {
        port_ = port;
        host_ = host;
        serverPort_ = servPort;
        e_ = errorWriter;
        isActive_ = true;

        bR_ = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void stop() {
        if (isActive_) {
            isActive_ = false;
            classFileReader_.stop();
            classSender_.stop();
            classReceiver_.stop();
        }
    }

    @Override
    public void run() {
        folder_ = new File("c:"+File.separator+"udp_directory_client");// папка для файлов на отправку
        if(!folder_.exists()) {
            folder_.mkdir();
        }

//        System.out.println("Enter filename: ");
//        try {
//            filename_ = bR_.readLine();
//        } catch (IOException e) {
//            e_.write("An error occurred while reading name of file.");
//            return;
//        }
        filename_ = "Patterny_proektirovania.pdf";

        file_ = new File(folder_, filename_);

        if (!file_.exists()){
            e_.write("File not found.");
            return;
        }


        //Channel<DatagramPacket> packetChannel_ = new Channel<>(channelSize_, e_);


        Channel<byte[]> byteChannel_ = new Channel<>(channelSize_, e_);
        classFileReader_ = new FileReader(this, file_,packSize_-4,byteChannel_,e_);

        try {
            sendSocket_ = new DatagramSocket();
        } catch (SocketException e) {
            e_.write("Can't create a DatagramSocket sendSocket: " + e.getMessage());
            return;
        }

        try {
            receiveSocket_ = new DatagramSocket(port_);
        } catch (SocketException e) {
            e_.write("Can't create a DatagramSocket receiveSocket: " + e.getMessage());
            return;
        }

        classReceiver_ = new ClientReceiver(this,receiveSocket_);

        classSender_ = new ClientSender(winSize_, e_, sendSocket_, byteChannel_, classReceiver_, new InetSocketAddress(host_, serverPort_), packSize_);
        Thread threadClientSender = new Thread(classSender_);
        threadClientSender.setName("CLIENT_SENDER");
        threadClientSender.start();

        Thread threadFileReader = new Thread(classFileReader_);
        threadFileReader.setName("FILE_READER");
        threadFileReader.start();

        Thread threadClientReceiver = new Thread(classReceiver_);
        threadClientReceiver.setName("CLIENT_RECEIVER");
        threadClientReceiver.start();

        System.out.println("\tBye, ClientMain");
    }

    @Override
    public void setDeliveredPacket(int index) {
        classSender_.setDeliveredPacket(index);
    }
}
