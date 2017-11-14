import audioExtraction.VoiceSampleExtractor;
import java.util.Scanner;

/**
 * Created by caiomoraes on 07/10/17.
 */
public class AudioDBGen
{
    public static void main(String[] args)
    {
        VoiceSampleExtractor voiceSampleExtractor = new VoiceSampleExtractor();
        String videoFile;
        if (args.length < 1)
        {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Not enough parameters.");
            System.out.println("Enter video file name:");
            videoFile = scanner.nextLine();
        }
        else
        {
            videoFile = args[0];
        }
        voiceSampleExtractor.generateDataBase(videoFile);
    }
}
