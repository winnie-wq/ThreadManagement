package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {

    public static void log(String msg){

        SimpleDateFormat sdf=
                new SimpleDateFormat("HH:mm:ss");

        System.out.println(
                "["+sdf.format(new Date())+"] "+msg
        );

    }

}