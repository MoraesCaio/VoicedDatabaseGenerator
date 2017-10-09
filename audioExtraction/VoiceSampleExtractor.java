package audioExtraction;

import audioExtraction.subtitle.SubtitleLine;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        /*Extracting audio*/
        String audioFileName = extractAudio(videoFile);


        /*Extracting voice samples*/
        try
        {
            extractSamples(audioFileName, srtFile);
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
     * @param srtFile
     * @throws IOException
     */
    private void extractSamples(String audioFile, String srtFile) throws IOException
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


        /*PARSING SRT FILE*/
        //Reading content
        byte[] encoded = Files.readAllBytes(Paths.get(srtFile));
        String content = new String(encoded, StandardCharsets.UTF_8);

        //regex
        String endLine = "\\r?\\n";
        String blankSpace = "[ \\t]*";
        String regexTimeMark = "(?s)(\\d+)" + blankSpace + endLine +
                "(\\d{1,2}:\\d\\d:\\d\\d,\\d\\d\\d)" + blankSpace + "-->" + blankSpace +
                "(\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d)" + blankSpace + "(?:\\d.*?)??" + endLine
                + "(?:.*?)" + endLine + endLine;
        Pattern pTimeMark = Pattern.compile(regexTimeMark);

        //Parsing subtitleLines (periods most likely with voice)
        Matcher mTimeMark = pTimeMark.matcher(content);

        //Initializing fetching
        mTimeMark.find();
        SubtitleLine penultimateSubtitleLine = new SubtitleLine(
                Integer.parseInt(mTimeMark.group(1)),
                mTimeMark.group(2),
                mTimeMark.group(3)
        );

        //First interval with voice
        executor.execute(new ExtractorThread(fileName, penultimateSubtitleLine, true));
        SubtitleLine lastSubtitleLine, noVoiceTimeMark;

        //No voice time marks
        while (mTimeMark.find())
        {
            lastSubtitleLine = new SubtitleLine(
                    Integer.parseInt(mTimeMark.group(1)),
                    mTimeMark.group(2),
                    mTimeMark.group(3)
            );
            executor.execute(new ExtractorThread(fileName, lastSubtitleLine, true));

            //interval without voice
            noVoiceTimeMark = new SubtitleLine(
                    penultimateSubtitleLine.idx,
                    penultimateSubtitleLine.endTime,
                    lastSubtitleLine.startTime
                    );
            executor.execute(new ExtractorThread(fileName, noVoiceTimeMark, false));


            //SWAP
            penultimateSubtitleLine = lastSubtitleLine;
        }

        executor.shutdown();

        //waiting all threads to finish}
        while (!executor.isTerminated());
        System.out.println("Finished all threads.");

        //Cleaning invalid files
        cleanFiles(voiceDir);
        cleanFiles(noVoiceDir);
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