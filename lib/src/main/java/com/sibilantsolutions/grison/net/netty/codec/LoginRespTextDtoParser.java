package com.sibilantsolutions.grison.net.netty.codec;

import static com.sibilantsolutions.grison.driver.foscam.dto.LoginRespDetailsDto.FIRMWARE_VERSION_LEN;
import static com.sibilantsolutions.grison.driver.foscam.dto.LoginRespDetailsDto.RESERVE1;
import static com.sibilantsolutions.grison.driver.foscam.dto.LoginRespDetailsDto.RESERVE2;
import static com.sibilantsolutions.grison.net.netty.codec.parse.NettyByteBufHelper.readBytes;

import java.util.function.Function;

import com.sibilantsolutions.grison.driver.foscam.domain.ResultCodeE;
import com.sibilantsolutions.grison.driver.foscam.dto.LoginRespDetailsDto;
import com.sibilantsolutions.grison.driver.foscam.dto.LoginRespTextDto;
import com.sibilantsolutions.grison.driver.foscam.type.FosInt16;
import com.sibilantsolutions.grison.net.netty.codec.parse.NettyFosTypeReader;
import io.netty.buffer.ByteBuf;

public class LoginRespTextDtoParser implements Function<ByteBuf, LoginRespTextDto> {
    @Override
    public LoginRespTextDto apply(ByteBuf buf) {
        final FosInt16 result = NettyFosTypeReader.fosInt16(buf);

        final LoginRespTextDto.Builder builder = LoginRespTextDto.builder()
                .resultCode(result);

        if (ResultCodeE.fromValue(result) == ResultCodeE.CORRECT) {
            final byte[] cameraId = readBytes(LoginRespDetailsDto.CAMERA_ID_LEN, buf);
            final byte[] reserve1 = readBytes(RESERVE1.length, buf);
            final byte[] reserve2 = readBytes(RESERVE2.length, buf);
            final byte[] firmwareVersion = readBytes(FIRMWARE_VERSION_LEN, buf);

            return builder
                    .loginRespDetails(LoginRespDetailsDto.builder()
                            .cameraId(cameraId)
                            .reserve1(reserve1)
                            .reserve2(reserve2)
                            .firmwareVersion(firmwareVersion)
                            .build())
                    .build();
        } else {
            return builder.build();
        }

    }
}
