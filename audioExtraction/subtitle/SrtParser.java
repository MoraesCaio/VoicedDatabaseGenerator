package audioExtraction.subtitle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caiomoraes on 07/10/17.
 */
public class SrtParser
{
    private Map<Integer, SubtitleLine> subtitleLines;
    private Map<Integer, SubtitleLine> noVoiceTimeMarks;

    /*Constructor*/
    public SrtParser()
    {
        this.subtitleLines = new HashMap<Integer, SubtitleLine>();
        this.noVoiceTimeMarks = new HashMap<Integer, SubtitleLine>();
    }

    public void parseSrtFile(String srtFile) throws IOException
    {
        System.out.println("Parsing " + srtFile);
        //Resetting for new files
        this.subtitleLines = new HashMap<Integer, SubtitleLine>();
        this.noVoiceTimeMarks = new HashMap<Integer, SubtitleLine>();

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

        //Initiating fetching
        mTimeMark.find();
        SubtitleLine penultimateSubtitleLine = new SubtitleLine(
                Integer.parseInt(mTimeMark.group(1)),
                mTimeMark.group(2),
                mTimeMark.group(3),
                mTimeMark.group(4));

        int idx = 1;
        subtitleLines.put(idx, penultimateSubtitleLine);
        SubtitleLine lastSubtitleLine;

        //No voice time marks
        while (mTimeMark.find())
        {
            idx++;
            lastSubtitleLine = new SubtitleLine(
                    Integer.parseInt(mTimeMark.group(1)),
                    mTimeMark.group(2),
                    mTimeMark.group(3),
                    mTimeMark.group(4)
            );
            subtitleLines.put(idx, lastSubtitleLine);

            noVoiceTimeMarks.put(idx-1, new SubtitleLine(
                    idx,
                    penultimateSubtitleLine.endTime,
                    lastSubtitleLine.startTime,
                    ""
            ));

            //SWAP
            penultimateSubtitleLine = lastSubtitleLine;
        }

        /*Printing results*/
        /*for (SubtitleLine subtitleLine : subtitleLines.values())
        {
            System.out.println(subtitleLine);
        }*/
    }

    /*Getters*/
    public Map<Integer, SubtitleLine> getSubtitleLines()
    {
        return subtitleLines;
    }

    public Map<Integer, SubtitleLine> getNoVoiceTimeMarks()
    {
        return noVoiceTimeMarks;
    }
}
