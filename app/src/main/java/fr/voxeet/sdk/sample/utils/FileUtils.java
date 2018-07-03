package fr.voxeet.sdk.sample.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kevinleperf on 18/04/2018.
 */

public class FileUtils {
    public static File extractAssetToTempFile(@NonNull Context context, @NonNull String fileName) {
        File output = new File(context.getApplicationInfo().dataDir, fileName);
        try {
            if (!output.exists())
                output.createNewFile();
            AssetManager assetManager = context.getAssets();

            InputStream in = assetManager.open(fileName);
            FileOutputStream out = new FileOutputStream(output);
            copyFile(in, out);
            out.flush();
            in.close();
            out.close();
            in = null;
            out = null;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            Log.e("tag", e.getMessage());
        }

        return output;
    }


    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
