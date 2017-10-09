package audioExtraction.subtitle;

import java.util.InvalidPropertiesFormatException;

/**
 * Created by caiomoraes on 07/10/17.
 */
public class SubtitleLine
{
    public int idx;
    public TimeMark startTime;
    public TimeMark endTime;
    public String text;

    /*Constructors*/
    public SubtitleLine() throws InvalidPropertiesFormatException
    {
        this(0, "", "", "");
    }

    public SubtitleLine(int idx, String startTime, String endTime, String text) throws InvalidPropertiesFormatException
    {
        this(idx, new TimeMark(startTime), new TimeMark(endTime), text);
    }

    public SubtitleLine(int idx, TimeMark startTime, TimeMark endTime, String text)
    {
        this.idx = idx;
        this.startTime = startTime;
        this.endTime = endTime;
        this.text = text;
    }

    /*Getters*/
    public int getIdx()
    {
        return idx;
    }

    public String getStartTime()
    {
        return startTime.toString();
    }

    @Override
    public String toString()
    {
        return "Index: \t\t\t" + idx +
                "\nStart time: \t" + startTime +
                "\nEnd Time: \t\t" + endTime +
                "\nText: \t\t\t" + text;
    }
}
