import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

/**
 * Created by caiomoraes on 07/10/17.
 */
public class AudioDBGen
{
    public static void main(String[] args)
    {
        SrtParser srtParser = new SrtParser();
        List<SubtitleLine> subtitleLines = new ArrayList<>();

        TimeMark tm1, tm2;
        try
        {
            tm1 = new TimeMark("00:01:20,700");
            tm2 = new TimeMark("00:01:21,900");
            System.out.println(TimeMark.getDuration(tm1, tm2));
        }
        catch (InvalidPropertiesFormatException e)
        {
            e.printStackTrace();
        }

        /*Parsing*/
        /*try
        {
            subtitleLines = srtParser.parseSrtFile("TresMinutos.srt", true);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


        *//*Printing results*//*
        for (SubtitleLine subtitleLine : subtitleLines)
        {
            System.out.println(subtitleLine);
        }*/
    }
}
