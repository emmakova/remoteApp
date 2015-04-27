package emit.esy.es.spyphone.model;

import java.io.Serializable;

/**
 * Created by Emil Makovac on 27/04/2015.
 */
public class CallLogItem implements Serializable {

    String name, phoneNumber, callType, duration, date;

    public CallLogItem(String name, String phoneNumber, String callType, String duration, String date) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.callType = callType;
        this.duration = duration;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
