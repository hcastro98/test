package gsc.memorial.syncog.syncod.handling;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import gsc.logging.ILoggingManager;
import gsc.memorial.syncog.syncod.CogGraph;
import gsc.memorial.syncog.syncod.CogGraphType.GraphConception;
import gsc.memorial.syncog.syncod.CogGraphType.GraphConception.GraphInstConception;
import gsc.memorial.syncog.syncod.CogGraphType.GraphConception.GraphOntConception;
import gsc.memorial.syncog.syncod.CogGraphType.GraphPerception;
import gsc.memorial.syncog.syncod.CogGraphType.GraphSensation;
import gsc.memorial.syncog.syncod.CogNType;
import gsc.memorial.syncog.syncod.CogNode;
import gsc.memorial.syncog.syncod.CogNodeRefType;
import gsc.memorial.syncog.syncod.CogNodeType.PayloadElement;
import gsc.memorial.syncog.syncod.GraphLinkSetType;
import gsc.memorial.syncog.syncod.GraphNodeType;
import gsc.memorial.syncog.syncod.GraphNodule;
import gsc.memorial.syncog.syncod.GraphNoduleRef;
import gsc.memorial.syncog.syncod.InterCogNodeRelType;
import gsc.memorial.syncog.syncod.InterGraphNoduleRef;
import gsc.memorial.syncog.syncod.SynCoD;
import gsc.memorial.syncog.syncod.SynCoD.Graphal;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Authorships;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Authorships.Statement;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Cognition;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Cognition.Integration;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Cognition.Layers;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Cognition.Layers.Conception;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Cognition.Layers.Conception.Instantial;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Cognition.Layers.Conception.Ontological;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Cognition.Layers.Perception;
import gsc.memorial.syncog.syncod.SynCoD.Graphal.Cognition.Layers.Sensation;
import utils.files.FileUtils;
import utils.randomnesss.RandomnessUtils;

public class SynCoDHandler
{
	public static final String H_ASCENDING_LINKSET = "HAscending";
	public static final String H_DESCENDING_LINKSET = "VAscending";
	public static final String V_ASCENDING_LINKSET = "VAscending";
	public static final String V_DESCENDING_LINKSET = "VDescending";
	
	private static final int LINK_OP = 1;
	private static final int UNLINK_OP = 2;
	private static final int ADD_OP = 3;
	private static final int REMOVE_OP = 4;
	
	private ILoggingManager m_loggingManager = null;
	private Logger m_log = null;

	private SynCoD m_synCoD = null;
	private File m_synCoDFile = null;
	private boolean m_inflateMode = false;
	
	public SynCoDHandler(SynCoD synCoD, boolean inflatedMode, ILoggingManager loggingManager)
	{
		boolean success = true;
		
		m_loggingManager = loggingManager;
		m_log = m_loggingManager.getLogger(SynCoDHandler.class,false);
		
		if(!validateInitParameters(1,synCoD,inflatedMode,null,null,null))
		{
			success = false;
			m_log.error("The initialization parameters given to the SynCoDHandler were not adequate. The SynCoDHandler is not operational.");
		}
		
		if(success)
		{
			m_synCoD = synCoD;
			m_inflateMode = inflatedMode;

			StringBuilder errMsg = new StringBuilder();
			if(!validate(errMsg, false))
			{
				success = false;
				m_log.error("The SynCoDHandler is not operational. The provided SynCoD is not valid." + errMsg.toString());
			}
			
			if(success)
			{
				if(m_inflateMode)
				{
					if(!inflateOrDeflate(true))
					{
						success = false;
						m_inflateMode = false;
						m_log.error("The inflation of the SynCoD was not successfull");
					}
				}
			}

		}
	}

	public SynCoDHandler(File synCoDFile, boolean inflatedMode, ILoggingManager loggingManager)
	{
		boolean success = true;

		m_loggingManager = loggingManager;
		m_log = m_loggingManager.getLogger(SynCoDHandler.class,false);
		
		if(!validateInitParameters(2,null,false,synCoDFile,null,null))
		{
			success = false;
			m_log.error("The initialization parameters given to the SynCoDHandler were not adequate. The SynCoDHandler is not operational.");
		}
		
		if(success)
		{
			m_synCoDFile = synCoDFile;
			m_synCoD = SynCoDFileHandler.loadSynCoDFile(synCoDFile);
			m_inflateMode = inflatedMode;
			
			StringBuilder errMsg = new StringBuilder();
			if(!validate(errMsg, false))
			{
				success = false;
				m_log.error("The SynCoDHandler is not operational. The provided SynCoD is not valid." + errMsg.toString());
			}

			if(success)
			{
				if(m_inflateMode)
				{
					if(!inflateOrDeflate(true))
					{
						success = false;
						m_inflateMode = false;
						m_log.error("The inflation of the SynCoD was not successfull");
					}
				}
			}
		}
	}
	
	/**
	 * The synCoDID must be universally unique
	 * 
	 * @param synCoDID
	 * @param synCoDTye
	 * @param logDirPath
	 * @param ownerUserID
	 */
	public SynCoDHandler(String synCoDID, String synCoDTye, String ownerUserID, boolean inflatedMode, ILoggingManager loggingManager)
	{
		boolean success = true;

		m_loggingManager = loggingManager;
		m_log = m_loggingManager.getLogger(SynCoDHandler.class,false);
		
		if(!validateInitParameters(3,null,false,null,synCoDTye,ownerUserID))
		{
			success = false;
			m_log.error("The initialization parameters given to the SynCoDHandler were not adequate. The SynCoDHandler is not operational.");
		}

		if(success)
		{
			if(synCoDID==null){synCoDID=generateNewSynCoDID(ownerUserID);}

			m_synCoD = buildBaseSynCoDStructure(synCoDID,synCoDTye,ownerUserID);
			m_inflateMode = inflatedMode;

			if(m_inflateMode)
			{
				if(!inflateOrDeflate(true))
				{
					success = false;
					m_inflateMode = false;
					m_log.error("The inflation of the SynCoD was not successfull");
				}
			}
		}

	}

	public boolean validate(StringBuilder errMsg, boolean finalValid)
	{
		boolean success = true;
		
		//validate all Statements (id, novelty and CogGraph)
		ArrayList<Statement> statements = getStatements(null, false, null, null, null, null, null);
		for (int i = 0; i < statements.size(); i++)
		{
			Statement statement = statements.get(i);
			success = success && validateStatement(statement,finalValid,errMsg);
		}
				
		//validate all CogNode parameters
		ArrayList<CogNode> cogNodes = getCognitionNodes(null, null, null, null, null, null, null, null, null);
		for (int i = 0; i < cogNodes.size(); i++)
		{
			CogNode cogNode = cogNodes.get(i);
			//System.out.println("Validating CogNode " + cogNode.getId());
			if(!validateCogNode(cogNode,finalValid,errMsg)){success = false;}
		}
			
		//validate the root data of all CogGraphs
		ArrayList<CogGraph> cogGraphs = getCogGraphs(null, null, null);
		for (int i = 0; i < cogGraphs.size(); i++)
		{
			CogGraph cogGraph = cogGraphs.get(i);
			if(!validateCogGraph(cogGraph,finalValid,errMsg)){success = false;}
		}
		
		return success;
	}
	
	private boolean validateStatement(Statement statement, boolean finalValid, StringBuilder validationResults)
	{
		boolean success = true;
		
		validationResults.append("\nValidation results for Statement " + statement.getId() + "\n");
		
		if(statement.isNew())
		{
			String localErrorMsg = "Its a new Statement but: \n";
			if(!validateSynCoDID(GraphNodeType.STATEMENT, null, statement.getId(), m_synCoD.getParentSynCoRID()))
			{
				success = false;
				localErrorMsg = localErrorMsg + "- its ID is not valid\n";
			}
			if(!statement.getStatingUser().equals(m_synCoD.getCreatorUser()))
			{
				success = false;
				localErrorMsg = localErrorMsg + "- its creator user is not the same as that of the current SynCoR.\n";
			}
			if(!statement.getOriginalSynCoR().equals(m_synCoD.getParentSynCoRID()))
			{
				success = false;
				localErrorMsg = localErrorMsg + "- its indicated originalSynCoR is not the current one.\n";
			}
			if(finalValid)
			{
				if(statement.getCogGraph()==null)
				{
					success = false;
					localErrorMsg = localErrorMsg + "- no CogGraph is indicated.\n";
				}
				else if(getCogGraphs(statement.getCogGraph(), null, null).size()<1)
				{
					success = false;
					localErrorMsg = localErrorMsg + "- its indicated CogGraph was not found in the current SynCoD.\n";	
				}
			}
			if(!success){validationResults.append(localErrorMsg);}
		}
		else
		{
			String localErrorMsg = "Its not a new Statement but: \n";
			if(!validateSynCoDID(GraphNodeType.STATEMENT,null,statement.getId(),statement.getOriginalSynCoR()))
			{
				success = false;
				localErrorMsg = localErrorMsg + "- its id is not corrrectly formed.\n";
			}
	
			if(statement.getCogGraph()!=null)
			{
				ArrayList<CogGraph> cogGraphs = getCogGraphs(statement.getCogGraph(), null, null);
				if(cogGraphs.size()==1 && cogGraphs.get(0).isNew())
				{
					success = false;
					localErrorMsg = localErrorMsg + "- its CogGraph is new\n";
				}
			}
			
			if(finalValid)
			{
				if(statement.getCogGraph()==null)
				{
					success = false;
					localErrorMsg = localErrorMsg + "- no CogGraph is indicated.\n";
				}
				else if(getCogGraphs(statement.getCogGraph(), null, null).size()<1)
				{
					success = false;
					localErrorMsg = localErrorMsg + "- its indicated CogGraph was not found in the current SynCoD.\n";	
				}
			}
			if(!success){validationResults.append(localErrorMsg);}
		}
	
		
		if(statement.getCogGraph()!=null)
		{
			String localErrorMsg = "";
			if(statement.isCogGraphParent() && !statement.getCogGraph().contains(statement.getId()))
			{
				success = false;
				localErrorMsg = localErrorMsg + "- it declares istelf to be the parent of CogGraph " + statement.getCogGraph() + " but its IDs do not match.\n";			
			}
			else if(!statement.isCogGraphParent() && statement.getCogGraph().contains(statement.getId()))
			{
				success = false;
				localErrorMsg = localErrorMsg + "- it declares istelf not to be the parent of CogGraph " + statement.getCogGraph() + " but its IDs match.\n";		
			}
			
			ArrayList<Statement> stmts = getStatements(null, null, statement.getStatingUser(), null, null,null, statement.getCogGraph());
			if(!(stmts.size()<1 || (stmts.size()==1 && stmts.get(0).equals(statement))))
			{
				success = false;
				localErrorMsg = localErrorMsg + "- another Statement by the same user already states the same CogGraph.\n";
			}
			if(!success)
			{
				validationResults.append(localErrorMsg);
			}
		}
		
		if(!success){validationResults.append("It is NOT a Valid Statement!\n");}
		else{validationResults.append("It is a Valid Statement!\n");}
		
		return success;
	}

	private boolean validateCogNode(CogNode cogNode, boolean finalValid, StringBuilder validationResults)
	{
		boolean success = true;
		
		validationResults.append("\nValidation results for CogNode " + cogNode.getId() + "\n");

		if(!cogNode.isNew())
		{
			String localErrMsg = "Its not a new CogNode but: \n";
			if(!validateSynCoDID(GraphNodeType.COG_NODE,cogNode.getCogType(),cogNode.getId(),cogNode.getOriginalSynCoR()))
			{
				success = false;
				localErrMsg = localErrMsg + "- its id is not correctly formed.\n";
			}
	
			ArrayList<CogNode> cogNodes = getCognitionNodes(cogNode.getId(), null,cogNode.getCogType(), cogNode.getCogSubType(), null, null, null, null, null);
			if(!(cogNodes.size()<1 || (cogNodes.size()==1 && cogNodes.get(0).equals(cogNode))))
			{
				success = false;
				localErrMsg = localErrMsg + "- another CogNode with the same ID already exists.\n";
			}
			if(!success){validationResults.append(localErrMsg);}
		}
		else
		{
			String localErrMsg = "Its a new CogNode but: \n";
			if(cogNode.getOriginalSynCoR()!=null && !cogNode.getOriginalSynCoR().equals(m_synCoD.getParentSynCoRID()))
			{
				success = false;
				localErrMsg = localErrMsg + "- its indicated originalSynCoR is not the current one.\n";
			}
			if(!success){validationResults.append(localErrMsg);}
		}
	
		if(!success){validationResults.append("It is NOT a Valid CogNode!\n");}
		else{validationResults.append("It is a Valid CogNode!\n");}
		
		return success;
	}

	private boolean validateCogGraph(CogGraph cogGraph, boolean finalValid, StringBuilder validationResults)
	{
		boolean success = true;
		
		validationResults.append("\nValidation results for CogGraph " + cogGraph.getId() + "\n");
		
		if(!cogGraph.isNew())
		{
			String localErrMsg = "Its not a new CogNode but: \n";
			Statement parentStatement = getCogGraphParentStatement(cogGraph.getId());
			if(parentStatement!=null && !validateSynCoDID(GraphNodeType.COG_GRAPH,null,cogGraph.getId(),parentStatement.getId()))
			{
				success = false;
				localErrMsg = localErrMsg + "- its id is not corrrectly formed.\n";
			}

			ArrayList<CogGraph> cogGs = getCogGraphs(cogGraph.getId(), null, null);
			if(!(cogGs.size()<1 || (cogGs.size()==1 && cogGs.get(0).equals(cogGraph))))
			{
				success = false;
				localErrMsg = localErrMsg + "- another CogGraph with the same ID already exists.\n";
			}
			if(!success){validationResults.append(localErrMsg);}
		}
		else if(cogGraph.isNew())
		{
			String localErrMsg = "Its a new CogNode but: \n";
			
			//if this is a new Graph so must its Statement be a new one.
			Statement parentStatement = getCogGraphParentStatement(cogGraph.getId());
			if(parentStatement!=null)
			{
				if(!parentStatement.isNew())
				{
					success = false;
					localErrMsg = localErrMsg + "- it is indicated original Statement is not declared as new.\n";
				}
				if(!m_synCoD.getParentSynCoRID().equals(parentStatement.getOriginalSynCoR()))
				{
					success = false;
					localErrMsg = localErrMsg + "- its original Statement does no have the current SynCoR as its original one.\n";
				}
			}
			if(!success){validationResults.append(localErrMsg);}
		}
		
		if(finalValid)
		{
			if(cogGraph.isNew())
			{
				String localErrMsg = "";
				Statement parentStatement = getCogGraphParentStatement(cogGraph.getId());
				if(parentStatement==null)
				{
					success = false;
					localErrMsg = localErrMsg + " its original Statement was not found within the current SynCoD.";				
				}
				else
				{
					if(!parentStatement.isNew())
					{
						success = false;
						localErrMsg = localErrMsg + " it is indicated original Statement is not declared as new.";
					}
					if(!m_synCoD.getParentSynCoRID().equals(parentStatement.getOriginalSynCoR()))
					{
						success = false;
						localErrMsg = localErrMsg + " its original Statement does no have the current SynCoR as its original one.";
					}
				}
				if(!success){validationResults.append(localErrMsg);}
			}

			//Validate the inner structure of the CogGraph
			List<GraphNodule> gNodules = cogGraph.getGraphConception().getGraphOntConception().getGraphNodule();
			for (int i = 0; i < gNodules.size(); i++)
			{
				GraphNodule graphNodule = gNodules.get(i);
				if(!validateGraphNodule(cogGraph,graphNodule,finalValid, validationResults)){success = false;}
			}
			
			gNodules = cogGraph.getGraphConception().getGraphInstConception().getGraphNodule();
			for (int i = 0; i < gNodules.size(); i++)
			{
				GraphNodule graphNodule = gNodules.get(i);
				if(!validateGraphNodule(cogGraph,graphNodule,finalValid, validationResults)){success = false;}
			}
			
			gNodules = cogGraph.getGraphPerception().getGraphNodule();
			for (int i = 0; i < gNodules.size(); i++)
			{
				GraphNodule graphNodule = gNodules.get(i);
				if(!validateGraphNodule(cogGraph,graphNodule,finalValid, validationResults)){success = false;}
			}
			
			gNodules = cogGraph.getGraphSensation().getGraphNodule();
			for (int i = 0; i < gNodules.size(); i++)
			{
				GraphNodule graphNodule = gNodules.get(i);
				if(!validateGraphNodule(cogGraph,graphNodule,finalValid, validationResults)){success = false;}
			}
			
			//TODO validate the connectivity of the entire structure
		}
		if(!success){validationResults.append("It is NOT a Valid CogGraph!\n");}
		else{validationResults.append("It is a Valid CogGraph!\n");}
		
		return success;
	}

	private boolean validateGraphNodule(CogGraph cogGraph, GraphNodule graphNodule, boolean finalValid,StringBuilder validationResults)
	{
		boolean success = true;
		
		validationResults.append("\nValidation results for GraphNodule " + cogGraph.getId() + ":" + graphNodule.getId() + "\n");
	
		if(!graphNodule.isNew())
		{
			String localErrMsg = "Its not a new GraphNodule but: \n";
			if(!validateSynCoDID(GraphNodeType.GRAPH_NODULE,null,graphNodule.getId(),cogGraph.getId()))
			{
				success = false;
				localErrMsg = localErrMsg + "- its id is not corrrectly formed.\n";
			}
			if(cogGraph.isNew())
			{
				success = false;
				localErrMsg = localErrMsg + "- but its indicated CogGraph is new.\n";
			}
			if(!success){validationResults.append(localErrMsg);}
		}
		else
		{
			String localErrMsg = "Its a new GraphNodule but: \n";
			if(!cogGraph.isNew())
			{
				success = false;
				localErrMsg = localErrMsg + "- but its indicated CogGraph is not new.\n";
			}
			if(!graphNodule.getGraphID().equals(graphNodule.getOrigGraphID()))
			{
				success = false;
				localErrMsg = localErrMsg + "- its indicated originalGraph is not the graph the GraphNodule is to be added to.\n";
			}
			if(!success){validationResults.append(localErrMsg);}
		}

		CogNType cNType = null;
		if(graphNodule.getCogNodeRef()!=null){cNType=graphNodule.getCogNodeRef().getTargetCNType();}

		ArrayList<GraphNodule> gNs = getGraphNodules(graphNodule.getId(), null, cogGraph, graphNodule.getOrigGraphID(),cNType , null); 
		if(!(gNs.size()<1 || (gNs.size()==1 && gNs.get(0).equals(graphNodule))))
		{
			success = false;
			validationResults.append("Another GraphNodule with the same ID already exists.");
		}
		
		if(finalValid)
		{
			if(!validateCogNode2GraphNoduleLink(cogGraph, graphNodule, validationResults)){success = false;}
			if(!validateInterGraphNoduleLinks(cogGraph, graphNodule, validationResults)){success = false;}
		}
		if(!success){validationResults.append("It is NOT a Valid GraphNodule!\n");}
		else{validationResults.append("It is a Valid GraphNodule!\n");}
		return success;
	}

	private boolean validateCogNode2GraphNoduleLink(CogGraph cogGraph, GraphNodule graphNodule, StringBuilder validationResults)
	{
		boolean success = true;
		
		CogNode cogNode = getCognitionNodes(graphNodule.getCogNodeRef().getTargetCNID(), null, graphNodule.getCogNodeRef().getTargetCNType(), null, null, null, null, null, null).get(0);
		
		boolean found = false;
		List<GraphNoduleRef> graphNoduleRefs = cogNode.getGraphNoduleRef();
		for (int i = 0; i < graphNoduleRefs.size(); i++)
		{
			GraphNoduleRef graphNoduleRef = graphNoduleRefs.get(i);
			if(graphNoduleRef.getTargetGraphNoduleID().equals(graphNodule.getId()))
			{
				found = true;
				break;
			}
		}
		success = found;
		
		if(!success){validationResults.append("GraphNodule " + graphNodule.getId() + " is NOT Valid. It references CogNode " + cogNode.getId() +  " which does not reference back to it!\n");}
		
		return success;
	}
	
	private boolean validateInterGraphNoduleLinks(CogGraph cogGraph, GraphNodule graphNodule, StringBuilder validationResults)
	{
		boolean success = true;
		
		List<InterGraphNoduleRef> interGraphNoduleRefs = null;
		
		if(graphNodule.getVAscendingGraphLinks()!=null)
		{
			interGraphNoduleRefs = graphNodule.getVAscendingGraphLinks().getInterGraphNoduleRef();
			for (int i = 0; i < interGraphNoduleRefs.size(); i++)
			{
				boolean found = false;
				InterGraphNoduleRef interGraphNoduleRef = interGraphNoduleRefs.get(i);
				String targetGNID = interGraphNoduleRef.getTargetGraphNID();
				ArrayList<GraphNodule> gNs = getGraphNodules(targetGNID, null, cogGraph, null, interGraphNoduleRef.getTargetCogNType(), null);
				if(!gNs.isEmpty())
				{
					GraphNodule targetGN = gNs.get(0);
					List<InterGraphNoduleRef> innerInterGNRefs = targetGN.getVDescendingGraphLinks().getInterGraphNoduleRef();
					for (int j = 0; j < innerInterGNRefs.size(); j++)
					{
						InterGraphNoduleRef innerInterGNRef = innerInterGNRefs.get(j);
						if(innerInterGNRef.getTargetGraphNID().equals(graphNodule.getId()) &&  innerInterGNRef.getRelType().equals(interGraphNoduleRef.getRelType()))
						{
							found = true;
							break;
						}
					}
				}
				if(!found)
				{
					success = found;
					validationResults.append("GraphNodule " + graphNodule.getId() + " is NOT Valid. It references GraphNodule " + targetGNID + " by a VAscending link with a " + interGraphNoduleRef.getRelType() + " rel, but it is not refered to, by it.");
				}
			}
		}
		
		if(graphNodule.getVDescendingGraphLinks()!=null)
		{
			interGraphNoduleRefs = graphNodule.getVDescendingGraphLinks().getInterGraphNoduleRef();
			for (int i = 0; i < interGraphNoduleRefs.size(); i++)
			{
				boolean found = false;
				InterGraphNoduleRef interGraphNoduleRef = interGraphNoduleRefs.get(i);
				String targetGNID = interGraphNoduleRef.getTargetGraphNID();
				ArrayList<GraphNodule> gNs = getGraphNodules(targetGNID, null, cogGraph, null, interGraphNoduleRef.getTargetCogNType(), null);
				if(!gNs.isEmpty())
				{
					GraphNodule targetGN = gNs.get(0);
					List<InterGraphNoduleRef> innerInterGNRefs = targetGN.getVAscendingGraphLinks().getInterGraphNoduleRef();
					for (int j = 0; j < innerInterGNRefs.size(); j++)
					{
						InterGraphNoduleRef innerInterGNRef = innerInterGNRefs.get(j);
						if(innerInterGNRef.getTargetGraphNID().equals(graphNodule.getId()) &&  innerInterGNRef.getRelType().equals(interGraphNoduleRef.getRelType()))
						{
							found = true;
							break;
						}
					}
				}
				if(!found)
				{
					success = found;
					validationResults.append("GraphNodule " + graphNodule.getId() + " is NOT Valid. It references GraphNodule " + targetGNID + " by a VDscending link with a " + interGraphNoduleRef.getRelType() + " rel, but it is not refered to, by it.");
				}
			}
		}

		if(graphNodule.getHAscendingGraphLinks()!=null)
		{
			interGraphNoduleRefs = graphNodule.getHAscendingGraphLinks().getInterGraphNoduleRef();
			for (int i = 0; i < interGraphNoduleRefs.size(); i++)
			{
				boolean found = false;
				InterGraphNoduleRef interGraphNoduleRef = interGraphNoduleRefs.get(i);
				String targetGNID = interGraphNoduleRef.getTargetGraphNID();
				ArrayList<GraphNodule> gNs = getGraphNodules(targetGNID, null, cogGraph, null, interGraphNoduleRef.getTargetCogNType(), null);
				if(!gNs.isEmpty())
				{
					GraphNodule targetGN = gNs.get(0);
					List<InterGraphNoduleRef> innerInterGNRefs = targetGN.getHDescendingGraphLinks().getInterGraphNoduleRef();
					for (int j = 0; j < innerInterGNRefs.size(); j++)
					{
						InterGraphNoduleRef innerInterGNRef = innerInterGNRefs.get(j);
						if(innerInterGNRef.getTargetGraphNID().equals(graphNodule.getId()) &&  innerInterGNRef.getRelType().equals(interGraphNoduleRef.getRelType()))
						{
							found = true;
							break;
						}
					}
				}
				if(!found)
				{
					success = found;
					validationResults.append("GraphNodule " + graphNodule.getId() + " is NOT Valid. It references GraphNodule " + targetGNID + " by a HAscending link with a " + interGraphNoduleRef.getRelType() + " rel, but it is not refered to, by it.");
				}
			}
		}

		if(graphNodule.getHDescendingGraphLinks()!=null)
		{
			interGraphNoduleRefs = graphNodule.getHDescendingGraphLinks().getInterGraphNoduleRef();
			for (int i = 0; i < interGraphNoduleRefs.size(); i++)
			{
				boolean found = false;
				InterGraphNoduleRef interGraphNoduleRef = interGraphNoduleRefs.get(i);
				String targetGNID = interGraphNoduleRef.getTargetGraphNID();
				ArrayList<GraphNodule> gNs = getGraphNodules(targetGNID, null, cogGraph, null, interGraphNoduleRef.getTargetCogNType(), null);
				if(!gNs.isEmpty())
				{
					GraphNodule targetGN = gNs.get(0);
					List<InterGraphNoduleRef> innerInterGNRefs = targetGN.getHAscendingGraphLinks().getInterGraphNoduleRef();
					for (int j = 0; j < innerInterGNRefs.size(); j++)
					{
						InterGraphNoduleRef innerInterGNRef = innerInterGNRefs.get(j);
						if(innerInterGNRef.getTargetGraphNID().equals(graphNodule.getId()) &&  innerInterGNRef.getRelType().equals(interGraphNoduleRef.getRelType()))
						{
							found = true;
							break;
						}
					}
				}
				if(!found)
				{
					success = found;
					validationResults.append("GraphNodule " + graphNodule.getId() + " is NOT Valid. It references GraphNodule " + targetGNID + " by a HDescending link with a " + interGraphNoduleRef.getRelType() + " rel, but it is not refered to, by it.");
				}
			}
		}
		return success;
	}



	public SynCoD getSynCoD(){return m_synCoD;}
	
	public String getSynCoD_ID(){return m_synCoD.getParentSynCoRID();}
	
	public String getSynCoDType(){return m_synCoD.getSynCoDType();}

	private String generateNewSynCoDID(String ownerUserID)
	{
		String synCoDID = null;
		
		String rndmID = (System.currentTimeMillis()/(1000)) + RandomnessUtils.buildRandomString(4);
		
		synCoDID = ownerUserID + ":syncor-" + rndmID; 
		
		return synCoDID;
	}

	public Ontological getOntologicalConceptionRoot()
	{
		Ontological ontological = null;
		try
		{
			ontological = m_synCoD.getGraphal().getCognition().getLayers().getConception().getOntological();
		}
		catch (Exception e) 
		{
			m_log.error("Exception while trying to access the root ontological conception node.");
			e.printStackTrace();
		}
		return ontological;
	}
	
	public Instantial getInstantialConceptionRoot()
	{
		Instantial instantial = null;
		try
		{
			instantial = m_synCoD.getGraphal().getCognition().getLayers().getConception().getInstantial();
		}
		catch (Exception e) 
		{
			m_log.error("Esception while trying to access the root instantial conception node.");
			e.printStackTrace();
		}
		return instantial;
	}
	
	public Perception getPerceptionRoot()
	{
		Perception perception = null;
		try
		{
			perception = m_synCoD.getGraphal().getCognition().getLayers().getPerception();
		}
		catch (Exception e) 
		{
			m_log.error("Esception while trying to access the root perception node.");
			e.printStackTrace();
		}
		return perception;
	}
	
	public Sensation getSensationRoot()
	{
		Sensation sensation = null;
		try
		{
			sensation = m_synCoD.getGraphal().getCognition().getLayers().getSensation();
		}
		catch (Exception e) 
		{
			m_log.error("Esception while trying to access the root sensation node.");
			e.printStackTrace();
		}
		return sensation;
	}
	
	public Integration getCognitiveIntegrationRoot()
	{
		Integration integration = null;
		try
		{
			integration = m_synCoD.getGraphal().getCognition().getIntegration();
		}
		catch (Exception e) 
		{
			m_log.error("Esception while trying to access the root cognitive integration node.");
			e.printStackTrace();
		}
		return integration;
	}
	
	
	public Authorships getAuthorshipsRoot()
	{
		Authorships authorships = null;
		try
		{
			authorships = m_synCoD.getGraphal().getAuthorships();
		}
		catch (Exception e) 
		{
			m_log.error("Esception while trying to access the root authorships node.");
			e.printStackTrace();
		}
		return authorships;
	}

	public CogGraph getStatementCogGraph(String statementID)
	{
		CogGraph cogGraph = null;
		Statement statement = getStatements(statementID,null,null,null,null,null,null).get(0);
		String cogGraphID = statement.getCogGraph();
		cogGraph = getCogGraphs(cogGraphID, null, null).get(0);
		return cogGraph;
	}
	
	public ArrayList<Statement> getCogGraphStatements(String cogGraphID)
	{
		ArrayList<Statement> statements = getStatements(null,null,null,null,null,null,cogGraphID);
		return statements;
	}
	
	public Statement getCogGraphParentStatement(String cogGraphID)
	{
		Statement parentStatement = null;
		ArrayList<Statement> statements = getStatements(null,null,null,null,true,null,cogGraphID);
		if(statements.size()==1)
		{
			parentStatement = statements.get(0);
		}
		return parentStatement;
	}
	
	public ArrayList<Statement> getStatements(String statementID, Boolean isNew, String userID, String originalSynCoDID, Boolean isGraphParent, Boolean status, String cogGraphID)
	{
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		Authorships authorships = getAuthorshipsRoot();
		List<Statement> fullStatementsList = authorships.getStatement();
		
		if(statementID==null && isNew ==null && userID ==null && originalSynCoDID==null && status==null && cogGraphID==null)
		{
			statements.addAll(fullStatementsList);
		}
		else
		{
			for (int i = 0; i < fullStatementsList.size(); i++)
			{
				Statement statement = fullStatementsList.get(i);
				if(statementID!=null)
				{
					if(statement.getId().equals(statementID))
					{
						statements.add(statement);
						break;
					}
				}
				else
				{
					if(isNew!=null && !isNew.equals(statement.isNew())){continue;}
					else if(userID!=null && !userID.equals(statement.getStatingUser())){continue;}
					else if(cogGraphID!=null && !cogGraphID.equals(statement.getCogGraph())){continue;}
					else if(originalSynCoDID!=null && !originalSynCoDID.equals(statement.getOriginalSynCoR())){continue;}
					else if(isGraphParent!=null && !isGraphParent.equals(statement.isCogGraphParent())){continue;}
					else if(status!=null && !status.equals(statement.isStatus().equals(status))){continue;}
					statements.add(statement);
				}
			}
		}

		return statements;
	}
	
	public ArrayList<CogGraph> getCogGraphs(String cogGraphID, Boolean isNew, GraphNodeType subType)
	{
		ArrayList<CogGraph> targetCogGraphs = new ArrayList<>();
		
		List<CogGraph> fullCogGraphList = getCognitiveIntegrationRoot().getCogGraph();

		if(cogGraphID==null && isNew==null && subType==null){targetCogGraphs.addAll(fullCogGraphList);}
		else
		{
			for (int i = 0; i < fullCogGraphList.size(); i++)
			{
				CogGraph cogGraph = fullCogGraphList.get(i);
				if(cogGraphID!=null)
				{
					if(cogGraph.getId().equals(cogGraphID))
					{
						targetCogGraphs.add(cogGraph);
						break;
					}
				}
				else
				{
					if(isNew!=null && !cogGraph.isNew().equals(isNew)){continue;}
					else if(subType!=null && !cogGraph.getSubType().equals(subType)){continue;}
					targetCogGraphs.add(cogGraph);
				}
			}
		}
		return targetCogGraphs;
	}
	
	public ArrayList<CogNode> getCognitionNodes(String nodeID, Boolean isNew, CogNType cogNodeType, CogNType cogNodeSubType, String origSynCoRID, String nodeLable, Boolean isAtomic, String graphID, String graphNoduleID)
	{
		 ArrayList<CogNode> matchingCogNodes = new ArrayList<CogNode>();
		 
		 List<CogNode> fullCogNodeList = getCogNodesOfGivenType(cogNodeType);

		 if(nodeID==null && isNew==null && cogNodeType==null && cogNodeSubType==null && origSynCoRID==null && nodeLable==null && isAtomic==null && graphID==null && graphNoduleID==null)
		 {
			 matchingCogNodes.addAll(fullCogNodeList);
		 }
		 else
		 {
			 for (int i = 0; i < fullCogNodeList.size(); i++)
			 {
				 CogNode cogNode = fullCogNodeList.get(i);
				 if(nodeID!=null)
				 {
					if(cogNode.getId().equals(nodeID))
					{
						matchingCogNodes.add(cogNode);
						break;
					}
				 }
				 else
				 {
					if(isNew!=null && !cogNode.isNew().equals(isNew)){continue;}
					else if(cogNodeType!=null && !cogNode.getCogType().equals(cogNodeType)){continue;}
					else if(cogNodeSubType!=null && !cogNode.getCogSubType().equals(cogNodeSubType)){continue;}
					else if(origSynCoRID!=null && !cogNode.getOriginalSynCoR().equals(origSynCoRID)){continue;}
					else if(isAtomic!=null && !cogNode.isIsAtomic().equals(isAtomic)){continue;}
					
					if(graphID!=null || graphNoduleID!=null)
					{
						List<GraphNoduleRef> graphNoduleRefs = cogNode.getGraphNoduleRef();
						for (int j = 0; j < graphNoduleRefs.size(); j++)
						{
							GraphNoduleRef graphNoduleRef = graphNoduleRefs.get(j);
							if(graphNoduleID!=null)
							{
								if(graphNoduleRef.getTargetGraphNoduleID().equals(graphNoduleID))
								{
									matchingCogNodes.add(cogNode);
									break;
								}
							}
							else
							{
								if(graphID!=null && !graphNoduleRef.getGraphID().equals(graphID)){continue;}
								matchingCogNodes.add(cogNode);
							}
						}
					}
					else{matchingCogNodes.add(cogNode);}
				 }
			 }
		 }

		 return matchingCogNodes;
	}
	
	/**
	 * Will get the GraphNoduls through the integration. This is safer as it will work even if the graph is not yet inflated
	 * 
	 * @param cogGraph_or_ID
	 * @param comprisingCogNodeType
	 * @param graphNoduleID
	 * @return
	 */
	public ArrayList<GraphNodule> getGraphNodules(String graphNoduleID, Boolean isNew, Object cogGraph_or_ID, String origGraphID, CogNType comprisingCogNodeType, String comprisingCogNodeID)
	{
		ArrayList<GraphNodule> graphNodules = new ArrayList<GraphNodule>();
				
		ArrayList<CogGraph> cogGraphs = new ArrayList<CogGraph>();
		if(cogGraph_or_ID!=null)
		{
			if(cogGraph_or_ID instanceof CogGraph){cogGraphs.add((CogGraph)cogGraph_or_ID);}
			else
			{
				CogGraph cogGraph = getCogGraphs((String) cogGraph_or_ID,null,null).get(0);
				cogGraphs.add(cogGraph);
			}
		}
		else{cogGraphs.addAll(getCogGraphs(null, null, null));}

		for (int i = 0; i < cogGraphs.size(); i++)
		{
			CogGraph cogGraph = cogGraphs.get(i);
			
			List<GraphNodule> ontConcGraphNodulesList = cogGraph.getGraphConception().getGraphOntConception().getGraphNodule();
			List<GraphNodule> instConcGraphNodulesList = cogGraph.getGraphConception().getGraphInstConception().getGraphNodule();
			List<GraphNodule> perceptGraphNodulesList = cogGraph.getGraphPerception().getGraphNodule();
			List<GraphNodule> sensGraphNodulesList = cogGraph.getGraphSensation().getGraphNodule();
			
			ArrayList<GraphNodule> relevantGraphNodules = new ArrayList<GraphNodule>();
			
			if(comprisingCogNodeType==null)
			{
				relevantGraphNodules.addAll(ontConcGraphNodulesList);
				relevantGraphNodules.addAll(instConcGraphNodulesList);
				relevantGraphNodules.addAll(perceptGraphNodulesList);
				relevantGraphNodules.addAll(sensGraphNodulesList);
			}
			else if(CogNType.CONCEPT.equals(comprisingCogNodeType)){relevantGraphNodules.addAll(ontConcGraphNodulesList);}
			else if(CogNType.CONCEPTIVE_INSTANCE.equals(comprisingCogNodeType)){relevantGraphNodules.addAll(instConcGraphNodulesList);}
			else if(CogNType.PERCEPTIVE_INSTANCE.equals(comprisingCogNodeType)){relevantGraphNodules.addAll(perceptGraphNodulesList);}
			else if(CogNType.SENS_REC.equals(comprisingCogNodeType)){relevantGraphNodules.addAll(sensGraphNodulesList);}
			
			if(graphNoduleID==null && isNew==null && origGraphID==null && comprisingCogNodeID==null){graphNodules.addAll(relevantGraphNodules);}
			else
			{
				for (int j = 0; j < relevantGraphNodules.size(); j++)
				{
					GraphNodule graphNodule =  relevantGraphNodules.get(j);
					if(graphNoduleID!=null)
					{
						if(graphNodule.getId().equals(graphNoduleID))
						{
							graphNodules.add(graphNodule);
							break;
						}
					}
					else
					{
						if(isNew!=null && !graphNodule.isNew().equals(isNew)){continue;}
						else if(origGraphID!=null && !graphNodule.getOrigGraphID().equals(origGraphID)){continue;}
						else if(comprisingCogNodeID!=null && !graphNodule.getCogNodeRef().getTargetCNID().equals(comprisingCogNodeID)){continue;}
						graphNodules.add(graphNodule);
						if(cogGraph_or_ID!=null && comprisingCogNodeID!=null){break;}
					}
				}
			}	
		}
		return graphNodules;
	}

	public boolean bindStatement2CogGraph(Statement statement, CogGraph cogGraph)
	{
		boolean success = true;
		if(statement.isNew()!=cogGraph.isNew())
		{
			success = false;
			m_log.error("The given Statement (" + statement.getId() + ") was not connected to CogGraph " + cogGraph.getId() + ". Both must be either new or not new.");
		}
		else if(statement.isNew() && !cogGraph.getId().startsWith(statement.getId()))
		{
			success = false;
			m_log.error("The given Statement (" + statement.getId() + ") was not connected to CogGraph " + cogGraph.getId() + ". They are new but their IDs do not match.");			
		}
		
		if(cogGraph.getId()!=null && getStatements(null, null, statement.getStatingUser(), null, null,null, cogGraph.getId()).size()>0)
		{
			success = false;
			m_log.error("The given Statement (" + statement.getId() + ") was not bound to the indicated CogGraph as another Statement by the same user already states the same CogGraph " + cogGraph.getId() + ".");
		}
		
		if(success){statement.setCogGraph(cogGraph.getId());}
		
		return success;
	}
	
	public boolean reendorseStatement(String statementID)
	{
		boolean success = true;
		
		Statement statement = getStatements(statementID, null, null, null,null, null, null).get(0);
		if(!statement.getStatingUser().equals(m_synCoD.getCreatorUser()))
		{
			success = false;
			m_log.error("The given Statement (" + statementID + ") cannot be reendorsed as it does not belong to the current user");
		}
		else
		{
			if(statement.isStatus())
			{
				m_log.info("The given Statement (" + statementID + ") is allready under endorsement by the current user");
			}
			else
			{
				statement.setStatus(true);
				statement.setAltered(true);
			}
		}
		return success;
	}
	
	public boolean unendorseStatement(String statementID)
	{
		boolean success = true;
		
		Statement statement = getStatements(statementID, null, null, null, null, null, null).get(0);
		success = unendorseStatement(statement);
		return success;
	}
	
	public boolean unendorseStatement(Statement statement)
	{
		boolean success = true;
	
		if(!statement.getStatingUser().equals(m_synCoD.getCreatorUser()))
		{
			success = false;
			m_log.error("The given Statement (" + statement.getId() + ") cannot be unendorsed as it does not belong to the current user");
		}
		else
		{
			if(!statement.isStatus())
			{
				m_log.info("The given Statement (" + statement.getId() + ") has allready been unendorsement by the current user");
			}
			else
			{
				statement.setStatus(false);
				statement.setAltered(true);
			}
		}
		return success;
	}
	
	
	public Statement addStatement(String statementID, boolean isNew, String statingUser, String origSynCoRID, Boolean status, Boolean isGraphParent, String cogGraphID)
	{
		Statement statement = null;
		boolean success = true;
		
		if(!isNew)
		{
			if(statementID==null)
			{
				success = false;
				m_log.error("The given Statement (" + statementID + ") was not added. It is not new but no ID is provided.");
			}
			if(statingUser==null)
			{
				success = false;
				m_log.error("The given Statement (" + statementID + ") was not added. It is not new but no creator User is provided.");				
			}
			if(origSynCoRID==null)
			{
				success = false;
				m_log.error("The given Statement (" + statementID + ") was not added. It is not new but no original SynCoR is provided.");
			}
		}
		else if(isNew)
		{
			if(statementID!=null)
			{
				success = false;
				m_log.error("The given Statement (" + statementID + ") was not added. It is new but an ID was provided.");
			}
			else{statementID=generateSynCoDID(GraphNodeType.STATEMENT, null, m_synCoD.getParentSynCoRID());}	
			
			if(statingUser==null){statingUser=m_synCoD.getCreatorUser();}
			if(origSynCoRID==null){origSynCoRID=m_synCoD.getParentSynCoRID();}
			if(status==null){status=true;}
		}
		
		if(success)
		{
			statement = new Statement();
			statement.setId(statementID);
			statement.setNew(isNew);
			statement.setStatingUser(statingUser);
			statement.setOriginalSynCoR(origSynCoRID);
			statement.setCogGraphParent(isGraphParent);
			statement.setStatus(status);
			statement.setCogGraph(cogGraphID);

			StringBuilder errMsg = new StringBuilder();
			if(!validateStatement(statement, false, errMsg))
			{
				success = false;
				m_log.error("The given Statement (" + statementID + ") was not added." + errMsg.toString());
			}
			else
			{
				success = getAuthorshipsRoot().getStatement().add(statement);
				if(!success){statement=null;}				
			}			
		}

		return statement;
	}

	public CogGraph addCogGraph(String cogGraphID, Boolean isNew, GraphNodeType subType, String originalStatementID)
	{
		//before a CogGraph is added, if it is a new one (and thus its original Statement must be located within the current SynCoR)
		//its original Statement must be added. At the time of addition of that statement the CogGraph's ID will not be available yet.
		//The procedure is: add the statement with null for the value of the CogGraph; add the CogGraph; bind the Statement to the CogGraph.
		CogGraph cogGraph = null;
		
		boolean success = true;
		
		if(!isNew)
		{
			if(cogGraphID==null)
			{
				success = false;
				m_log.error("The given CogGraph (" + cogGraphID + ") was not added. It is not new but no ID is provided.");
			}
		}
		else if(isNew)
		{
			if(cogGraphID!=null)
			{
				success = false;
				m_log.error("The given CogGraph (" + cogGraphID + ") was not added. It is new but an ID was provided.");
			}
			else{cogGraphID=generateSynCoDID(GraphNodeType.COG_GRAPH, null, originalStatementID);}			
		}

		cogGraph = null;
		if(success)
		{
			//build CogGraph
			cogGraph = new CogGraph();
			cogGraph.setId(cogGraphID);
			cogGraph.setNew(isNew);
			cogGraph.setSubType(subType);
			
			//add CogGraph default inner structure
			GraphConception gConcept = new GraphConception();
			GraphOntConception gOntConception = new GraphOntConception(); 
			GraphInstConception gInstConception = new GraphInstConception();
			gConcept.setGraphOntConception(gOntConception);
			gConcept.setGraphInstConception(gInstConception);
			cogGraph.setGraphConception(gConcept);
			GraphPerception gPerception = new GraphPerception();
			GraphSensation gSensation = new GraphSensation();
			cogGraph.setGraphPerception(gPerception);
			cogGraph.setGraphSensation(gSensation);
			
			StringBuilder errMsg = new StringBuilder();
			if(!validateCogGraph(cogGraph, false, errMsg))
			{
				success = false;
				m_log.error("The given CogGraph (" + cogGraph.getId() + ") was not added.\n" + errMsg.toString());
			}
			else
			{
				//insert CogGraph into the Cognitive Integration Area
				success = getCognitiveIntegrationRoot().getCogGraph().add(cogGraph);
			}
			
			if(!success){cogGraph=null;}
		}
				
		return cogGraph;
	}

	public CogNode addCogNode(String cogNodeID, boolean isNew, CogNType cogNodeType, CogNType cogNodeSubType, String origSynCoRID, String nodeLable, boolean isAtomic, HashMap<String,String> payload)
	{
		CogNode cogNode = null;
		
		boolean success = true;
		
		if(!isNew)
		{
			if(cogNodeID==null)
			{
				success = false;
				m_log.error("The given CogNode (" + cogNodeID + ") was not added. It is not new but no ID as provided.");
			}
		}
		else
		{
			if(cogNodeID!=null)
			{
				success = false;
				m_log.error("The given CogNode (" + cogNodeID + ") was not added. It is new but an ID was provided.");
			}
			else{cogNodeID=generateSynCoDID(GraphNodeType.COG_NODE, cogNodeType, m_synCoD.getParentSynCoRID());}

			if(origSynCoRID==null){origSynCoRID=m_synCoD.getParentSynCoRID();}
		}
		
		if(success)
		{
			//build CogNode
			cogNode = new CogNode();
			cogNode.setId(cogNodeID);
			cogNode.setNew(isNew);
			cogNode.setCogType(cogNodeType);
			cogNode.setCogSubType(cogNodeSubType);
			cogNode.setOriginalSynCoR(origSynCoRID);
			cogNode.setNodeLabel(nodeLable);
			cogNode.setIsAtomic(isAtomic);
			
			//build CogNode payload
			addPayloadElementsToCogNode(cogNode,payload);
			
			StringBuilder errMsg = new StringBuilder();
			if(!validateCogNode(cogNode, false, errMsg))
			{
				success = false;
				m_log.error("The given CogNode (" + cogNode.getId() + ") was not added." + errMsg.toString());
			}
			else
			{
				//insert CogNode into the Cognition Layers area
				if(CogNType.CONCEPT.equals(cogNode.getCogType()))
				{
					success = getOntologicalConceptionRoot().getCogNode().add(cogNode);
				}
				else if(CogNType.CONCEPTIVE_INSTANCE.equals(cogNode.getCogType()))
				{
					success = getInstantialConceptionRoot().getCogNode().add(cogNode);
				}
				else if(CogNType.PERCEPTIVE_INSTANCE.equals(cogNode.getCogType()))
				{
					success = getPerceptionRoot().getCogNode().add(cogNode);
				}
				else if(CogNType.SENS_REC.equals(cogNode.getCogType()))
				{
					success = getSensationRoot().getCogNode().add(cogNode);
				}
				
				if(!success){cogNode = null;}
			}
		}
		return cogNode;
	}

	public GraphNodule addGraphNodule(String graphNoduleID, boolean isNew, CogGraph cogGraph, String originalGraphID, CogNType associatedCogNodeType)
	{
		boolean success = true;
		
		GraphNodule graphNodule = null;
		
		if(graphNoduleID==null){graphNoduleID=generateSynCoDID(GraphNodeType.GRAPH_NODULE,null, cogGraph.getId());}

		//build GraphNodule
		graphNodule = new GraphNodule();
		graphNodule.setId(generateSynCoDID(GraphNodeType.GRAPH_NODULE,null,cogGraph.getId()));
		graphNodule.setNew(isNew);
		graphNodule.setGraphID(cogGraph.getId());
		graphNodule.setOrigGraphID(originalGraphID);
		
		StringBuilder errMsg = new StringBuilder();
		if(!validateGraphNodule(cogGraph, graphNodule, false, errMsg))
		{
			success = false;
			m_log.error("The given GraphNodule (" + graphNodule.getId() + ") presents vaidity errors." + errMsg.toString());
		}
		else
		{
			if(CogNType.CONCEPT.equals(associatedCogNodeType))
			{
				success = cogGraph.getGraphConception().getGraphOntConception().getGraphNodule().add(graphNodule);
			}
			else if(CogNType.CONCEPTIVE_INSTANCE.equals(associatedCogNodeType))
			{
				success = cogGraph.getGraphConception().getGraphInstConception().getGraphNodule().add(graphNodule);
			}
			else if(CogNType.PERCEPTIVE_INSTANCE.equals(associatedCogNodeType))
			{
				success = cogGraph.getGraphPerception().getGraphNodule().add(graphNodule);
			}
			else if(CogNType.SENS_REC.equals(associatedCogNodeType))
			{
				success = cogGraph.getGraphSensation().getGraphNodule().add(graphNodule);
			}
		}

		if(!success)
		{
			m_log.error("The given GraphNodule (" + graphNodule.getId() + ") was not added.");
			graphNodule = null;
		}
		
		return graphNodule;
	}

	public GraphNodule linkCogNodeToCogGraph(CogGraph cogGraph, CogNode cogNode, GraphNodule graphNodule)
	{
		boolean success = true;
		
		//if graphNodule is null it is assumed that this is a new Graph
		//if it is not a new graph than a non null GraphNodule needs to be delivered
		//build GraphNodule and add it to the CogGraph in the right place
		if(graphNodule==null){graphNodule = addGraphNodule(null, cogNode.isNew(), cogGraph, cogGraph.getId(), cogNode.getCogType());}

		//link the GraphNodule to the CogNode and viceversa
		success = handleCogNode2GraphNoduleLink(cogNode,graphNodule,ADD_OP);
		
		if(success && m_inflateMode){success = handleCogNode2GraphNoduleLink(cogNode,graphNodule,LINK_OP);}
		
		if(!success){m_log.error("CogNode " + cogNode.getId() + " could not be added to CogGraph " + cogGraph.getId() + ".");}
		
		return graphNodule;
	}

	public GraphNodule unlinkCogNodeFromCogGraph(CogGraph cogGraph, CogNode cogNode, GraphNodule graphNodule)
	{
		boolean success = true;

		//unlink the GraphNodule from the CogNode and viceversa
		if(m_inflateMode){success = handleCogNode2GraphNoduleLink(cogNode,graphNodule,LINK_OP);}
		
		success = handleCogNode2GraphNoduleLink(cogNode,graphNodule,REMOVE_OP);

		if(!success){m_log.error("CogNode " + cogNode.getId() + " could not be removed from CogGraph " + cogGraph.getId() + ".");}
		
		return graphNodule;
	}
	
	/**
	 * This method may only be invoked after the CogNodes have been added to SynCoD and their corresponding GraphNodules have been added to them and the Graph
	 * 
	 * @param cogGraph
	 * @param cogNodeA
	 * @param cogNodeB
	 * @param origGraphID
	 * @param relType
	 * @param isNew
	 * @param vertical
	 * @return
	 */
	public boolean linkCogNodesInCogGraph(CogGraph cogGraph, CogNode cogNodeA, CogNode cogNodeB, String origGraphID, InterCogNodeRelType relType, boolean isNew, boolean vertical)
	{
		//the type of InterCogNodeRelType is sufficient to determine if it is vertical or not
		boolean success = true;
		
		//for this operation to be performed both of the CogNodes need to have been connected to the CogGraph before
		//i.e. both the CogNodes must have a GraphNodule of the CogGraph in scope inside
		
		GraphNodule graphNoduleA = getGraphNodules(null, null, cogGraph, origGraphID, cogNodeA.getCogType(), cogNodeA.getId()).get(0);
		GraphNodule graphNoduleB = getGraphNodules(null, null, cogGraph, origGraphID, cogNodeB.getCogType(), cogNodeB.getId()).get(0);
		
		//link the GraphNodules to one another
		success = handleInterGraphNoduleLink(graphNoduleA,graphNoduleB,vertical,ADD_OP,origGraphID,relType,isNew);

		if(success && m_inflateMode){success = handleInterGraphNoduleLink(graphNoduleA,graphNoduleB,vertical,LINK_OP,origGraphID,relType,isNew);}
		
		if(!success){m_log.error("CogNode " + cogNodeA.getId() + " could not be linked to CogNode " + cogNodeB.getId() + " in CogGraph " + cogGraph.getId() + ". ");}
		
		return success;
	}

	public boolean unlinkCogNodesInCogGraph(CogGraph cogGraph, CogNode cogNodeA, CogNode cogNodeB, String origGraphID, InterCogNodeRelType relType, boolean isNew, boolean vertical)
	{
		boolean success = true;
		
		//for this operation to be performed both of the CogNodes need to have been connected to the CogGraph before
		//i.e. both the CogNodes must have a GraphNodule of the CogGraph in scope inside
		
		GraphNodule graphNoduleA = getGraphNodules(null, null, cogGraph, origGraphID, cogNodeA.getCogType(), cogNodeA.getId()).get(0);
		GraphNodule graphNoduleB = getGraphNodules(null, null, cogGraph, origGraphID, cogNodeB.getCogType(), cogNodeB.getId()).get(0);
		
		//link the GraphNodules to one another
		if(m_inflateMode){success = handleInterGraphNoduleLink(graphNoduleA,graphNoduleB,vertical,UNLINK_OP,origGraphID,relType,isNew);}
		if(success){success = handleInterGraphNoduleLink(graphNoduleA,graphNoduleB,vertical,REMOVE_OP,origGraphID,relType,isNew);}

		if(!success){m_log.error("CogNode " + cogNodeA.getId() + " could not be unlinked from CogNode " + cogNodeB.getId() + " in CogGraph " + cogGraph.getId() + ". ");}
		
		return success;
	}
	
	public boolean removeStatement(String statementID)
	{
		boolean succes = true;
		Statement statement = getStatements(statementID, null, null, null, null, null, null).get(0);
		if(statement!=null){succes = removeStatement(statement);}
		else
		{
			m_log.error("No Statement was found with id " + statementID + ".");
			succes=false;
		}
		return succes;
	}
	
	public boolean removeStatement(Statement statement)
	{
		boolean success = true;
		success = getAuthorshipsRoot().getStatement().remove(statement);
		return success;
	}
	
	public boolean removeCogGraph(Object cogGraph_or_ID)
	{
		boolean success = true;
		CogGraph cogGraph = null;
		String cogGraphId = null;
		
		//to remove a CogGraph it must contain no GraphNodule
	
		if(cogGraph_or_ID instanceof String)
		{
			cogGraphId = (String) cogGraph_or_ID;
			cogGraph = getCogGraphs(cogGraphId, null, null).get(0);
		}
		else
		{
			cogGraph = (CogGraph) cogGraph_or_ID;
			cogGraphId = cogGraph.getId();
		}
		if(cogGraph==null)
		{
			success = false;
			m_log.error("The CogGraph could not be removed as it was not found");
		}
		
		if(success)
		{
			GraphNodule graphNodule = null;
			ArrayList<GraphNodule> gNs = getGraphNodules(null, null, cogGraph_or_ID, null, null, null);
			for (int i = 0; i < gNs.size(); i++)
			{
				graphNodule = gNs.get(i);
				success = success && removeGraphNodule(graphNodule,cogGraph,null);
			}
			if(!success)
			{
				m_log.error("The removal of GraphNodule " + graphNodule.getId() + " failed. CogGraph " + cogGraph.getId() + " cannot be removed.");
			}
		}
		
		if(success)
		{			
			//remove CogGraph into the Cognitive Integration Area
			getCognitiveIntegrationRoot().getCogGraph().remove(cogGraph);
		}
		
		return success;
	}
	
	public boolean removeGraphNodule(GraphNodule graphNodule, CogGraph cogGraph, CogNode cogNode)
	{
		boolean succes = true;

		if(cogGraph==null)
		{
			cogGraph = getCogGraphs(graphNodule.getGraphID(),null,null).get(0);
		}
		if(cogNode==null)
		{
			cogNode = getCognitionNodes(graphNodule.getCogNodeRef().getTargetCNID(), null, graphNodule.getCogNodeRef().getTargetCNType(), null, null, null, null, null, null).get(0);
		}
		
		CogNType cNType = graphNodule.getCogNodeRef().getTargetCNType();
		if(CogNType.CONCEPT.equals(cNType))
		{
			cogGraph.getGraphConception().getGraphOntConception().getGraphNodule().remove(graphNodule);
		}
		else if(CogNType.CONCEPTIVE_INSTANCE.equals(cNType))
		{
			cogGraph.getGraphConception().getGraphInstConception().getGraphNodule().remove(graphNodule);
		}
		else if(CogNType.PERCEPTIVE_INSTANCE.equals(cNType))
		{
			cogGraph.getGraphPerception().getGraphNodule().remove(graphNodule);
		}
		else if(CogNType.SENS_REC.equals(cNType))
		{
			cogGraph.getGraphSensation().getGraphNodule().remove(graphNodule);
		}
		
		unlinkCogNodeFromCogGraph(cogGraph, cogNode, graphNodule);

		return succes;
	}

	public boolean removeCogNode(String cogNodeID, CogNType cogNodeType)
	{
		boolean succes = true;

		CogNode cogNode = getCognitionNodes(cogNodeID, null, cogNodeType, null, null, null, null, null, null).get(0);
		if(cogNode!=null){succes = removeCogNode(cogNode);}
		else
		{
			m_log.error("No CogNode was found with id " + cogNodeID + ".");
			succes=false;
		}
		return succes;
	}
	
	public boolean removeCogNodes()
	{
		boolean success = true;

		ArrayList<CogNode> cogNodes = getCognitionNodes(null, null, null, null, null, null, null, null, null);
		for (int i = 0; i < cogNodes.size(); i++)
		{
			success = success && removeCogNode(cogNodes.get(i));
		}
		
		return success;
	}
	
	public boolean removeCogGraphs()
	{
		boolean success = true;

		ArrayList<CogGraph> cogGraphs = getCogGraphs(null, null, null);
		for (int i = 0; i < cogGraphs.size(); i++)
		{
			success = success && removeCogGraph(cogGraphs.get(i));
		}
		
		return success;
	}
	
	public boolean removeCogNode(CogNode cogNode)
	{
		boolean success = true;
		
		//a CogNode can only be removed if it has no inner GraphNoduleRef elements
		//If (in the case it is a SensRec) it is still referenced by any SynCoR it must be removed from their as well
		
		if(cogNode.getGraphNoduleRef().size()>0)
		{
			m_log.error("CogNode " + cogNode.getId() + " cannot be removed as it still has inner GraphNoduleRef elements.");
			success = false;
		}
		if(success)
		{
			if(CogNType.CONCEPT.equals(cogNode.getCogType())){getOntologicalConceptionRoot().getCogNode().remove(cogNode);}
			else if(CogNType.CONCEPTIVE_INSTANCE.equals(cogNode.getCogType())){getInstantialConceptionRoot().getCogNode().remove(cogNode);}
			else if(CogNType.PERCEPTIVE_INSTANCE.equals(cogNode.getCogType())){getPerceptionRoot().getCogNode().remove(cogNode);}
			else if(CogNType.SENS_REC.equals(cogNode.getCogType())){getSensationRoot().getCogNode().remove(cogNode);}
		}
		return success;
	}
	
	public String getCogNodePayloadValue(CogNode cogNode, String payloadElementName)
	{
		String payloadElementValue = null;
		
		List<PayloadElement> payloadElemens = cogNode.getPayloadElement();
		for (int i = 0; i < payloadElemens.size(); i++)
		{
			PayloadElement payloadElement = payloadElemens.get(i);
			if(payloadElementName.equals(payloadElement.getName()))
			{
				payloadElementValue = payloadElement.getValue();
				break;
			}
		}
		return payloadElementValue;
	}

	public String buildSynCoDID(GraphNodeType typeOfObjectToIDentify, CogNType cogNodeType, String parentID, String count)
	{
		String id = null;
		
		if(GraphNodeType.SYN_CO_R.equals(typeOfObjectToIDentify))
		{
			id = parentID + ":" + "syncor" + "-" + count;
		}
		else if(GraphNodeType.STATEMENT.equals(typeOfObjectToIDentify))
		{
			id = parentID + ":" + "stmnt" + "-" + count;
		}
		else if(GraphNodeType.COG_GRAPH.equals(typeOfObjectToIDentify))
		{
			id = parentID + ":" + "cgraph" + "-" + count;
		}
		else if(GraphNodeType.GRAPH_NODULE.equals(typeOfObjectToIDentify))
		{
	
			id =  parentID + ":" + "gn" + "-" + count;
		}
		else if(GraphNodeType.COG_NODE.equals(typeOfObjectToIDentify))
		{
			if(CogNType.CONCEPT.equals(cogNodeType))
			{
				id = parentID + ":" + "cn" + "-" + "cncpt" + "-" + count;
			}
			else if(CogNType.CONCEPTIVE_INSTANCE.equals(cogNodeType))
			{
				id = parentID+ ":" + "cn" + "-" + "cncptinst" + "-" + count;
			}
			else if(CogNType.PERCEPTIVE_INSTANCE.equals(cogNodeType))
			{
				id = parentID + ":" + "cn" + "-" + "prcpt" + "-" + count;
			}
			else if(CogNType.SENS_REC.equals(cogNodeType))
			{
				id = parentID + ":" + "cn" + "-" + "snsrec" + "-" + count;
			}
		}
		
		return id;
	}

	public boolean save(String outSynCoDFilePath)
	{
		boolean success = true;
		if(outSynCoDFilePath==null && m_synCoDFile == null)
		{
			success = false;
			m_log.error("It is not possible to save SynCoD " + m_synCoD.getParentSynCoRID() + " because no file was specified.");
		}
		if(success) 
		{
			File outputFile = m_synCoDFile;
			if(outSynCoDFilePath!=null){outputFile = new File(outSynCoDFilePath);}
			success = SynCoDFileHandler.saveSynCoD2File(m_synCoD, outputFile);
		}

		return success;
	}

	private boolean handleInterGraphNoduleLink(GraphNodule graphNoduleA, GraphNodule graphNoduleB, 
											   Boolean vertical, int opType, String origGraphID,
											   InterCogNodeRelType relType, Boolean isNew)
	{
		boolean success = true;
		
		//it is assumed that if the connection is vertical the top Nodule is graphNoduleA
		//and if the connection is horizontal the leftmost Nodule is graphNoduleB
		if(relType!=null)
		{
			if(relType.equals(InterCogNodeRelType.C_INST_CATEGORIZATION)||
					   relType.equals(InterCogNodeRelType.C_INST_GROUNDING) ||
					   relType.equals(InterCogNodeRelType.P_INST_GROUNDING) ||
					   relType.equals(InterCogNodeRelType.SENS_SEG_GROUNDING) ||
					   relType.equals(InterCogNodeRelType.CONC_CATEGORIZATION) ||
					   relType.equals(InterCogNodeRelType.C_INST_V_INCLUSION) ||
					   relType.equals(InterCogNodeRelType.P_INST_V_INCLUSION))
			{vertical=true;}
			else{vertical=false;}
		}

		
		List<InterGraphNoduleRef> referencingNoduleInterGraphNoduleRefs = null;
		List<InterGraphNoduleRef> referencedNoduleInterGraphNoduleRefs = null;
		if(vertical)
		{
			if(graphNoduleA.getVDescendingGraphLinks()==null)
			{
				GraphLinkSetType vDscendingLinks = new GraphLinkSetType();
				graphNoduleA.setVDescendingGraphLinks(vDscendingLinks);
			}
			if(graphNoduleB.getVAscendingGraphLinks()==null)
			{
				GraphLinkSetType vAscendingLinks = new GraphLinkSetType();
				graphNoduleB.setVAscendingGraphLinks(vAscendingLinks);
			}
			referencingNoduleInterGraphNoduleRefs = graphNoduleA.getVDescendingGraphLinks().getInterGraphNoduleRef();
			referencedNoduleInterGraphNoduleRefs = graphNoduleB.getVAscendingGraphLinks().getInterGraphNoduleRef();
		}
		else
		{
			if(graphNoduleA.getHDescendingGraphLinks()==null)
			{
				GraphLinkSetType hDscendingLinks = new GraphLinkSetType();
				graphNoduleA.setHDescendingGraphLinks(hDscendingLinks);
			}
			if(graphNoduleB.getHAscendingGraphLinks()==null)
			{
				GraphLinkSetType hAscendingLinks = new GraphLinkSetType();
				graphNoduleB.setHAscendingGraphLinks(hAscendingLinks);
			}
			referencingNoduleInterGraphNoduleRefs = graphNoduleA.getHDescendingGraphLinks().getInterGraphNoduleRef();
			referencedNoduleInterGraphNoduleRefs = graphNoduleB.getHAscendingGraphLinks().getInterGraphNoduleRef();
		}
		
		if(ADD_OP == opType)
		{
			if(relType==null || isNew==null)
			{
				m_log.error("When adding a link between GraphNodule the values given to isNew and relType must not be null.");
			}
			else
			{
				InterGraphNoduleRef newReferencingInterGraphNoduleRef = new InterGraphNoduleRef();
				newReferencingInterGraphNoduleRef.setOrigGraphID(origGraphID);
				newReferencingInterGraphNoduleRef.setTargetCogNType(graphNoduleB.getCogNodeRef().getTargetCNType());
				newReferencingInterGraphNoduleRef.setTargetCogNID(graphNoduleB.getCogNodeRef().getTargetCNID());
				newReferencingInterGraphNoduleRef.setTargetGraphNID(graphNoduleB.getId());
				newReferencingInterGraphNoduleRef.setRelType(relType);
				newReferencingInterGraphNoduleRef.setNew(isNew);
				
				InterGraphNoduleRef newReferencedInterGraphNoduleRef = new InterGraphNoduleRef();
				newReferencedInterGraphNoduleRef.setOrigGraphID(origGraphID);
				newReferencedInterGraphNoduleRef.setTargetCogNType(graphNoduleA.getCogNodeRef().getTargetCNType());
				newReferencedInterGraphNoduleRef.setTargetCogNID(graphNoduleA.getCogNodeRef().getTargetCNID());
				newReferencedInterGraphNoduleRef.setTargetGraphNID(graphNoduleA.getId());
				newReferencedInterGraphNoduleRef.setRelType(relType);
				newReferencedInterGraphNoduleRef.setNew(isNew);
				
				//newReferencingInterGraphNoduleRef.setGraphNodule(graphNoduleB);
				referencingNoduleInterGraphNoduleRefs.add(newReferencingInterGraphNoduleRef);
				
				//newReferencedInterGraphNoduleRef.setGraphNodule(graphNoduleA);
				referencedNoduleInterGraphNoduleRefs.add(newReferencedInterGraphNoduleRef);
			}
		}
		else
		{
			boolean found = false;
			for (int i = 0; i < referencingNoduleInterGraphNoduleRefs.size(); i++)
			{
				InterGraphNoduleRef referencingNoduleInterGraphNoduleRef = referencingNoduleInterGraphNoduleRefs.get(i);
				if(LINK_OP == opType)
				{
					if(referencingNoduleInterGraphNoduleRef.getTargetGraphNID().equals(graphNoduleB.getId()))
					{
						referencingNoduleInterGraphNoduleRef.setGraphNodule(graphNoduleB);
						found = true;
					}
				}
				else if(UNLINK_OP == opType)
				{
					if(referencingNoduleInterGraphNoduleRef.getTargetGraphNID().equals(graphNoduleB.getId()))
					{
						referencingNoduleInterGraphNoduleRef.setGraphNodule(null);
						found = true;
					}
				}
				else if(REMOVE_OP == opType)
				{
					if(referencingNoduleInterGraphNoduleRef.getTargetGraphNID().equals(graphNoduleB.getId()))
					{
						referencingNoduleInterGraphNoduleRefs.remove(referencingNoduleInterGraphNoduleRef);
						found = true;
					}
				}
				if(found)
				{
					for (int j = 0; j < referencedNoduleInterGraphNoduleRefs.size(); j++)
					{
						InterGraphNoduleRef referencedNoduleInterGraphNoduleRef = referencedNoduleInterGraphNoduleRefs.get(j);
						if(LINK_OP == opType)
						{
							if(referencedNoduleInterGraphNoduleRef.getTargetGraphNID().equals(graphNoduleA.getId()))
							{
								referencedNoduleInterGraphNoduleRef.setGraphNodule(graphNoduleA);
								found = true;
								break;
							}
						}
						else if(UNLINK_OP == opType)
						{
							if(referencedNoduleInterGraphNoduleRef.getTargetGraphNID().equals(graphNoduleA.getId()))
							{
								referencedNoduleInterGraphNoduleRef.setGraphNodule(null);
								found = true;
								break;
							}
						}
						else if(REMOVE_OP == opType)
						{
							if(referencedNoduleInterGraphNoduleRef.getTargetGraphNID().equals(graphNoduleA.getId()))
							{
								referencedNoduleInterGraphNoduleRefs.remove(referencedNoduleInterGraphNoduleRef);
								found = true;
								break;
							}
						}
					}
					break;
				}
			}
			if(!found)
			{
				success = false;
				m_log.error("It was not possible to add/link or remove/unlink GraphNodule " + graphNoduleA.getId() + " from the desired GraphNodule " + graphNoduleB.getId() + " .");
			}
		}
		return success;
	}

	private boolean handleCogNode2GraphNoduleLink(CogNode cogNode, GraphNodule graphNodule, int opType)
	{
		boolean success = true;
		
		//each CogNode may refer any number of GraphNodules. A GraphNodule may refer only to one CogNode.
		List<GraphNoduleRef> graphNoduleRefs = cogNode.getGraphNoduleRef();
		
		if(ADD_OP == opType)
		{
			GraphNoduleRef graphNoduleRef = new GraphNoduleRef();
			graphNoduleRef.setTargetGraphNoduleID(graphNodule.getId());
			graphNoduleRef.setGraphID(graphNodule.getGraphID());
			cogNode.getGraphNoduleRef().add(graphNoduleRef);
			
			CogNodeRefType cogNodeRef = new CogNodeRefType();
			cogNodeRef.setTargetCNID(cogNode.getId());
			cogNodeRef.setTargetCNType(cogNode.getCogType());
			graphNodule.setCogNodeRef(cogNodeRef);
		}
		else
		{
			boolean found = false;
			for (int j = 0; j < graphNoduleRefs.size(); j++)
			{
				GraphNoduleRef graphNoduleRef = graphNoduleRefs.get(j);
				
				if(LINK_OP == opType)
				{
					if(graphNoduleRef.getTargetGraphNoduleID().equals(graphNodule.getId()))
					{
						graphNoduleRef.setGraphNodule(graphNodule);
						graphNodule.getCogNodeRef().setCogNode(cogNode);
						found = true;
					}
				}
				else if(UNLINK_OP == opType)
				{
					if(graphNoduleRef.getTargetGraphNoduleID().equals(graphNodule.getId()))
					{
						graphNoduleRef.setGraphNodule(null);
						graphNodule.getCogNodeRef().setCogNode(null);
						found = true;
					}
				}
				else if(REMOVE_OP == opType)
				{
					if(graphNoduleRef.getTargetGraphNoduleID().equals(graphNodule.getId()))
					{
						cogNode.getGraphNoduleRef().remove(graphNoduleRef);
						graphNodule.setCogNodeRef(null);
						found = true;
					}
				}
				if(found){break;}
			}
			
			if(!found)
			{
				success = false;
				m_log.error("It was not possible to add/link or remove/unlink GraphNodule " + graphNodule.getId() + " from the desired CogNode.");
			}
		}
		return success;
	}

	private void addPayloadElementsToCogNode(CogNode cogNode, HashMap<String, String> payload)
	{
		if(payload!=null && !payload.isEmpty())
		{
			Iterator<String> payloadNames = payload.keySet().iterator();
			while (payloadNames.hasNext())
			{
				String payloadName = payloadNames.next();
				String payloadValue = payload.get(payloadName);
				PayloadElement payloadElement = new PayloadElement();
				payloadElement.setName(payloadName);
				payloadElement.setValue(payloadValue);
				cogNode.getPayloadElement().add(payloadElement);
			}
		}
	}

	private String generateSynCoDID(GraphNodeType typeOfObjectToIDentify, CogNType cogNodeType, String parentID)
	{
		String id = null;
		
		int count = 0;

		if(GraphNodeType.STATEMENT.equals(typeOfObjectToIDentify))
		{			
			ArrayList<Statement> statements = getStatements(null, null, null, null, null, null, null);
			if(statements.size()>0)
			{
				for (int i = 0; i < statements.size(); i++)
				{
					Statement statement = statements.get(i);
					String existingID = statement.getId();
					int seqNr = getIDSeqNr(existingID);
					if(seqNr>count){count=seqNr;}
				}
				count+=1;
			}
		}
		else if(GraphNodeType.COG_GRAPH.equals(typeOfObjectToIDentify))
		{
			//TODO corrigir  - apenas os cogGraphs do mesmo parent statement entram para a conta
			ArrayList<CogGraph> cogGraphs = getCogGraphs(null, null, null);
			if(cogGraphs.size()>0)
			{
				for (int i = 0; i < cogGraphs.size(); i++)
				{
					CogGraph cogGraph = cogGraphs.get(i);
					String existingID = cogGraph.getId();
					int seqNr = getIDSeqNr(existingID);
					if(seqNr>count){count=seqNr;}
				}
				count+=1;
			}
		}
		else if(GraphNodeType.GRAPH_NODULE.equals(typeOfObjectToIDentify))
		{
			ArrayList<GraphNodule> graphNodules = getGraphNodules(null, null, parentID, null, null, null);
			if(graphNodules.size()>0)
			{
				for (int i = 0; i < graphNodules.size(); i++)
				{
					GraphNodule graphNodule = graphNodules.get(i);
					String existingID = graphNodule.getId();
					int seqNr = getIDSeqNr(existingID);
					if(seqNr>count){count=seqNr;}
				}
				count+=1;
			}
		}
		else if(GraphNodeType.COG_NODE.equals(typeOfObjectToIDentify))
		{
			if(CogNType.CONCEPT.equals(cogNodeType))
			{
				ArrayList<CogNode> cogNodes = getCognitionNodes(null, null, CogNType.CONCEPT, null, parentID, null, null, null, null);
				if(cogNodes.size()>0)
				{
					for (int i = 0; i < cogNodes.size(); i++)
					{
						CogNode cogNode = cogNodes.get(i);
						String existingID = cogNode.getId();
						int seqNr = getIDSeqNr(existingID);
						if(seqNr>count){count=seqNr;}
					}
					count+=1;
				}
			}
			else if(CogNType.CONCEPTIVE_INSTANCE.equals(cogNodeType))
			{
				ArrayList<CogNode> cogNodes = getCognitionNodes(null, null, CogNType.CONCEPTIVE_INSTANCE, null, parentID, null, null, null, null);
				if(cogNodes.size()>0)
				{
					for (int i = 0; i < cogNodes.size(); i++)
					{
						CogNode cogNode = cogNodes.get(i);
						String existingID = cogNode.getId();
						int seqNr = getIDSeqNr(existingID);
						if(seqNr>count){count=seqNr;}
					}
					count+=1;
				}
			}
			else if(CogNType.PERCEPTIVE_INSTANCE.equals(cogNodeType))
			{
				ArrayList<CogNode> cogNodes = getCognitionNodes(null, null, CogNType.PERCEPTIVE_INSTANCE, null, parentID, null, null, null, null);
				if(cogNodes.size()>0)
				{
					for (int i = 0; i < cogNodes.size(); i++)
					{
						CogNode cogNode = cogNodes.get(i);
						String existingID = cogNode.getId();
						int seqNr = getIDSeqNr(existingID);
						if(seqNr>count){count=seqNr;}
					}
					count+=1;
				}
			}
			else if(CogNType.SENS_REC.equals(cogNodeType))
			{
				ArrayList<CogNode> cogNodes = getCognitionNodes(null, null, CogNType.SENS_REC, null, parentID, null, null, null, null);
				if(cogNodes.size()>0)
				{
					for (int i = 0; i < cogNodes.size(); i++)
					{
						CogNode cogNode = cogNodes.get(i);
						String existingID = cogNode.getId();
						int seqNr = getIDSeqNr(existingID);
						if(seqNr>count){count=seqNr;}
					}
					count+=1;
				}
			}
		}
		id = buildSynCoDID(typeOfObjectToIDentify,cogNodeType,parentID,String.valueOf(count));
		return id;
	}
	
	private boolean validateSynCoDID(GraphNodeType nodeType, CogNType cogType, String id, String parentID)
	{
		boolean isValid=true;
		
		if(GraphNodeType.STATEMENT.equals(nodeType))
		{
			String idPart = parentID + ":" + "stmnt" + "-";
			if(!id.startsWith(idPart)){isValid=false;}
		}
		else if(GraphNodeType.COG_GRAPH.equals(nodeType))
		{
			String idPart = parentID + ":" + "cgraph" + "-";
			if(!id.startsWith(idPart)){isValid=false;}
		}
		else if(GraphNodeType.GRAPH_NODULE.equals(nodeType))
		{
			String idPart = parentID + ":" + "gn" + "-";
			if(!id.startsWith(idPart)){isValid=false;}
		}
		else if(GraphNodeType.COG_NODE.equals(nodeType))
		{
			if(CogNType.CONCEPT.equals(cogType))
			{
				String idPart = parentID + ":" + "cn" + "-" + "cncpt" + "-";
				if(!id.startsWith(idPart)){isValid=false;}
			}
			else if(CogNType.CONCEPTIVE_INSTANCE.equals(cogType))
			{
				String idPart = parentID + ":" + "cn" + "-" + "cncptinst" + "-";
				if(!id.startsWith(idPart)){isValid=false;}
			}
			else if(CogNType.PERCEPTIVE_INSTANCE.equals(cogType))
			{
				String idPart = parentID + ":" + "cn" + "-" + "prcpt" + "-";
				if(!id.startsWith(idPart)){isValid=false;}
			}
			else if(CogNType.SENS_REC.equals(cogType))
			{
				String idPart = parentID + ":" + "cn" + "-" + "snsrec" + "-";
				if(!id.startsWith(idPart)){isValid=false;}
			}
		}

		
		return isValid;
	}
	
	private int getIDSeqNr(String existingID)
	{
		int seqNr = 0;
		String[] parts = existingID.split("-");
		seqNr = Integer.valueOf(parts[parts.length-1]);
		return seqNr;
	}


	private SynCoD buildBaseSynCoDStructure(String synCoDID, String synCoDType, String ownerUserID)
	{
		SynCoD synCoD = null;
		
		synCoD = new SynCoD();
		synCoD.setParentSynCoRID(synCoDID);
		synCoD.setSynCoDType(synCoDType);
		synCoD.setCreatorUser(ownerUserID);
		
		if(ISynCoDConstants.TABULAR_SYNCOD_TYPE.equals(synCoDType))
		{
			//TODO
		}
		else if(ISynCoDConstants.GRAPHAL_SYNCOD_TYPE.equals(synCoDType))
		{
			Graphal graphal = new Graphal();
			synCoD.setGraphal(graphal);
			
			Cognition cognition = new Cognition();
			Authorships authorships = new Authorships();
			graphal.setCognition(cognition);
			graphal.setAuthorships(authorships);
			
			Layers layers = new Layers();
			Integration integration = new Integration();
			cognition.setLayers(layers);
			cognition.setIntegration(integration);
			
			Conception conception = new Conception();
			Perception perception = new Perception();
			Sensation sensation = new Sensation();
			layers.setConception(conception);
			layers.setPerception(perception);
			layers.setSensation(sensation);
			
			Ontological ontological = new Ontological();
			Instantial instantial = new Instantial();
			conception.setOntological(ontological);
			conception.setInstantial(instantial);	
		}
		return synCoD;
	}

	private boolean inflateOrDeflate(boolean inflate)
	{
		boolean success = true;

		//for each Graph in the Integration Area
		List<CogGraph> cogGraphs = getCognitiveIntegrationRoot().getCogGraph();
		for (int i = 0; i < cogGraphs.size(); i++)
		{
			CogGraph cogGraph = cogGraphs.get(i);
			
			if(!weaveCogGraph(cogGraph,inflate))
			{
				success = false;
				m_log.error("Failure at the weaving/unweaving of CogGraph " + cogGraph.getId() + ".");
			}
		}

		return success;
	}

	private boolean weaveCogGraph(CogGraph cogGraph, boolean inflate)
	{
		boolean success = true;
		
		String operationName = "inflation";
		if(!inflate){operationName="deflation";}
		
		//handle the linking/unlinking of GraphNodules to CogNodes and viceversa	
		success = success && handleGraphNodules2CogNodesInterLinkingForEntireGraph(cogGraph,inflate);
	
		//handle the linking/unlinking of GraphNodules to all their related GraphNodules
		success = success && handleGraphNodulesInterLinksForEntireGraph(cogGraph,inflate);
	
		if(!success){m_log.error("Failure at " + operationName + " of CogGraph " + cogGraph.getId() +  ". It was not possible to fully complete the operation.");}
		
		return success;
	}

	private boolean handleGraphNodules2CogNodesInterLinkingForEntireGraph(CogGraph cogGraph, boolean inflate)
	{
		boolean success = true;
		
		String operationName = "inflation";
		if(!inflate){operationName="deflation";}
		
		List<GraphNodule> ontConcGraphNodulesList = cogGraph.getGraphConception().getGraphOntConception().getGraphNodule();
		List<GraphNodule> instConcGraphNodulesList = cogGraph.getGraphConception().getGraphInstConception().getGraphNodule();
		List<GraphNodule> perceptGraphNodulesList = cogGraph.getGraphPerception().getGraphNodule();
		List<GraphNodule> sensGraphNodulesList = cogGraph.getGraphSensation().getGraphNodule();
		
		success = success && handleGraphNoduleAndCogNodeInterlinksForGraphSection(CogNType.CONCEPT,ontConcGraphNodulesList,inflate);
		success = success && handleGraphNoduleAndCogNodeInterlinksForGraphSection(CogNType.CONCEPTIVE_INSTANCE,instConcGraphNodulesList,inflate);
		success = success && handleGraphNoduleAndCogNodeInterlinksForGraphSection(CogNType.CONCEPTIVE_INSTANCE,perceptGraphNodulesList,inflate);
		success = success && handleGraphNoduleAndCogNodeInterlinksForGraphSection(CogNType.SENS_REC,sensGraphNodulesList,inflate);
		
		if(!success){m_log.error("Failure at " + operationName + " of CogGraph " + cogGraph.getId() +  ". Not possible to fully interconnect/disconnect the entire GraphNodule-to-CogNode tissue.");}
		
		return success;
	}

	private boolean handleGraphNoduleAndCogNodeInterlinksForGraphSection(CogNType cogNodeType, List<GraphNodule> graphNodules, boolean inflate)
	{
		boolean success = true;
		
		String operationName = "inflation";
		if(!inflate){operationName="deflation";}
		String graphID = null;
		
		//Link/unlink CogNode to GraphNodule
		for (int j = 0; j < graphNodules.size(); j++)
		{
			GraphNodule graphNodule = graphNodules.get(j);
			graphID = graphNodule.getGraphID();
			CogNode targetCogNode = null;
			String targetCogNodeID = null;
			if(!inflate)
			{
				targetCogNode = graphNodule.getCogNodeRef().getCogNode();
				success = handleCogNode2GraphNoduleLink(targetCogNode, graphNodule, UNLINK_OP);
			}
			else
			{
				targetCogNodeID = graphNodule.getCogNodeRef().getTargetCNID();
				targetCogNode = getCognitionNodes(targetCogNodeID, null, cogNodeType, null, null, null, null, null, null).get(0);
				success = handleCogNode2GraphNoduleLink(targetCogNode, graphNodule, LINK_OP);
			}
		}
		if(!success){m_log.error("Failure at " + operationName + " of CogGraph " + graphID  +  ". It was not possible to fully interconnect/disconnect its GraphNodule-to-CogNode tissue in section " + cogNodeType + ".");}
		
		return success;
	}

	private boolean handleGraphNodulesInterLinksForEntireGraph(CogGraph cogGraph, boolean inflate)
	{
		boolean success = true;
		
		String operationName = "inflation";
		if(!inflate){operationName="deflation";}
		
		List<GraphNodule> ontConcGraphNodulesList = cogGraph.getGraphConception().getGraphOntConception().getGraphNodule();
		List<GraphNodule> instConcGraphNodulesList = cogGraph.getGraphConception().getGraphInstConception().getGraphNodule();
		List<GraphNodule> perceptGraphNodulesList = cogGraph.getGraphPerception().getGraphNodule();
		List<GraphNodule> sensGraphNodulesList = cogGraph.getGraphSensation().getGraphNodule();
		
		ArrayList<GraphNodule> fullGraphNodulesList = new ArrayList<GraphNodule>();
		
		fullGraphNodulesList.addAll(ontConcGraphNodulesList);
		fullGraphNodulesList.addAll(instConcGraphNodulesList);
		fullGraphNodulesList.addAll(perceptGraphNodulesList);
		fullGraphNodulesList.addAll(sensGraphNodulesList);
		
		for (int i = 0; i < fullGraphNodulesList.size(); i++)
		{
			boolean innerSucess = true;
			GraphNodule graphNodule = fullGraphNodulesList.get(i);
			
			if(graphNodule.getVAscendingGraphLinks()!=null)
			{
				innerSucess = innerSucess && handleInterGraphNoduleLinksInGraphLinkSet(graphNodule,graphNodule.getVAscendingGraphLinks().getInterGraphNoduleRef(),cogGraph,V_ASCENDING_LINKSET,inflate);
			}
			if(graphNodule.getVDescendingGraphLinks()!=null)
			{
				innerSucess = innerSucess && handleInterGraphNoduleLinksInGraphLinkSet(graphNodule,graphNodule.getVDescendingGraphLinks().getInterGraphNoduleRef(),cogGraph,V_DESCENDING_LINKSET,inflate);
			}
			if(graphNodule.getHAscendingGraphLinks()!=null)
			{
				innerSucess = innerSucess && handleInterGraphNoduleLinksInGraphLinkSet(graphNodule,graphNodule.getHAscendingGraphLinks().getInterGraphNoduleRef(),cogGraph,H_ASCENDING_LINKSET,inflate);
			}
			if(graphNodule.getHDescendingGraphLinks()!=null)
			{
				innerSucess = innerSucess && handleInterGraphNoduleLinksInGraphLinkSet(graphNodule,graphNodule.getHDescendingGraphLinks().getInterGraphNoduleRef(),cogGraph,H_DESCENDING_LINKSET,inflate);
			}			
			if(!innerSucess)
			{
				m_log.error("Failure at " + operationName + " of CogGraph " + cogGraph.getId() +  ". Not possible to fully connect/disconnect all GraphNodules for GraphNodule " + graphNodule.getId() + ".");
			}
			success = success && innerSucess;
		}
		
		if(!success){m_log.error("Failure at inflation of CogGraph " + cogGraph.getId() +  ". Not possible to fully interconnect the entire GraphNodule tissue.");}
		
		return success;
	}

	private boolean handleInterGraphNoduleLinksInGraphLinkSet(GraphNodule graphNodule,
															  List<InterGraphNoduleRef> interGraphNoduleRefs,
															  CogGraph cogGraph, String linkSetType, boolean inflate)
	{
		boolean success = true;
		
		String operationName = "inflation";
		if(!inflate){operationName="deflation";}
		
		int opType = LINK_OP;
		if(!inflate){opType = UNLINK_OP;}
		
		for (int j = 0; j < interGraphNoduleRefs.size(); j++)
		{
			boolean vertical = true;
			InterGraphNoduleRef interGraphNoduleRef = interGraphNoduleRefs.get(j);
			CogNType targetGraphNoduleCogNodeType = interGraphNoduleRef.getTargetCogNType();
			String targetGraphNoduleID = interGraphNoduleRef.getTargetGraphNID();
			GraphNodule targetGraphNodule = getGraphNodules(null, null, cogGraph, null, targetGraphNoduleCogNodeType, targetGraphNoduleID).get(0);
			
			if(V_ASCENDING_LINKSET.equals(linkSetType))
			{
				success = handleInterGraphNoduleLink(targetGraphNodule, graphNodule, vertical, opType, null, null, null);
			}
			else if(V_DESCENDING_LINKSET.equals(linkSetType))
			{
				success = handleInterGraphNoduleLink(graphNodule, targetGraphNodule, vertical,opType,null, null, null);
			}
			if(H_ASCENDING_LINKSET.equals(linkSetType))
			{
				vertical = false;
				success = handleInterGraphNoduleLink(targetGraphNodule, graphNodule, vertical, opType, null, null, null);
			}
			if(H_DESCENDING_LINKSET.equals(linkSetType))
			{
				vertical = false;
				success = handleInterGraphNoduleLink(graphNodule, targetGraphNodule, vertical,opType,null, null, null);
			}
		}
		if(!success){m_log.error("Failure at " + operationName + " of CogGraph " + graphNodule.getGraphID() +  ". Not possible to fully connect/disconnect all " + linkSetType + " GraphNodules for GraphNodule " + graphNodule.getId() + ".");}
		return success;
	}

	private List<CogNode> getCogNodesOfGivenType(CogNType cogNodeType)
	{
		List<CogNode> targetCogNodes = new ArrayList<CogNode>();
		if(cogNodeType == null)
		{
			targetCogNodes.addAll(getOntologicalConceptionRoot().getCogNode());
			targetCogNodes.addAll(getInstantialConceptionRoot().getCogNode());
			targetCogNodes.addAll(getPerceptionRoot().getCogNode());
			targetCogNodes.addAll(getSensationRoot().getCogNode());
		}
		else
		{
			if(cogNodeType.equals(CogNType.CONCEPT))
			{
				targetCogNodes = getOntologicalConceptionRoot().getCogNode();
			}
			else if(cogNodeType.equals(CogNType.CONCEPTIVE_INSTANCE))
			{
				targetCogNodes = getInstantialConceptionRoot().getCogNode();
			}
			else if(cogNodeType.equals(CogNType.PERCEPTIVE_INSTANCE))
			{
				targetCogNodes = getPerceptionRoot().getCogNode();
			}
			else if(cogNodeType.equals(CogNType.SENS_REC))
			{
				targetCogNodes = getSensationRoot().getCogNode();
			}
			else
			{
				m_log.error("The specified type of CogNode [" + cogNodeType + "] is not correct.");
			}			
		}
		return targetCogNodes;
	}
	
	@SuppressWarnings("unused")
	private Object getObjectInnerField(Object object, String getterMethodName, Object argument, Class<?> argumentClass)
	{
		Object innerFieldValue = null;
		try
		{
		    Method m = null;
		    if(argument != null)
		    {
		    	Class<?> argFinalClass = argumentClass;
		    	if(getterMethodName.equals("equals"))
		    	{
		    		Object t = new Object();
		    		argFinalClass = t.getClass();
		    	}
		    	
		    	m = object.getClass().getMethod(getterMethodName,argFinalClass);
		    	innerFieldValue = m.invoke(object,argument);
		    }
		    else
		    {
		    	m = object.getClass().getMethod(getterMethodName);
		    	innerFieldValue = m.invoke(object);
		    }
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return innerFieldValue;
	}

	private boolean validateInitParameters(int constructorID, Object synCoD_or_ID, boolean inflatedMode, File synCoDFile, String synCoDTye, String ownerUserID)
	{
		boolean valid = true;
		
		if(constructorID==1){valid = valid && validateInitSynCoD((SynCoD)synCoD_or_ID,inflatedMode);}
		
		else if(constructorID==2){valid = valid && validateDiskInstance(synCoDFile,false);}
		
		else if(constructorID==3)
		{
			if(synCoDTye==null || !(synCoDTye.equals(ISynCoDConstants.TABULAR_SYNCOD_TYPE) || synCoDTye.equals(ISynCoDConstants.GRAPHAL_SYNCOD_TYPE))){valid = false;}
			
			if(synCoD_or_ID!=null && !((String)synCoD_or_ID).startsWith(ownerUserID)){valid = false;}
		}
		
		return valid;
	}

	private boolean validateDiskInstance(File diskInstance, boolean isDirectory)
	{
		boolean success = true;
		if(!FileUtils.diskItemExists(diskInstance)){success = false;}
		else
		{
			if(isDirectory && !FileUtils.directoryExists(diskInstance)){success = false;}
			else if(!isDirectory && !diskInstance.isFile()){success = false;}
		}
		return success;
	}

	private boolean validateInitSynCoD(SynCoD synCoD, boolean inflatedMode)
	{
		boolean success = true;
	
		//TODO Auto-generated method stub
	
		return success;
	}
}
