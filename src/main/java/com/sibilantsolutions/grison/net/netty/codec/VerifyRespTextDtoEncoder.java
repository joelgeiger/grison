package com.sibilantsolutions.grison.net.netty.codec;

import java.util.List;

import com.sibilantsolutions.grison.driver.foscam.dto.VerifyRespTextDto;
import com.sibilantsolutions.grison.net.netty.codec.dto.FoscamTextByteBufDTO;
import com.sibilantsolutions.grison.net.netty.codec.parse.NettyFosTypeWriter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

@ChannelHandler.Sharable
public class VerifyRespTextDtoEncoder extends MessageToMessageEncoder<VerifyRespTextDto> {
    @Override
    protected void encode(ChannelHandlerContext ctx, VerifyRespTextDto msg, List<Object> out) {

        final ByteBuf textBuf = ctx.alloc().buffer(msg.encodedLength(), msg.encodedLength());

        NettyFosTypeWriter.write(msg.resultCode(), textBuf);

        msg.reserve().ifPresent(reserve -> NettyFosTypeWriter.write(reserve, textBuf));

        out.add(FoscamTextByteBufDTO.create(msg.opCode(), textBuf));

    }

}
