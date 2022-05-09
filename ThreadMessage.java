/**
 * 
 * @author Alyssa
 * @author Truong
 */
class ThreadMessage extends Message{
    private int clientSender;
    private int clientReceiver;

    /**
     * Constructor with 4 args
     * @param label
     * @param content
     * @param clientSender
     * @param clientReceiver
     */
    public ThreadMessage(String label, String content, int clientSender, int clientReceiver) {
        super(label, content);
        this.clientSender = clientSender;
        this.clientReceiver = clientReceiver;
    }

    /**
     * Constructor with 2 args
     * @param clientSender
     * @param clientReceiver
     */
    public ThreadMessage(int clientSender, int clientReceiver) {
        super();
        this.clientSender = clientSender;
        this.clientReceiver = clientReceiver;
    }

    /**
     * Get client's sending order
     * @return 
     */
    public int getClientSender() {
        return clientSender;
    }

    /**
     * Set client's sending order
     * @param clientSender
     */
    public void setClientSender(int clientSender) {
        this.clientSender = clientSender;
    }

    /**
     * Get client's receiving order
     * @return
     */
    public int getClientReceiver() {
        return clientReceiver;
    }

    /**
     * Set client's receiving order
     * @param clientReceiver
     */
    public void setClientReceiver(int clientReceiver) {
        this.clientReceiver = clientReceiver;
    }
}
