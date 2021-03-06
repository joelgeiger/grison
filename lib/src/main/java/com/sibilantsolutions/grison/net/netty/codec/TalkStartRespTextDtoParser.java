package com.sibilantsolutions.grison.net.netty.codec;

import java.util.function.Function;

import com.sibilantsolutions.grison.driver.foscam.domain.ResultCodeE;
import com.sibilantsolutions.grison.driver.foscam.dto.TalkStartRespTextDto;
import com.sibilantsolutions.grison.driver.foscam.type.FosInt16;
import com.sibilantsolutions.grison.net.netty.codec.parse.NettyFosTypeReader;
import io.netty.buffer.ByteBuf;

public class TalkStartRespTextDtoParser implements Function<ByteBuf, TalkStartRespTextDto> {
    @Override
    public TalkStartRespTextDto apply(ByteBuf buf) {
        final FosInt16 result = NettyFosTypeReader.fosInt16(buf);
        final TalkStartRespTextDto.Builder builder = TalkStartRespTextDto.builder()
                .result(result);
        if (ResultCodeE.fromValue(result) == ResultCodeE.CORRECT) {
            builder.dataConnectionId(NettyFosTypeReader.fosInt32(buf));
        }
        return builder.build();
    }
}
