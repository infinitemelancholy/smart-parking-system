package parking.util;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public final class DateTimeUtil{
    public static final DateTimeFormatter DISPLAY_FORMAT=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private DateTimeUtil(){
    }
    public static String format(LocalDateTime time){
        return time.format(DISPLAY_FORMAT);
    }
    public static String toStorage(LocalDateTime time){
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    public static LocalDateTime fromStorage(String text){
        return LocalDateTime.parse(text,DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
