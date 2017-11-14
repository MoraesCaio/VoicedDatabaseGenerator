package audioExtraction;

import audioExtraction.subtitle.SrtParser;
import audioExtraction.subtitle.SubtitleLine;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
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
     * @param videoFile video file name
     */
    public void generateDataBase(String videoFile)
    {
        if (!new File(videoFile).exists())
        {
            System.out.println("Video file does not exist!");
            return;
        }

        String srtFile = videoFile.substring(0, videoFile.length()-4) + ".srt";

        SrtParser srtParser = new SrtParser();
        Map<Integer, SubtitleLine> subtitleLines = new HashMap<Integer, SubtitleLine>();
        Map<Integer, SubtitleLine> noVoiceTimeMarks = new HashMap<Integer, SubtitleLine>();


        //Extracting audio
        String audioFileName = extractAudio(videoFile);


        //Parsing Srt file
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


        //Extracting voice samples
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
            System.out.println("Creating output directory...");
            Files.createDirectories(Paths.get(fileName));

            //call: ffmpeg -y -i origin.mp4 origin/origin.mp3
            System.out.println("Extracting the whole audio from " + videoFile);
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
        String voiceDir = fileName + "/Voice/";
        String noVoiceDir = fileName + "/NoVoice/";

        //Folders
        System.out.println("Creating voice and unvoiced sample directories...");
        Files.createDirectories(Paths.get(voiceDir));
        Files.createDirectories(Paths.get(noVoiceDir));

        //Thread pool with size equal to the number of processors
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        System.out.println("Extracting samples...");
        //Samples with voice
        for (SubtitleLine subtitleLine : subtitleLines.values())
        {
            Runnable worker = new ExtractorThread(fileName, subtitleLine, true);
            executor.execute(worker);
        }

        //Samples without voice
        for (SubtitleLine noVoiceTimeMark : noVoiceTimeMarks.values())
        {
            Runnable worker = new ExtractorThread(fileName, noVoiceTimeMark, false);
            executor.execute(worker);
        }

        executor.shutdown();

        //waiting all threads to finish}
        while (!executor.isTerminated());
        System.out.println("All extracting threads have ended.");

        //Cleaning invalid files
        System.out.println("Removing invalid files...");
        cleanFiles(voiceDir);
        cleanFiles(noVoiceDir);


        //Renaming remaining files
        System.out.println("Renaming remaining files...");
        renameFiles("./"+voiceDir);
        renameFiles("./"+noVoiceDir);
    }


    /**
     * Deletes files in specified folder with size equal to or less than mp3 header size in bytes.
     * @param directoryPath String
     * @throws IOException
     */
    public void cleanFiles(String directoryPath) throws IOException
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

    /**
     * Renames files from fileName/Voice and fileName/NoVoice to fill gaps created as result from cleaning invalid
     *  files.
     * @param directory String containing absolute path of directory terminated with '/'.
     */
    public void renameFiles(String directory)
    {
        if (!directory.endsWith("/")) directory = directory + "/";

        //Fetching files
        String[] files = new File(directory).list();

        //Sorting files by its last 6 digits
        Arrays.sort(files, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return extractInt(o1) - extractInt(o2);
            }

            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                while (num.length() > 6){
                    num = num.substring(1);
                }
                // return 0 if no digits found
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });

        String baseNewFileName = directory + files[0].substring(0, files[0].lastIndexOf("Voice")+"Voice".length());

        //Renaming files (0-based)
        for (int i = 0; i < files.length; i++)
        {
            File f1 = new File(directory+files[i]);
            File f2 = new File(baseNewFileName+i+".mp3 ");
            f1.renameTo(f2);
        }
    }
}