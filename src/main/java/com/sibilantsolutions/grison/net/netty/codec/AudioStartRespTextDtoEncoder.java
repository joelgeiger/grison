package com.sibilantsolutions.grison.net.netty.codec;

import java.util.List;

import com.sibilantsolutions.grison.driver.foscam.dto.AudioStartRespTextDto;
import com.sibilantsolutions.grison.net.netty.codec.dto.FoscamTextByteBufDTO;
import com.sibilantsolutions.grison.net.netty.codec.parse.NettyFosTypeWriter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

@ChannelHandler.Sharable
public class AudioStartRespTextDtoEncoder extends MessageToMessageEncoder<AudioStartRespTextDto> {
    @Override
    protected void encode(ChannelHandlerContext ctx, AudioStartRespTextDto msg, List<Object> out) {

        final ByteBuf textBuf = ctx.alloc().buffer(msg.encodedLength(), msg.encodedLength());

        NettyFosTypeWriter.write(msg.result(), textBuf);
        msg.dataConnectionId().ifPresent(dataConnectionId -> NettyFosTypeWriter.write(dataConnectionId, textBuf));

        out.add(FoscamTextByteBufDTO.create(msg.opCode(), textBuf));

    }

}
