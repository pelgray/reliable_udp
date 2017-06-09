package Server;

import CommonUtils.CallBack;
import CommonUtils.LogMessageErrorWriter;
import CommonUtils.SlidingWindow;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by 1 on 06.06.2017.
 */
public class Server implements CallBack{
    private int sendPort_; // для сообщений
    private int receivePort_; // для данных
    private LogMessageErrorWriter err_;


    private boolean isActive_;
    private int packSize_ = 5000; //размер одного пакета
    int winSize_ = 5; //размер окна, а также циклического буфера 5
    BufferedReader bR_;
    int channelSize_ = 5000;
    private long countPack;
    private SlidingWindow window_;
    private String host_;



    private File file_;
    private File folder_;
    private String filename_;
    private DatagramSocket sendSocket_;
    private DatagramSocket receiveSocket_;

    // запускаемые в отдельных потоках классы
    FileWriter classFileWriter_;
    ServerSender classSender_;
    ServerReceiver classReceiver_;


    public Server(int receivePort_,String host, int sendPort_, LogMessageErrorWriter err_) {
        this.sendPort_ = sendPort_;
        this.receivePort_ = receivePort_;
        this.err_ = err_;
        host_ = host;
        isActive_ = true;
        bR_ = new BufferedReader(new InputStreamReader(System.in));
    }

    public void setCountPack(long num){
        countPack = num;
        classFileWriter_.setCountPack(num);
        classSender_.setCountPack(num);
    }

    public boolean isActive(){
        return isActive_;
    }

    @Override
    public void stop() {
        if (isActive_){
            isActive_ = false;
            classReceiver_.stop();
            classSender_.stop();
            classFileWriter_.stop();
        }
    }

    @Override
    public void run() {
        folder_ = new File("c:"+File.separator+"udp_directory_server");// папка для файлов на прием
        if(!folder_.exists()) {
            folder_.mkdir();
        }

//        System.out.println("Enter filename: ");
//        try {
//            filename_ = bR_.readLine();
//        } catch (IOException e) {
//            err_.write("An error occurred while reading name of file.");
//            return;
//        }
        filename_ = "Patterny_proektirovania.pdf";

        file_ = new File(folder_, filename_);

        if (file_.exists()){
            err_.write("File was found and will be deleted.");
            file_.delete();
        }
        try {
            file_.createNewFile();
        } catch (IOException e) {
            err_.write("An error while creating a new file: " + e.getMessage());
        }
        try {
            receiveSocket_ = new DatagramSocket(receivePort_);
        } catch (SocketException e) {
            err_.write("Can't create a DatagramSocket receiveSocket: " + e.getMessage());
            return;
        }
        window_ = new SlidingWindow(winSize_, err_);
        classReceiver_ = new ServerReceiver(receiveSocket_,packSize_,err_, this, window_);
        Thread threadServerReceiver = new Thread(classReceiver_);
        threadServerReceiver.setName("SERVER_RECEIVER");
        threadServerReceiver.start();

        try {
            sendSocket_ = new DatagramSocket();
        } catch (SocketException e) {
            err_.write("Can't create a DatagramSocket sendSocket: " + e.getMessage());
            return;
        }
        classSender_ = new ServerSender(sendSocket_, err_,channelSize_, new InetSocketAddress(host_, sendPort_));
        Thread threadServerSender = new Thread(classSender_);
        threadServerSender.setName("SERVER_SENDER");
        threadServerSender.start();

        classFileWriter_ = new FileWriter(window_, file_, err_, this);
        Thread threadFileWriter = new Thread(classFileWriter_);
        threadFileWriter.setName("FILE_WRITER");
        threadFileWriter.start();
        System.out.println("\tBye, ServerMain");
    }

    @Override
    public void setDeliveredPacket(int index) {
        classSender_.setDeliveredPacket(index);
    }
}
