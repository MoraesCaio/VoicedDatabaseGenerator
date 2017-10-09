package audioExtraction;

import audioExtraction.subtitle.SrtParser;
import audioExtraction.subtitle.SubtitleLine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by caiomoraes on 08/10/17.
 */
public class VoiceSampleExtractor
{
    public void generateDataBase(String videoFile, String srtFile)
    {
        SrtParser srtParser = new SrtParser();
        List<SubtitleLine> subtitleLines = new ArrayList<>();
        List<SubtitleLine> noVoiceTimeMarks = new ArrayList<>();


        /*Extracting audio*/
        String audioFileName = extractAudio(videoFile);


        /*Parsing Srt file*/
        try
        {
            srtParser.parseSrtFile(srtFile);
            subtitleLines = srtParser.getSubtitleLines();
            noVoiceTimeMarks = srtParser.getNoVoiceTimeMarks();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


        /*Extracting voice samples*/
        try
        {
            extractSamples(audioFileName, subtitleLines, noVoiceTimeMarks);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String extractAudio(String videoFile)
    {
        String audioFile = "";
        try
        {
            String fileName = StringUtils.getFileNameWithoutExtension(videoFile);
            audioFile = fileName + ".mp3";

            //folder
            Files.createDirectories(Paths.get(fileName));

            //call: ffmpeg -y -i origin.mp4 origin/origin.mp3
            Process p = Runtime.getRuntime().exec("ffmpeg -y -i " + videoFile + " " + fileName + "/" + audioFile);
            p.waitFor();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        return audioFile;
    }

    private void extractSamples(String audioFile, List<SubtitleLine> subtitleLines, List<SubtitleLine> noVoiceTimeMarks) throws IOException
    {
        String fileName = StringUtils.getFileNameWithoutExtension(audioFile);

        //Folders
        Files.createDirectories(Paths.get(fileName));
        Files.createDirectories(Paths.get(fileName+"/Voice"));
        Files.createDirectories(Paths.get(fileName+"/NoVoice"));

        //Thread pool with size equal to the number of processors
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        //Samples with voice
        for (SubtitleLine subtitleLine : subtitleLines)
        {
            Runnable worker = new ExtractorThread(fileName, subtitleLine, true);
            executor.execute(worker);
        }

        //Samples without voice
        for (SubtitleLine noVoiceTimeMark : noVoiceTimeMarks)
        {
            Runnable worker = new ExtractorThread(fileName, noVoiceTimeMark, false);
            executor.execute(worker);
        }

        executor.shutdown();

        //waiting all threads to finish}
        while (!executor.isTerminated());

        System.out.println("Finished all threads.");

    }

}
