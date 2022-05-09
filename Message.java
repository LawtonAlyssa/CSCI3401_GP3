import java.util.*;
/**
 * 
 * @author Alyssa
 * @author Truong
 */
public class Message {
    private String label = null;
    private String content = "";

    /**
     * Constructor to initialize values for label and content fields
     * @param label
     * @param content
     */
    public Message(String label, String content) {
        this.label = label;
        this.content = content;
    }

    /**
     * Empty arg constructor
     */
    public Message() {

    }

    /**
     * Get label
     * @return label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set label
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get content
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * Set content
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Parsing input from the user
     * @param input: input to be handled
     * @return message
     */
    public static Message parse(String input) {
        try{
            return new Message(input.split("-", 2)[0], input.split("-", 2)[1]);
        } catch (Exception e) {
            System.out.println("Unable to parse Message: " + input);
            return null;
        }
    }

    /**
     * Build a string containing sender, multiple recipients and messages
     * @param sender
     * @param recipients
     * @param msg
     * @return newly built string
     */
    public static String build(String sender, ArrayList<String> recipients, String msg) {
        StringBuilder out = new StringBuilder();
        out.append(sender).append("-");
        out.append(String.join(";", recipients)).append("-");
        out.append(msg);
        return out.toString();
    }

    /**
     * Build a string containing sender, one recipient and messages
     * @param sender
     * @param recipient
     * @param msg
     * @return newly built string
     */
    public static String build(String sender, String recipient, String msg) {
        StringBuilder out = new StringBuilder();
        out.append(sender).append("-");
        out.append(recipient).append("-");
        out.append(msg);
        return out.toString();
    }
    
}
