package CommonUtils;

import java.util.Date;

/**
 * Created by 1 on 07.04.2017.
 */
public class DFLTlogMessageError implements LogMessageErrorWriter{
    MessageErrorType _type;

    public DFLTlogMessageError(MessageErrorType type) {
        _type = type;
    }

    @Override
    public void write(String msg) {
        switch (_type){
            case STANDARD:
                try {
                    throw new Exception("Who called me?");
                }
                catch( Exception e )
                {
                    System.err.println(e.getStackTrace()[1].getClassName() + ": " + msg);
                }
                break;
            case SHORTCUT:
                System.err.println(msg);
                break;
            case DETAILED:
                try {
                    throw new Exception("Who called me?");
                }
                catch( Exception e )
                {
                    System.err.println(e.getStackTrace()[1].getClassName() + ", date [" + (new Date()) + "]: \t\t" + msg);
                }
                break;
        }
    }
}
