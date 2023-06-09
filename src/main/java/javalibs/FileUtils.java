package javalibs;
/*
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 6/5/15
 */
@SuppressWarnings({"WeakerAccess", "SpellCheckingInspection", "unused"})
public class FileUtils{
    private static volatile FileUtils _instance;
    private Out out = Out.get();

    private FileUtils(){ }

    /**
     * Get an instance
     * @return javalibs.FileUtils object
     */
    public static FileUtils get() {
        if(_instance == null){
            synchronized(FileUtils.class){
                if(_instance == null){
                    _instance = new FileUtils();
                }
            }
        }
        return _instance;
    }

    /**
     * Get the file separate for the OS this system is running on
     * @return The separate string
     */
    public String sep(){
        return File.separator;
    }

    /**
     * Determine if a file already exists
     * @param pathToFile The path to the file
     * @return True if the file exists, else false
     */
    public boolean fexists(String pathToFile) { return new File(pathToFile).exists(); }

    /**
     * Get the current working directory
     * @return The path to the current working directory
     */
    public String getWorkingDir(){
        Path WD = Paths.get("");
        return WD.toAbsolutePath().toString();
    }

    /**
     * Create a directory if it doesn't exist
     * @param dirName Path to the directory
     */
    public void checkAndCreateDir(String dirName){
        // Return early if the directory already exists
        if(fexists(dirName)) return;

        // Create the directory (and parents, if necessary)
        File tmp = new File(dirName);
        TSL.get().trace("Creating directory: " + tmp.toString());
        if(!tmp.mkdirs())
            TSL.get().err("Failed to create directory path: " + dirName);
    }

    /**
     * Get the number of lines in a file
     * @param filePath the path to the file
     * @return number or lines, -1 if error
     */
    public long lineCount(String filePath){
        long lines = 0;
        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(filePath));
            while (br.readLine() != null) ++lines;
            br.close();
        }
        catch(IOException e){
            return -1;
        }
        return lines;
    }

    /**
     * Read a file in as a string
     * @param filePath The path to the file
     * @return The file, as a string, if it's found and read successfully
     */
    public String readFullFile(String filePath) {
        BufferedReader br = null;
        String all = null;
        try{
            br = Files.newBufferedReader(Paths.get(filePath));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while(line != null){
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            all = sb.toString();
        }
        catch(FileNotFoundException e){
            TSL.get().err(filePath + " not found.");
        }
        catch(IOException e){
            TSL.get().exception(e);
        }
        finally{
            if(br != null){
                try{
                    br.close();
                }
                catch(IOException e){
                    TSL.get().errFrom("Couldn't close the BR");
                }
            }
        }
        return all;
    }

    /**
     * Read a file line by line
     * @param filePath The path to the file
     * @return A list of lines of the file (as strings) if file is found and readable
     */
    public List<String> readLineByLine(String filePath) {
        List<String> lines = new ArrayList<>();
        BufferedReader br = null;
        try{
            br = Files.newBufferedReader(Paths.get(filePath));
            String line = br.readLine();
            while(line != null){
                lines.add(line);
                line = br.readLine();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            TSL.get().err("Problem with readLineByLine");
        }
        finally{
            if(br != null){
                try{
                    br.close();
                }
                catch(IOException e){
                    TSL.get().errFrom("Couldn't close the BR");
                }
            }
        }
        return lines;
    }

    /**
     * Returns a list of absolute file paths to files in a directory that start with the
     * supplied prefix
     * @param prefix The prefix string that a file should match with
     * @param path The path to the directory containing the files
     * @return The list of absolute paths, null if nothing matching in the directory
     */
    public List<String> getAllFilePathsInDirWithPrefix(String prefix, String path){
        List<String> filepaths = new ArrayList<>();

        File[] files = new File(path).listFiles();
        if(files == null)
            return Collections.emptyList();
        for(File f : files) {
            if(f.isFile() && f.getName().startsWith(prefix))
                filepaths.add(f.getAbsolutePath());
        }
        return sortedFilePaths(filepaths);
    }

    /**
     * Returns a list of absolute file paths to files in a directory
     * @param path The path to the directory
     * @return The list of absolute paths, null if no files in the directory
     */
    public List<String> getAllFilePathsInDir(String path){
        List<String> filepaths = new ArrayList<>();
        File[] files = new File(path).listFiles();
        if(files == null)
            return Collections.emptyList();
        for(File f : files) {
            if (f.isFile())
                filepaths.add(f.getAbsolutePath());
        }
        return sortedFilePaths(filepaths);
    }

    private List<String> sortedFilePaths(List<String> paths){
        if(paths.isEmpty()) return Collections.emptyList();
        Comparator<String> comparator = Comparator.comparing((String x) -> x);
        paths.sort(comparator);
        return paths;
    }

    /**
     * Delete a file if it exists. If the file does not exist this will return true.
     * Note, if the file cannot be deleted this eats the security exception and returns
     * false.
     * @param pathToFile The path to the file to be deleted
     * @return True if the file does not exist at the end of the function, else false
     */
    public boolean deleteFile(String pathToFile){
        File f = new File(pathToFile);
        if(!f.exists())
            return true;
        try {
            return f.delete();
        }
        catch(SecurityException e){
            TSL.get().exception(e);
            return false;
        }
    }

    public boolean deleteDir(String pathToDir){
        File dir = new File(pathToDir);
        if(!dir.exists() || !dir.isDirectory()) return true;
        return rmdir(dir);
    }

    private boolean rmdir(File dir){
        File[] contents = dir.listFiles();
        if(contents != null){
            for(File f: contents){
                if(!Files.isSymbolicLink(f.toPath()))
                    rmdir(f);
            }
        }
        boolean okay;
        try {
            okay = dir.delete();
        }
        catch(SecurityException e){
            TSL.get().exception(e);
            return false;
        }
        return okay;
    }

    /**
     * Returns java File objects for all Files in a directory
     * @param path Path to the directory
     * @return The list of File objects, null if no files in the directory
     */
    public List<File> getAllFileObjectsInDir(String path){
        List<File> files = new ArrayList<>();
        File[] fs = new File(path).listFiles();
        if(fs == null)
            return Collections.emptyList();
        for(File f : fs)
            if(f.isFile())
                files.add(f);
        if(files.isEmpty()) return null;

        return files;
    }

    /**
     * Write a new file to disk. Will overwrite existing file
     * @param fileName Path to the file
     * @param str The string to write to the file
     */
    public boolean writeNewFile(String fileName, String str){
        BufferedWriter writer;
        try {
            writer = Files.newBufferedWriter(Paths.get(fileName));
            writer.write(str);
            writer.close();
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Append data to an existing file
     * @param filename Path to the file
     * @param str The string to write to the new file
     */
    public boolean appendToFile(String filename, String str){
        BufferedWriter writer;
        try{
            writer = Files.newBufferedWriter(Paths.get(filename),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            writer.write(str);
            writer.close();
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Split a string on a character
     * @param str The string to split
     * @param splitChar The character to split over
     * @return Both sides of the string
     */
    public String[] splitOnChar(String str, char splitChar){
        return str.split(Character.toString(splitChar));
    }

    /**
     *
     * @param path Path to file to mod time
     * @param newModTime Mod time in string format MM/dd/yyyy
     * @return
     */
    public boolean changeModTime(String path, String newModTime) {
        if(!fexists(path)) return false;

        File f = new File(path);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        Date modTime;
        try {
            modTime = sdf.parse(newModTime);
        }
        catch(Exception e) {
            return false;
        }

        return f.setLastModified(modTime.getTime());
    }
}
