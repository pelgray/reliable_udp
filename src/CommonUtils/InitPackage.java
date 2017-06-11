package CommonUtils;

import java.io.Serializable;

/**
 * Created by pelgray on 06.06.2017.
 */
public class InitPackage implements Serializable {
    private static final long serialVersionUID = 1;
    private final String filename_; // имя файла
    private final long countPack_; // количество пакетов
    private final long sizeLastPack_; // размер последнего пакета без индекса

    public InitPackage(String filename_, long countPack_, long sizeLastPack_) {
        this.filename_ = filename_;
        this.countPack_ = countPack_;
        this.sizeLastPack_ = sizeLastPack_;
    }

    public String getFilename() {
        return filename_;
    }

    public long getCountPack() {
        return countPack_;
    }

    public long getSizeLastPack() {
        return sizeLastPack_;
    }
}
