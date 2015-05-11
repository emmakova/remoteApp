package emit.esy.es.spyphone.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil Makovac on 11/05/2015.
 */
public class SmsConversation implements Serializable{

    String address;

    public List<Sms> getConList() {
        return conList;
    }

    public void setConList(List<Sms> conList) {
        this.conList = conList;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    List<Sms> conList;

    public SmsConversation(String address) {
        this.address = address;
        this.conList = new ArrayList<>();
    }

    public void add(Sms sms) {
        conList.add(sms);
    }
}
