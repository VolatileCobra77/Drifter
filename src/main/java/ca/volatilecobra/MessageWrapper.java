package ca.volatilecobra;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MessageWrapper {
    @SerializedName("type")
    private Class<?> type;

    @SerializedName("contents")
    private Object contents;

    public MessageWrapper(Class<?> type, Object contents) {
        this.type = type;
        this.contents = contents;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Object getContents() {
        return contents;
    }

    public void setContents(Object contents) {
        this.contents = contents;
    }
}
class Id{
    public int id;
    public Id(int id) {
        this.id = id;
    }
}
class SuccessMessage{
    public boolean success;
    public SuccessMessage(boolean success) {
        this.success = success;
    }
}
class miscReq{
    public String request;
    public miscReq(String request) {
        this.request = request;
    }
}
class playersList{
    public List<ClientPlayer> ClientPlayers;
    public playersList(List<ClientPlayer> users) {
        ClientPlayers = users;
    }
}