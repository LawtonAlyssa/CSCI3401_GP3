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
    
}
