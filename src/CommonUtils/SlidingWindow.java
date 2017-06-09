package CommonUtils;

import java.net.DatagramPacket;

/**
 * Created by 1 on 06.06.2017.
 */
public class SlidingWindow {
    private RingBuffer buf = null;
    private int size;
    private int currNum = 0; // текущий номер последнего добавленного объекта (для стороны отсылки)
    private int currInd = 1; // текущий индекс полученного в упорядоченном порядке объекта
    private boolean canPut = true; // можно ли добавить объект
    private boolean canTake = false; // можно ли забрать объект
    private final Object lock = new Object();
    private LogMessageErrorWriter err;
    private long countOfPack;
    private boolean isAllDelivered;
    private boolean isAllAdded;

    public SlidingWindow(int size, LogMessageErrorWriter errorWriter) {
        this.size = size;
        this.err = errorWriter;
        buf = new RingBuffer(size);
        isAllDelivered = false;
        isAllAdded = false;
    }

    public void setCountOfPack(long countOfPack) {
        this.countOfPack = countOfPack;
    }

    public boolean isAllDelivered() {
        synchronized (lock) {
            if (isAllAdded){
                DatagramPacket[] res = (DatagramPacket[]) buf.takeUnmarked();
                isAllDelivered = (res.length == 0);
            }
            return (isAllDelivered);
        }
    }

    public boolean canPut() {
        synchronized (lock) {
            if (!isAllAdded) {
                canPut = buf.checkPut(); // ???
                lock.notify();
            }
            return canPut;
        }
    }

    public void setAllAdded(){
        synchronized (lock){
            isAllAdded = true;
            canPut = false;
        }
    }

    // для стороны отправки
    public void put(Object pack){
        synchronized (lock) {
            if(!isAllAdded) {
                while (!canPut) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        err.write("The error of waiting in 'put'-condition.");
                    }
                }
                buf.put(new Struct(currNum, pack));
                currNum++;
                //if (buf.available() == size) canPut = false;
                canPut = buf.checkPut();
            }
        }
    }

    // для стороны отправки
    public void setDelivered(int ind){
        synchronized (lock) {
            canPut = buf.setStatus(ind);
            DatagramPacket[] res = (DatagramPacket[]) buf.takeUnmarked();
            isAllDelivered = (res.length == 0);
            //if (!isAllAdded)
                lock.notifyAll();
        }
    }

    // для стороны отправки
    public DatagramPacket[] getNonDelivered(){
        synchronized (lock) {
            if(canPut) {
                try {
                    lock.wait(5000);
                } catch (InterruptedException e) {
                    err.write("The error of waiting in 'put'-condition.");
                }
            }
            DatagramPacket[] res = (DatagramPacket[]) buf.takeUnmarked();
            if (res.length == 0) isAllDelivered = true;
            return res;
        }
    }

    // для стороны приема
    public void put(Object ob, int ind){
        synchronized (lock) {
            while(!canPut) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    err.write("The error of waiting in 'put'-condition.");
                }
            }
            buf.put(new Struct(ind, ob));
            //System.out.println("Receive packet #" + ind);
            if (ind == currInd){
                canTake = true;
                lock.notify();
            }
            canPut = buf.checkPut();
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
            //System.out.println("Write packet #"+currInd);
            currInd++;
            canTake = buf.checkTake(currInd);
            canPut = buf.checkPut();
            lock.notify();
            return rtn;
        }
    }
}
