package audioExtraction;

import audioExtraction.subtitle.SubtitleLine;
import audioExtraction.subtitle.TimeMark;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

/**
 * Created by caiomoraes on 08/10/17.
 */
public class ExtractorThread implements Runnable
{
    private String fileName;
    private int idx;
    private String startTime;
    private String duration;
    private boolean withVoice;

    /*Constructor*/
    ExtractorThread(String fileName, SubtitleLine subtitleLine, Boolean withVoice) throws InvalidPropertiesFormatException
    {
        this.fileName = fileName;
        this.idx = subtitleLine.getIdx();
        this.startTime = subtitleLine.startTime;
        this.duration = TimeMark.getDuration(startTime, subtitleLine.endTime).toString();
        this.withVoice = withVoice;
    }

    @Override
    public void run()
    {
        //ffmpeg -y -ss 30.96 -t 2:01.91 -i origin/origin.mp3 origin/(No)Voice/originVoice1.mp3
        try
        {
            StringBuilder stringBuilder = new StringBuilder(
                    "ffmpeg -y -ss " + new TimeMark(startTime) +
                            " -t " + duration +
                            " -i " + fileName + "/" + fileName + ".mp3 " +
                            fileName + "/" + ((withVoice)?"":"No") + "Voice/" +
                            fileName + ((withVoice)?"":"No") + "Voice" + idx + ".mp3");

            System.out.println("Executing command: " + stringBuilder.toString());

            Process p = Runtime.getRuntime().exec(stringBuilder.toString());
            p.waitFor();
        }
        catch (InterruptedException | IOException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
