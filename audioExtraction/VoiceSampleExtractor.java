package audioExtraction;

import audioExtraction.subtitle.SrtParser;
import audioExtraction.subtitle.SubtitleLine;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by caiomoraes on 08/10/17.
 */
public class VoiceSampleExtractor
{
    /**
     * Generate an audio data base from a video file, taking time marks from its srt file as parameter (to determine
     *  which sample has voice or not), with the following structure:
     *      filename/
     *          filename.mp3 //whole audio of the video
     *          Voice/
     *              voiceSamples.mp3 //samples with voice
     *          NoVoice/
     *              noVoiceSamples.mp3 //samples without voice
     * @param videoFile
     * @param srtFile
     */
    public void generateDataBase(String videoFile, String srtFile)
    {
        SrtParser srtParser = new SrtParser();
        Map<Integer, SubtitleLine> subtitleLines = new HashMap<Integer, SubtitleLine>();
        Map<Integer, SubtitleLine> noVoiceTimeMarks = new HashMap<Integer, SubtitleLine>();


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


    /**
     * Extract the whole audio of the video file and saves it as an mp3 file in folder filename/.
     * @param videoFile String
     * @return
     */
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

    /**
     * Generate samples, with voice, in folder filename/Voice and, without voice, in folder filename/NoVoice.
     * @param audioFile
     * @param subtitleLines
     * @param noVoiceTimeMarks
     * @throws IOException
     */
    private void extractSamples(String audioFile, Map<Integer, SubtitleLine> subtitleLines, Map<Integer, SubtitleLine> noVoiceTimeMarks) throws IOException
    {
        String fileName = StringUtils.getFileNameWithoutExtension(audioFile);
        String voiceDir = fileName + "/Voice";
        String noVoiceDir = fileName + "/NoVoice";

        //Folders
        Files.createDirectories(Paths.get(fileName));
        Files.createDirectories(Paths.get(voiceDir));
        Files.createDirectories(Paths.get(noVoiceDir));

        //Thread pool with size equal to the number of processors
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        //Samples with voice
        for (SubtitleLine subtitleLine : subtitleLines.values())
        {
            Runnable worker = new ExtractorThread(fileName, subtitleLine, true);
            executor.execute(worker);
        }
        cleanFiles(fileName + "/Voice");

        //Samples without voice
        for (SubtitleLine noVoiceTimeMark : noVoiceTimeMarks.values())
        {
            Runnable worker = new ExtractorThread(fileName, noVoiceTimeMark, false);
            executor.execute(worker);
        }
        cleanFiles(fileName + "/NoVoice");

        executor.shutdown();

        //waiting all threads to finish}
        while (!executor.isTerminated());

        System.out.println("Finished all threads.");

    }


    /**
     * Deletes files in specified folder with size equal to or less than mp3 header size in bytes.
     * @param directoryPath String
     * @throws IOException
     */
    private void cleanFiles(String directoryPath) throws IOException
    {
        int mp3HeaderSizeBytes = 1005;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directoryPath)))
        {
            for (Path entry : stream)
            {
                File file = entry.toFile();
                if (file.isFile())
                {
                    if (file.length() <= mp3HeaderSizeBytes)
                    {
                        file.delete();
                    }
                }
            }
        }
    }
}