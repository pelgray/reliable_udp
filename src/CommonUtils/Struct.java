package CommonUtils;

/**
 * Created by 1 on 07.06.2017.
 */
public class Struct{
    int num;
    boolean status; // true - доставлено
    Object data;

    public Struct(int num, Object data) {
        this.num = num;
        this.status = false;
        this.data = data;
    }
}