import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class FileHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private static final Logger log = LogManager.getLogger(FileHandler.class);
    private Path path = Paths.get("server", "ServerStorage");
    private final String PATH = path.toString();
    private SQLService SQLService = new SQLService();
    private String nick;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        if (msg instanceof CommandMessage) {
            CommandMessage cm = (CommandMessage) msg;
            switch (cm.getCommand()) {
                case AUTH:
                    nick = SQLService.getNickByLoginAndPass(cm.getParam(), cm.getSecondParam());
                    if (nick != null) {
                        ctx.writeAndFlush(new CommandMessage(Command.AUTH, nick));
                        log.info(String.format("Client %s logged in.", nick));
                        return;
                    } else log.info("Wrong login or password.");
                    break;
                case SIGN_UP:
                    NewUser newUser = new NewUser(cm.getParam(), cm.getSecondParam(), cm.getThirdParam());
                    SQLService.signUpUser(newUser);
                    log.info("New user signed up.");
                    break;
                case OPEN_ACCESS:
                    SQLService.openAccess(nick, cm.getParam(), "allow");
                    log.info(String.format("%s opened access to the user: %s.", nick, cm.getParam()));
                    break;
                case WHOLE_FILES_LIST:
                    ListMessage lm = new ListMessage();
                    List<String> fromNick = SQLService.getNickGivingAccess(nick, "allow");
                    log.info(String.format("Access from nick: %s", fromNick));
                    if (Files.exists(Paths.get(PATH, nick))) {
                        lm.createList(Paths.get(PATH), nick, fromNick);
                        ctx.writeAndFlush(lm);
                        log.info(String.format("File list has been sent: %s.", lm.getFilesList().toString()));
                    } else {
                        Files.createDirectory(Paths.get(PATH, nick));
                        log.info("Directory is created.");
                    }
                    break;
                case DIRECTORY_FILES_LIST:
                    ListMessage l = new ListMessage();
                    if (Files.exists(Paths.get(PATH, nick))) {
                        l.createList(Paths.get(PATH, nick));
                        ctx.writeAndFlush(l);
                        log.info(String.format("File list has been sent: %s.", l.getFilesList().toString()));
                    }
                    break;
                case FILE_REQUEST:
                    String[] str = cm.getParam().split("/", 4);
                    if (Files.exists(Paths.get(PATH, str[2], cm.getSecondParam()))) {
                        FileMessage fm = new FileMessage(Paths.get(PATH, str[2], cm.getSecondParam()));
                        ctx.writeAndFlush(fm);
                    } else log.info(String.format("File %s does not exist.", cm.getSecondParam()));
                    break;
                case FILE_DELETE:
                    Files.deleteIfExists(Paths.get(PATH, nick, cm.getParam()));
                    ctx.writeAndFlush(new CommandMessage(Command.DIRECTORY_FILES_LIST));
                    log.info(String.format("File %s has been deleted.", cm.getParam()));
                    break;
                case FILE_RENAME:
                    changeFileName(Paths.get(PATH, nick, cm.getParam()), cm.getSecondParam());
                    ctx.writeAndFlush(new CommandMessage(Command.DIRECTORY_FILES_LIST));
                    log.info(String.format("File %s has been renamed to %s.", cm.getParam(), cm.getSecondParam()));
                    break;
                case FILE_DIR:
                    ListMessage m = new ListMessage();
                    m.createList(Paths.get(PATH, cm.getParam()));
                    ctx.channel().writeAndFlush(m);
                    log.info(String.format("Server side file path: %s.", m.getFilesList().toString()));
                    break;
            }
        }
        if (msg instanceof FileMessage) {
            FileMessage fm = (FileMessage) msg;
            Files.write(Paths.get(PATH, nick, fm.getName()), fm.getData(), StandardOpenOption.CREATE);
            ctx.writeAndFlush(new CommandMessage(Command.DIRECTORY_FILES_LIST));
            log.info(String.format("File - %s (%s bytes) uploaded to server.", fm.getName(), fm.getData().length));
        }
    }

    private void changeFileName(Path path, String newFileName) throws IOException {
        Path newName = path.resolveSibling(newFileName);
        Files.move(path, newName, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(String.format("Client %s disconnected.", nick));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}