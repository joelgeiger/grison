package com.sibilantsolutions.grison.driver.foscam;


public class TalkStartRespText extends AbstractStartRespText
{

    public static TalkStartRespText parse( String data )
    {
        TalkStartRespText text = new TalkStartRespText();

        text.parseImpl( data );

        return text;
    }

}
