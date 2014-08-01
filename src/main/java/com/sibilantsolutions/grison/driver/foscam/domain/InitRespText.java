package com.sibilantsolutions.grison.driver.foscam.domain;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class InitRespText implements DatastreamI
{
    private ResultCodeE resultCode;     //INT16 (2 bytes; little endian)

    public ResultCodeE getResultCode()
    {
        return resultCode;
    }

    public void setResultCode( ResultCodeE resultCode )
    {
        this.resultCode = resultCode;
    }

    static public InitRespText parse( byte[] data, int offset, int length )
    {
        InitRespText text = new InitRespText();

        ByteBuffer bb = ByteBuffer.wrap( data, offset, length );
        bb.order( ByteOrder.LITTLE_ENDIAN );

        text.resultCode = ResultCodeE.fromValue( bb.getShort() );

        return text;
    }

    @Override
    public byte[] toDatastream()
    {
        ByteBuffer bb = ByteBuffer.allocate( 2 );
        bb.order( ByteOrder.LITTLE_ENDIAN );

        bb.putShort( resultCode.getValue() );

        return bb.array();
    }

}
