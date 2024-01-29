
import java.util.logging.Logger;




import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggingUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = Logger.getLogger(JavaIOUtils.class.getName());

    public static void log(String message) {
        Date date = new Date();
        logger.info("[" + DATE_FORMAT.format(date) + "] " + message);
    }
}
