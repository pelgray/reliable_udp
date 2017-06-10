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
    private String host_;
    private LogMessageErrorWriter err_;

    private boolean isActive_;
    private final int packSize_ = 2048; //размер одного пакета
    private final int winSize_ = 5; //размер окна, а также циклического буфера 5
    private final int channelSize_ = 5000;
    private SlidingWindow window_;

    private File file_;
    private File folder_;
    private String filename_;
    private DatagramSocket sendSocket_;
    private DatagramSocket receiveSocket_;
    private Server classServer_;

    // запускаемые в отдельных потоках классы
    private FileWriter classFileWriter_;
    private ServerSender classSender_;
    private ServerReceiver classReceiver_;


    public Server(int receivePort_,String host, int sendPort_, LogMessageErrorWriter err_) {
        this.sendPort_ = sendPort_;
        this.receivePort_ = receivePort_;
        this.err_ = err_;
        host_ = host;
        classServer_ = this;
        isActive_ = true;
        folder_ = new File("c:"+File.separator+"udp_directory_server");// папка для файлов на прием
        if(!folder_.exists()) {
            folder_.mkdir();
        }
    }

    public boolean isActive(){
        return isActive_;
    }

    public void startFileWriter(String filename, long countPack){
        filename_ = filename;

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
        classFileWriter_ = new FileWriter(window_, file_, err_, classServer_, countPack);
        Thread threadFileWriter = new Thread(classFileWriter_);
        threadFileWriter.setName("FILE_WRITER");
        threadFileWriter.start();
    }

    @Override
    public void stop() {
        if (isActive_){
            isActive_ = false;
            if (classReceiver_ != null) classReceiver_.stop();
            if (classSender_ != null) classSender_.stop();
            if (classFileWriter_ != null) classFileWriter_.stop();
        }
    }

    @Override
    public void run() {
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
    }

    @Override
    public void setDeliveredPacket(int index) {
        classSender_.setDeliveredPacket(index);
    }
}
