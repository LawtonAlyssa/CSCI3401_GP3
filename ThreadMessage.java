/**
 * ThreadMessage
 */
class ThreadMessage extends Message{
    private int clientSender;
    private int clientReceiver;

    public ThreadMessage(String label, String content, int clientSender, int clientReceiver) {
        super(label, content);
        this.clientSender = clientSender;
        this.clientReceiver = clientReceiver;
    }

    public ThreadMessage(int clientSender, int clientReceiver) {
        super();
        this.clientSender = clientSender;
        this.clientReceiver = clientReceiver;
    }

    public int getClientSender() {
        return clientSender;
    }

    public void setClientSender(int clientSender) {
        this.clientSender = clientSender;
    }

    public int getClientReceiver() {
        return clientReceiver;
    }

    public void setClientReceiver(int clientReceiver) {
        this.clientReceiver = clientReceiver;
    }
}
