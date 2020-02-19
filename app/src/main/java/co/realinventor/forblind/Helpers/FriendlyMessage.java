package co.realinventor.forblind.Helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FriendlyMessage {

    private String id;
    private String text;
    private String fileUrl;
    private long time;
    private String sender;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String text, String sender, long time, String fileUrl) {
        this.text = text;
        this.fileUrl = fileUrl;
        this.time = time;
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public static long getCurrentTime(){
        return Calendar.getInstance().getTimeInMillis();
    }

    public String getTimeInString(){
        Date dt = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        return  sdf.format(dt);
    }

    public boolean isFromMe(){
        return sender.equals("me");
    }
}
