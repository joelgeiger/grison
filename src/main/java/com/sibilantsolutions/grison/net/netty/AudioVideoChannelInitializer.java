package com.sibilantsolutions.grison.net.netty;

import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sibilantsolutions.grison.driver.foscam.domain.AudioVideoProtocolOpCodeE;
import com.sibilantsolutions.grison.driver.foscam.domain.Command;
import com.sibilantsolutions.grison.driver.foscam.domain.ProtocolE;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class AudioVideoChannelInitializer extends ChannelInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AudioVideoChannelInitializer.class);

    private static final String EXCEPTION_TRAPPER = "exceptionTrapper";

    private static final int MAX_FRAME_LENGTH = 65_536;

    private static final int READ_TIMEOUT_SECS = 65;
    private static final int KEEPALIVE_SEND_TIMEOUT_SECS = 73;
    private static final int WRITE_TIMEOUT_SECS = KEEPALIVE_SEND_TIMEOUT_SECS + 5;

    private final Subscriber<Command> audioVideoDatastream;
    private final NioEventLoopGroup group;

    public AudioVideoChannelInitializer(Subscriber<Command> audioVideoDatastream, NioEventLoopGroup group) {
        this.audioVideoDatastream = audioVideoDatastream;
        this.group = group;
    }

    @Override
    protected void initChannel(Channel ch) {
        LOG.info("{} initChannel.", ch);

        ch.pipeline()
                //Log lifecycle events ("com.sibilantsolutions.grison.net.netty.ChannelLifecycleLoggingHandler"; INFO level).
                .addLast(new ChannelLifecycleLoggingHandler())
                //Log lifecycle events AND datastream ("io.netty.handler.logging.LoggingHandler"; DEBUG level).
                //This will get replaced by a TRACE level logger after the intial login.
                .addLast(new LoggingHandler())
                .addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, MAX_FRAME_LENGTH, 0x0F, 4, 4, 0, true))
                .addLast(new FoscamCommandCodec())
                .addLast(new IdleStateHandler(READ_TIMEOUT_SECS, WRITE_TIMEOUT_SECS, 0))
                .addLast(new IdleStateEventHandler())
                //Receive and drop inbound pings.  We don't respond to these.  We send outbound
                //pings on a set schedule, not dependent on inbound pings.
                .addLast(new KeepAliveInboundDropper(ProtocolE.AUDIO_VIDEO_PROTOCOL, AudioVideoProtocolOpCodeE.Keep_Alive))
                //Emit KeepAliveTimerEvents at regular intervals.
                .addLast(new KeepAliveTimerEventScheduler(KEEPALIVE_SEND_TIMEOUT_SECS))
                .addLast(new KeepAliveSender(ProtocolE.AUDIO_VIDEO_PROTOCOL, AudioVideoProtocolOpCodeE.Keep_Alive))
                .addLast(new SimpleChannelInboundHandler<Command>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
                        audioVideoDatastream.onNext(msg);
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        audioVideoDatastream.onComplete();
                    }
                })
                .addLast(EXCEPTION_TRAPPER, new ChannelInboundHandlerAdapter() {
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        LOG.error("{} exception at end of pipeline; closing channel:", ctx.channel(),
                                new RuntimeException(cause));
                        ctx.close();
                    }
                })
        ;

        ch.closeFuture().addListener((ChannelFutureListener) future -> {
            LOG.info("{} Channel closed, shutting down group={}.", future.channel(), group);
            group.shutdownGracefully(0, 2, TimeUnit.SECONDS);
        });
    }
}