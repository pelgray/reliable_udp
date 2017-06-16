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

    public int available(){
        return available;
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
//            for (int i = 0; i < capacity; i++) {
//                if (elements[i] != null && elements[i].num == ind) {
//                    return true;
//                }
//            }
            int place = ind%capacity;
            if (elements[place] != null && elements[place].num == ind) return true;
        }
        return false;
    }

    public void put(Struct element){
//            System.out.println("[BUFFER put BEFORE]: writePos " + writePos + ", available " + available + ", element " + element.num + "\texistSmth = " + existSmth);
//            for (int i = 0; i<capacity; i++){
//                if (elements[i] != null) {
//                    System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//                }
//                else{
//                    System.out.println("\t\t\t" + i + " is null");
//                }
//            }


        elements[writePos] = element;
        writePos++;
        if(writePos == capacity){
            writePos = 0;
        }
        if (available != capacity) available++;
        if (!existSmth) existSmth = true;


//            System.out.println("[BUFFER put AFTER]: writePos " + writePos + ", available " + available + ", element " + element.num + "\texistSmth = " + existSmth);
//            for (int i = 0; i<capacity; i++){
//                if (elements[i] != null) {
//                    System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//                }
//                else{
//                    System.out.println("\t\t\t" + i + " is null");
//                }
//            }
    }

    public void put(Struct element, int place){
//        System.out.println("[BUFFER put (place) BEFORE]: place " + place + ", available " + available + ", element " + element.num + "\texistSmth = " + existSmth);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }


        elements[place] = element;
        if (available != capacity) available++;
        if (!existSmth) existSmth = true;


//        System.out.println("[BUFFER put (place) AFTER]: place " + place + ", available " + available + ", element " + element.num + "\texistSmth = " + existSmth);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }
    }

    public Object take(int ind) {
//            System.out.println("[BUFFER take BEFORE]: writePos " + writePos + ", available " + available + ", element " + ind + "\texistSmth = " + existSmth);
//            for (int i = 0; i<capacity; i++){
//                if (elements[i] != null) {
//                    System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//                }
//                else{
//                    System.out.println("\t\t\t" + i + " is null");
//                }
//            }


        if(!existSmth){
            return null;
        }
//        int in = capacity;
//        for (int index = 0;index<capacity; index++){
//            if (elements[index] != null && elements[index].num == ind) {
//                elements[index].status = true;
//                available--;
//                if (available == 0) existSmth = false;
//                //return elements[index].data;
//                in = index;
//            }
//        }

        int in = ind%capacity;
        boolean flag = false;

            if (elements[in] != null && elements[in].num == ind) {
                elements[in].status = true;
                available--;
                if (available == 0) existSmth = false;
                flag = true;
            }


//            System.out.println("[BUFFER take AFTER]: writePos " + writePos + ", available " + available + ", element " + ind + "\texistSmth = " + existSmth);
//            for (int i = 0; i<capacity; i++){
//                if (elements[i] != null) {
//                    System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//                }
//                else{
//                    System.out.println("\t\t\t" + i + " is null");
//                }
//            }

//        if (in != capacity)
//        return elements[in].data;
//        else
//        return null; // это было

        if (flag)
        return elements[in].data;
        else
        return null;
    }
    
    // возвращает сигнал, можно ли продолжать добавлять
    public boolean setStatus(int ind){
//        System.out.println("[BUFFER setStatus BEFORE]: ind " + ind + ", available " + available + ",\texistSmth = " + existSmth);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }

        for (int i = 0; i<capacity; i++){
            if (elements[i] != null && elements[i].num == ind) {
                if (!elements[i].status) {
                    elements[i].status = true;
                    available--;
                    if (available == 0) existSmth = false;
                }
            }
        }

//        System.out.println("[BUFFER setStatus AFTER]: ind " + ind + ", available " + available + "\texistSmth = " + existSmth);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }
        return available < capacity && ((elements[writePos] == null) || (elements[writePos].status));
    }

    public DatagramPacket[] takeUnmarked(){

//        System.out.println("[BUFFER [takeUnmarked]  ]:  ARRAY" + "\texistSmth = " + existSmth);
//        for (int i = 0; i<capacity; i++){
//            if (elements[i] != null) {
//                System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
//            }
//            else{
//                System.out.println("\t\t\t" + i + " is null");
//            }
//        }

        //System.out.println("[BUFFER [takeUnmarked]  ]:  LINKED_LIST, \texistSmth = " + existSmth);

        LinkedList<Object> res = new LinkedList<>();
        if (existSmth){
            for (int i = 0; i<capacity; i++){
                if (elements[i] != null && !elements[i].status) {
                    res.add(elements[i].data);
                    //System.out.println("\t\t\t" + i + " #" + elements[i].num + " " + elements[i].status);
                }
            }
        }
        return res.toArray(new DatagramPacket[res.size()]);
    }
}
