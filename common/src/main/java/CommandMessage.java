public class CommandMessage extends AbstractMessage {
    private Command command;
    private String param;
    private String secondParam;
    private String thirdParam;

    public Command getCommand() {
        return command;
    }

    public String getParam() {
        return param;
    }

    public String getSecondParam() {
        return secondParam;
    }

    public String getThirdParam() {
        return thirdParam;
    }


    public CommandMessage(Command command, String param, String secondParam, String thirdParam) {
        this.command = command;
        this.param = param;
        this.secondParam = secondParam;
        this.thirdParam = thirdParam;
    }

    public CommandMessage(Command command, String param, String secondParam) {
        this.command = command;
        this.param = param;
        this.secondParam = secondParam;
    }

    public CommandMessage(Command command, String param) {
        this.command = command;
        this.param = param;
    }

    public CommandMessage(Command command) {
        this.command = command;
    }
}
