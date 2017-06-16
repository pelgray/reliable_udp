package CommonUtils;

import java.net.DatagramPacket;
import java.util.LinkedList;

/**
 * Created by pelgray on 07.06.2017.
 */
class RingBuffer {
    private Struct[] elements = null;

    private int capacity  = 0; // максимальное количество объектов
    private int writePos  = 0; // позиция, на которой остановились
    private int available = 0; // текущее кол-во объектов
    private boolean existSmth = false; // есть ли что-нибудь в буфере

    public RingBuffer(int capacity) {
        this.capacity = capacity;
        this.elements = new Struct[capacity];
    }

    public boolean checkPutSending() {
        return available < capacity && ((elements[writePos] == null) || (elements[writePos].status));
    }

    public boolean checkPutReceiving() {
        return available < capacity;
    }

    public boolean checkPut(int place) {
        return ((elements[place] == null) || (elements[place].status));
    }

    public boolean checkTake(int ind){
        if (existSmth) {
            int place = ind%capacity;
            if (elements[place] != null && elements[place].num == ind) return true;
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

    public void put(Struct element, int place){
        elements[place] = element;
        if (available != capacity) available++;
        if (!existSmth) existSmth = true;
    }

    public Object take(int ind) {
        if(!existSmth){
            return null;
        }
        int in = ind%capacity;
        if (elements[in] != null && elements[in].num == ind) {
            elements[in].status = true;
            available--;
            if (available == 0) existSmth = false;
            return elements[in].data;
        }
        return null;
    }
    
    // возвращает сигнал, можно ли продолжать добавлять
    public boolean setStatus(int ind){
        for (int i = 0; i<capacity; i++){
            if (elements[i] != null && elements[i].num == ind) {
                if (!elements[i].status) {
                    elements[i].status = true;
                    available--;
                    if (available == 0) existSmth = false;
                }
            }
        }
        return available < capacity && ((elements[writePos] == null) || (elements[writePos].status));
    }

    public DatagramPacket[] takeUnmarked(){
        LinkedList<Object> res = new LinkedList<>();
        if (existSmth){
            for (int i = 0; i<capacity; i++){
                if (elements[i] != null && !elements[i].status) {
                    res.add(elements[i].data);
                }
            }
        }
        return res.toArray(new DatagramPacket[res.size()]);
    }
}
