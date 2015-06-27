package loader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;


import application.Catalog;
import application.Question;


public class FilesystemLoader implements CatalogLoader {

    /**
    * RegEx to capture the question block.
    * <p>
    * Captures three groups:
    * <p>
    *  1. Group: Contains the question<br>
    *  2. Group (optional): Timeout<br>
    *  3. Group: Answer block (all possible answers)<br>
    */
    private static final String QUESTION_BLOCK_REGEX =
        "(.+)\n(?:TIMEOUT: ([0-9]+)\n)??((?:[+-] .+\n){4}?)";
    /**
     * RegEx captures the individual answers in the captured answer block
     * from the more general expression above.
     * <p>
     * There are two capture groups:
     * <p>
     *  1. Group: +/-, which states if the answer is true or false<br>
     *  2. Group: Contains the answer<br>
     */
    private static final String ANSWER_REGEX = "([+-]) (.+)\n";

    private final Pattern blockPattern = Pattern.compile(QUESTION_BLOCK_REGEX);
    private final Pattern questionPattern = Pattern.compile(ANSWER_REGEX);

    private File[] catalogDir;
    private final Map<String, Catalog> catalogs =
        new HashMap<String, Catalog>();
    private final String location;

    public FilesystemLoader(String location) {
        this.location = location;
    }

    @Override
    public Map<String, Catalog> getCatalogs() throws LoaderException {

        if (!catalogs.isEmpty()) {
            return catalogs;
        }

        // Construct URL for package location
        URL url = this.getClass().getClassLoader().getResource(location);

        File dir;
        try {
            // Make sure the Java package exists
            if (url != null) {
                dir = new File(url.toURI());
            } else {
                dir = new File("/");
            }
        } catch (URISyntaxException e) {
            // Try to load from the root of the classpath
            dir = new File("/");
        }

        /*
        // Add catalog files
        if (dir.exists() && dir.isDirectory()) {
            this.catalogDir = dir.listFiles(new CatalogFilter());
            for (File f : catalogDir) {
                catalogs.put(f.getName(),
                    new Catalog(f.getName(), new QuestionFileLoader(f)));
            }
        }
         */

        
        // catalog path on local machine
        // dir = new File("C:\\Users\\dschwenk\\Documents\\eclipse\\workspace\\Quiz-Challenge-Server\\catalogs");
        
        // (used absolute path as tomcat integrated in eclipse does not uses projectfolder/WEB-INF/classes as 'root' path (like 
        // later the Hochschule Tomcat), instead Tomcat program folder is used ("C:\Program Files (x86)\Tomcat\apache-tomcat-8.0.21-windows-x86\apache-tomcat-8.0.21\bin"))
        
        
        // get file names
        String[] files = listFilesInfolder(dir);

    	// Liste verwaltet die XML-Dateien (Fragenkataloge)
    	ArrayList<Document> xmlDocuments = new ArrayList<Document>();
    	
		// fuege XML-Dateien (Fragekataloge) zur ArrayList hinzu
		for(String filename : files){
			try {
				if(isXMLFile(filename)){
					xmlDocuments.add(new SAXBuilder().build(filename));
				}
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}    	
    	
		// fuege Kataloge aus der ArrayList der Katalogverwaltung hinzu
		for(Document doc : xmlDocuments){
			Element fragenkatalog = doc.getRootElement();
			catalogs.put(fragenkatalog.getAttributeValue("name"), new Catalog(fragenkatalog.getAttributeValue("name"), new QuestionFileLoader(doc)));
		}
		
        return catalogs;
    }
    
	/**
	 * Methode liest vorhandene Dateien in einem Verzeichnis aus 
	 * @param folder Verzeichnis in dem die Fragenkataloge liegen
	 * @return Stringarray mit den im uebergeben Verzeichnis enthaltenen Dateiname (Dateien)
	 */
	private String[] listFilesInfolder(File folder) {
		ArrayList<String> fileList = new ArrayList<String>();
    	// fuege der Liste alle Dateien aus dem Verzeichnis hinzu
		for (File fileEntry : folder.listFiles()) {
	    	fileList.add(fileEntry.getAbsolutePath());
	    }
	    return fileList.toArray(new String[fileList.size()]);
	}    
    

	/**
	 * Methode ueberprueft anhand der Dateiendung ob es sich um eine XML-Datei handelt
	 * @param xmlFile zu pruefende Datei
	 * @return true falls XML-Datei
	 */
	private boolean isXMLFile(String xmlFile) {
		String extension = "";

		// suche letzten '.' im Dateinamen
		int i = xmlFile.lastIndexOf('.');
		// sofern Punkt vorhanden, String zurecht schneiden
		if (i > 0) {
		    extension = xmlFile.substring(i+1);
		}
		// pruefe Dateieindung
		if(extension.equals("xml")){
			return true;			
		} else {
			return false;	
		}
	}
	

    @Override
    public Catalog getCatalogByName(String name) throws LoaderException {
        if (catalogs.isEmpty()) {
            getCatalogs();
        }

        return this.catalogs.get(name);
    }

    /**
     * Filter class for selecting only files with a .cat extension.
     *
     * @author Simon Westphahl
     *
     */
    private class CatalogFilter implements FileFilter {

        /**
         * Accepts only files with a .cat extension.
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname.isFile() && pathname.getName().endsWith(".cat"))
                return true;
            else
                return false;
        }

    }

    private class QuestionFileLoader implements QuestionLoader {

        //private final File catalogFile;
    	private final Document catalogDocument;
    	private final List <Question> questions = new ArrayList<Question>();


        public QuestionFileLoader(Document file) {
        	catalogDocument = file;
        }
        /*
        public QuestionFileLoader(Document doc) {
        	catalogDocument = doc;
        }    	
        */
        @Override
        public List<Question> getQuestions(Catalog catalog)
            throws LoaderException {

            if (!questions.isEmpty()) {
                return questions;
            }

            Element fragenkatalog = catalogDocument.getRootElement();
            
            /*
            Scanner scanner;
            try {
                scanner = new Scanner(catalogFile, "UTF-8");
            } catch (FileNotFoundException e) {
                throw new LoaderException();
            }

            // Search the whole file for questions
            for (String questionBlock = scanner.findWithinHorizon(blockPattern, 0);
                 questionBlock != null;
                 questionBlock = scanner.findWithinHorizon(blockPattern, 0)) {

                MatchResult m = scanner.match();
                Question question = new Question(m.group(1));

                // The 2nd group is optional
                if (m.group(2) != null) {
                    question.setTimeout(
                        new Integer(m.group(2)));
                }

                // Match the answers
                Matcher am = questionPattern.matcher(m.group(3));
                while (am.find()) {
                    if (am.group(1).equals("+")) {
                        question.addAnswer(am.group(2));
                    } else {
                        question.addBogusAnswer(am.group(2));
                    }
                }

                // Make sure the question is complete
                if (question.isComplete())
                    // Add some randomization
                    question.shuffleAnswers();
                    questions.add(question);
            }
            */
			for(Element questionElement : fragenkatalog.getChildren()) {
				Question question = new Question(questionElement.getChildText("frage"));				
				String timeout = questionElement.getAttributeValue("timeout");
				if(timeout == null){
					timeout = "10";					
				}
				question.setTimeout(Long.parseLong(timeout));
				for(Element answer : questionElement.getChildren("antwort")) {					
					if(answer.getAttributeValue("richtig").equals("true"))
						question.addAnswer(answer.getText());
					else
						question.addBogusAnswer(answer.getText());
				}
				questions.add(question);
			}            
            return questions;
        }

    }
}
