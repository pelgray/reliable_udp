package Client;

import CommonUtils.LogMessageErrorFactoryMethod;
import CommonUtils.LogMessageErrorWriter;

import static CommonUtils.MessageErrorType.STANDARD;

/**
 * Created by 1 on 06.06.2017.
 */
public class ClientStarter {
    public static void main(String[] args) {
        int port_; // порт для передачи данных
        String host_; // адрес, куда
        int servPort_; //порт для приема сообщений

        // из аргументов:
        // порт для передачи файла
        // адрес, куда
        // порт для приема сообщений

        LogMessageErrorWriter errorWriter = (new LogMessageErrorFactoryMethod()).getWriter("default", STANDARD);
        if (errorWriter == null){
            System.err.println("ClientStarter: The error of getting the errorWriter. Check the name or type.");
            return;
        }
        try {
            port_ = Integer.parseInt(args[0]); // получение номера порта для приема сообщений из аргументов
        } catch (NumberFormatException e) {
            errorWriter.write("Wrong port format. Should be integer. Try again.");
            return;
        }

        host_ = args[1]; // получение хоста из аргументов
        try {
            servPort_ = Integer.parseInt(args[2]); // получение номера порта отправки данных из аргументов
        } catch (NumberFormatException e) {
            errorWriter.write("Wrong port format. Should be integer. Try again.");
            return;
        }

        System.out.println("The own port = " + port_+
                            "\nThe server port = " + servPort_);

        Client classClient = new Client(port_, host_, servPort_, errorWriter);
        Thread client = new Thread(classClient);
        client.setName("CLIENT");
        client.start();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("Shutting down...");
            classClient.stop();
            System.out.println("The Client gonna be stop now. Bye-bye!");
        }));
    }
}
