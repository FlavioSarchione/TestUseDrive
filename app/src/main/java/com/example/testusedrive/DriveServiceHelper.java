package com.example.testusedrive;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper
{
    private final Executor mExcutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;

    public DriveServiceHelper(Drive mDriveService)
    {
        this.mDriveService = mDriveService;
    }

    public Task<String> createFilePDF(String filePath)
    {
        return Tasks.call(mExcutor, () -> {
            boolean fileExists = false;
            String idFile = "";
            File fileMetaData = new File();
            fileMetaData.setName("MyPDFFile");
            java.io.File file = new java.io.File(filePath);

            FileContent mediaContent = new FileContent("application/pdf", file);

            File myFile = null;
            try
            {
                // Check if file exist
                String pageToken = null;
                do {
                    FileList readedFiles = mDriveService.files().list()
                            .setQ("mimeType='application/pdf'")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name)")
                            .setPageToken(pageToken)
                            .execute();
                    for (File fileRead : readedFiles.getFiles())
                    {
                        if(fileRead.getName().equals("MyPDFFile"))
                        {
                            fileExists = true;
                            idFile = fileRead.getId();
                        }
                    }
                    pageToken = readedFiles.getNextPageToken();
                } while (pageToken != null);
                if(fileExists)
                {
                    myFile = mDriveService.files().update(idFile, fileMetaData).execute();
                }
                else
                {
                    myFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (null == myFile)
            {
                throw new IOException("Null result when requesting file creation");
            }

            return myFile.getId();
        });
    }
}
