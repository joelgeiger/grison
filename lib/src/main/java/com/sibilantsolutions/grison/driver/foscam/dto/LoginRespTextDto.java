package com.sibilantsolutions.grison.driver.foscam.dto;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;

import com.google.auto.value.AutoValue;
import com.sibilantsolutions.grison.driver.foscam.domain.ResultCodeE;
import com.sibilantsolutions.grison.driver.foscam.type.FosInt16;

@AutoValue
public abstract class LoginRespTextDto implements FoscamTextDto {

    public abstract FosInt16 resultCode();

    public abstract Optional<LoginRespDetailsDto> loginRespDetails();

    public static Builder builder() {
        return new AutoValue_LoginRespTextDto.Builder();
    }

    @Override
    public final FoscamOpCode opCode() {
        return FoscamOpCode.Login_Resp;
    }

    @Override
    public final int encodedLength() {
        return 2 + loginRespDetails().map(loginRespDetailsDto -> LoginRespDetailsDto.ENCODED_LENGTH).orElse(0);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder resultCode(FosInt16 resultCode);

        public abstract Builder loginRespDetails(LoginRespDetailsDto loginRespDetails);

        abstract LoginRespTextDto autoBuild();  //Not public

        public LoginRespTextDto build() {
            LoginRespTextDto dto = autoBuild();
            if (ResultCodeE.fromValue(dto.resultCode()) == ResultCodeE.CORRECT) {
                checkArgument(dto.loginRespDetails().isPresent(), "missing loginRespDetails");
            } else {
                checkArgument(!dto.loginRespDetails().isPresent(), "expected loginRespDetails to be absent");
            }

            return dto;
        }
    }

}
