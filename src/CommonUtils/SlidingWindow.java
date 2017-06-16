package CommonUtils;

import java.net.DatagramPacket;

/**
 * Created by pelgray on 06.06.2017.
 */
public class SlidingWindow {
    private RingBuffer buf = null;
    private int size;
    private int currNum = 0; // текущий номер последнего добавленного объекта (для стороны отсылки)
    private int currInd = 1; // текущий индекс полученного в упорядоченном порядке объекта
    private boolean canPut = true; // можно ли добавить объект
    private boolean canTake = false; // можно ли забрать объект
    private final Object lock = new Object();
    private final LogMessageErrorWriter err;

    public SlidingWindow(int size, LogMessageErrorWriter errorWriter) {
        this.err = errorWriter;
        this.size = size;
        buf = new RingBuffer(size);
    }

    // для стороны отправки
    public boolean put(Object pack){
        synchronized (lock) {
            //System.out.println("[SW] put");
            buf.put(new Struct(currNum, pack));
            currNum++;
            return buf.checkPutSending();
        }
    }

    // для стороны отправки
    public boolean setDelivered(int ind){
        synchronized (lock) {
            //System.out.println("[SW] setDelivered");
            return buf.setStatus(ind);
        }
    }

    // для стороны отправки
    public DatagramPacket[] getNonDelivered(){
        synchronized (lock) {
            //System.out.println("[SW] getNonDelivered");
            return buf.takeUnmarked();
        }
    }



    //---------------------------------------------------------------------------------------------------------------------------------------
    // для стороны приема
    public boolean put(Object ob, int ind){
        synchronized (lock) {
            while(!canPut) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    err.write("The error of waiting in 'put'-condition.");
                }
            }
            if (ind >= currInd && ind <currInd+size) {
                int place = ind%size;
                if (buf.checkPut(place)) {
                    buf.put(new Struct(ind, ob), place);
                    if (ind == currInd) {
                        canTake = true;
                        lock.notify();
                    }
                    canPut = buf.checkPutReceiving();
                    return true;
                }
                else return false;
            }
            else return false;
        }
    }

    // для стороны приема
    public Object take(){
        synchronized (lock){
            while (!canTake){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    err.write("The error of waiting in 'take'-condition.");
                }
            }
            Object rtn = buf.take(currInd);
            currInd++;
            canTake = buf.checkTake(currInd);
            canPut = buf.checkPutReceiving();
            if (canPut) lock.notify();
            return rtn;
        }
    }
}
