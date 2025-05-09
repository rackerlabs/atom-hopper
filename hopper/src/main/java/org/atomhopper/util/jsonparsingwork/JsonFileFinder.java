package org.atomhopper.util.jsonparsingwork;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JsonFileFinder {

    public static void main(String[] args){
        String message_samples_path = "message_samples";
        List<String> entryJsonFiles = findEntryJsonFiles(message_samples_path);

        System.out.println("JSON files found!");
        for(String filepath: entryJsonFiles){
            System.out.println(filepath);
        }

    }

    public static List<String> findEntryJsonFiles(String message_samples_path){
        List<String> entry_JSON_files = new ArrayList<>();

        //makes a new file object , we are not creating a new file , rather just an object to interact with this folder(messafe_samples)
        File mainfolderObj = new File(message_samples_path);
        //this obj reps message_samples folder, we will use to perform operations on the folder

        //check if this message_samples folder exi
        if(mainfolderObj.exists() && mainfolderObj.isDirectory()){
            //the for loop iterates over the object returned by the mainfolderobj.listfiles()
            for(File sub_folder : mainfolderObj.listFiles()){
                if(sub_folder.isDirectory()){
                    //if any given sub_folder is a directory

                    //an object rep directory json within subfolder
                    File jsonFolder = new File(sub_folder, "json");

                    if(jsonFolder.exists() && jsonFolder.isDirectory()){
                        for(File file: jsonFolder.listFiles()){

                            if(file.exists() && file.getName().endsWith("entry.json")){
                                entry_JSON_files.add(file.getAbsolutePath());
                            }

                        }
                    }


                }
            }
        }
        else{
            System.out.println("The specified samples folder does not exist or is not a directory.");
        }

        return entry_JSON_files;
    }

}
