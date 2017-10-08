import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        byte[] encoded = Files.readAllBytes(Paths.get(srtFile));
        String content = new String(encoded, StandardCharsets.UTF_8);

        //regex
        String nl = "\\r?\\n";
        String sp = "[ \\t]*";
        String regTimeMark = "(?s)(\\d+)" + sp + nl + "(\\d{1,2}:\\d\\d:\\d\\d,\\d\\d\\d)" + sp + "-->"+ sp + "(\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d)" + sp + "(?:\\d.*?)??" + nl + "(.*?)" + nl + nl;
        Pattern pTimeMark = Pattern.compile(regTimeMark);

        //Parsing srt
        Matcher mTimeMark = pTimeMark.matcher(content);
        while (mTimeMark.find())
        {
            subtitleLines.add(new SubtitleLine(
                    Integer.parseInt(mTimeMark.group(1)),
                    mTimeMark.group(2),
                    mTimeMark.group(3),
                    mTimeMark.group(4)
            ));
        }

        return subtitleLines;
    }
}
