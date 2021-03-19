
import util.util;
import Frames.frArgomento;
import Frames.frSituazione;
import Frames.frmDialog;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class MainFrame extends javax.swing.JFrame {
    
    util Myutil = new util();
    
    public String user_home = null;                            
    public String first;
    
    JSONParser jsonP = new JSONParser();
    
    JSONArray empList = new JSONArray();    
    JSONObject emps = new JSONObject(); 
    JSONObject empObj = new JSONObject();
    
    JSONArray lTodo = new JSONArray();
    JSONObject iToDo = new JSONObject(); 
    JSONObject oTodo = new JSONObject();   
    
    // json field
    String IdRec;
    String IdDir;
    String ArgName;
    String Semaforo;
    String LastUpd;
    
// wa globale
    int RowIte;      
    int NumArg = 0;
    
    public MainFrame() {
 
        initComponents();

        user_home = System.getenv("ROOT_Followme");     // da parametri di sistema
        first = user_home + "Followme\\Followme.json";
        
        // prepara se stesso
        setTitle("Follow Me Home" );
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // formatta tabella situazione argomenti

        tbArgomenti.getColumnModel().getColumn(0).setPreferredWidth(150);       
        tbArgomenti.getColumnModel().getColumn(0).setHeaderValue("Data");

        tbArgomenti.getColumnModel().getColumn(1).setPreferredWidth(100);       
        tbArgomenti.getColumnModel().getColumn(1).setHeaderValue("Directory");

        tbArgomenti.getColumnModel().getColumn(2).setPreferredWidth(1500);       
        tbArgomenti.getColumnModel().getColumn(2).setHeaderValue("Argomento");        
        
        tbArgomenti.getColumnModel().getColumn(3).setPreferredWidth(50);       
        tbArgomenti.getColumnModel().getColumn(3).setHeaderValue("Semaforo");        
        
        tbArgomenti.getColumnModel().getColumn(4).setPreferredWidth(150);       
        tbArgomenti.getColumnModel().getColumn(4).setHeaderValue("Last Upd");        
         
        if (user_home == null) {
            JOptionPane.showMessageDialog(null, "Manca variabile d'ambiente ROOT_Followme -> A b o r t !");
            System.exit(0);
        }

        // verifica infrastruttura dati
        String par = "T";
        if (OK_struttura(par) == false){
            int input = JOptionPane.showConfirmDialog(null,
                    "Infrastruttura mancante o incompleta, eseguo correzione automatica?",
                    "Seleziona una opzione...",            
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
            if (input == 1 || input == 2) {
                System.exit(0);
            }
            else{
                // aggiorna infrastruttura dati
                par = "U";
                if (OK_struttura(par) == false){
                    JOptionPane.showMessageDialog(null, "A b o r t !");
                    System.exit(0);}
                JOptionPane.showMessageDialog(null, "ok, fatto!");
            }
        }
            
        // leggi altri variabili dal file followme.json
        leggiFollowme();        

    }
 
    
    private void leggiFollowme(){
        // azzerea tabella dei todo per progetto
        DefaultTableModel model = (DefaultTableModel)tbArgomenti.getModel();
        model.setRowCount(0);     
        
 
        
        try {
            FileReader reader;            
            reader = new FileReader(first);
            Object obj;
            obj = jsonP.parse(reader);                          // ha letto tutti i records        
            empList = (JSONArray) obj;                          // carica i records in array
            //Iterate over emp array
            empList.forEach(emp -> parseObj((JSONObject)emp));  // per ogni record esegue        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        getColumnSortOrder();
    } 
   
    private void  getColumnSortOrder(){
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tbArgomenti.getModel());
        tbArgomenti.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys;
        sortKeys = new ArrayList<>(25);
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
    }
    private void parseObj(JSONObject emp) {
        
        empObj = (JSONObject) emp.get("Arg");                  // legge argomenti
        IdRec = (String) empObj.get("IdRec"); 
        IdDir = (String) empObj.get("DirName");        
        ArgName = (String) empObj.get("ArgName");
        Semaforo = (String) empObj.get("Semaforo");
        LastUpd = (String) empObj.get("LastUpd");
        
        NumArg = +1;
        DefaultTableModel model = (DefaultTableModel)tbArgomenti.getModel();
        model.addRow(new Object []{IdRec, 
                                   IdDir, 
                                   ArgName,
                                   Semaforo,
                                   LastUpd});                
    }   
    private void runSituazione(){

        frSituazione frsituazione = new frSituazione();
        Desktop.add(frsituazione);
        frsituazione.setVisible(true);
        frsituazione.firstAction();
    }
    
    private void runArgomento(){
        // legge lo stato del semaforo dell'argomento scelto
        if ("off".equals(Myutil.GetSemaforo(IdRec))) {
            frArgomento frargomento = new frArgomento();
            frargomento.setTitle(IdDir + " - " + ArgName);
            frargomento.setFirst(IdRec, IdDir);
            boolean ok = Myutil.SetSemaforo(IdRec, "on");
            if (!ok) {JOptionPane.showMessageDialog(null, "Semaforo in errore");
                      System.exit(0);}
            leggiFollowme();                     
            Desktop.add(frargomento);
            frargomento.setVisible(true);

        }else{
            JOptionPane.showMessageDialog(null, "Argomento già aperto!");}
    }    

 
    private boolean OK_struttura(String par){

        // dir principale applicazione sotto dati utente
        File dir_followme = new File(user_home + "\\Followme");
        if(!dir_followme.exists()){
            if ("T".equals(par)) {
               return false;
            }
            else{
                dir_followme.mkdir();
                JOptionPane.showMessageDialog(null, "Directory " + user_home + "\\Followme" + " creata.");        
             }
        }
        
        // file variabili di ambiente 
        File txt_followme = new File(user_home + "\\Followme" + "\\Followme.json");
        if(!txt_followme.exists()){
            if ("T".equals(par)) {
               return false;
            }
            else
                try {
                    txt_followme.createNewFile();
                    JOptionPane.showMessageDialog(null, "File Followme.txt in " + user_home + "\\Followme creato");
                } catch (IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }            
        }
        
        // dir main delle dir 
        File dir_main = new File(user_home + "\\Followme\\Main");
        if(!dir_main.exists()){
            if ("T".equals(par)) {
               return false;
            }
            else{
                dir_main.mkdir();
                JOptionPane.showMessageDialog(null, "Directory " + user_home + "\\Followme\\Main" + " creata.");        
             }
        }        
        return true;
    }
    
    private void exSelRec(int row){
        
        IdRec = tbArgomenti.getValueAt(row, 0).toString(); 
        IdDir = tbArgomenti.getValueAt(row, 1).toString();
        ArgName = tbArgomenti.getValueAt(row, 2).toString();

        runArgomento();
    }     

    private void runSpegniTutti(){
        boolean ok = Myutil.SetSemaforo("all", "off");
        if (!ok) {
            JOptionPane.showMessageDialog(null, "Semaforo in errore");
            System.exit(0);
        }       
}
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Desktop = new javax.swing.JDesktopPane();
        jpTabella = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbArgomenti = new javax.swing.JTable();
        btRefresh = new javax.swing.JButton();
        BtNewArgomento = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuBar1 = new javax.swing.JMenuBar();
        MenuFile = new javax.swing.JMenu();
        MenuExit = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        help = new javax.swing.JMenu();
        Informazioni = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
        });
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        Desktop.setToolTipText("");
        Desktop.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                DesktopFocusGained(evt);
            }
        });
        Desktop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                DesktopMouseEntered(evt);
            }
        });
        Desktop.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                DesktopComponentShown(evt);
            }
        });

        jpTabella.setBorder(javax.swing.BorderFactory.createTitledBorder("Situazione degli argomenti archiviati"));

        tbArgomenti.setAutoCreateRowSorter(true);
        tbArgomenti.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Data", "Directory", "Argomento", "Semaforo", "Last Upd"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbArgomenti.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbArgomentiMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbArgomenti);

        btRefresh.setText("Refresh");
        btRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRefreshActionPerformed(evt);
            }
        });

        BtNewArgomento.setText("New");
        BtNewArgomento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtNewArgomentoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpTabellaLayout = new javax.swing.GroupLayout(jpTabella);
        jpTabella.setLayout(jpTabellaLayout);
        jpTabellaLayout.setHorizontalGroup(
            jpTabellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpTabellaLayout.createSequentialGroup()
                .addGroup(jpTabellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(BtNewArgomento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1799, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 14, Short.MAX_VALUE))
        );
        jpTabellaLayout.setVerticalGroup(
            jpTabellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpTabellaLayout.createSequentialGroup()
                .addGroup(jpTabellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpTabellaLayout.createSequentialGroup()
                        .addComponent(btRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BtNewArgomento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                .addContainerGap())
        );

        Desktop.setLayer(jpTabella, javax.swing.JLayeredPane.DEFAULT_LAYER);
        Desktop.setLayer(jSeparator1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout DesktopLayout = new javax.swing.GroupLayout(Desktop);
        Desktop.setLayout(DesktopLayout);
        DesktopLayout.setHorizontalGroup(
            DesktopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DesktopLayout.createSequentialGroup()
                .addContainerGap(1863, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jpTabella, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        DesktopLayout.setVerticalGroup(
            DesktopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DesktopLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jpTabella, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(283, Short.MAX_VALUE))
        );

        MenuFile.setText("File");

        MenuExit.setText("Exit");
        MenuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuExitActionPerformed(evt);
            }
        });
        MenuFile.add(MenuExit);

        jMenuBar1.add(MenuFile);

        jMenu1.setText("    ");
        jMenuBar1.add(jMenu1);

        help.setText("?");
        help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });

        Informazioni.setText("Annotazioni");
        Informazioni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InformazioniActionPerformed(evt);
            }
        });
        help.add(Informazioni);

        jMenuBar1.add(help);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Desktop))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Desktop)
                .addGap(34, 34, 34))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void MenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuExitActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_MenuExitActionPerformed

    private void DesktopFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_DesktopFocusGained
        // TODO add your handling code here:
//        leggiFollowme();   
    }//GEN-LAST:event_DesktopFocusGained

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
        // TODO add your handling code here:
//        leggiFollowme(); 
    }//GEN-LAST:event_btRefreshActionPerformed

    private void tbArgomentiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbArgomentiMouseClicked
        // TODO add your handling code here:
        RowIte = tbArgomenti.rowAtPoint(evt.getPoint());
        exSelRec(RowIte);         
    }//GEN-LAST:event_tbArgomentiMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        runSpegniTutti();
    }//GEN-LAST:event_formWindowClosing

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // TODO add your handling code here:
   
    }//GEN-LAST:event_formFocusGained

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // TODO add your handling code here:

    }//GEN-LAST:event_formWindowActivated

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowGainedFocus

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
        // TODO add your handling code here:

    }//GEN-LAST:event_formMouseEntered

    private void DesktopMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_DesktopMouseEntered
        // TODO add your handling code here:
        leggiFollowme();
    }//GEN-LAST:event_DesktopMouseEntered

    private void BtNewArgomentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtNewArgomentoActionPerformed
        // TODO add your handling code here:
        runSituazione();
    }//GEN-LAST:event_BtNewArgomentoActionPerformed

    private void helpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpActionPerformed
        // TODO add your handling code here:
       
    }//GEN-LAST:event_helpActionPerformed

    private void InformazioniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InformazioniActionPerformed
        // TODO add your handling code here:
        frmDialog information = new frmDialog(this, true);
        information.setVisible(true);         
    }//GEN-LAST:event_InformazioniActionPerformed

    private void DesktopComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_DesktopComponentShown
        // TODO add your handling code here:
        
    }//GEN-LAST:event_DesktopComponentShown
 
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtNewArgomento;
    private javax.swing.JDesktopPane Desktop;
    private javax.swing.JMenuItem Informazioni;
    private javax.swing.JMenuItem MenuExit;
    private javax.swing.JMenu MenuFile;
    private javax.swing.JButton btRefresh;
    private javax.swing.JMenu help;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel jpTabella;
    private javax.swing.JTable tbArgomenti;
    // End of variables declaration//GEN-END:variables
}
