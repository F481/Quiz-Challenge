import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Element;


/**
 * Webprogrammierung Praktikum - Aufgabe 2
 * 
 * Klasse zeigt für ein gegebenes Verzeichnis alle darin enthaltenen Fragekataloge an und
 * für einen ausgewählten Katalog, die darin enthaltenen Fragen
 * 
 * @author Gruppe Frick, Schwenk, Strohm
 */
public class catalogReader {

	// Liste verwaltet die XML-Dateien (Fragenkataloge)
	private ArrayList<Document> xmlDocuments;


	/**
	 * Methode fuegt Kataloge (XML-Dateien) zur Katalogverwaltung hinzu
	 */
	private void getXMLDocuments(){
		// Pfad zum Verzeichnis der die Fragekataloge beinhaltet
		String path = "../catalogs";
		File catalogFolder = new File(path);
		
		// Dateien in Verzeichnis auslesen
		String[] files = listFilesInfolder(catalogFolder);
		
		// fuege XML-Dateien (Fragekataloge) zur Katalogverwaltung hinz
		this.xmlDocuments = new ArrayList<Document>();
		for(String filename : files){
			try {
				if(isXMLFile(filename)){
					this.xmlDocuments.add(new SAXBuilder().build(filename));
				}
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}
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
	 * Methode gibt die verfuegbaren Kataloge aus
	 */
	public void printCatalogs(){
		// Kataloge auslesen
		this.getXMLDocuments();
		// gebe verfuegbare Kataloge aus
		System.out.println("Verfügbare Kataloge:");
		int i = 1;
		for(Document doc : this.xmlDocuments){
			Element fragenkatalog = doc.getRootElement();
			System.out.println(" - " + fragenkatalog.getAttributeValue("name"));
		}
	}


	/**
	 * Methode gibt Fragen + Antworten aus
	 * @param katalogwahl
	 */
	public void printQuestions(String katalogwahl){

		boolean catalogExists = false;

		// gehe Liste aller Kataloge durch
		for(Document xmlDocuments : this.xmlDocuments){
			Element fragenkatalog = xmlDocuments.getRootElement();
			// pruefe ob Katalogname uebereinstimmt
			if(fragenkatalog.getAttributeValue("name").equals(katalogwahl)){
				catalogExists = true;
				// Gebe Katalog + Fragenanzahl aus
				System.out.print("\nGewählter Katalog: " + katalogwahl);
				System.out.print("\nAnzahl Fragen: " + fragenkatalog.getAttributeValue("fragenanzahl") + "\n");				
				// gehe Fragen durch
				int i = 1;
				for (Element question : fragenkatalog.getChildren()){
					// gebe Frage aus
					System.out.print("\n\t[Frage " + i++ + "] " + question.getChildText("frage"));
					// gebe Timeout aus
					if(question.getAttributeValue("timeout") == null){
						System.out.println(" (Timeout: - )");
					} else {
						System.out.println(" (Timeout: " + question.getAttributeValue("timeout") + ")");						
					}					
					// gebe Antworten aus
					for (Element antwort : question.getChildren("antwort")){
						System.out.print("\t  - " + antwort.getText());
						if(antwort.getAttributeValue("richtig").equals("true")){
							System.out.print(" (richtig)\n");							
						} else {
							System.out.print("\n");							
						}						
					}
				}
			}
		}
		// Katalog existiert nicht
		if(catalogExists == false){
			System.out.println("\nGewählter Katalog nicht vorhanden!");
		}
	}


	/**
	 * Main-Methode
	 * @param args
	 */
	public static void main(String[] args) {

		catalogReader reader = new catalogReader();
		
		// gebe verfuegbare Kataloge aus
		reader.printCatalogs();
		
		// zu welchem Katalog sollen Fragen ausgegben werden
		System.out.print("\nZu welchem Katalog sollen die Fragen ausgegeben werden: ");
		Scanner scan = new Scanner(System.in);
		String katalogwahl = scan.nextLine();		
		
		// gebe Fragen zu gewaehltem Katalog aus
		reader.printQuestions(katalogwahl);
	}
}