import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


public class Network {
    private static Network network = new Network();
    private SocketChannel channel;
    private ClientHandler clientHandler = new ClientHandler();
    private static final String HOST = "localhost";
    private static final int PORT = 8189;

    public static Network getInstance(){
        if(network == null)
            network = new Network();
        return network;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    private Network(){
        new Thread(() ->{
            EventLoopGroup worker = new NioEventLoopGroup();
            try{
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(worker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                channel = socketChannel;
                                socketChannel.pipeline()
                                        .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                                new ObjectEncoder(),
                                                clientHandler);
                            }
                        });
                ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                worker.shutdownGracefully();
            }
        }).start();
    }

    public boolean sendMessage(AbstractMessage msg){
        try {
            channel.writeAndFlush(msg);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void close() {
        channel.close();
    }
}
