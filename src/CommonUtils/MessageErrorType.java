package CommonUtils;

/**
 * Created by 1 on 07.04.2017.
 */
public enum MessageErrorType {
    SHORTCUT("shortcut"), DETAILED("detailed"), STANDARD("standard");

    private String title;

    MessageErrorType(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
