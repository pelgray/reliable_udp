package CommonUtils;

public enum MessageErrorType {
    SHORTCUT("shortcut"), DETAILED("detailed"), STANDARD("standard");

    /**
     * Created by pelgray on 07.04.2017.
     */
    private final String title;

    MessageErrorType(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
