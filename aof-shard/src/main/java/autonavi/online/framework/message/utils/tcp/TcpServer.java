package autonavi.online.framework.message.utils.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class TcpServer {
	/** 用于分配处理业务线程的线程组个数 */
	protected static final int BIZGROUPSIZE = Runtime.getRuntime()
			.availableProcessors() * 2; // 默认
	/** 业务出现线程大小 */
	protected static final int BIZTHREADSIZE = 4;
	private static final EventLoopGroup bossGroup = new NioEventLoopGroup(
			BIZGROUPSIZE);
	private static final EventLoopGroup workerGroup = new NioEventLoopGroup(
			BIZTHREADSIZE);

	public ChannelFuture run(String ip, int port,
			final ChannelHandler... handlers) throws Exception {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup);
		b.channel(NioServerSocketChannel.class);
		b.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("frameDecoder",
						new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,
								4, 0, 4));
				pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
//				pipeline.addLast("decoder",
//						new StringDecoder(CharsetUtil.UTF_8));
//				pipeline.addLast("encoder",
//						new StringEncoder(CharsetUtil.UTF_8));
				pipeline.addLast(new ObjectEncoder());
				pipeline.addLast(new ObjectDecoder(ClassResolvers
						.weakCachingConcurrentResolver(null)));
				pipeline.addLast(handlers);
			}
		});

		ChannelFuture f = b.bind(ip, port).sync();
		System.out.println("TCP服务器已启动");
		// 定义一个"钩子"，在JVM停止的时候先停止对应的"钩子"
		Runtime.getRuntime().addShutdownHook(new ShutdownHooks());
		return f;
	}

	private static class ShutdownHooks extends java.lang.Thread {

		@Override
		public void run() {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
