
package Frames;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class frSituazione extends javax.swing.JInternalFrame {
    public String user_home = null;                            
    public String first;
    public String MainDir;
    JSONParser jsonP = new JSONParser();
    
    JSONArray empList = new JSONArray();    
    JSONObject emps = new JSONObject(); 
    JSONObject empObj = new JSONObject();
    
    JSONArray lTodo = new JSONArray();
    JSONObject iToDo = new JSONObject(); 
    JSONObject oTodo = new JSONObject();  
    
    File dirArgomento;
    // json field of Followme 
    String IdRec;
    String DirName;
    String ArgName;
    String Semaforo;
    String LastUpd;
    
    // per id record in json file
    LocalDateTime adesso;
    String patGMA = "dd/MM/yyyy";
    SimpleDateFormat simpleDateGMA = new SimpleDateFormat(patGMA,Locale.getDefault());    
    
    // wa globale
    int RowIte;    
    
    public frSituazione() {
        initComponents();

        user_home = System.getenv("ROOT_Followme");  
        MainDir = user_home + "\\Followme\\Main";
        first = user_home + "\\Followme\\Followme.json";
        
        // centratura della finestra
        Dimension dim = getToolkit().getScreenSize();
        this.setLocation(
        dim.width / 2 - this.getWidth() / 2,
        dim.height / 2 - this.getHeight() / 2);          
        
        txRoot.setText(MainDir);        
        setNew();
        leggiArg();                         // popola tabella riepilogativa
    }
    private void leggiArg() {
    
        // azzerea tabella dei todo per progetto
         
        DefaultTableModel model = (DefaultTableModel)tbArg.getModel();
        model.setRowCount(0);           
        
        //Read JSON File        
        try(FileReader reader = new FileReader(first)){
            Object obj = jsonP.parse(reader);   // ha letto tutti i records
            empList = (JSONArray) obj;           // carica i records in array
            //Iterate over emp array
            empList.forEach(emp -> parseNoteObj((JSONObject)emp));  // per ogni record esegue
        }
        catch (FileNotFoundException e) {
             } catch (IOException | ParseException e) {
             }        
    } 
    private void parseNoteObj(JSONObject emp) {
        
        empObj = (JSONObject) emp.get("Arg");                  // legge tutti gli oggetti (record)
        // ripristino in i/o area dei dati progetto


        DirName = (String) empObj.get("DirName");        
        ArgName = (String) empObj.get("ArgName"); 
        IdRec = (String) empObj.get("IdRec"); 
        Semaforo = (String) empObj.get("Semaforo");
        LastUpd = (String) empObj.get("LastUpd");
        
        // popola tabella situazione
        DefaultTableModel model = (DefaultTableModel)tbArg.getModel();
        model.addRow(new Object []{DirName,
                                    ArgName,
                                    IdRec,
                                    Semaforo,
                                    LastUpd});              
    }    
    
    private void runbtJson() {

        dirArgomento = new File(MainDir + "\\" + txNewDirName.getText());
        
        
        if (ckDataUserOk()) {
             if (lbNew.isVisible()) {
                 dirArgomento.mkdir();
                 try {
                    makeFileJson();
                         // popola tabella riepilogativa
                 } catch (IOException ex) {
                     Logger.getLogger(frSituazione.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 addArgomento();
                 setNew();
                 leggiArg();                 
             }else{
                 updArgomento();
                 setNew();           
                 leggiArg();       
            }
        }
        txNewDirName.grabFocus();
    }
    
    private void addArgomento(){
        emps = new JSONObject(); 
        empObj = new JSONObject();

        // prepara record 
        emps.put("IdRec", IdRec);
        emps.put("DirName", DirName);
        emps.put("ArgName", ArgName);
        emps.put("Semaforo", "off");
        emps.put("LastUpd", LastUpd);
        
        empObj.put("Arg", emps);

        empList.add(empObj);             // aggiunge ad array        
  
        // scarica file aggiornato
        scarica();
        
        // aggiorna tabella argomenti
        leggiArg();        
    }
    
    private void scarica(){
        if (empList.size() > 0) {               // solo se records presenti
            // scarica file aggiornato
            try(FileWriter file = new FileWriter(first)){
                file.write(empList.toJSONString());
                file.flush();
            }
            catch(IOException e) {}            
        }else{
          JOptionPane.showMessageDialog(null,
          "Tentato scarico file  V U O T O !  Operazione non eseguita.",
          " A T T E N Z I O N E",
          JOptionPane.ERROR_MESSAGE); 
          txArgomento.requestFocus();
        }
        
//        resetTransito();
    }    

    private void updArgomento(){
        
        empList.forEach(emp -> parseProgetti((JSONObject)emp));  
        // scarica file aggiornato
        scarica(); 
    } 
    private void parseProgetti(JSONObject emp) {
        
        String idLetto;
        empObj = (JSONObject) emp.get("Arg");                   // legge tutti gli oggetti (record)
        idLetto = (String) empObj.get("IdRec"); 
        if (idLetto.equals(IdRec)) {                        // trovato aggiorna in array 
            empObj.put("ArgName", ArgName);
 
        }
    }        
    
    private void makeFileJson() throws IOException{
        // files ToDos 
        String lPath = MainDir + "\\" + txNewDirName.getText() + "\\ToDos.json";
        File todo = new File(lPath);
        if(!todo.exists()){
            try (   //todo.createNewFile();
                FileWriter fr = new FileWriter(lPath); 
                BufferedWriter br = new BufferedWriter(fr)) {
                br.write("[]");
            }            
            JOptionPane.showMessageDialog(null, lPath + " creato.");            
        } 
        
        // files Notes 
        lPath = MainDir + "\\" + txNewDirName.getText() + "\\Notes.json";
        File note = new File(lPath);
        if(!note.exists()){
            try (   //note.createNewFile();
                FileWriter fr1 = new FileWriter(lPath); 
                BufferedWriter br1 = new BufferedWriter(fr1)) { 
                br1.write("[]");            
            }
            JOptionPane.showMessageDialog(null, lPath + " creato.");            
        }         
    }
    
    private void setNew(){
        lbNew.setVisible(true);
        txNewDirName.setEditable(true);
        IdRec = "";
        DirName = "";
        ArgName = "";
        txArgomento.setText("");
        txNewDirName.setText("");
    }
    
    private boolean ckDataUserOk(){
        if (lbNew.isVisible()) {
            // Nome directory
            if (txNewDirName.getText().length()==0) {
              JOptionPane.showMessageDialog(null,
              "Nome directory non valida",
              "Controllo dati argomento",
              JOptionPane.ERROR_MESSAGE); 
              txNewDirName.requestFocus();
              return false;            
            }
            DirName = txNewDirName.getText().toUpperCase();

            if(dirArgomento.exists() && lbNew.isVisible()){
                JOptionPane.showMessageDialog(null,
                "la Directory esiste già",
                "Controllo dati argomento",
                JOptionPane.ERROR_MESSAGE); 
                txNewDirName.requestFocus();
                return false;                    
            }       
            if(!dirArgomento.exists() && !lbNew.isVisible()){
                JOptionPane.showMessageDialog(null,
                "questa Directory non esiste!",
                "Controllo dati argomento",
                JOptionPane.ERROR_MESSAGE); 
                txNewDirName.requestFocus();
                return false;                    
            }
        }
        // descrizione argoemento
        if (txArgomento.getText().length() == 0) {
            txArgomento.setText(txNewDirName.getText());
        }
        ArgName = txArgomento.getText();
        ArgName = txArgomento.getText().toUpperCase();
        
        // prepara id del record json 
        if (lbNew.isVisible()) {
            adesso = LocalDateTime.now ();
            IdRec = adesso.toString().substring(0, 10) + " " + adesso.toString().substring(11, 19); 
        }        
        LastUpd = IdRec;
        return true;
    }
    private void exSelRec(int row){
        // restore in area transito e display del record selezionato

        DirName = tbArg.getValueAt(row, 0).toString();
        ArgName = tbArg.getValueAt(row, 1).toString();
        IdRec = tbArg.getValueAt(row, 2).toString();
        
    // disabilita Add nuovo
        lbNew.setVisible(false);        
        txNewDirName.setText(DirName);
        txNewDirName.setEditable(false);
        txArgomento.setText(ArgName);
        txArgomento.requestFocus();

    }    
    public void firstAction(){
        txNewDirName.grabFocus();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txNewDirName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txArgomento = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txRoot = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbArg = new javax.swing.JTable();
        btJson = new javax.swing.JButton();
        lbNew = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnService = new javax.swing.JMenu();
        ckCustomNavigation = new javax.swing.JCheckBoxMenuItem();

        setClosable(true);
        setIconifiable(true);
        setTitle("Argomenti ");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
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
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        jLabel1.setText("Directory name");

        txNewDirName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txNewDirNameActionPerformed(evt);
            }
        });
        txNewDirName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txNewDirNameKeyPressed(evt);
            }
        });

        jLabel2.setText("Descrizione Argomento");

        txArgomento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txArgomentoKeyPressed(evt);
            }
        });

        jLabel3.setText("Root");

        txRoot.setEditable(false);
        txRoot.setBackground(new java.awt.Color(204, 204, 204));

        tbArg.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Directory", "Argomento", "Data creazione"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbArg.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                tbArgAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        tbArg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbArgMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbArg);

        btJson.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btJson.setForeground(new java.awt.Color(255, 0, 0));
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

        lbNew.setForeground(new java.awt.Color(255, 0, 51));
        lbNew.setText("New");

        mnService.setText("Service");

        ckCustomNavigation.setSelected(true);
        ckCustomNavigation.setText("Custom navigation");
        mnService.add(ckCustomNavigation);

        jMenuBar1.add(mnService);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btJson, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 674, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(15, 15, 15)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(txNewDirName, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(lbNew))
                                        .addComponent(txRoot, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(0, 0, Short.MAX_VALUE))
                                .addComponent(txArgomento)))))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txNewDirName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbNew, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txArgomento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btJson)
                .addGap(16, 16, 16)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btJsonActionPerformed
        // TODO add your handling code here:
        runbtJson();
    }//GEN-LAST:event_btJsonActionPerformed

    private void tbArgAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tbArgAncestorAdded
        
    }//GEN-LAST:event_tbArgAncestorAdded

    private void tbArgMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbArgMouseClicked
        // TODO add your handling code here:
        // TODO add your handling code here:
        RowIte = tbArg.rowAtPoint(evt.getPoint());
        exSelRec(RowIte);        
    }//GEN-LAST:event_tbArgMouseClicked

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        // TODO add your handling code here:

    }//GEN-LAST:event_formInternalFrameClosing

    private void txNewDirNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txNewDirNameKeyPressed
        if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {txArgomento.requestFocus(); return;}
            if (cod == 27) {btJson.requestFocus(); }
        }        
    }//GEN-LAST:event_txNewDirNameKeyPressed

    private void txNewDirNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txNewDirNameActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txNewDirNameActionPerformed

    private void txArgomentoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txArgomentoKeyPressed
        // TODO add your handling code here:
         if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {btJson.requestFocus(); return;}
            if (cod == 27) {txArgomento.requestFocus(); }
        } 
    }//GEN-LAST:event_txArgomentoKeyPressed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:
    
    }//GEN-LAST:event_formKeyPressed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_formInternalFrameClosed

    private void btJsonKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btJsonKeyPressed
        // TODO add your handling code here:
        if (ckCustomNavigation.isSelected()) {
            int cod=evt.getKeyCode();
            if (cod == 17) {txNewDirName.requestFocus(); return;}
            if (cod == 27) {txArgomento.requestFocus(); }
        }        
    }//GEN-LAST:event_btJsonKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btJson;
    private javax.swing.JCheckBoxMenuItem ckCustomNavigation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbNew;
    private javax.swing.JMenu mnService;
    private javax.swing.JTable tbArg;
    private javax.swing.JTextField txArgomento;
    private javax.swing.JTextField txNewDirName;
    private javax.swing.JTextField txRoot;
    // End of variables declaration//GEN-END:variables
}
