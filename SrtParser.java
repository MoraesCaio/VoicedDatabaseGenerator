import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caiomoraes on 07/10/17.
 */
public class SrtParser
{
    public List<SubtitleLine> parseSrtFile(String srtFile) throws IOException
    {
        return parseSrtFile(srtFile, true);
    }

    public List<SubtitleLine> parseSrtFile(String srtFile, boolean hasLineIdx) throws IOException
    {
        //IO respectively
        List<SubtitleLine> subtitleLines = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(srtFile));

        //regex
        String regTimeMark = "\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d";
        Pattern pTimeMark = Pattern.compile(regTimeMark);
        Matcher mTimeMark;

        //aux
        String text;
        String startTime;
        String endTime;

        //Parsing srt
        int size = lines.size();
        for (int i = 0; i < size; i++)
        {
            //Skipping line index
            if (hasLineIdx && ++i > size) break;


            /*Parsing time marks*/
            mTimeMark = pTimeMark.matcher(lines.get(i));

            //Start time
            if (mTimeMark.find())
                startTime = mTimeMark.group(0);
            else
                throw new InvalidPropertiesFormatException("Expected format for time marks: dd:dd:dd,ddd");

            //End time
            if (mTimeMark.find())
                endTime = mTimeMark.group(0);
            else
                throw new InvalidPropertiesFormatException("Expected format for time marks: dd:dd:dd,ddd");


            /*Subtitle text*/
            if (++i > size) break;
            text = lines.get(i);


            /*Adding subtitle line*/
            subtitleLines.add(new SubtitleLine(subtitleLines.size()+1, text, startTime, endTime));


            /*Skipping blank line*/
            if (++i > size) break;
        }

        return subtitleLines;
    }
}
