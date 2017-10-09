package audioExtraction.subtitle;

/**
 * Created by caiomoraes on 07/10/17.
 */
public class SubtitleLine
{
    public int idx;
    public String startTime;
    public String endTime;
    public String text;

    /*Constructors*/
    public SubtitleLine()
    {
        this(0, "", "", "");
    }

    public SubtitleLine(int idx, String startTime, String endTime, String text)
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
        return startTime;
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