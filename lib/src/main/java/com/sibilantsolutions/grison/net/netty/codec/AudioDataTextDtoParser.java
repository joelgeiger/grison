package com.sibilantsolutions.grison.net.netty.codec;

import static com.sibilantsolutions.grison.net.netty.codec.parse.NettyByteBufHelper.readBytes;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import java.util.function.Function;

import com.sibilantsolutions.grison.driver.foscam.dto.AudioDataTextDto;
import com.sibilantsolutions.grison.driver.foscam.type.FosInt32;
import com.sibilantsolutions.grison.driver.foscam.type.FosInt8;
import com.sibilantsolutions.grison.net.netty.codec.parse.NettyFosTypeReader;
import io.netty.buffer.ByteBuf;

public class AudioDataTextDtoParser implements Function<ByteBuf, AudioDataTextDto> {
    @Override
    public AudioDataTextDto apply(ByteBuf buf) {
        final FosInt32 timestampHundredths = NettyFosTypeReader.fosInt32(buf);
        final FosInt32 snOfPacket = NettyFosTypeReader.fosInt32(buf);
        final FosInt32 gatherTimeSecs = NettyFosTypeReader.fosInt32(buf);
        final FosInt8 audioFormat = NettyFosTypeReader.fosInt8(buf);
        final FosInt32 dataLength = NettyFosTypeReader.fosInt32(buf);
        final byte[] data = readBytes(checkPositiveOrZero(dataLength.value().intValue(), "dataLength"), buf);

        return AudioDataTextDto.builder()
                .timestampHundredths(timestampHundredths)
                .snOfPacket(snOfPacket)
                .gatherTimeSecs(gatherTimeSecs)
                .audioFormat(audioFormat)
                .dataLength(dataLength)
                .data(data)
                .build();
    }
}
