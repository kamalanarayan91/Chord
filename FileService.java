import java.io.File;


public class FileService {
	
	private static String folderName ;
	private static boolean isFileServiceAvailable;
	private static long maxHashNumber;
	
	private void FileService(String folderName, long maxIdentificationNumber)
	{
		this.setFolderName("/home/chilli/testChord/0");
		this.setServiceAvailability();
		if(this.getServiceAvailability())
		{
			this.setMaxHashNumber(maxIdentificationNumber);
		}
	}
	public static void main(String args[])
	{
		System.out.println("Searching in file folder : ");
		setFolderName("/home/chilli/testChord/1");
		setMaxHashNumber(Utilities.totalNodes);
		Hasher.getHashString("c.txt",maxHashNumber);
		reDistributeFiles(folderName);
		System.out.println(fetchFileNameHash("updater.ini"));
	}

	public static String[] getKeysforFilesinFolder(String folderName)
	{
		//Hashes only first level of files 
		String[] keys = null;
		File Folder = new File(folderName);
		if(Folder.isDirectory())
		{
			int i=0;
			keys = new String[Folder.listFiles().length];
			for (File file : Folder.listFiles()) {
			    //if (file.isFile()) {
			        keys[i]= Hasher.getHashString(file.getName(),maxHashNumber);
			    	System.out.println(file.getName()+" : " + keys[i]);
			    	i++;
			   // }
			}
		}
		//System.out.println(keys);
		return keys;
	}
	public static void reDistributeFiles(String folderName)
	{
		
	}
	private static void transferFiletoNode(String name, String hash) {
		String folderName = "/home/chilli/testChord/"+hash;
		System.out.println(folderName);
		
	}
	public static boolean folderExists(String folderName){
		File Folder = new File(folderName);
		try{
			return Folder.isDirectory();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			return false;
		}
				
	}
	public static boolean fileExists(String fileName){
		File file = new File(folderName+"/"+fileName);
		try{
			return file.isFile();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			return false;
		}
	}
	public static String fetchFileNameHash(String fileName)
	{
		String fileNameHash = "";
		if(fileExists(fileName))
		{
			fileNameHash = Hasher.getHashString(fileName,maxHashNumber);
		}
		return fileNameHash;
		
	}
	
	public String getFolderName()
	{
		return folderName;
	}
	public static void setFolderName(String foldrName)
	{
		if(folderExists(foldrName))
		{
			folderName = foldrName;
		}
	}
	public boolean getServiceAvailability()
	{
		return this.isFileServiceAvailable;
	}
	public void setServiceAvailability()
	{
		if(folderExists(this.folderName))
		{
			this.isFileServiceAvailable = true;
		}
		else
		{
			this.isFileServiceAvailable = false;
		}
	}
	public long getMaxHashNumber()
	{
		return this.maxHashNumber;
	}
	public static void setMaxHashNumber(long HashNumber)
	{
		maxHashNumber = Math.abs(HashNumber);
	}


}
