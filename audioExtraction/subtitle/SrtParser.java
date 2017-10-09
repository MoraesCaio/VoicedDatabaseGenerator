package audioExtraction.subtitle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caiomoraes on 07/10/17.
 */
public class SrtParser
{
    private List<SubtitleLine> subtitleLines;
    private List<SubtitleLine> noVoiceTimeMarks;

    /*Constructor*/
    public SrtParser()
    {
        this.subtitleLines = new ArrayList<SubtitleLine>();
        this.noVoiceTimeMarks = new ArrayList<SubtitleLine>();
    }

    public void parseSrtFile(String srtFile) throws IOException
    {
        //Resetting for new files
        this.subtitleLines = new ArrayList<SubtitleLine>();
        this.noVoiceTimeMarks = new ArrayList<SubtitleLine>();

        //Reading content
        byte[] encoded = Files.readAllBytes(Paths.get(srtFile));
        String content = new String(encoded, StandardCharsets.UTF_8);

        //regex
        String nl = "\\r?\\n";
        String sp = "[ \\t]*";
        String regTimeMark = "(?s)(\\d+)" + sp + nl + "(\\d{1,2}:\\d\\d:\\d\\d,\\d\\d\\d)" + sp + "-->"+ sp + "(\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d)" + sp + "(?:\\d.*?)??" + nl + "(.*?)" + nl + nl;
        Pattern pTimeMark = Pattern.compile(regTimeMark);

        //Parsing subtitleLines (periods most likely with voice)
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

        /*Printing results*/
        for (SubtitleLine subtitleLine : subtitleLines)
        {
            System.out.println(subtitleLine);
        }

        //Parsing periods without voice
        String startTime;
        String endTime;
        for (int i = 0; i < subtitleLines.size()-1; i++)
        {
            int idx = i + 1;
            startTime = subtitleLines.get(i).startTime;
            endTime = subtitleLines.get(i+1).endTime;

            noVoiceTimeMarks.add(new SubtitleLine(
                    idx,
                    startTime,
                    endTime,
                    ""
            ));
        }
    }

    /*Getters*/
    public List<SubtitleLine> getSubtitleLines()
    {
        return subtitleLines;
    }

    public List<SubtitleLine> getNoVoiceTimeMarks()
    {
        return noVoiceTimeMarks;
    }
}
