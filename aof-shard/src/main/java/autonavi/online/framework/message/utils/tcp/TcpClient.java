package autonavi.online.framework.message.utils.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class TcpClient {

	private Timer timer;
	private Channel channel;
	EventLoopGroup group = new NioEventLoopGroup();
	public static boolean isConnect = false;
	private CountDownLatch connectedLatch = new CountDownLatch(1);

	/**
	 * 
	 * @param host
	 *            IP
	 * @param port
	 *            端口
	 * @param restartInterval
	 *            重启间隔，比如连接中断了，会按这个间隔进行重试。如果该值等于小于0，表示不进行重试。单位为秒
	 * @param handler
	 *            用户自定义handler
	 */
	public void run(final String host, final int port, int restartInterval,
			final ChannelHandler... handlers) {
		if (restartInterval > 0) {
			this.timer = new Timer();
			this.timer.schedule(new TimerTask() {

				@Override
				public void run() {
					if (!isConnect) {
						TcpClient tcpClient = new TcpClient();
						Bootstrap bootstrap = tcpClient.getBootstrap(handlers);
						channel = tcpClient.getChannel(bootstrap, host, port);
						connectedLatch.countDown();
					}
				}
			}, 0, restartInterval * 1000);
		} else {
			Bootstrap bootstrap = this.getBootstrap(handlers);
			this.channel = this.getChannel(bootstrap, host, port);
			connectedLatch.countDown();
		}
	}

	public void close() throws InterruptedException {
		try {
			channel.closeFuture();
		} finally {
			// Shut down the event loop to terminate all threads.
			group.shutdownGracefully();
		}
	}

	/**
	 * 初始化Bootstrap
	 * 
	 * @return
	 */
	private final Bootstrap getBootstrap(final ChannelHandler... handlers) {
		final Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
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
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		return bootstrap;
	}

	/**
	 * 获取Channel
	 * 
	 * @param bootstrap
	 * @param host
	 * @param port
	 * @return
	 */
	private final Channel getChannel(Bootstrap bootstrap, String host, int port) {
		Channel channel = null;
		try {
			ChannelFuture cf = bootstrap.connect(host, port).sync();
			// cf.addListener(new ChannelFutureListener() {
			//
			// @Override
			// public void operationComplete(ChannelFuture future) throws
			// Exception {
			// // TODO Auto-generated method stub
			//
			// }
			// });
			channel = cf.channel();
			isConnect = true;
			System.out.println(String.format("连接Server(IP[%s],PORT[%s])成功",
					host, port));

		} catch (Exception e) {
			System.out.println(String.format("连接Server(IP[%s],PORT[%s])失败",
					host, port));
			isConnect = false;
			group.shutdownGracefully();
		}
		return channel;
	}

	/**
	 * 发送消息给服务端
	 * 
	 * @param msg
	 * @throws Exception
	 */
	public void sendMsg(Object msg) throws Exception {
		connectedLatch.await();
		if (this.channel != null) {
			this.channel.writeAndFlush(msg).sync();
		} else {
			System.out.println("消息发送失败,连接尚未建立!");
			isConnect = false;
			group.shutdownGracefully();
		}
	}
}
