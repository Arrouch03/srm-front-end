package ma.srm.srm.frontend.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * Convertit un Uri en File dans le cache de l'application
     * @param uri L'Uri du fichier sélectionné
     * @param context Contexte de l'application
     * @return Fichier temporaire dans le cache
     * @throws Exception en cas d'erreur de lecture/écriture
     */
    public static File uriToFile(Uri uri, Context context) throws Exception {
        if (uri == null) {
            throw new IllegalArgumentException("Uri ne peut pas être null");
        }
        if (context == null) {
            throw new IllegalArgumentException("Context ne peut pas être null");
        }

        String fileName = getFileName(uri, context);
        File file = new File(context.getCacheDir(), fileName);

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {

            if (inputStream == null) {
                throw new Exception("Impossible d'ouvrir l'Uri en entrée");
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        return file;
    }

    /**
     * Récupère le nom du fichier depuis l'Uri
     * @param uri Uri du fichier
     * @param context Contexte de l'application
     * @return Nom du fichier
     */
    private static String getFileName(Uri uri, Context context) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
