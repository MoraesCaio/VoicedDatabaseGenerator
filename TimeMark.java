import java.util.InvalidPropertiesFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caiomoraes on 07/10/17.
 */
public class TimeMark
{
    private String timestamp;
    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;
    final static String regTimeMark = "(\\d\\d):(\\d\\d):(\\d\\d),(\\d\\d\\d)";
    final static int hourInMilliseconds = 60 * 60 * 1000;
    final static int minuteInMilliseconds = 60 * 1000;
    final static int secondInMilliseconds = 1000;

    public TimeMark() throws InvalidPropertiesFormatException
    {
        this("00:00:00,000");
    }

    public TimeMark(String timestamp) throws InvalidPropertiesFormatException
    {
        this.timestamp = timestamp;

        Pattern patternTimeMark = Pattern.compile(regTimeMark);
        Matcher matcherTimeMark = patternTimeMark.matcher(timestamp);
        if (!matcherTimeMark.find())
            throw new InvalidPropertiesFormatException("Expected format for time marks: dd:dd:dd,ddd");

        hours = Integer.parseInt(matcherTimeMark.group(1));
        minutes = Integer.parseInt(matcherTimeMark.group(2));
        seconds = Integer.parseInt(matcherTimeMark.group(3));
        milliseconds = Integer.parseInt(matcherTimeMark.group(4));
    }

    public TimeMark(int hours, int minutes, int seconds, int milliseconds)
    {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
    }

    public void addHours(int hours)
    {
        this.hours += hours;
        if (this.hours < 0)
        {
            addMinutes(this.hours * 60);
            this.hours = 0;
        }
    }

    public void addMinutes(int minutes)
    {
        this.minutes += minutes;
        if (this.minutes >= 60)
        {
            addHours(this.minutes / 60);
            this.minutes = this.minutes % 60;
        }
        else if (this.minutes < 0)
        {
            addSeconds(this.minutes * 60);
            this.minutes = 0;
        }
    }

    public void addSeconds(int seconds)
    {
        this.seconds += seconds;
        if (this.seconds >= 60)
        {
            addMinutes(this.seconds / 60);
            this.seconds = this.seconds % 60;
        }
        else if (this.seconds < 0)
        {
            addMilliseconds(this.seconds * 1000);
            this.seconds = 0;
        }
    }

    public void addMilliseconds(int milliseconds)
    {
        this.milliseconds += milliseconds;
        if (this.milliseconds >= 1000)
        {
            addSeconds(this.milliseconds / 1000);
            this.milliseconds = this.milliseconds % 1000;
        }
        else if (this.milliseconds < 0)
        {
            this.milliseconds = 0;
        }
    }

    public static TimeMark getDuration(TimeMark start, TimeMark end)
    {
        long startTotal = start.getTotalMilliseconds();
        long endTotal = end.getTotalMilliseconds();
        long duration;
        if (endTotal >= startTotal)
        {
            duration = endTotal - startTotal;
        }
        else
        {
            duration = startTotal - endTotal;
        }

        int milliseconds = (int) duration % 1000;
        duration /= 1000;
        int seconds = (int) duration % 60;
        duration /= 60;
        int minutes = (int) duration % 60;
        duration /= 60;
        int hours = (int) duration;

        return new TimeMark(hours, minutes, seconds, milliseconds);

        /*int hours        = (int) duration / hourInMilliseconds;
        int minutes      = (int) (duration - (hours*hourInMilliseconds))     / minuteInMilliseconds;
        int seconds      = (int) (duration - (minutes*minuteInMilliseconds)) / secondInMilliseconds;
        int milliseconds = (int) (duration / (60 * 60 * 1000);*/
    }

    public long getTotalMilliseconds()
    {
        return ((hours*60+minutes)*60+seconds)*1000+milliseconds;
    }

    @Override
    public String toString()
    {
        return ((hours < 10)?"0":"")+hours+ ":"+
                ((minutes < 10)?"0":"")+minutes+":"+
                ((seconds < 10)?"0":"")+seconds+","+
                ((milliseconds < 100)?"0":"")+
                ((milliseconds < 10)?"0":"")+
                milliseconds;
    }
}
