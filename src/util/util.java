/*
Dispone le seguenti utility:

1 - gestione semaforo su cartelle "Argomento"
    1.1 - on / off  a specifica cartella o a tutte
    1.2 - ritorna lo stato del semaforo di specifica cartella

*/
package util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class util {
    
    JSONParser jsonP = new JSONParser();
    
    JSONArray empList = new JSONArray();    
    JSONObject emps = new JSONObject(); 
    JSONObject empObj = new JSONObject();

    //  Path e field di "Arg" in Followme.json ---
                          
    String PathFollowmeJson;    
    
    public String IdRec;
    public String IdDir;
    String ArgName;
    String Semaforo;
    String LastUpd;
    
    boolean FollowmeJsonOk;
    // -------------------------------------
    
    public boolean SetSemaforo(String recArg, String Stato){
        ReadPathFollowmeJson();
        
        FollowmeJsonOk=false;
        // porta in memoria Foolowme.Json
        try {
            FileReader reader;            
            reader = new FileReader(PathFollowmeJson);
            Object obj;
            obj = jsonP.parse(reader);                                  
            empList = (JSONArray) obj;                          
            if (empList.size() == 0) {return true;}
            //Iterate over emp array
            empList.forEach(emp -> SetSemArgObj((JSONObject)emp, recArg, Stato)); 
            if (FollowmeJsonOk) {scaricaFollowmeJson();}
                        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return FollowmeJsonOk;
    }
    private void ReadPathFollowmeJson(){
        try {
            PathFollowmeJson = System.getenv("ROOT_Followme")
                    + "Followme\\Followme.json";            
            FileReader reader;
            reader = new FileReader(PathFollowmeJson);
            Object obj;
            obj = jsonP.parse(reader);                          
            empList = (JSONArray) obj;     
        } catch (FileNotFoundException ex) {
            Logger.getLogger(util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(util.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private void SetSemArgObj(JSONObject emp, String recArg, String Stato){
        String idLetto;
        empObj = (JSONObject) emp.get("Arg");                   
        idLetto = (String) empObj.get("IdRec"); 
        if ("all".equals(recArg)) {
            empObj.put("Semaforo", Stato);
            FollowmeJsonOk=true;
        }else{
            if (idLetto.equals(recArg)) {                        
                empObj.put("Semaforo", Stato);
                FollowmeJsonOk=true;
            }
        }        
    }
    private void scaricaFollowmeJson(){
        if (empList.size() > 0) {               // solo se records presenti
            // scarica file aggiornato
            try(FileWriter file = new FileWriter(PathFollowmeJson)){
                file.write(empList.toJSONString());
                file.flush();
            }
            catch(IOException e) {}            
        }else{
          JOptionPane.showMessageDialog(null,
          "Tentato scarico file  V U O T O !  Operazione non eseguita.",
          " A T T E N Z I O N E",
          JOptionPane.ERROR_MESSAGE); 
        }
    } 
    public String GetSemaforo(String recArg){
        Semaforo = "";
        ReadPathFollowmeJson(); 
        empList.forEach(emp -> parseCkSemaforo((JSONObject)emp, recArg));       
        return Semaforo;
    }
    private void parseCkSemaforo(JSONObject emp, String recArg) {
        empObj = (JSONObject) emp.get("Arg");                   
        String idLetto = (String) empObj.get("IdRec"); 
        if (idLetto.equals(recArg)) {                        
            Semaforo = (String) empObj.get("Semaforo");                   
        }
    }
    
    public String FileExtension(String FileName){
        String Extension = "";
        boolean dotTrue = false;
        String car; 
        car = FileName.substring(0, 1);
        long lenPath = FileName.length();
        for (int i = 0; i < lenPath; i ++) {
            car = FileName.substring(i, (i+1));
            if (".".equals(car)) {dotTrue = true;}
            if (dotTrue) {
                if (!".".equals(car)) {Extension = Extension + car;}
            }
        }
        return Extension;    
    }
    public boolean setLastUpd(String recArg, String lastupd){
        ReadPathFollowmeJson(); 
        
        FollowmeJsonOk=false;
        // porta in memoria Foolowme.Json
        try {
            FileReader reader;            
            reader = new FileReader(PathFollowmeJson);
            Object obj;
            obj = jsonP.parse(reader);                                  
            empList = (JSONArray) obj;                          
            if (empList.isEmpty()) {return false;}
            //Iterate over emp array
            empList.forEach(emp -> SetLastArgObj((JSONObject)emp, recArg, lastupd)); 
            if (FollowmeJsonOk) {scaricaFollowmeJson();}
                        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return FollowmeJsonOk;       
    }
    private void SetLastArgObj(JSONObject emp, String recArg, String lastupd){
        String idLetto;
        empObj = (JSONObject) emp.get("Arg");                   
        idLetto = (String) empObj.get("DirName"); 
        if (idLetto.equals(recArg)) {                        
            empObj.put("LastUpd", lastupd);
            FollowmeJsonOk=true;
        }
    }   
}
    
