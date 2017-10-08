/**
 * Created by caiomoraes on 07/10/17.
 */
public class SubtitleLine
{
    public int idx;
    public String text;
    public String startTime;
    public String endTime;

    public SubtitleLine()
    {
        this(0, "", "", "");
    }

    public SubtitleLine(int idx, String text, String startTime, String endTime)
    {
        this.idx = idx;
        this.text = text;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString()
    {
        return "Index: \t\t\t" + idx +
                "\nText: \t\t\t" + text +
                "\nStart time: \t" + startTime +
                "\nEnd Time: \t\t" + endTime;
    }
}
