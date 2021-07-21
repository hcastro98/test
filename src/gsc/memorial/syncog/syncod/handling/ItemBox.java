package gsc.memorial.syncog.syncod.handling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ItemBox
{
	private HashMap<String, Item> m_items = null;
	private String m_boxTitle = null;
	
	public ItemBox(String boxTitle)
	{
		m_items = new HashMap<String,Item>();
		m_boxTitle = boxTitle;
	}

	public String getTitle(String id)
	{
		return m_boxTitle;
	}

	public Item createItem(String title, String id)
	{
		Item item = new Item(title, id);
		m_items.put(id,item);
		return item;
	}
	
	public Item getItem(String id)
	{
		return m_items.get(id);
	}

	public void removeItem(String id)
	{
		m_items.remove(id);
	}
	
	public void printBoxContents()
	{
		System.out.println("+++++++++++++++++++++++++++++++++++");
		System.out.println(m_boxTitle);
		System.out.println("+++++++++++++++++++++++++++++++++++");
		
		Set<String> keys = m_items.keySet();
		Iterator<String> itemIDs = keys.iterator();
		while (itemIDs.hasNext())
		{
			String itemID = itemIDs.next();
			Item item = m_items.get(itemID);
			printItemContents(item,"    ");
		}
	}
	
	private void printItemContents(Item item, String indent)
	{
		System.out.println(indent + "----------------------------------");
		System.out.println(indent + item.getTitle());
		for (int i = 0; i < item.getPayload().size(); i++)
		{
			String payloadEntry = item.getPayloadEntry(i);
			System.out.println(indent + payloadEntry);
		}
		
		Iterator<String> itemIterator = item.getInnerItemIterator();
		while (itemIterator.hasNext())
		{
			Item innerItem = item.getInnerItem(itemIterator.next());
			printItemContents(innerItem,indent + "    ");
		}
		System.out.println(indent + "----------------------------------");
	}

	private class Item
	{
		private String m_title = null;
		private String m_id = null;
		
		private HashMap<String,Item> m_innerItems = null;
		private ArrayList<String> m_payload = null;
		
		public Item(String title, String id)
		{
			m_title = title;
			m_id = id;
			m_innerItems = new HashMap<String,Item>();
		}
		
		@SuppressWarnings("unused")
		public String getID(){return m_id;}
		
		public String getTitle(){return m_title;}
		
		@SuppressWarnings("unused")
		public void createInnerItem(String title, String id)
		{
			Item item = new Item(title, id);
			m_innerItems.put(id,item);
		}
		
		public Iterator<String> getInnerItemIterator()
		{
			return m_innerItems.keySet().iterator();
		}
		
		public Item getInnerItem(String id)
		{
			return m_innerItems.get(id);
		}
		
		@SuppressWarnings("unused")
		public void removeInnerItem(String id)
		{
			m_innerItems.remove(id);
		}
		
		public ArrayList<String> getPayload()
		{
			return m_payload;
		}

		@SuppressWarnings("unused")
		public void addPayloadEntry(String entry)
		{
			m_payload.add(entry);
		}

		public String getPayloadEntry(int index)
		{
			return m_payload.get(index);
		}
	}
}
