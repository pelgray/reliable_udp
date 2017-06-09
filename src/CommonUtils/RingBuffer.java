package CommonUtils;

import java.net.DatagramPacket;
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

    public int available(){
        return this.available;
    }

    public boolean checkPut(){
        if (available == capacity) return (elements[writePos].status);
        return true;
    }

    public boolean checkTake(int ind){
        if (existSmth) {
            for (int i = 0; i < capacity; i++) {
                if (elements[i] != null && elements[i].num == ind) {
                    return true;
                }
            }
        }
        return false;
    }

    public void put(Struct element){
        elements[writePos] = element;
        writePos++;
        if(writePos == capacity){
            writePos = 0;
        }
        if (available != capacity) available++;
        if (!existSmth) existSmth = true;
    }

    public Object take(int ind) {
        if(!existSmth){
            return null;
        }
        for (int i = 0; i<capacity; i++){
            if (elements[i] != null && elements[i].num == ind) {
                elements[i].status = true;
                available--;
                if (available == 0) existSmth = false;
                return elements[i].data;
            }
        }
        return null;
    }
    
    // возвращает сигнал, можно ли продолжать добавлять
    public boolean setStatus(int ind){
        for (int i = 0; i<capacity; i++){

            if (elements[i] != null && elements[i].num == ind) {
                elements[i].status = true;
                return (writePos == i || elements[writePos] == null);
            }
        }
        return false;
    }

    public DatagramPacket[] takeUnmarked(){
        LinkedList<Object> res = new LinkedList<>();
        for (int i = 0; i<capacity; i++){
            if (elements[i] != null && !elements[i].status) {
                res.add(elements[i].data);
            }
        }
        return res.toArray(new DatagramPacket[res.size()]);
    }
}
