package Client;

import CommonUtils.Channel;
import CommonUtils.CallBack;
import CommonUtils.LogMessageErrorWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by pelgray on 06.06.2017.
 */
public class Client implements CallBack {
    // путь к файлу - спрашивается при запуске (точнее, название файла в отведенной папке)

    private boolean isActive_;
    private final int packSize_ = 2048; //размер одного пакета
    private final int winSize_ = 5; //размер окна, а также циклического буфера 5
    private final int channelSize_ = 5000;
    private final LogMessageErrorWriter e_;

    private final int port_; //порт для приема сообщений
    private final String host_; // адрес, куда
    private final int serverPort_; // порт для передачи данных
    private final BufferedReader bR_;

    private final File folder_; // папка, в которой этот файл находится

    // запускаемые в отдельных потоках классы
    private FileReader classFileReader_;
    private ClientSender classSender_;
    private ClientReceiver classReceiver_;


    public void setCountPack(long num) {
        classSender_.setCountOfPack(num);
    }

    public Client(int port, String host, int servPort, LogMessageErrorWriter errorWriter) {
        port_ = port;
        host_ = host;
        serverPort_ = servPort;
        e_ = errorWriter;
        isActive_ = true;
        folder_ = new File("c:"+File.separator+"udp_directory_client");// папка для файлов на отправку
        if(!folder_.exists()) {
            folder_.mkdir();
        }

        bR_ = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void stop() {
        if (isActive_) {
            isActive_ = false;
            if (classFileReader_ != null) classFileReader_.stop();
            if (classSender_ != null) classSender_.stop();
            if (classReceiver_ != null) classReceiver_.stop();
        }
    }

    @Override
    public void run() {
        System.out.println("Choose file. Please, write the number from list:");
        File[] listFiles = folder_.listFiles();
        for (int i = 0; i<listFiles.length; i++){
            System.out.println("\t" + i + ") " + listFiles[i].getName());
            long size = listFiles[i].length();
            if (size<1000) System.out.printf("\t\t\t\t[SIZE: %d B]%n", size);
            else {
                if (size < 1000000) System.out.printf("\t\t\t\t[SIZE: %,.2f KB]%n", size * 0.001);
                else System.out.printf("\t\t\t\t[SIZE: %,.2f MB]%n", size * 1e-6);
            }
        }
        System.out.println();
        int number;
        try {
            number = Integer.parseInt(bR_.readLine());
        }  catch (NumberFormatException e) {
            e_.write("It is not number. Try again.");
            return;
        } catch (IOException e) {
            e_.write("An error occurred while reading number from list.");
            return;
        }
        String filename_;
        if (number>=0 && number<listFiles.length) {
            filename_ = listFiles[number].getName();
            System.out.println("Ok, you choose a file: " + filename_);
            long size = listFiles[number].length();
            if (size<1000) System.out.printf("\t\t\t\t[SIZE: %d B]%n", size);
            else {
                if (size < 1000000) System.out.printf("\t\t\t\t[SIZE: %,.2f KB]%n", size * 0.001);
                else System.out.printf("\t\t\t\t[SIZE: %,.2f MB]%n", size * 1e-6);
            }
        }
        else {
            System.out.println("This number is not in the list. Try again.");
            return;
        }

        File file_ = new File(folder_, filename_);

        if (!file_.exists()){
            e_.write("File not found.");
            return;
        }

        Channel<byte[]> byteChannel_ = new Channel<>(channelSize_, e_);
        classFileReader_ = new FileReader(this, file_,packSize_-4,byteChannel_,e_);

        DatagramSocket sendSocket_;
        try {
            sendSocket_ = new DatagramSocket();
        } catch (SocketException e) {
            e_.write("Can't create a DatagramSocket sendSocket: " + e.getMessage());
            return;
        }

        DatagramSocket receiveSocket_;
        try {
            receiveSocket_ = new DatagramSocket(port_);
        } catch (SocketException e) {
            e_.write("Can't create a DatagramSocket receiveSocket: " + e.getMessage());
            return;
        }

        classReceiver_ = new ClientReceiver(this, receiveSocket_);

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
    }

    @Override
    public void setDeliveredPacket(int index) {
        classSender_.setDeliveredPacket(index);
    }
}
