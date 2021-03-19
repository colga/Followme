
package Frames;

import util.util;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class frArgomento extends javax.swing.JInternalFrame {
    
    public String IdDir;                // nome directory argotno (per Path)
    public String IdRec;                // rec json file interessato (per semaforo)
    util Myutil = new util();
    String    PathDir;
    String    PathToDos;
    String    PathNotes;

    JSONParser jsonP = new JSONParser();
    
    JSONArray todoList = new JSONArray();    
    JSONObject todoObj = new JSONObject();
    
    JSONArray noteList = new JSONArray();    
    JSONObject noteObj = new JSONObject();
    
    // i/o dati elementari controllati
    String DatAp;           // Data Apertura argomento (chiave per todo)
    String Descr;           // Descrizione argomento
    String Impor;           // Importanza argomento : (A)lta, (M)edia, (B)assa
    String Prior;           // Priorità argomento   : (A)lta, (M)edia, (B)assa
    String Stato;           // Stato argomento      : (A)perto, (C)hiuso
    String DatCh;           // Data chiusura Argomento
    String DatUA;           // Data Ultimo Aggiornamento ( in creazione = DatAp )
    
    String Titol;           // Descrizione del titolo note
    String NotAr;           // Nota sull'argomento
    String DatAn;           // Data Apertura nota (chiave per note)
    String DatNo;           // Data Ultimo aggiornamento nota ( in creazione = DatAn )
    
    // wa di servizio
    int RowIte;
    LocalDateTime adesso;
    boolean selFromtbTodos;
    boolean selFromtbNotes;
    
    public frArgomento() {
        
        initComponents();
        // centratura della finestra
        Dimension dim = getToolkit().getScreenSize();
        this.setLocation(
        dim.width / 2 - this.getWidth() / 2,
        dim.height / 2 - this.getHeight() / 2); 

        // formatta tabella todos
        tbTodos.getColumnModel().getColumn(0).setPreferredWidth(1000);       
        tbTodos.getColumnModel().getColumn(0).setHeaderValue("Descrizione");
        
        tbTodos.getColumnModel().getColumn(1).setPreferredWidth(100);       
        tbTodos.getColumnModel().getColumn(1).setHeaderValue("Imp");

        tbTodos.getColumnModel().getColumn(2).setPreferredWidth(100);       
        tbTodos.getColumnModel().getColumn(2).setHeaderValue("Pri");  
        
        tbTodos.getColumnModel().getColumn(3).setPreferredWidth(100);       
        tbTodos.getColumnModel().getColumn(3).setHeaderValue("Sts");        
        
        tbTodos.getColumnModel().getColumn(4).setPreferredWidth(100);        
        tbTodos.getColumnModel().getColumn(4).setHeaderValue("Data");
        
        tbTodos.getColumnModel().getColumn(5).setPreferredWidth(100);        
        tbTodos.getColumnModel().getColumn(5).setHeaderValue("Data Key");
        
        // formatta tabella notes 
        tbNotes.getColumnModel().getColumn(0).setPreferredWidth(1000);       
        tbNotes.getColumnModel().getColumn(0).setHeaderValue("Descrizione");
        
        tbNotes.getColumnModel().getColumn(1).setPreferredWidth(100);       
        tbNotes.getColumnModel().getColumn(1).setHeaderValue("Data");
        
        tbNotes.getColumnModel().getColumn(2).setPreferredWidth(100);       
        tbNotes.getColumnModel().getColumn(2).setHeaderValue("Data Key");

        tbNotes.getColumnModel().getColumn(3).setPreferredWidth(0);       
        tbNotes.getColumnModel().getColumn(3).setHeaderValue("Nota");
        
    }
    
    private void initCiclo(){
        
        selFromtbTodos = false;
        selFromtbNotes = false;
        
        DatAp = "";        
        Descr = "";           
        Impor = "";            
        Prior = "";             
        Stato = "";             
        DatCh = "";           
        DatUA = "";             
        Titol = "";          
        NotAr = "";             
        DatAn = "";             
        DatNo = "";
        
        txDescr.setText("");
        txTitol.setText("");
        txNotAr.setText("");
        cbImpor.setSelectedIndex(1);
        cbPrior.setSelectedIndex(1);
        cbStato.setSelectedIndex(0);
        leggiTodos();            
        leggiNotes();
    }
    
    private void ckinput(){
    
        // per To Do
        if ((txTitol.getText().length()==0 &&    // obbligato se solo todo
            txNotAr.getText().length()==0) || txDescr.getText().length() > 0){
            if (OkckinputTodo()){
                if (!selFromtbTodos){
                    exJsonTodoAdd();
                    boolean setLastUpd = Myutil.setLastUpd(IdDir, DatUA);
                }else{
                    exJsonTodoUpd();
                    boolean setLastUpd = Myutil.setLastUpd(IdDir, DatUA);
               }
            }
        }            
        // per Note (opzionale)
        if (txTitol.getText().length()!=0 || 
            txNotAr.getText().length()!=0){
            if (OkckinputNote()){
                if (!selFromtbTodos){
                    exJsonNoteAdd();
                    boolean setLastUpd = Myutil.setLastUpd(IdDir, DatNo);
                }else{
                    exJsonNoteUpd();
                    boolean setLastUpd = Myutil.setLastUpd(IdDir, DatNo);
                }
             }
        }
        initCiclo();           
    }
    

    private void leggiTodos(){
        // dispone ii todo del Json file in array e carica tabella
        // azzerea tabella dei todo 
        if (parRidotta.isSelected()) {
            exTodoRidotta();
        }else{
            exTodoEstesa();
        }
        DefaultTableModel modelTodo = (DefaultTableModel)tbTodos.getModel();
        modelTodo.setRowCount(0);
            
        FileReader reader;
        Object obj;

        try {
            reader = new FileReader(PathToDos);
            obj = jsonP.parse(reader);                                 
            todoList = (JSONArray) obj;                          
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Errore in lettura " + PathToDos + " -> " + ex); 
        } catch (IOException | ParseException ex) {
            JOptionPane.showMessageDialog(null, "Errore in parsing " + PathToDos + " -> " + ex); 
        }
        
        todoList.forEach(rec -> GetTodosObj((JSONObject)rec));      // per ogni record esegue        

        
    }    
     private void GetTodosObj(JSONObject rec) {
        
        todoObj = (JSONObject) rec.get("Arg");                  
        
        DatAp = (String) todoObj.get("DatAp"); 
        Descr = (String) todoObj.get("Descr"); 
        Impor = (String) todoObj.get("Impor"); 
        Prior = (String) todoObj.get("Prior"); 
        Stato = (String) todoObj.get("Stato"); 
        DatCh = (String) todoObj.get("DatCh"); 
        DatUA = (String) todoObj.get("DatUA"); 
        
        if (parAperti.isSelected() && "C".equals(Stato)) {return;}
        
        DefaultTableModel modelTodo = (DefaultTableModel)tbTodos.getModel();
        modelTodo.addRow(new Object []{Descr, 
                                   Impor, 
                                   Prior,
                                   Stato,
                                   DatUA,
                                   DatAp});                
    }     
    
     private void exJsonTodoAdd(){
 
        DefaultTableModel modelTodo = (DefaultTableModel)tbTodos.getModel();
    
        JSONObject fields = new JSONObject(); 
        JSONObject recObj = new JSONObject();

        // prepara intestazione 
        fields.put("DatAp", DatAp);
        fields.put("Descr", Descr);
        fields.put("Impor", Impor);
        fields.put("Prior", Prior);
        fields.put("Stato", Stato);
        fields.put("DatCh", DatCh);
        fields.put("DatUA", DatUA);
        
        recObj.put("Arg", fields);
        todoList.add(recObj);                     
  
        // scarica file aggiornato
        scaricaTodo();         
        
    }   
    private void scaricaTodo(){
        if (todoList.size() > 0) {               
            // scarica file aggiornato
            try(FileWriter file = new FileWriter(PathToDos)){
                file.write(todoList.toJSONString());
                file.flush();
            }
            catch(IOException e) {}            
        }else{
          JOptionPane.showMessageDialog(null,
          "Tentato scarico file  V U O T O !  Operazione non eseguita.",
          " A T T E N Z I O N E",
          JOptionPane.ERROR_MESSAGE); 
          txDescr.requestFocus();
        }

    }      
     
     private void exJsonTodoUpd(){
        todoList.forEach(rec -> updTodo((JSONObject)rec));  // per ogni record esegue
        scaricaTodo();         
    }    
    private void updTodo(JSONObject rec) {
        
        String idLetto;
        todoObj = (JSONObject) rec.get("Arg");              // legge tutti gli oggetti (record)
        idLetto = (String) todoObj.get("DatAp"); 
        if (idLetto.equals(DatAp)) {                        // trovat, aggiorna in array 
            // argomento
            todoObj.put("Descr", Descr);
            todoObj.put("Impor", Impor); 
            todoObj.put("Prior", Prior); 
            todoObj.put("Stato", Stato); 
            todoObj.put("DatUA", DatUA); 
        }
    }    
    private void leggiNotes(){
        // dispone ii note del Json file in array e carica tabella
        // azzerea tabella delle note 
        if (parNoteRidotte.isSelected()) {
            exNoteRidotte();
        }else{
            exNoteEstese();
        }
        DefaultTableModel modelNote = (DefaultTableModel)tbNotes.getModel();
        modelNote.setRowCount(0);
            
        FileReader reader;
        Object obj;

        try {
            reader = new FileReader(PathNotes);
            obj = jsonP.parse(reader);                                 
            noteList = (JSONArray) obj;                          
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Errore in lettura " + PathNotes + " -> " + ex); 
        } catch (IOException | ParseException ex) {
            JOptionPane.showMessageDialog(null, "Errore in parsing " + PathNotes + " -> " + ex); 
        }
        
        noteList.forEach(rec -> GetNotesObj((JSONObject)rec));         
    } 
    private void GetNotesObj(JSONObject rec) {
        
        noteObj = (JSONObject) rec.get("Not");                  
        
        Titol = (String) noteObj.get("Titol"); 
        NotAr = (String) noteObj.get("NotAr"); 
        DatAn = (String) noteObj.get("DatAn"); 
        DatNo = (String) noteObj.get("DatNo"); 
 
        DefaultTableModel modelNote = (DefaultTableModel)tbNotes.getModel();
        modelNote.addRow(new Object []{Titol, 
                                    DatNo,
                                    DatAn,
                                    NotAr});                
    }      
    
    private void exJsonNoteAdd(){
        DefaultTableModel modelNote = (DefaultTableModel)tbNotes.getModel();
        
        JSONObject fields = new JSONObject(); 
        JSONObject recObj = new JSONObject();
        
        fields.put("DatAn", DatAn);
        fields.put("Titol", Titol);
        fields.put("NotAr", NotAr);
        fields.put("DatNo", DatNo);
        
        recObj.put("Not", fields);
        noteList.add(recObj); 

        // scarica file aggiornato
        scaricaNote();         
        
    }   
    private void scaricaNote(){
        if (noteList.size() > 0) {               
            // scarica file aggiornato
            try(FileWriter file = new FileWriter(PathNotes)){
                file.write(noteList.toJSONString());
                file.flush();
            }
            catch(IOException e) {}            
        }else{
          JOptionPane.showMessageDialog(null,
          "Tentato scarico file  V U O T O !  Operazione non eseguita.",
          " A T T E N Z I O N E",
          JOptionPane.ERROR_MESSAGE); 
          txTitol.requestFocus();
        }

    }      
    
    
    private void exJsonNoteUpd(){
        noteList.forEach(rec -> updNote((JSONObject)rec));  
        scaricaNote();        
    }    
    private void updNote(JSONObject rec) {
        
        String idLetto;
        noteObj = (JSONObject) rec.get("Not");              // legge tutti gli oggetti (record)
        idLetto = (String) noteObj.get("DatAn"); 
        if (idLetto.equals(DatAn)) {                        // trovat, aggiorna in array 
            // argomento
            noteObj.put("Titol", Titol);
            noteObj.put("NotAr", NotAr); 
            noteObj.put("DatNo", DatNo); 
        }
    }    
   
    private boolean OkckinputTodo() {
        adesso = LocalDateTime.now ();
        
        // importanza todo
        if (cbImpor.getSelectedIndex()== 0) Impor = "A";
        if (cbImpor.getSelectedIndex()== 1) Impor = "M";
        if (cbImpor.getSelectedIndex()== 2) Impor = "B";

        // Priorità todo
        if (cbPrior.getSelectedIndex()== 0) Prior = "A";
        if (cbPrior.getSelectedIndex()== 1) Prior = "M";
        if (cbPrior.getSelectedIndex()== 2) Prior = "B";

        // Stato todo
        if (cbStato.getSelectedIndex()== 0) Stato = "A";
        if (cbStato.getSelectedIndex()== 1) Stato = "C";        
        
        // Descrione todo
        if (txDescr.getText().length()==0) {
          JOptionPane.showMessageDialog(null,
          "Descrizione argomento obbligatorio, se semza nota",
          "Controllo dati todo",
          JOptionPane.ERROR_MESSAGE); 
          txDescr.requestFocus();
          return false;
        }
        Descr = txDescr.getText();
        
        if (!selFromtbTodos) {
            DatAp  = adesso.toString().substring(0, 10) + " " + adesso.toString().substring(11, 19);         
            DatCh = " ";
            DatUA = DatAp;
        }else{
            DatUA  = adesso.toString().substring(0, 10) + " " + adesso.toString().substring(11, 19);         
            if ("C".equals(Stato)) {
               DatCh = adesso.toString().substring(0, 10) + " " + adesso.toString().substring(11, 19);
            }
        }
        
        return true;
    }
    private boolean OkckinputNote() {
        adesso = LocalDateTime.now ();

        // Descrione titolo note
        if (txTitol.getText().length()==0) {
          JOptionPane.showMessageDialog(null,
          "Descrizione titolo nota obbligatorio",
          "Controllo dati note",
          JOptionPane.ERROR_MESSAGE); 
          txTitol.requestFocus();
          return false;
        }        
        txTitol.setText(txTitol.getText().toUpperCase());
        Titol = txTitol.getText();

        // Descrione note
        if (txNotAr.getText().length()==0) {
          JOptionPane.showMessageDialog(null,
          "Descrizione nota obbligatorio",
          "Controllo dati note",
          JOptionPane.ERROR_MESSAGE); 
          txNotAr.requestFocus();
          return false;
        }        
        NotAr = txNotAr.getText();
        if (!selFromtbNotes) {
            DatAn  = adesso.toString().substring(0, 10) + " " + adesso.toString().substring(11, 19);         
            DatNo = DatAn;
        }else{
            DatNo  = adesso.toString().substring(0, 10) + " " + adesso.toString().substring(11, 19);         
    } 
    return true;        
}
    
    private void exTodoRidotta(){
        tbTodos.getColumnModel().getColumn(1).setMinWidth(0);
        tbTodos.getColumnModel().getColumn(1).setMaxWidth(0);
        tbTodos.getColumnModel().getColumn(1).setWidth(0);        
        
        tbTodos.getColumnModel().getColumn(2).setMinWidth(0);
        tbTodos.getColumnModel().getColumn(2).setMaxWidth(0);
        tbTodos.getColumnModel().getColumn(2).setWidth(0);        
        
        tbTodos.getColumnModel().getColumn(3).setMinWidth(0);
        tbTodos.getColumnModel().getColumn(3).setMaxWidth(0);
        tbTodos.getColumnModel().getColumn(3).setWidth(0);        
        
        tbTodos.getColumnModel().getColumn(4).setMinWidth(0);
        tbTodos.getColumnModel().getColumn(4).setMaxWidth(0);
        tbTodos.getColumnModel().getColumn(4).setWidth(0);        

        tbTodos.getColumnModel().getColumn(5).setMinWidth(0);
        tbTodos.getColumnModel().getColumn(5).setMaxWidth(0);
        tbTodos.getColumnModel().getColumn(5).setWidth(0);        
        
    }
    private void exNoteRidotte(){
        tbNotes.getColumnModel().getColumn(2).setMinWidth(0);
        tbNotes.getColumnModel().getColumn(2).setMaxWidth(0);
        tbNotes.getColumnModel().getColumn(2).setWidth(0);        
        
        tbNotes.getColumnModel().getColumn(3).setMinWidth(0);
        tbNotes.getColumnModel().getColumn(3).setMaxWidth(0);
        tbNotes.getColumnModel().getColumn(3).setWidth(0);        
     }
    private void exNoteEstese(){
        tbNotes.getColumnModel().getColumn(2).setMinWidth(1);
        tbNotes.getColumnModel().getColumn(2).setMaxWidth(1000);
        tbNotes.getColumnModel().getColumn(2).setPreferredWidth(100); 

        tbNotes.getColumnModel().getColumn(3).setMinWidth(1);
        tbNotes.getColumnModel().getColumn(3).setMaxWidth(1000);
        tbNotes.getColumnModel().getColumn(3).setPreferredWidth(100); 
     }  
    
    private void exTodoEstesa(){
        tbTodos.getColumnModel().getColumn(1).setMinWidth(1);
        tbTodos.getColumnModel().getColumn(1).setMaxWidth(1000);
        tbTodos.getColumnModel().getColumn(1).setPreferredWidth(100);       

        tbTodos.getColumnModel().getColumn(2).setMinWidth(1);
        tbTodos.getColumnModel().getColumn(2).setMaxWidth(1000);
        tbTodos.getColumnModel().getColumn(2).setPreferredWidth(100); 

        tbTodos.getColumnModel().getColumn(3).setMinWidth(1);
        tbTodos.getColumnModel().getColumn(3).setMaxWidth(1000);
        tbTodos.getColumnModel().getColumn(3).setPreferredWidth(100); 
 
        tbTodos.getColumnModel().getColumn(4).setMinWidth(1);
        tbTodos.getColumnModel().getColumn(4).setMaxWidth(1000);
        tbTodos.getColumnModel().getColumn(4).setPreferredWidth(100);         
    
        tbTodos.getColumnModel().getColumn(5).setMinWidth(1);
        tbTodos.getColumnModel().getColumn(5).setMaxWidth(1000);
        tbTodos.getColumnModel().getColumn(5).setPreferredWidth(100);     
    }    

    private void runOffSemaforo(){
        Myutil.SetSemaforo(IdRec, "off");
    }
    
    private void exSelTodo(int row){
        selFromtbTodos  = true;
        
        Descr = tbTodos.getValueAt(row, 0).toString();
        Impor = tbTodos.getValueAt(row, 1).toString();
        Prior = tbTodos.getValueAt(row, 2).toString(); 
        Stato = tbTodos.getValueAt(row, 3).toString();
        DatUA = tbTodos.getValueAt(row, 4).toString();
        DatAp = tbTodos.getValueAt(row, 5).toString();
 
        txDescr.setText(Descr);
        
          // importanza todo
        if ("A".equals(Impor)) cbImpor.setSelectedIndex(0);
        if ("M".equals(Impor)) cbImpor.setSelectedIndex(1);
        if ("B".equals(Impor)) cbImpor.setSelectedIndex(2);
        
        // Priorità todo
        if ("A".equals(Prior)) cbPrior.setSelectedIndex(0);
        if ("M".equals(Prior)) cbPrior.setSelectedIndex(1);
        if ("B".equals(Prior)) cbPrior.setSelectedIndex(2);
        
        // Stato todo
        if ("A".equals(Stato)) cbStato.setSelectedIndex(0);
        if ("C".equals(Stato)) cbStato.setSelectedIndex(1);
        
    }

    private void exSelNote(int row){
        selFromtbNotes  = true; 
        Titol = tbNotes.getValueAt(row, 0).toString();
        DatNo = tbNotes.getValueAt(row, 1).toString();
        DatAn = tbNotes.getValueAt(row, 2).toString();
        NotAr = tbNotes.getValueAt(row, 3).toString();
        txTitol.setText(Titol);
        txNotAr.setText(NotAr);
    }

   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem2 = new javax.swing.JCheckBoxMenuItem();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbTodos = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txDescr = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        cbImpor = new javax.swing.JComboBox<>();
        cbPrior = new javax.swing.JComboBox<>();
        cbStato = new javax.swing.JComboBox<>();
        btJson = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        txTitol = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbNotes = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        txNotAr = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu6 = new javax.swing.JMenu();
        ckCustomNavigation = new javax.swing.JCheckBoxMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        parAperti = new javax.swing.JCheckBoxMenuItem();
        parRidotta = new javax.swing.JCheckBoxMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        parNotaRidotta = new javax.swing.JMenu();
        parNoteRidotte = new javax.swing.JCheckBoxMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        jCheckBoxMenuItem2.setSelected(true);
        jCheckBoxMenuItem2.setText("jCheckBoxMenuItem2");

        setClosable(true);
        setIconifiable(true);
        setResizable(true);
        setFocusCycleRoot(false);
        setFocusable(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameActivated(evt);
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel2.setFocusCycleRoot(true);
        jPanel2.setFocusTraversalPolicyProvider(true);

        tbTodos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Descrizione", "Importanza", "Priorità", "Stato", "Data Var", "Data Crea"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbTodos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbTodosMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbTodos);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel4.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel4ComponentShown(evt);
            }
        });

        txDescr.setColumns(20);
        txDescr.setRows(5);
        txDescr.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txDescrMouseClicked(evt);
            }
        });
        txDescr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txDescrKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(txDescr);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Importanza");

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Priorità");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Stato");

        cbImpor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Alta", "Media", "Bassa" }));
        cbImpor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cbImporKeyPressed(evt);
            }
        });

        cbPrior.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Alta", "Madia", "Bassa" }));
        cbPrior.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cbPriorKeyPressed(evt);
            }
        });

        cbStato.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Aperto", "Chiuso" }));
        cbStato.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cbStatoKeyPressed(evt);
            }
        });

        btJson.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btJson.setForeground(new java.awt.Color(255, 0, 51));
        btJson.setText("Json");
        btJson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btJsonActionPerformed(evt);
            }
        });
        btJson.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btJsonKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbImpor, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cbPrior, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cbStato, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btJson)
                        .addContainerGap())))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(jLabel8)
                        .addComponent(jLabel9)
                        .addComponent(cbPrior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbStato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btJson))
                    .addComponent(cbImpor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        txTitol.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txTitolKeyPressed(evt);
            }
        });

        tbNotes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Titolo", "Ult Data", "Data Key", "Nota"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbNotes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbNotesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbNotes);

        txNotAr.setColumns(20);
        txNotAr.setRows(5);
        txNotAr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txNotArKeyPressed(evt);
            }
        });
        jScrollPane4.setViewportView(txNotAr);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txTitol, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 815, Short.MAX_VALUE)
            .addComponent(jScrollPane4)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txTitol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
        );

        jMenu6.setText("Service");

        ckCustomNavigation.setSelected(true);
        ckCustomNavigation.setText("Custom navigation");
        ckCustomNavigation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckCustomNavigationActionPerformed(evt);
            }
        });
        jMenu6.add(ckCustomNavigation);

        jMenuBar1.add(jMenu6);

        jMenu7.setText(" ");
        jMenuBar1.add(jMenu7);

        jMenu1.setText("ToDos");

        parAperti.setSelected(true);
        parAperti.setText("Solo Aperti");
        parAperti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parApertiActionPerformed(evt);
            }
        });
        jMenu1.add(parAperti);

        parRidotta.setSelected(true);
        parRidotta.setText("Ridotta");
        parRidotta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                parRidottaMouseClicked(evt);
            }
        });
        parRidotta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parRidottaActionPerformed(evt);
            }
        });
        jMenu1.add(parRidotta);

        jMenuBar1.add(jMenu1);
        jMenuBar1.add(jMenu2);

        jMenu3.setText(" ");
        jMenuBar1.add(jMenu3);

        parNotaRidotta.setText("Notes");

        parNoteRidotte.setSelected(true);
        parNoteRidotte.setText("Ridotta");
        parNoteRidotte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parNoteRidotteActionPerformed(evt);
            }
        });
        parNotaRidotta.add(parNoteRidotte);

        jMenuBar1.add(parNotaRidotta);

        jMenu4.setText(" ");
        jMenuBar1.add(jMenu4);

        jMenu5.setText(" ");
        jMenuBar1.add(jMenu5);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        // TODO add your handling code here:
        runOffSemaforo();
    }//GEN-LAST:event_formInternalFrameClosing

    private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
        // TODO add your handling code here:
    }//GEN-LAST:event_formInternalFrameActivated

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // TODO add your handling code here:

    }//GEN-LAST:event_formFocusGained

    private void tbTodosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbTodosMouseClicked
        // TODO add your handling code here:
        RowIte = tbTodos.rowAtPoint(evt.getPoint());
        exSelTodo(RowIte);        
    }//GEN-LAST:event_tbTodosMouseClicked

    private void tbNotesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbNotesMouseClicked
        // TODO add your handling code here:
        RowIte = tbNotes.rowAtPoint(evt.getPoint());
        exSelNote(RowIte);        

    }//GEN-LAST:event_tbNotesMouseClicked

    private void parRidottaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_parRidottaMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_parRidottaMouseClicked

    private void parRidottaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parRidottaActionPerformed
        // TODO add your handling code here:
        leggiTodos();
    }//GEN-LAST:event_parRidottaActionPerformed

    private void parApertiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parApertiActionPerformed
        // TODO add your handling code here:
        leggiTodos();
    }//GEN-LAST:event_parApertiActionPerformed

    private void parNoteRidotteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parNoteRidotteActionPerformed
        // TODO add your handling code here:
        leggiNotes();
    }//GEN-LAST:event_parNoteRidotteActionPerformed

    private void txDescrKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txDescrKeyPressed
        if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {cbImpor.requestFocus(); return;}
            if (cod == 27) {ckCustomNavigation.requestFocus(); }
        }
    }//GEN-LAST:event_txDescrKeyPressed

    private void txDescrMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txDescrMouseClicked
        // TODO add your handling code here:
        initCiclo();
    }//GEN-LAST:event_txDescrMouseClicked

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        // TODO add your handling code here:
        txDescr.requestFocus();
//        txDescr.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERS‌​AL_KEYS, null);
//        txDescr.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERS‌​AL_KEYS, null);
    }//GEN-LAST:event_formComponentShown

    private void jPanel4ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel4ComponentShown
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jPanel4ComponentShown

    private void ckCustomNavigationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckCustomNavigationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ckCustomNavigationActionPerformed

    private void cbImporKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cbImporKeyPressed
        // TODO add your handling code here:
        if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {cbPrior.requestFocus(); return;}
            if (cod == 27) {txDescr.requestFocus(); }
        }        
    }//GEN-LAST:event_cbImporKeyPressed

    private void cbPriorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cbPriorKeyPressed
        // TODO add your handling code here:
        // TODO add your handling code here:
        if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {cbStato.requestFocus(); return;}
            if (cod == 27) {cbImpor.requestFocus(); }
        }        
    }//GEN-LAST:event_cbPriorKeyPressed

    private void cbStatoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cbStatoKeyPressed
        // TODO add your handling code here:
        if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {btJson.requestFocus(); return;}
            if (cod == 27) {cbPrior.requestFocus(); }
        }         
    }//GEN-LAST:event_cbStatoKeyPressed

    private void btJsonKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btJsonKeyPressed
        // TODO add your handling code here:
        if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {txTitol.requestFocus(); return;}
            if (cod == 27) {cbStato.requestFocus(); }
        }        
    }//GEN-LAST:event_btJsonKeyPressed

    private void txTitolKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txTitolKeyPressed
        // TODO add your handling code here:
        if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {txNotAr.requestFocus(); return;}
            if (cod == 27) {btJson.requestFocus(); }
        }       
    }//GEN-LAST:event_txTitolKeyPressed

    private void txNotArKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txNotArKeyPressed
        // TODO add your handling code here:
        if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {txDescr.requestFocus(); return;}
            if (cod == 27) {txTitol.requestFocus(); }
        }         
    }//GEN-LAST:event_txNotArKeyPressed

    private void btJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btJsonActionPerformed
        // TODO add your handling code here:
        ckinput();
    }//GEN-LAST:event_btJsonActionPerformed
public void setFirst(String RecId, String DirId){
    IdDir = DirId;
    IdRec = RecId;
    PathDir = System.getenv("ROOT_Followme") + 
                   "\\Followme\\Main\\" + IdDir + "\\" ;

    PathToDos = System.getenv("ROOT_Followme") + 
                   "\\Followme\\Main\\" + IdDir + "\\Todos.json";

    PathNotes= System.getenv("ROOT_Followme") + 
                   "\\Followme\\Main\\" + IdDir + "\\Notes.json";
    initCiclo();

}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btJson;
    private javax.swing.JComboBox<String> cbImpor;
    private javax.swing.JComboBox<String> cbPrior;
    private javax.swing.JComboBox<String> cbStato;
    private javax.swing.JCheckBoxMenuItem ckCustomNavigation;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JCheckBoxMenuItem parAperti;
    private javax.swing.JMenu parNotaRidotta;
    private javax.swing.JCheckBoxMenuItem parNoteRidotte;
    private javax.swing.JCheckBoxMenuItem parRidotta;
    private javax.swing.JTable tbNotes;
    private javax.swing.JTable tbTodos;
    private javax.swing.JTextArea txDescr;
    private javax.swing.JTextArea txNotAr;
    private javax.swing.JTextField txTitol;
    // End of variables declaration//GEN-END:variables

 }

