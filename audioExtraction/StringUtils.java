package audioExtraction;

/**
 * Created by caiomoraes on 08/10/17.
 */
final class StringUtils
{
    private StringUtils(){}

    static String getFileNameWithoutExtension(String file)
    {
        int pos = file.lastIndexOf('.');
        if (pos > 0 && pos < (file.length() - 1))
        {
            // there is a '.' and it's not the first, or last character.
            return file.substring(0,  pos);
        }
        return file;
    }
}
