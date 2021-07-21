package gsc.memorial.syncog.tools;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import gsc.memorial.syncog.syncod.ObjectFactory;
import gsc.memorial.syncog.syncod.SynCoD;

public class SymCoGTools
{
	public static void printSynCoDXML2Console(SynCoD synCoD)
	{
	    try
	    {
	    	JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
		    Marshaller m = jaxbContext.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(synCoD, System.out);
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	}
}
