package AnnotedText2NIF.ConverterEngine;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import AnnotedText2NIF.IOContent.TextReader;

/**
 * Diese Klasse sammelt alle Informationen bzgl. jeder Annotation aus einem Text, 
 * und Speichert diese in einer Liste von DefinitionObject(s).
 * @author TTurke
 *
 */
public class GatherAnnotationInformations 
{
	/**
	 * This method just return a en.wiki link for a given annotation
	 * @param Annotation
	 * @return url
	 */
	public static Set<String> returnEnWikiUrl(String Annotation)
	{
		Set<String> us = new HashSet<String>();
		String prefix = "https://en.wikipedia.org/wiki/";
		
		if(Annotation.contains("|"))
		{
			String [] uris = Annotation.split("\\|");
			
			for(String cur : uris)
			{
				if(cur.substring(0,1).equals(" "))
				{
					if(cur.substring(cur.length()-1).equals(" "))
					{
						us.add(prefix+cur.substring(1,cur.length()-1).replace(" ", "_"));
					}else{
						us.add(prefix+cur.substring(1).replace(" ", "_"));
					}
				}else{
					
					if(cur.substring(cur.length()-1).equals(" "))
					{
						us.add(prefix+cur.substring(0,cur.length()-1).replace(" ", "_"));
					}else{
						us.add(prefix+cur.replace(" ", "_"));
					}
				}	
			}
			
		}else{	
			us.add(prefix+Annotation.replace(" ", "_"));
		}	
		
		return us;
	}
	
	/**
	 * This method gather all interval informations of all annotations inside the given text.
	 * @param input
	 * @return list of intervalls
	 */
	public static LinkedList<int[]> returnAnnotationRanges(String input)
	{
		LinkedList<int[]> output = new LinkedList<int[]>();
		String start = "[[";
		String end = "]]";
		int endIndex = input.indexOf(end);
		int nextStartItem = -1;
		
		for (int beginIndex = input.indexOf(start); beginIndex >= 0; beginIndex = input.indexOf(start, beginIndex + 1))
		{
			//Neues Array
			int[] coords =  new int[]{-1,-1};
			
			//Startindex
			coords[0] = beginIndex+2;
			
			//Next Startindex
			nextStartItem = input.indexOf(start, beginIndex + 1);
			
			//Endindex
			if(endIndex >= 0 && endIndex > beginIndex)
			{
				coords[1] = endIndex;
			}
			
			
			//Pr�fe auf Doppelstart
			if(nextStartItem > coords[0] && nextStartItem < coords[1])
			{
				//Neue Runde
				continue;
			}else{
				
				//Next Endindex
				endIndex = input.indexOf(end, endIndex + 1);
				
				//Alles Einf�gen
				if(coords[0] > 0 && coords[1] > 0 && coords[1] > coords[0])
				{
					output.add(coords);
				}
			}
		}
		return output;
	}
	
	/**
	 * This method return all known informations about the annotations inside the text.
	 * The url constuction is pre-defined!
	 * @param input
	 * @return list of DefinitionObject(s)
	 */
	public static LinkedList<DefinitionObject> getAnnotationDefs(String input)
	{
		LinkedList<DefinitionObject> output = new LinkedList<DefinitionObject>();
		
		for(int[] coords : returnAnnotationRanges(input)) 
		{
			output.add(new DefinitionObject(coords[0], coords[1], input.substring(coords[0], coords[1]), returnEnWikiUrl(input.substring(coords[0], coords[1]))));			
		}
		
		return output;
	}
	
	/**
	 * This method return all known informations about the annotations 
	 * inside the text of the given path for text file.
	 * @param path
	 * @return
	 */
	public static LinkedList<DefinitionObject> getAnnotationsOfFile(String path)
	{
		String input = TextReader.fileReader(path);
		return getAnnotationDefs(input);
	}
	
	/*
	 * EXAMPLE of USE
	 */
	public static void main(String[] args) 
	{
		String input = TextReader.fileReader("C:/Users/Subadmin/Desktop/Test1.txt");
		
		for(DefinitionObject defObj : getAnnotationDefs(input))
		{
			System.out.println(defObj.showAllContent());
		}
	}

}
