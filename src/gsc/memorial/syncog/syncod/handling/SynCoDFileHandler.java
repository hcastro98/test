package gsc.memorial.syncog.syncod.handling;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import gsc.memorial.syncog.syncod.ObjectFactory;
import gsc.memorial.syncog.syncod.SynCoD;

public class SynCoDFileHandler
{
	public static SynCoD loadSynCoDFile(String synCoDFilePath)
	{
		return loadSynCoDFile(new File(synCoDFilePath));
	}

	public static SynCoD loadSynCoDFile(File synCogFile)
	{
		SynCoD synCoD = null;

		try
		{
			/*JAXBContext jaxbContext = JAXBContext.newInstance(graphml.om.GraphmlType.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			GraphmlType graph = (GraphmlType) jaxbUnmarshaller.unmarshal(graphFile);*/
			
			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			//JAXBElement<graphml.om.GraphmlType> applicationElement = (JAXBElement<graphml.om.GraphmlType>) jaxbUnmarshaller.unmarshal(graphFile);		
		    //Marshaller m = jaxbContext.createMarshaller();
			//m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//m.marshal(applicationElement.getValue(), System.out);
		    
			StreamSource source = new StreamSource(synCogFile);
			JAXBElement<SynCoD> rootElem = jaxbUnmarshaller.unmarshal(source, SynCoD.class);
			synCoD = rootElem.getValue();
						
		   /* Marshaller m = jaxbContext.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(rootElem, System.out);*/
		}
		catch (JAXBException e)
		{
			e.printStackTrace();
		}

		return synCoD;
	}

	public static boolean saveSynCoD2File(SynCoD synCoD, File file)
	{
		boolean success = true;
	    try
	    {
	    	JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
		    Marshaller m = jaxbContext.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(synCoD, file);
	    }
	    catch (Exception e)
	    {
	    	success = false;
	    	e.printStackTrace();
	    }
	    return success;
	}

}
