package CommonUtils;

/**
 * Created by pelgray on 07.06.2017.
 */
class Struct{
    final int num;
    boolean status; // true - доставлено, либо записано
    final Object data;

    public Struct(int num, Object data) {
        this.num = num;
        this.status = false;
        this.data = data;
    }
}