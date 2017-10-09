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

    /*Constructors*/
    public SubtitleLine() throws InvalidPropertiesFormatException
    {
        this(0, "", "");
    }

    public SubtitleLine(int idx, String startTime, String endTime) throws InvalidPropertiesFormatException
    {
        this(idx, new TimeMark(startTime), new TimeMark(endTime));
    }

    public SubtitleLine(int idx, TimeMark startTime, TimeMark endTime)
    {
        this.idx = idx;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString()
    {
        return "Index: \t\t\t" + idx +
                "\nStart time: \t" + startTime +
                "\nEnd Time: \t\t" + endTime;
    }
}
