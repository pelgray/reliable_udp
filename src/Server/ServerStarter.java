package Server;

import Client.Client;
import CommonUtils.LogMessageErrorFactoryMethod;
import CommonUtils.LogMessageErrorWriter;

import static CommonUtils.MessageErrorType.STANDARD;

/**
 * Created by 1 on 07.06.2017.
 */
public class ServerStarter {
    public static void main(String[] args) {
        int port_; // порт для приема данных
        int clientPort_; //порт для отсылки сообщений
        String host_; // хост для отсылки сообщений

        LogMessageErrorWriter errorWriter = (new LogMessageErrorFactoryMethod()).getWriter("default", STANDARD);
        if (errorWriter == null){
            System.err.println("ServerStarter: The error of getting the errorWriter. Check the name or type.");
            return;
        }
        try {
            port_ = Integer.parseInt(args[0]); // получение номера порта для приема данных из аргументов
        } catch (NumberFormatException e) {
            errorWriter.write("Wrong port format. Should be integer. Try again.");
            return;
        }
        host_ = args[1];
        try {
            clientPort_ = Integer.parseInt(args[2]); // получение номера порта для сообщений из аргументов
        } catch (NumberFormatException e) {
            errorWriter.write("Wrong port format. Should be integer. Try again.");
            return;
        }

        System.out.println("наш порт = " + port_+
                "\nпорт клиента = " + clientPort_);

        Server classServer = new Server(port_, host_, clientPort_, errorWriter);
        Thread server = new Thread(classServer);
        server.setName("SERVER");
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("Shutting down...");
            classServer.stop();
            System.out.println("The Server gonna be stop now. Bye-bye!");
        }));
    }
}
