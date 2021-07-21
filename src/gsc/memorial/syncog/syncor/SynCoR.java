package gsc.memorial.syncog.syncor;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import gsc.logging.ILoggingManager;
import gsc.memorial.syncog.syncod.handling.SynCoDHandler;
import tools.packaging.PackagingModule;
import utils.files.FileUtils;

public class SynCoR
{
	private ILoggingManager m_loggingMngr = null;
	private Logger m_log = null;
	
	public static final String SENS_REC_TYPE = "SENS_REC_TYPE";
	public static final String SYNCOD_REC_TYPE = "SYNCOD_REC_TYPE";
	public static final String STRUCT_REC_TYPE = "STRUCT_REC_TYPE";
	
	public static final String SYNCOR_FILE_EXTENSION = "syncor";
	public static final String SENS_FILE_EXTENSION = "sens";
	public static final String SYNCOD_FILE_EXTENSION = "syncod";
	public static final String STRUCT_FILE_EXTENSION = "xml";
	
	public static final String SENS_FILE_ID_BASE = "sensrec";
	public static final String SYNCOD_FILE_ID_BASE = "syncod";
	public static final String STRUCT_FILE_ID_BASE = "Structure";
	
	public static final String SENS_FILE_DIR_NAME = "sensation";
	public static final String SYNCOD_FILE_DIR_NAME = "syncod";
	public static final String PACKING_DIR_NAME = "pack";

	private File m_synCoRFile = null;
	private String m_synCoRID = null;
	private String m_synCoRWorkingDirPath = null;
	private String m_packagingWorkingDirPath = null;
	private PackagingModule m_packagingModule = null;
	
	private SynCoDHandler m_synCoDHandler = null;
	private boolean m_synCoDHTaken = false;
	
	private SynCoR(String syncorWorkigDirPath, ILoggingManager loggingMngr)
	{
		m_loggingMngr = loggingMngr;
		m_log = m_loggingMngr.getLogger(SynCoR.class,false);
		setWorkingDir(syncorWorkigDirPath);
	}

	/**
	 * Constructor to be employed when a SynCoR already exists and it is going to be unpacked (at least initially)
	 * 
	 * @param syncorWorkingDirPath the SynCoR's working directory. Inside this folder the SynCoR will build another one 
	 * with the name of the SynCoR where it will place all of the SynCoR's unpacked contents
	 *  
	 * @param loggingDirPath the folder where the logging file will be created
	 *  
	 * @param packagedFile the SynCoR file
	 */
	public SynCoR(String syncorWorkingDirPath, File packagedFile, ILoggingManager loggingMngr)
	{
		this(syncorWorkingDirPath,loggingMngr);
		m_synCoRFile = packagedFile;
		m_synCoRID = FileUtils.getFileNameWithoutExtension(packagedFile.getName());
		m_packagingWorkingDirPath = m_synCoRWorkingDirPath + m_synCoRID + "/" + PACKING_DIR_NAME + "/";
		m_packagingModule = new PackagingModule(m_packagingWorkingDirPath, loggingMngr);
	}

	/**
	 * Constructor to be employed in the construction of a SynCoR anew or a SynCoR whose production has already begun
	 * 
	 * @param workingDirPath
	 * @param loggingDirPath
	 * @param syncorID
	 */
	public SynCoR(String workingDirPath, String syncorID, String synCoDType, ILoggingManager loggingMngr)
	{
		this(workingDirPath,loggingMngr);
		m_synCoRID = syncorID;
		m_packagingWorkingDirPath = m_synCoRWorkingDirPath + m_synCoRID + "/" + PACKING_DIR_NAME + "/";
		m_packagingModule = new PackagingModule(m_packagingWorkingDirPath, loggingMngr);
		m_packagingModule.setPackageData(syncorID,"SYNCOR");
		buildSynCoRBaseStructure(synCoDType);
	}
	
	public boolean unpackSynCoR(boolean overwrite)
	{
		return m_packagingModule.performUnpackaging(m_synCoRFile,m_synCoRID,m_synCoRWorkingDirPath,overwrite);
	}
	
	public boolean packSynCoR()
	{
		boolean success = true;
		
		if(m_synCoDHTaken)
		{
			success= false;
			m_log.info("SynCoR " + m_synCoRID + " cannot be packaged while the instance of the SynCoDHandler is still in use.");
		}
		else
		{
			m_synCoDHandler = null;
			
			String rootParent = m_synCoRID + "/";
			m_packagingModule.addItem(rootParent, SENS_FILE_DIR_NAME, false, "TAR", null);
			addInnerRecItemsToStructDescr(SENS_REC_TYPE);		

			m_packagingModule.addItem(rootParent, SYNCOD_FILE_DIR_NAME, false, "ZIP", null);
			addInnerRecItemsToStructDescr(SYNCOD_REC_TYPE);

			success = m_packagingModule.performPackaging(m_synCoRWorkingDirPath,true);
			
			if(success){m_synCoRFile = m_packagingModule.getPackage();}
		}
		
		return success;
	}
	
	public File getSensRec(int recIndex){return getSynCoRInnerRec(SENS_REC_TYPE,recIndex);}
	
	public SynCoDHandler getSynCoDHandler()
	{
		SynCoDHandler synCoDHandler = null;
		
		if(m_synCoDHandler==null)
		{
			File synCoDFile = getSynCoDRec();
			m_synCoDHandler = new SynCoDHandler(synCoDFile, false, m_loggingMngr);
		}
		
		if(m_synCoDHTaken)
		{
			m_log.info("The instance of SynCoDHandler for SynCoR " + m_synCoRID + " has ben requested by some object and not yet returned.");
		}
		else
		{
			synCoDHandler = m_synCoDHandler;
			m_synCoDHTaken = true;
		}
		return synCoDHandler;
	}
	
	public void releaseSynCoDHandler(SynCoDHandler synCoDHandler)
	{
		if(synCoDHandler.equals(m_synCoDHandler)){m_synCoDHTaken=false;}
	}
	
	public File getSynCoDRec(){return getSynCoDRec(0);}
	
	private File getSynCoDRec(int recIndex){return getSynCoRInnerRec(SYNCOD_REC_TYPE,recIndex);}
	
	public File getStructRec(){return getSynCoRInnerRec(STRUCT_REC_TYPE,-1);}
	
	public File addSensRec(File sensRec){return addSynCoRInnerRec(SENS_REC_TYPE,sensRec);}
	
	public File addSynCoDRec(File synCoDRec){return addSynCoRInnerRec(SYNCOD_REC_TYPE,synCoDRec);}
	
	public File addStructRec(File structRec){return addSynCoRInnerRec(STRUCT_REC_TYPE,structRec);}

	public boolean removeSensRec(int recIndex){return removeSynCoRInnerRec(SENS_REC_TYPE,recIndex);}
	
	public boolean removeSynCoDRec()
	{
		boolean success = true;
		if(m_synCoDHTaken)
		{
			success= false;
			m_log.info("The SynCoDRec for SynCoR " + m_synCoRID + " cannot be removed while the instance of the SynCoDHandler is still in use.");
		}
		else
		{
			m_synCoDHandler = null;
			success = removeSynCoDRec(0);
		}
		return success;
	}
	
	private boolean removeSynCoDRec(int recIndex){return removeSynCoRInnerRec(SYNCOD_REC_TYPE,recIndex);}
	
	public boolean removeStructRec(){return removeSynCoRInnerRec(STRUCT_REC_TYPE,-1);}

	public boolean replaceSensRec(File sensRec, int recIndex){return replaceSynCoRInnerRec(SENS_REC_TYPE, sensRec, recIndex);}

	public boolean replaceSynCoDRec(File synCoDRec)
	{
		boolean success = true;
		if(m_synCoDHTaken)
		{
			success= false;
			m_log.info("The SynCoDRec for SynCoR " + m_synCoRID + " cannot be replaced while the instance of the SynCoDHandler is still in use.");
		}
		else
		{
			m_synCoDHandler = null;
			success = replaceSynCoDRec(synCoDRec,0);
		}
		return success;
	}
	
	private boolean replaceSynCoDRec(File synCoDRec, int recIndex){return replaceSynCoRInnerRec(SYNCOD_REC_TYPE, synCoDRec, recIndex);}
	
	public boolean replaceStructRec(File structRec){return replaceSynCoRInnerRec(STRUCT_REC_TYPE, structRec,-1);}
	
	public Vector<File> getSynCoRInnerRecList()
	{
		Vector<File> innerRecList = new Vector<File>();
		
		Vector<File> innerSensRecList = getInnerSensRecList();
		if(innerSensRecList.size()>0){innerRecList.addAll(innerSensRecList);}

		Vector<File> innerSynCoDRecList = getInnerSynCoDRecList();
		if(innerSynCoDRecList.size()>0){innerRecList.addAll(innerSynCoDRecList);}
		
		String structRecFilePath = buildRecFullPath(STRUCT_REC_TYPE, -1);
		if(FileUtils.diskItemExists(structRecFilePath)){innerRecList.add(new File(structRecFilePath));}
		
		return innerRecList;
	}

	public File getSynCoR()
	{
		return m_synCoRFile;
	}
	
	public boolean cleanUp()
	{
		boolean success = true;
		String packDirPath = m_synCoRWorkingDirPath + m_synCoRID + "/" + PACKING_DIR_NAME + "/";
		success = FileUtils.removeDirectory(new File(packDirPath));
		return success;
	}
	
	public Vector<File> getInnerSensRecList()
	{
		Vector<File> innerSensRecList = new Vector<File>();
		String holdingDirName = getRecHoldingDirName(SENS_REC_TYPE);
		String holdingDirFullPath = m_synCoRWorkingDirPath + m_synCoRID + "/" + holdingDirName + "/";
		loadFolderInnerFileList(holdingDirFullPath,innerSensRecList);
		return innerSensRecList;
	}

	public Vector<File> getInnerSynCoDRecList()
	{
		Vector<File> innerSynCoDRecList = new Vector<File>();
		String holdingDirName = getRecHoldingDirName(SYNCOD_REC_TYPE);
		String holdingDirFullPath = m_synCoRWorkingDirPath + m_synCoRID + "/" + holdingDirName + "/";
		loadFolderInnerFileList(holdingDirFullPath,innerSynCoDRecList);
		return innerSynCoDRecList;
	}
	
	private void addInnerRecItemsToStructDescr(String recType)
	{
		String recHoldingDirPath = m_synCoRWorkingDirPath + m_synCoRID + "/";
		String recBaseName = null;
		String recExtension = null;
		String recParentRelPath = m_synCoRID + "/";
		
		if(SENS_REC_TYPE.equals(recType))
		{
			recHoldingDirPath = recHoldingDirPath + SENS_FILE_DIR_NAME + "/";
			recParentRelPath = recParentRelPath + SENS_FILE_DIR_NAME;
			recBaseName = SENS_FILE_ID_BASE;
			recExtension = SENS_FILE_EXTENSION;
		}
		else if(SYNCOD_REC_TYPE.equals(recType))
		{
			recHoldingDirPath = recHoldingDirPath + SYNCOD_FILE_DIR_NAME + "/";
			recParentRelPath = recParentRelPath + SYNCOD_FILE_DIR_NAME;
			recBaseName = SYNCOD_FILE_ID_BASE;
			recExtension = SYNCOD_FILE_EXTENSION;
		}
		
		File dir = new File(recHoldingDirPath);
		File[] recs = dir.listFiles();
		for (int i = 0; i < recs.length; i++)
		{
			File rec = recs[i];
			String recName = rec.getName();
			if(recName.contains(recBaseName))
			{
				String recFullPath = recHoldingDirPath + recName;
				String recItemID = FileUtils.getFileNameWithoutExtension(recName);
				m_packagingModule.addItem(recParentRelPath, recItemID, true,recExtension, recFullPath);
			}
		}
	}

	private boolean buildSynCoRBaseStructure(String synCoDType)
	{
		boolean success = true;
		
		String rootDirPath = m_synCoRWorkingDirPath + m_synCoRID + "/";
		File rootDir = new File(rootDirPath);
		
		String sensRedDirPath = rootDirPath + SENS_FILE_DIR_NAME + "/";
		File sensRecDir = new File(sensRedDirPath);
		
		String synCoDRecDirPath = rootDirPath + SYNCOD_FILE_DIR_NAME + "/";
		File synCoDRecDir = new File(synCoDRecDirPath);
		
		String synCoDFilePath = synCoDRecDirPath + "m_synCoRID.syncor";
		File synCoDFile = new File(synCoDFilePath);
	
		String packingDirPath = rootDirPath + PACKING_DIR_NAME + "/";
		File packingDir = new File(packingDirPath);
		
		try
		{
			if(!FileUtils.directoryExists(rootDir))
			{
				success = FileUtils.mkDirectory(rootDirPath);
			}
			else{m_log.info("Note that this SynCoR is being created on a pre-existing directory strucure. If it is the structure of a SynCoR object, the two will possibly be merged.");}
			if(success)
			{
				success = success && (FileUtils.directoryExists(sensRecDir) || FileUtils.mkDirectory(sensRecDir));
				success = success && (FileUtils.directoryExists(synCoDRecDir) || FileUtils.mkDirectory(synCoDRecDir));
				success = success && (FileUtils.directoryExists(packingDir) || FileUtils.mkDirectory(packingDir));
				
				success = success && (FileUtils.diskItemExists(synCoDFile) || createBaseSynCoDFile(synCoDFile,synCoDType));
				
			}
		}
		catch (IOException e)
		{
			success = false;
			e.printStackTrace();
		}
		return success;
	}

	private boolean createBaseSynCoDFile(File synCoDFile, String synCoDType)
	{
		boolean success = true;
		
		String creatorUser = m_synCoRID.split(":")[0];
		m_synCoDHandler = new SynCoDHandler(m_synCoRID,synCoDType,creatorUser,false,m_loggingMngr);
		success = m_synCoDHandler.save(null);
		
		return success;
	}

	private void loadFolderInnerFileList(String holdingDirFullPath, Vector<File> innerRecList)
	{
		File folder = new File(holdingDirFullPath);
		File[] innerFiles = folder.listFiles();
		for (int i = 0; i < innerFiles.length; i++)
		{
			File diskItem = innerFiles[i];
			if(diskItem.isFile()){innerRecList.addElement(diskItem);}
		}
	}
	
	private boolean replaceSynCoRInnerRec(String recType, File newRec, int recIndex)
	{
		boolean success = true;
		
		String fileToReplaceFullPath = buildRecFullPath(recType, recIndex);
		File oldFile = new File(fileToReplaceFullPath);
		
		if(oldFile.exists())
		{
			try {success = FileUtils.copyFile(newRec, oldFile);}
			catch (Exception e)
			{
				success = false;
				e.printStackTrace();
			}
		}
		else
		{
			success = false;
			m_log.error("File " + fileToReplaceFullPath + " cannot be replaced as it does not exist."); 
		}
		
		return success;
	}

	private boolean removeSynCoRInnerRec(String recType, int recIndex)
	{
		boolean success = true;
		
		String fileToRemoveFullPath = buildRecFullPath(recType, recIndex);
		
		success = FileUtils.removeDiskItem(new File(fileToRemoveFullPath));
		
		return success;
	}
	
	private File addSynCoRInnerRec(String recType, File rec)
	{
		File copyFile = null;
		
		int index = -1;
		if(!STRUCT_REC_TYPE.equals(recType)){index = getNextAvailableIndex(recType);}
		
		String copyFileFullPath = buildRecFullPath(recType, index);
		copyFile = new File(copyFileFullPath);
		
		try
		{
			if(!FileUtils.copyFile(rec, copyFile)){copyFile=null;}
		}
		catch (Exception e)
		{
			copyFile=null;
			e.printStackTrace();
		}
		
		return copyFile;
	}

	private int getNextAvailableIndex(String recType)
	{
		int index = -1;
		String targetDirName = null;
		if(SENS_REC_TYPE.equals(recType)){targetDirName=SENS_FILE_DIR_NAME;}
		else if(SYNCOD_REC_TYPE.equals(recType)){targetDirName=SYNCOD_FILE_DIR_NAME;}
		
		if(targetDirName!=null)
		{
			String fullDirPath = m_synCoRWorkingDirPath + m_synCoRID + "/" + targetDirName + "/";
			File dir = new File(fullDirPath);
			File[] files = dir.listFiles();
			index=files.length;
		}
		
		return index;
	}

	private File getSynCoRInnerRec(String recType, int recIndex)
	{ 
		File syncorInnerRec = null;
				
		String recFullPath = buildRecFullPath(recType,recIndex);
		if(FileUtils.diskItemExists(recFullPath))
		{
			syncorInnerRec = new File(recFullPath);
		}
	
		return syncorInnerRec;
	}

	private String buildRecFullPath(String recType, int recIndex)
	{
		String recFullPath = null;
		String recRelPath = buildRecRelPath(recType,recIndex);
		if(recRelPath!=null){recFullPath = m_synCoRWorkingDirPath + recRelPath;}
		return recFullPath;
	}
	
	private String buildRecRelPath(String recType, int recIndex)
	{
		String recRelPath = null;
		String recName = getRecName(recType,recIndex);
		String recExtension = getRecExtension(recType);
		String recHoldingDirName = getRecHoldingDirName(recType);
		if(recHoldingDirName!=null)
		{
			recRelPath =  m_synCoRID + "/" + recHoldingDirName + "/" + recName + "." + recExtension;	
		}
		else
		{
			recRelPath =  m_synCoRID + "/" + recName + "." + recExtension;
		}
		  
		return recRelPath;
	}

	private String getRecHoldingDirName(String recType)
	{
		String recHoldingDirName = null;
		if(SENS_REC_TYPE.equalsIgnoreCase(recType)){recHoldingDirName = SENS_FILE_DIR_NAME;}
		else if(SYNCOD_REC_TYPE.equalsIgnoreCase(recType)){recHoldingDirName = SYNCOD_FILE_DIR_NAME;}
		
		return recHoldingDirName;
	}

	private String getRecName(String recType, int recIndex)
	{
		String recName = null;
		
		String recNameBase = null;
		if(SENS_REC_TYPE.equalsIgnoreCase(recType)){recNameBase = SENS_FILE_ID_BASE;}
		else if(SYNCOD_REC_TYPE.equalsIgnoreCase(recType)){recNameBase = SYNCOD_FILE_ID_BASE;}
		else if(STRUCT_REC_TYPE.equalsIgnoreCase(recType)){recNameBase = STRUCT_FILE_ID_BASE;}
		
		if(recNameBase != null)
		{
			recName = recNameBase;
			if(recIndex >=0){recName = recName + recIndex;}
		}
		return recName;
	}

	private String getRecExtension(String recType)
	{
		String recExtension = null;
		if(SENS_REC_TYPE.equalsIgnoreCase(recType)){recExtension = SENS_FILE_EXTENSION;}
		else if(SYNCOD_REC_TYPE.equalsIgnoreCase(recType)){recExtension = SYNCOD_FILE_EXTENSION;}
		else if(STRUCT_REC_TYPE.equalsIgnoreCase(recType)){recExtension = STRUCT_FILE_EXTENSION;}
		
		return recExtension;
	}
	
	private void setWorkingDir(String workigDirPath)
	{
		m_synCoRWorkingDirPath = FileUtils.normalizeDirPath(workigDirPath);
	}
}
