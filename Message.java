import java.util.*;
/**
 * 
 * @author Alyssa
 * @author Truong
 */
public class Message {
    private String label = null;
    private String content = "";

    public Message(String label, String content) {
        this.label = label;
        this.content = content;
    }

    public Message() {

    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static Message parse(String input) {
        try{
            return new Message(input.split("-", 2)[0], input.split("-", 2)[1]);
        } catch (Exception e) {
            System.out.println("Unable to parse Message: " + input);
            return null;
        }
    }

    public static String build(String sender, ArrayList<String> recipients, String msg) {
        StringBuilder out = new StringBuilder();
        out.append(sender).append("-");
        out.append(String.join(";", recipients)).append("-");
        out.append(msg);
        return out.toString();
    }

    public static String build(String sender, String recipient, String msg) {
        StringBuilder out = new StringBuilder();
        out.append(sender).append("-");
        out.append(recipient).append("-");
        out.append(msg);
        return out.toString();
    }
    
}
