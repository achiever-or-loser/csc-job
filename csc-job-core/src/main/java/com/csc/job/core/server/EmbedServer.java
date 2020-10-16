package com.csc.job.core.server;

import com.csc.job.core.biz.ExecutorBiz;
import com.csc.job.core.biz.impl.ExecutorBizImpl;
import com.csc.job.core.biz.model.*;
import com.csc.job.core.thread.ExecutorRegistryThread;
import com.csc.job.core.util.CscJobRemotingUtil;
import com.csc.job.core.util.GsonTool;
import com.csc.job.core.util.ThrowableUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @PackageName: com.csc.job.core.server
 * @Author: 陈世超
 * @Create: 2020-10-14 15:43
 * @Version: 1.0
 */
public class EmbedServer {
    private static Logger logger = LoggerFactory.getLogger(EmbedServer.class);

    private ExecutorBiz executorBiz;
    private Thread thread;

    public void start(final String address, final int port, final String appName, final String accessToken) {
        executorBiz = new ExecutorBizImpl();
        thread = new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workGroup = new NioEventLoopGroup();
            ThreadPoolExecutor bizThreadPool = new ThreadPoolExecutor(0, 200,
                    60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(2000),
                    (r) -> {
                        return new Thread(r, "csc-rpc, EmbedServer bizThreadPool-" + r.hashCode());
                    },
                    (r, executor) -> {
                        throw new RuntimeException("csc-job, EmbedServer bizThreadPool is EXHAUSTED!");
                    });
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS))
                                        .addLast(new HttpServerCodec())
                                        .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                        .addLast(new EmbedHttpServerHandler(executorBiz, accessToken, bizThreadPool));
                            }
                        })
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture future = serverBootstrap.bind(port).sync();
                logger.info(">>>>>>>>>>> csc-job remoting server start success, nettype = {}, port = {}", EmbedServer.class, port);
                startRegistry(appName, address);
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    logger.info(">>>>>>>>>>> csc-job remoting server stop.");
                } else {
                    logger.error(">>>>>>>>>>> csc-job remoting server error.", e);
                }
            } finally {
                workGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void toStop() {
        if (thread != null && thread.isAlive()) thread.interrupt();
        stopRegistry();
        logger.info(">>>>>>>>>>> csc-job remoting server destroy success.");
    }


    public static class EmbedHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private static final Logger logger = LoggerFactory.getLogger(EmbedHttpServerHandler.class);
        private ExecutorBiz executorBiz;
        private String accessToken;
        private ThreadPoolExecutor bizThreadPool;

        public EmbedHttpServerHandler(ExecutorBiz executorBiz, String accessToken, ThreadPoolExecutor bizThreadPool) {
            this.executorBiz = executorBiz;
            this.accessToken = accessToken;
            this.bizThreadPool = bizThreadPool;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
            String requestData = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
            String url = fullHttpRequest.uri();
            HttpMethod httpMethod = fullHttpRequest.method();
            boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
            String accessTokenReq = fullHttpRequest.headers().get(CscJobRemotingUtil.CSC_JOB_ACCESS_TOKEN);
            bizThreadPool.execute(() -> {
                Object responseObj = process(httpMethod, url, requestData, accessTokenReq);
                String responseJson = GsonTool.toJson(responseObj);
                writeResponse(channelHandlerContext, keepAlive, responseJson);
            });
        }

        private Object process(HttpMethod httpMethod, String url, String requestData, String accessTokenReq) {
            if (HttpMethod.POST != httpMethod) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "invalid request, HttpMethod not support.");
            }
            if (url == null || url.trim().length() == 0) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping empty.");
            }
            if (accessToken != null && accessToken.trim().length() > 0 && !accessToken.equals(accessTokenReq)) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "The access token is wrong.");
            }
            try {
                if ("/beat".equals(url)) {
                    return executorBiz.beat();
                } else if ("/idleBeat".equals(url)) {
                    IdleBeatParam idleBeatParam = GsonTool.fromJson(requestData, IdleBeatParam.class);
                    return executorBiz.idleBeat(idleBeatParam);
                } else if ("/run".equals(url)) {
                    TriggerParam triggerParam = GsonTool.fromJson(requestData, TriggerParam.class);
                    return executorBiz.run(triggerParam);
                } else if ("/kill".equals(url)) {
                    KillParam killParam = GsonTool.fromJson(requestData, KillParam.class);
                    return executorBiz.kill(killParam);
                } else if ("/log".equals(url)) {
                    LogParam logParam = GsonTool.fromJson(requestData, LogParam.class);
                    return executorBiz.log(logParam);
                } else {
                    return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping(" + url + ") not found.");
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return new ReturnT<String>(ReturnT.FAIL_CODE, "request error:" + ThrowableUtil.toString(e));
            }
        }

        private void writeResponse(ChannelHandlerContext channelHandlerContext, boolean keepAlive, String responseJon) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(responseJon, CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            if (keepAlive) response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            channelHandlerContext.writeAndFlush(response);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error(">>>>>>>>>>> csc-job provider netty_http server caught exception", cause);
            ctx.close();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                ctx.channel().close();      // beat 3N, close if idle
                logger.debug(">>>>>>>>>>> csc-job provider netty_http server close an idle channel.");
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    public void startRegistry(final String appname, final String address) {
        // start registry
        ExecutorRegistryThread.getInstance().start(appname, address);
    }

    public void stopRegistry() {
        // stop registry
        ExecutorRegistryThread.getInstance().toStop();
    }
}
