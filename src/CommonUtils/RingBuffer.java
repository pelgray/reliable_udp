package CommonUtils;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by 1 on 07.06.2017.
 */
public class RingBuffer {
    public Struct[] elements = null;

    private int capacity  = 0; // максимальное количество объектов
    private int writePos  = 0; // позиция, на которой остановились
    private int available = 0; // текущее кол-во объектов
    private boolean existSmth = false; // есть ли что-нибудь в буфере

    public RingBuffer(int capacity) {
        this.capacity = capacity;
        this.elements = new Struct[capacity];
    }

    public synchronized boolean checkPut(){
//        System.out.println("[BUFFER checkPut BEFORE]: writePos " + writePos + " capacity " + capacity);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                DatagramPacket cur = (DatagramPacket) elements[i].data;
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " sys#" + ByteBuffer.wrap(cur.getData(), 0, 4).getInt() + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }
        return (elements[writePos] == null)?true:(elements[writePos].status);
    }

    public synchronized boolean checkTake(int ind){
        if (existSmth) {
            for (int i = 0; i < capacity; i++) {
                if (elements[i] != null && elements[i].num == ind) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void put(Struct element){
//        System.out.println("[BUFFER put BEFORE]: writePos " + writePos);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }

        elements[writePos] = element;
        writePos++;
        if(writePos == capacity){
            writePos = 0;
        }
        if (available != capacity) available++;
        if (!existSmth) existSmth = true;

//        System.out.println("[BUFFER put AFTER]: writePos " + writePos);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }
    }

    public synchronized Object take(int ind) {
        if(!existSmth){
            return null;
        }

//        System.out.println("[BUFFER take BEFORE]: ind " + ind + ", available " + available);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }

        int in = -1;
        for (int i = 0; i<capacity; i++){
            if (elements[i] != null && elements[i].num == ind) {
                elements[i].status = true;
                available--;
                if (available == 0) existSmth = false;
                in = i;
            }
        }

//        System.out.println("[BUFFER take AFTER]: ind " + ind + ", available " + available + ", found index " + in);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }

        return (in == -1)? null : elements[in].data;
    }
    
    // возвращает сигнал, можно ли продолжать добавлять
    public synchronized boolean setStatus(int ind){
//        System.out.println("[BUFFER setStatus BEFORE]: writePos " + writePos);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                DatagramPacket cur = (DatagramPacket) elements[i].data;
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " sys#" + ByteBuffer.wrap(cur.getData(), 0, 4).getInt() + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }
        for (int i = 0; i<capacity; i++){
            if (elements[i] != null && elements[i].num == ind) {
                elements[i].status = true;
            }
        }
//        System.out.println("[BUFFER setStatus AFTER]: writePos " + writePos);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                DatagramPacket cur = (DatagramPacket) elements[i].data;
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " sys#" + ByteBuffer.wrap(cur.getData(), 0, 4).getInt() + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }

        return (elements[writePos] == null||elements[writePos].status);
    }

    public synchronized DatagramPacket[] takeUnmarked(){
        LinkedList<Object> res = new LinkedList<>();
        //System.out.println("[BUFFER]: writePos " + writePos);
        for (int i = 0; i<capacity; i++){
            //DatagramPacket cur = (DatagramPacket) elements[i].data;
            //System.out.println("\t\t\t"+i + " #" + elements[i].num + " sys#" + ByteBuffer.wrap(cur.getData(), 0, 4).getInt() + " " + elements[i].status);
            if (elements[i] != null && !elements[i].status) {
                res.add(elements[i].data);
                //System.out.println("\t\t\t\tADD " + i);
            }
        }
        return res.toArray(new DatagramPacket[res.size()]);
    }
}
