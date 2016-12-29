package AnnotedText2NIF.ConverterEngine;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import AnnotedText2NIF.IOContent.TextReader;

/**
 * Diese Klasse sammelt alle Informationen bzgl. jeder Annotation aus einem Text, 
 * und Speichert diese in einer Liste von DefinitionObject(s).
 * @author TTurke
 *
 */
public class GatherAnnotationInformations 
{
	private String not_annot_text;
	public static final String real_prefix = "http://en.wikipedia.org/wiki/";
	public static final String dummy_prefix = "http://aksw.org/NOTINWIKI";
	
	public static final String simpleRex = Pattern.quote("[[") + "(.*?)" + Pattern.quote("]]");				//allow inner brackets => [[outer text [[inner text]]
	public static final String optimalRex = Pattern.quote("[[") + "([^\\[\\]]*)" + Pattern.quote("]]");		//denie inner brackets => [[url_entity_text]] or [[url_text|entity_text]]
	
	//#############################################################################
	//############################ USAGE METHODS ##################################
	//#############################################################################
	
	/**
	 * This method return all known informations about the annotations 
	 * inside the text of the given path for text file.
	 * @param path
	 * @return
	 */
	public LinkedList<DefinitionObject> getAnnotationsOfFile(String path, GatherAnnotationInformations gai)
	{
		String input = TextReader.fileReader(path);
		return gai.gatherDefsFast(input);
	}
	
	//TODO store url error if we need to create a dummy
	//TODO store error if entity has to much separator
	//TODO ermögliche das nicht dauernd der text mehrfach durchlaufen werden muss
	/**
	 * This method return all known informations about the annotations inside the text.
	 * The URL construction is pre-defined!
	 * @param input
	 * @return list of definition objects
	 */
	public LinkedList<DefinitionObject> gatherDefsFast(String input)
	{
		LinkedList<DefinitionObject> dobjs =  new LinkedList<DefinitionObject>();
		String url = "",content = "", url_part = "";
		int begin = -1, end = -1, new_start = 0;
		
		Matcher matcher = Pattern.compile(optimalRex).matcher(input);
		
		while (matcher.find())
		{	
			
			if(!matcher.group().contains("|"))
			{
				//Handling easy
				begin = matcher.start()-new_start;
				end = matcher.end()-new_start;
				
				//Entity
				content = matcher.group().replace("[[", "").replace("]]", "");
				
				//Url
				url_part = content.replace(" ", "_");
				url = real_prefix+url_part;
				
				//TODO check url exist then create object => if exist set prefix with content if not set dummy with content
				
				//Definition object
				dobjs.add(new DefinitionObject(begin, end-4, content, url));
				
				//Replace text
				input = input.replace(matcher.group(), matcher.group().replace("[[", "").replace("]]", ""));
				
				//edit index down grade values
				new_start += 4;
				
			}else{
				
				if(matcher.group().split("\\|").length > 2)
				{
					
					//TODO report error about to much separator entity
					//TODO delete or insert the entity as cleaned
					System.out.println("CALLED FOR ERROR ENTITY! => "+matcher.group());
					
				}else{
					
					//Handling complex
					begin = matcher.start()-new_start;
					end = matcher.end()-matcher.group().substring(0, matcher.group().indexOf("|")+1).length()-2-new_start;
					
					//Url
					url_part = matcher.group().substring(2, matcher.group().indexOf("|")).replace(" ", "_");
					url = real_prefix+url_part;
					
					//Entity
					content = matcher.group().substring(matcher.group().indexOf("|")+1, matcher.group().indexOf("]]"));
					
					//TODO check url exist then create object => if exist set prefix with content if not set dummy with content
					
					//Definition object
					dobjs.add(new DefinitionObject(begin, end, content, url));
					
					//Replace text
					input = input.replace(matcher.group(), matcher.group().substring(matcher.group().indexOf("|")+1, matcher.group().indexOf("]]")));
					
					//edit index down grade values
					new_start += 5+url_part.length();
					
				}
			}
		}
		
		//TODO speichere Doppelklammerfehler
		if(input.contains("[["))
		{
			//report!
			//replace
		}
		
		if(input.contains("]]"))
		{
			//report!
			//replace
		}
		
		setNot_annot_text(input);
		return dobjs;
	}
	
	
	/**
	 * Simple to String method for char arrays
	 * @param in
	 * @return a String
	 */
	public static String createSFL(LinkedList<Character> in)
	{
		String out = "";
		
		for (int i = 0; i < in.size(); i++) 
		{
			out += in.get(i);
		}
		
		return out;
	}
	
	//#############################################################################
	//########################## GETTERS & SETTERS ################################
	//#############################################################################
	
	public String getNot_annot_text() {
		return not_annot_text;
	}
	
	public void addTo_Not_annot_text(String in){
		if(this.not_annot_text != null){
			this.not_annot_text += "\n"+in;
		}else{
			this.not_annot_text = "";
			this.not_annot_text += in;
		}
		
	}

	public void setNot_annot_text(String not_annot_text) {
		this.not_annot_text = not_annot_text;
	}
	
	//#############################################################################
	//############################### EXAMPLE #####################################
	//#############################################################################
	
	/*
	 * EXAMPLE of USE
	 */
	public static void main(String[] args) throws IOException
	{
		TextReader tr = new TextReader();
		String infile_name = "epoch15.txt";
		String path = tr.getResourceFileAbsolutePath(infile_name);
		String input = TextReader.fileReader(path);
		GatherAnnotationInformations gai = new GatherAnnotationInformations();
		
		LinkedList<DefinitionObject> dobjs = gai.gatherDefsFast(input);
		
		System.out.println(input);
		System.out.println(gai.getNot_annot_text());
		
		for (int i = 0; i < dobjs.size(); i++) 
		{
			System.out.println("ELEMENT: "+(i+1));
			System.out.println("T: "+gai.getNot_annot_text().substring(dobjs.get(i).getStartPos(), dobjs.get(i).getEndPos()));
			System.out.print("S: "+dobjs.get(i).getStartPos()+" | ");
			System.out.print("E: "+dobjs.get(i).getEndPos()+" | ");
			System.out.print("L: "+(dobjs.get(i).getEndPos()-dobjs.get(i).getStartPos())+" | ");
			System.out.print("W: "+dobjs.get(i).getContent()+" | ");
			System.out.println("U: "+dobjs.get(i).getEngWikiUrl()+"\n");
		}	
	}
}
