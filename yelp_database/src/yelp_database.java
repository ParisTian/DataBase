
import java.awt.GridLayout;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author yuwentia
 */
public class yelp_database extends javax.swing.JFrame {
    
    
    String user_since_date  = "1990-01-01";
    String user_rcount_logic = ">";
    String user_friends_logic = ">";
    String user_star_logic = ">";
    String user_rcount_value_str = "0";
    int    user_rcount_value     = 0;
    String user_friends_value_str = "0";
    int    user_friends_value     = 0;
    String user_star_value_str = "0";
    float  user_star_value    = 0;    
    
    String select_attr_logic       = "AND";

    /**
     * Creates new form 
     */
    public yelp_database() {
        initComponents();
        resetUI();
        displayMainCate();
    }
    final  String URL = "jdbc:oracle:thin:@" + "localhost" + ":" + "1521" + ":" + "ParisDB";
    final  String USERNAME = "system";
    final  String PASSWORD = "1234";
    final  int MAX_RECORD = 1000;
    final  int verbose = 1;

    private Connection connectionToDB(){
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        System.out.println("CONNECT to Oracle Database:");
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            Connection c = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return c;
        } catch (SQLException ex) {
           System.out.println("Exception from func: - " + func_name);
           return null;
        }
    }
    private void closeConnection(Connection connect){
        System.out.println("CLOSE connection with Oracle Database:");
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        try {
            connect.close();
        } catch (SQLException ex) {
            System.out.println("ERROR from func: - " + func_name);
        }
    }

    private void resetUI(){
        jPanelMain.removeAll();
        jPanelMain.repaint();
        jPanelSub.removeAll();
        jPanelSub.repaint();
        jTextAreaQueryCmd.setText("");
        //jTextAreaRText.setText("");
        DefaultTableModel table1 = (DefaultTableModel)jTableBusinessSearch.getModel();
        table1.setRowCount(0);
        DefaultTableModel table2 = (DefaultTableModel)jTableReviewList.getModel();
        table2.setRowCount(0);

    }

    public void displayMainCate(){
        jTextAreaQueryCmd.setLineWrap(rootPaneCheckingEnabled);
        //jScrollPane5.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        System.out.println("Display Main Category:");
        Connection connect = connectionToDB();
        Statement  state;
        int count_mcat = 0;
        jCheckBoxMainCategory = new JCheckBox[30];
        try {
            state = connect.createStatement();
            ResultSet  query_result = state.executeQuery("SELECT DISTINCT main_cat FROM BCATEGORY");
            int count = 0;

            jPanelMain.setLayout(new GridLayout(0, 1));
           HashSet<String> main_cat_set = new HashSet<>();
            // get the main category information.
            while(query_result.next() && count++ < MAX_RECORD){
                String main_cat_str = query_result.getString(1);
                System.out.println(main_cat_str);
                String[] main_cat_array = main_cat_str.split("#");
                // the first string is empty because main_cat always start with #
                for(int i = 1; i < main_cat_array.length; i++) {
                    if(!main_cat_set.contains(main_cat_array[i])) {
                        main_cat_set.add(main_cat_array[i]);
                    }
                }
            }
            String[] main_cat_array = new String[main_cat_set.size()];
            int i = 0; 
            for(String str : main_cat_set) {
                main_cat_array[i++] = str;
            }
            
            Arrays.sort(main_cat_array);
            for(i = 0; i < main_cat_array.length; i++)            {
                // System.out.println(i + " " + main_cat_array[i]);
                jCheckBoxMainCategory[i] = new JCheckBox(main_cat_array[i]);
                jPanelMain.add(jCheckBoxMainCategory[i]);
                ActionListener monitor_main_cat = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        displaySubCategory(ae);
                    }            
                };
                jCheckBoxMainCategory[i].addActionListener(monitor_main_cat);
            }
            pack();
            query_result.close();
            state.close();
            connect.close();
        } catch (SQLException ex) {
            System.out.println("ERROR from func: - " + func_name);
        }
    }
    
    String main_cat_logic = select_attr_logic;// select_attr_logic;
    String main_cat_where = new String();
    public void displaySubCategory(ActionEvent ae){
        // Get the selected main category
        ArrayList<String> selected_mcat = new ArrayList<>();
        for(int i = 0; i < jCheckBoxMainCategory.length; i ++) {
            if(jCheckBoxMainCategory[i]!= null && jCheckBoxMainCategory[i].isSelected()) {
                selected_mcat.add(jCheckBoxMainCategory[i].getText());
            }
        }
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        System.out.println("Display Sub Category:");
        Connection connect = connectionToDB();
        Statement  state;
        int count_scat = 0;
        jCheckBoxSubCategory = new JCheckBox[1000];
        try {
            jPanelSub.removeAll();
            jPanelSub.repaint();
            jPanelSub.setLayout(new GridLayout(0, 1));
            HashSet<String> sub_cat_set = new HashSet<>();
            state = connect.createStatement();
            main_cat_where = new String();
            for(String mcat: selected_mcat) {
                main_cat_where = main_cat_where + " C.main_cat LIKE '%#"+mcat+"#%' " + select_attr_logic;
            }
            if(main_cat_where.length() > select_attr_logic.length()) {
                main_cat_where = main_cat_where.substring(0, main_cat_where.length() - select_attr_logic.length());
            } else {
                System.out.println("No main cat !!!!!");
                //jPanelSub.removeAll();
                connect.close();
                return;               
            }
            String main_cat_sql_cmd = "SELECT DISTINCT sub_cat FROM BCATEGORY C " + "WHERE (" + main_cat_where + " )";

            //main_cat_sql_cmd = main_cat_sql_cmd.substring(0, main_cat_sql_cmd.length() - main_cat_logic.length());
            System.out.println(main_cat_sql_cmd);
            jTextAreaQueryCmd.setText(main_cat_sql_cmd);
            ResultSet  query_result = state.executeQuery(main_cat_sql_cmd);
            int count = 0;
            // get the sub category information.
            while(query_result.next() && count++ < MAX_RECORD){
                String sub_cat_str = query_result.getString(1);
                System.out.println(sub_cat_str);
                String[] sub_cat_array = sub_cat_str.split("#");
                // the first string is empty because main_cat always start with #
                for(int i = 1; i < sub_cat_array.length; i++) {
                    if(!sub_cat_set.contains(sub_cat_array[i])) {
                        sub_cat_set.add(sub_cat_array[i]);
                    }
                }
            }
            
            cat_sql_cmd = "SELECT DISTINCT C.business_id FROM BCATEGORY C WHERE ";
            if(main_cat_where.length() != 0) {
                cat_sql_cmd = cat_sql_cmd + " " + main_cat_where;
            } else {
                cat_sql_cmd = new String();
            }

            String[] sub_cat_array = new String[sub_cat_set.size()];
            int i = 0; 
            for(String str : sub_cat_set) {
                sub_cat_array[i++] = str;
                // System.out.println("Sub Cate: " + str);
            }

            Arrays.sort(sub_cat_array);
            for(i = 0; i < sub_cat_array.length; i++){
                System.out.println(i + " " + sub_cat_array[i]);
                jCheckBoxSubCategory[i] = new JCheckBox(sub_cat_array[i]);
                jPanelSub.add(jCheckBoxSubCategory[i]);
                // System.out.println("Add " + jCheckBoxSubCategory[i].getText());        
                ActionListener monitor_sub_cat = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        selectSubCat(ae);
                    }            
                };
                jCheckBoxSubCategory[i].addActionListener(monitor_sub_cat);
            }
            pack();
            state.close();
            connect.close();
        } catch (SQLException ex) {
            System.out.println("ERROR from func: - " + func_name);
        }                      
        return;
    }
    
    String sub_cate_logic = select_attr_logic;
    String sub_cate_where = new String();
    String cat_sql_cmd = new String();
    public void selectSubCat(ActionEvent ae) {
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        ArrayList<String> selected_scat = new ArrayList<>();
        
        for(int i = 0; i < jCheckBoxSubCategory.length; i ++) {
            if(jCheckBoxSubCategory[i]!= null && jCheckBoxSubCategory[i].isSelected()) {
                selected_scat.add(jCheckBoxSubCategory[i].getText());
            }
        }
        sub_cate_where = new String();
        for(String scat: selected_scat) {
            sub_cate_where = sub_cate_where + " C.sub_cat LIKE '%#"+scat+"#%' " + select_attr_logic;
        }
        if(sub_cate_where.length() > select_attr_logic.length()) {
            sub_cate_where = sub_cate_where.substring(0, sub_cate_where.length() - select_attr_logic.length());
        }
        cat_sql_cmd = "SELECT DISTINCT C.business_id FROM BCATEGORY C WHERE ";
        if(main_cat_where.length() != 0) {
            cat_sql_cmd = cat_sql_cmd + " " + main_cat_where;
        }
        if(sub_cate_where.length() != 0) {
            cat_sql_cmd = cat_sql_cmd + " " + select_attr_logic + " " + sub_cate_where;
        }
        if(main_cat_where.length() == 0 && sub_cate_where.length() == 0) {
            cat_sql_cmd = new String();
        }
        jTextAreaQueryCmd.setText(cat_sql_cmd);
        //cat_sql_cmd = "SELECT DISTINCT C.business_id FROM BCATEGORY C WHERE (" + 
        //        main_cat_where + " ) " + select_attr_logic +" ( " + sub_cate_where + ")";
        // sub_cat_sql_cmd = sub_cat_sql_cmd.substring(0, sub_cat_sql_cmd.length() - sub_cate_logic.length());
        System.out.println(cat_sql_cmd);       

        return;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelCat = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanelMain = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanelSub = new javax.swing.JPanel();
        jComboBoxCIFromDay = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxCIToDay = new javax.swing.JComboBox<>();
        jComboBoxCIFromHour = new javax.swing.JComboBox<>();
        jComboBoxCIToHour = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxCILogic = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldCIValue = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jDateChooserUserSince = new com.toedter.calendar.JDateChooser();
        jComboBoxUCountLogic = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jTextFieldUCountValue = new javax.swing.JTextField();
        jComboBoxUFriendLogic = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldUFriendValue = new javax.swing.JTextField();
        jComboBoxUStarLogic = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldUStarValue = new javax.swing.JTextField();
        jComboBoxSelectLogic = new javax.swing.JComboBox<>();
        jButtonExecuteQuery = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jDateChooserReviewFrom = new com.toedter.calendar.JDateChooser();
        jLabel20 = new javax.swing.JLabel();
        jDateChooserReviewTo = new com.toedter.calendar.JDateChooser();
        jLabel21 = new javax.swing.JLabel();
        jComboBoxRStarLogic = new javax.swing.JComboBox<>();
        jLabel22 = new javax.swing.JLabel();
        jTextFieldRStarValue = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jComboBoxRVoteLogic = new javax.swing.JComboBox<>();
        jTextFieldRVoteValue = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextAreaQueryCmd = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableBusinessSearch = new javax.swing.JTable();
        jLabel18 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTableReviewList = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Main Category");

        jLabel2.setText("Business");

        jLabel3.setText("Sub Category");

        javax.swing.GroupLayout jPanelMainLayout = new javax.swing.GroupLayout(jPanelMain);
        jPanelMain.setLayout(jPanelMainLayout);
        jPanelMainLayout.setHorizontalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 137, Short.MAX_VALUE)
        );
        jPanelMainLayout.setVerticalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 312, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(jPanelMain);

        javax.swing.GroupLayout jPanelSubLayout = new javax.swing.GroupLayout(jPanelSub);
        jPanelSub.setLayout(jPanelSubLayout);
        jPanelSubLayout.setHorizontalGroup(
            jPanelSubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 133, Short.MAX_VALUE)
        );
        jPanelSubLayout.setVerticalGroup(
            jPanelSubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 312, Short.MAX_VALUE)
        );

        jScrollPane3.setViewportView(jPanelSub);

        javax.swing.GroupLayout jPanelCatLayout = new javax.swing.GroupLayout(jPanelCat);
        jPanelCat.setLayout(jPanelCatLayout);
        jPanelCatLayout.setHorizontalGroup(
            jPanelCatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanelCatLayout.createSequentialGroup()
                .addGroup(jPanelCatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelCatLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(65, 65, 65)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelCatLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanelCatLayout.setVerticalGroup(
            jPanelCatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCatLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelCatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelCatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jComboBoxCIFromDay.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Day", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" }));
        jComboBoxCIFromDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCIFromDayActionPerformed(evt);
            }
        });

        jLabel4.setText("CheckIn");

        jLabel5.setText("From");

        jLabel6.setText("To");

        jComboBoxCIToDay.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Day", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" }));
        jComboBoxCIToDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCIToDayActionPerformed(evt);
            }
        });

        jComboBoxCIFromHour.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Hour", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", " " }));
        jComboBoxCIFromHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCIFromHourActionPerformed(evt);
            }
        });

        jComboBoxCIToHour.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Hour", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", " " }));
        jComboBoxCIToHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCIToHourActionPerformed(evt);
            }
        });

        jLabel7.setText("Num Of Checkins:");

        jComboBoxCILogic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { ">, <, =", ">", "<", "=" }));
        jComboBoxCILogic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCILogicActionPerformed(evt);
            }
        });

        jLabel8.setText("Value");

        jTextFieldCIValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldCIValueActionPerformed(evt);
            }
        });

        jLabel9.setText("Users");

        jLabel10.setText("Member Since:");

        jLabel11.setText("Review Count:");

        jLabel12.setText("Num. of Friends:");

        jLabel13.setText("Avg. Starts:");

        jLabel14.setText("Select:");

        jDateChooserUserSince.setDateFormatString("yyyy-MM-dd");

        jComboBoxUCountLogic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { ">,<,=", ">", "<", "=" }));
        jComboBoxUCountLogic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxUCountLogicActionPerformed(evt);
            }
        });

        jLabel15.setText("Value:");

        jTextFieldUCountValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldUCountValueActionPerformed(evt);
            }
        });

        jComboBoxUFriendLogic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { ">,<,=", ">", "<", "=" }));
        jComboBoxUFriendLogic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxUFriendLogicActionPerformed(evt);
            }
        });

        jLabel16.setText("Value:");

        jComboBoxUStarLogic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { ">,<,=", ">", "<", "=" }));
        jComboBoxUStarLogic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxUStarLogicActionPerformed(evt);
            }
        });

        jLabel17.setText("Value:");

        jComboBoxSelectLogic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AND, OR between attributes", "AND", "OR" }));
        jComboBoxSelectLogic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSelectLogicActionPerformed(evt);
            }
        });

        jButtonExecuteQuery.setText("Execute Query");
        jButtonExecuteQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteQueryActionPerformed(evt);
            }
        });

        jLabel19.setText("From");

        jDateChooserReviewFrom.setDateFormatString("yyyy-MM-dd");

        jLabel20.setText("To");

        jDateChooserReviewTo.setDateFormatString("yyyy-MM-dd");

        jLabel21.setText("Stars:");

        jComboBoxRStarLogic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { ">,<,=", ">", "<", "=" }));
        jComboBoxRStarLogic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRStarLogicActionPerformed(evt);
            }
        });

        jLabel22.setText("Review:");

        jTextFieldRStarValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldRStarValueActionPerformed(evt);
            }
        });

        jLabel23.setText("Votes:");

        jComboBoxRVoteLogic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { ">,<,=", ">", "<", "=" }));
        jComboBoxRVoteLogic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRVoteLogicActionPerformed(evt);
            }
        });

        jTextFieldRVoteValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldRVoteValueActionPerformed(evt);
            }
        });

        jLabel24.setText("Value:");

        jLabel25.setText("Value:");

        jTextAreaQueryCmd.setColumns(20);
        jTextAreaQueryCmd.setRows(5);
        jScrollPane5.setViewportView(jTextAreaQueryCmd);

        jTableBusinessSearch.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Business ID", "Business Name", "City", "State", "Stars"
            }
        ));
        jTableBusinessSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableBusinessSearchMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(jTableBusinessSearch);

        jLabel18.setText("Review");

        jLabel26.setText("Business List:");

        jTableReviewList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "No.", "Review ID", "Date", "Stars", "Votes", "Text"
            }
        ));
        jScrollPane7.setViewportView(jTableReviewList);
        if (jTableReviewList.getColumnModel().getColumnCount() > 0) {
            jTableReviewList.getColumnModel().getColumn(0).setPreferredWidth(3);
            jTableReviewList.getColumnModel().getColumn(1).setPreferredWidth(26);
            jTableReviewList.getColumnModel().getColumn(2).setPreferredWidth(10);
            jTableReviewList.getColumnModel().getColumn(3).setPreferredWidth(3);
            jTableReviewList.getColumnModel().getColumn(4).setPreferredWidth(3);
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jDateChooserUserSince, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBoxUFriendLogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jComboBoxUStarLogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(19, 19, 19)
                                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextFieldUFriendValue, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                                    .addComponent(jTextFieldUStarValue)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jComboBoxUCountLogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextFieldUCountValue, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBoxSelectLogic, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jPanelCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel5)
                                            .addComponent(jLabel6)
                                            .addComponent(jComboBoxCIFromDay, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jComboBoxCIToDay, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jComboBoxCIFromHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jComboBoxCIToHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jLabel7)
                                    .addComponent(jComboBoxCILogic, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addGap(18, 18, 18)
                                        .addComponent(jTextFieldCIValue)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jDateChooserReviewTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jDateChooserReviewFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel23)
                                            .addComponent(jLabel24)
                                            .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTextFieldRStarValue, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jTextFieldRVoteValue, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jComboBoxRVoteLogic, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel20)
                                            .addComponent(jLabel22))
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jComboBoxRStarLogic, 0, 78, Short.MAX_VALUE)))
                                .addGap(31, 31, 31)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButtonExecuteQuery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(47, 47, 47))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane7)
                        .addContainerGap())
                    .addComponent(jScrollPane4)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jDateChooserUserSince, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jComboBoxUCountLogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(jTextFieldUCountValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabel5)
                                                    .addComponent(jLabel19))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jComboBoxCIFromDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jComboBoxCIFromHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addComponent(jDateChooserReviewFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel6)
                                            .addComponent(jLabel20))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jComboBoxCIToDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jComboBoxCIToHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jDateChooserReviewTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel7)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel21)
                                                .addComponent(jComboBoxRStarLogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(jLabel22))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jComboBoxCILogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextFieldRStarValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel25)))
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel8)
                                    .addComponent(jTextFieldCIValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel23)
                                    .addComponent(jComboBoxRVoteLogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextFieldRVoteValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel24)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addComponent(jButtonExecuteQuery, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jComboBoxUFriendLogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel16)
                                .addComponent(jTextFieldUFriendValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jComboBoxUStarLogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel17)
                                .addComponent(jTextFieldUStarValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(jComboBoxSelectLogic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    String checkin_to_day = "Sat";
    String checkin_to_hour = "23";
    int    checkin_to_hour_value = 6*24+23;
    private void jComboBoxCIToHourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCIToHourActionPerformed
        // TODO add your handling code here:
        checkin_to_hour = (String)jComboBoxCIToHour.getSelectedItem();
        checkin_to_hour_value = convertDayToHour(checkin_to_day, checkin_to_hour);
    }//GEN-LAST:event_jComboBoxCIToHourActionPerformed
    String checkin_value_str = "0";
    int    checkin_value   = 0;
    private void jTextFieldCIValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldCIValueActionPerformed
        // TODO add your handling code here:
        //checkin_value_str = jTextFieldCIValue.getText();
        if (checkin_value_str.length() != 0) {
            checkin_value   = Integer.valueOf(checkin_value_str);
        }        
    }//GEN-LAST:event_jTextFieldCIValueActionPerformed
    
    private void jButtonExecuteQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteQueryActionPerformed
        // TODO add your handling code here:
        getInpuFromUI();
        displaySearchResult();        
    }//GEN-LAST:event_jButtonExecuteQueryActionPerformed
    String final_query_cmd = new String();
    public void displaySearchResult() {
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        Object[] column_list = {"BusinessID", "Business name", "City", "State", "Star"};
        DefaultTableModel table = new DefaultTableModel(column_list, WIDTH);
        jTableBusinessSearch.setModel(table);
        //DefaultTableModel table = (DefaultTableModel)jTableBusinessSearch.getModel();
        //jTableBusinessSearch.setModel(table);

        Connection connect = connectionToDB();
        
        try {
            Statement state = connect.createStatement();
            if(cat_sql_cmd.length() != 0) {
                String checkin_query_cmd = "SELECT CI.business_id FROM CHECKIN CI ";
                checkin_query_cmd = checkin_query_cmd + " WHERE hour >= " + checkin_from_hour_value + " AND " + 
                        " hour <= " + checkin_to_hour_value + " AND CI.business_id IN ( " + cat_sql_cmd + " ) ";
                checkin_query_cmd = checkin_query_cmd + " GROUP BY CI.business_id " ;
                checkin_query_cmd = checkin_query_cmd + " HAVING SUM(CI.checkin_count) > " + checkin_value + " ";
                final_query_cmd = "SELECT B.business_id, B.name, B.city, B.state, B.stars FROM BUSINESS B WHERE B.business_id IN ( " + 
                                    checkin_query_cmd + " )";
                System.out.println("final_query_cmd: "+final_query_cmd);
                 jTextAreaQueryCmd.setText(final_query_cmd);
                 ResultSet business_search = state.executeQuery(final_query_cmd);
                 table.setRowCount(0);
                 while (business_search.next()) {
                     table.addRow(new Object[] { business_search.getString("business_id"), 
                                                 business_search.getString("name"), 
                                                 business_search.getString("city"),
                                                 business_search.getString("state"),
                                                 business_search.getInt("stars")});
                 }
            } else {
                // Users search
                final_query_cmd = "SELECT U.name, U.yelping_since, U.average_stars FROM YUSER U WHERE ";
                final_query_cmd = final_query_cmd + " U.yelping_since " +  " >= DATE'"+user_since_date +"'" ;
                final_query_cmd = final_query_cmd + " " + select_attr_logic + " U.review_count " + user_rcount_logic + " " + user_rcount_value;
                final_query_cmd = final_query_cmd + " " + select_attr_logic + " U.friends " + user_friends_logic + " " + user_friends_value;
                final_query_cmd = final_query_cmd + " " + select_attr_logic + " U.average_stars " + user_star_logic + " " + user_star_value;
                Object[] new_column_list = {"name", "Yelp Since", "Avrg_Star"};
                table = new DefaultTableModel(new_column_list, WIDTH);
                jTableBusinessSearch.setModel(table);
                System.out.println("final_query_cmd: "+final_query_cmd);
                jTextAreaQueryCmd.setText(final_query_cmd);
                ResultSet business_search = state.executeQuery(final_query_cmd);
                table.setRowCount(0);
                while (business_search.next()) {
                    table.addRow(new Object[] { business_search.getString("name"), 
                                                 business_search.getString("yelping_since"), 
                                                 business_search.getInt("average_stars")});
                }

            }
            state.close();
        } catch (SQLException ex) {
            System.out.println("ERROR from func: " + func_name);
        }
        closeConnection(connect);
    }
    
    String review_from_date = "1900-01-01";
    String review_to_date   = "2020-01-01";

    public void displayReview(String business_id){
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        // Get input from UI
        review_from_date = ((JTextField)jDateChooserReviewFrom.getDateEditor().getUiComponent()).getText();
        if(review_from_date.length() == 0) {
            review_from_date = "1900-01-01";
        }
        review_to_date   = ((JTextField)jDateChooserReviewTo.getDateEditor().getUiComponent()).getText();
        if(review_to_date.length() == 0) {
            review_to_date = "2900-01-01";
        } 
        review_star_value_str = jTextFieldRStarValue.getText();
        System.out.println("Update Review star value: " + review_star_value_str);
        if(review_star_value_str.length() != 0) {
            review_star_value   = Float.valueOf(review_star_value_str);
        }
        review_vote_value_str = jTextFieldRVoteValue.getText();
        if(review_vote_value_str.length() != 0) {
            review_vote_value = Integer.valueOf(review_vote_value_str);
        }            
        
        System.out.println("review_from_D: "+ review_from_date + " TO: " + review_to_date + " review star " + review_star_logic
                           + " " + review_star_value + " Vote value " + review_vote_logic + " " + review_vote_value);
        
        Connection connect = connectionToDB();        
        String query_business_review = "SELECT R.review_id, R.r_date, R.stars, R.votes, R.text FROM REVIEW R WHERE R.business_id = ";
        query_business_review = query_business_review + "'" + business_id + "'";
        // add time slot
        query_business_review = query_business_review + " AND R.r_date " + " > " 
                + "DATE'" + review_from_date + "'" + " AND R.r_date " + " < " +
                "DATE'" + review_to_date + "'" ;
        // add star, vote
        String review_star_vote = "( R.stars " + review_star_logic + " " + review_star_value + " "
                                    + select_attr_logic + " R.votes " + review_vote_logic + " " + review_vote_value + " )";
        query_business_review = query_business_review + " AND " + review_star_vote;
        jTextAreaQueryCmd.setText(query_business_review);
        System.out.println(query_business_review);
        DefaultTableModel table = (DefaultTableModel)jTableReviewList.getModel();
        jTableReviewList.setModel(table);
        try {
            Statement state = connect.createStatement();
            ResultSet business_review = state.executeQuery(query_business_review);
            int id = 1;
            StringBuilder review_text = new StringBuilder();
            table.setRowCount(0);
            int i = 1;
            while (business_review.next()) {
                table.addRow(new Object[] { i++,
                                            business_review.getString("review_id"), 
                                            business_review.getString("r_date").substring(0,10), 
                                            business_review.getString("stars"),
                                            business_review.getString("votes"),
                                            business_review.getString("text")});
            }

            state.close();
        } catch (SQLException ex) {
            System.out.println("ERROR from func: " + func_name);
        }         
        closeConnection(connect);
    }
    
    public void getInpuFromUI(){
        
        checkin_value_str = jTextFieldCIValue.getText();
        if (checkin_value_str.length() != 0) {
            checkin_value   = Integer.valueOf(checkin_value_str);
        }
        System.out.println("checkin_from D: " + checkin_from_day + " H: " + checkin_from_hour + 
                " to D: " + checkin_to_day + " H: " + checkin_to_hour + " checkin value " + checkin_logic + " " + checkin_value);
        
        String temp = ((JTextField)jDateChooserUserSince.getDateEditor().getUiComponent()).getText();
        if ( temp.length() != 0) {
            user_since_date  = temp;
        }


        user_rcount_value_str = jTextFieldUCountValue.getText();
        if(user_rcount_value_str.length() != 0) {        
            user_rcount_value = Integer.valueOf(user_rcount_value_str);
        }
        


        user_friends_value_str = jTextFieldUFriendValue.getText();
        if(user_friends_value_str.length() != 0) {
            user_friends_value = Integer.valueOf(user_friends_value_str);
        }
        user_star_value_str = jTextFieldUStarValue.getText();
        if(user_star_value_str.length() != 0) {        
            user_star_value = Float.valueOf(user_star_value_str);
        }
               
        System.out.println(" user_since_date: " + user_since_date + " review count: "+ user_rcount_logic + " "+user_rcount_value +
                " num of friends: " + user_friends_logic + " " + user_friends_value + " star : " + user_star_logic + " " + user_star_value);
        
        //System.out.println(" select_attr_logic: " + select_attr_logic);
    }
    
    String checkin_from_day = "Sun";
    String checkin_from_hour = "0";
    int    checkin_from_hour_value = 0;
    private void jComboBoxCIFromDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCIFromDayActionPerformed
        // TODO add your handling code here:
        checkin_from_day =  (String)jComboBoxCIFromDay.getSelectedItem();
        checkin_from_hour_value = convertDayToHour(checkin_from_day, checkin_from_hour);
    }//GEN-LAST:event_jComboBoxCIFromDayActionPerformed

    private int convertDayToHour(String day_str, String hour_str) {
        int day =   day_str.equals("Sun") ? 0 :
                    day_str.equals("Mon") ? 1 :
                    day_str.equals("Tus") ? 2 :
                    day_str.equals("Wed") ? 3 :
                    day_str.equals("Thu") ? 4 :
                    day_str.equals("Fri") ? 5 :
                    day_str.equals("Sat") ? 6 : 0;
        int hour = hour_str.length() == 0 ? 0 : Integer.valueOf(hour_str);
        return (day * 24 + hour);
    }
    private void jComboBoxSelectLogicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSelectLogicActionPerformed
        // TODO add your handling code here:
        select_attr_logic = (String)jComboBoxSelectLogic.getSelectedItem(); 
        resetUI();
        displayMainCate();

    }//GEN-LAST:event_jComboBoxSelectLogicActionPerformed
    String review_star_logic   = ">";
    private void jComboBoxRStarLogicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRStarLogicActionPerformed
        // TODO add your handling code here:
        review_star_logic = (String)jComboBoxRStarLogic.getSelectedItem();
    }//GEN-LAST:event_jComboBoxRStarLogicActionPerformed

    String checkin_logic   = ">";
    private void jComboBoxCILogicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCILogicActionPerformed
        // TODO add your handling code here:
        checkin_logic   = (String)jComboBoxCILogic.getSelectedItem();
    }//GEN-LAST:event_jComboBoxCILogicActionPerformed

    private void jComboBoxCIFromHourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCIFromHourActionPerformed
        // TODO add your handling code here:
        checkin_from_hour = (String)jComboBoxCIFromHour.getSelectedItem();
        checkin_from_hour_value = convertDayToHour(checkin_from_day, checkin_from_hour);
    }//GEN-LAST:event_jComboBoxCIFromHourActionPerformed

    private void jComboBoxCIToDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCIToDayActionPerformed
        // TODO add your handling code here:
        checkin_to_day = (String)jComboBoxCIToDay.getSelectedItem();
        checkin_to_hour_value = convertDayToHour(checkin_to_day, checkin_to_hour);
    }//GEN-LAST:event_jComboBoxCIToDayActionPerformed
    String review_star_value_str   = "0";
    float  review_star_value   = 0;
    private void jTextFieldRStarValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldRStarValueActionPerformed
        // TODO add your handling code here:
/*
        review_star_value_str = jTextFieldRStarValue.getText();
        System.out.println("Update Review star value: " + review_star_value_str);
        if(review_star_value_str.length() != 0) {
            review_star_value   = Float.valueOf(review_star_value_str);
        }
        */
    }//GEN-LAST:event_jTextFieldRStarValueActionPerformed
    
    String review_vote_logic   = ">";
    String review_vote_value_str   = "0";
    int    review_vote_value   = 0;
    private void jComboBoxRVoteLogicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRVoteLogicActionPerformed
        // TODO add your handling code here:
        review_vote_logic = (String)jComboBoxRVoteLogic.getSelectedItem();        
    }//GEN-LAST:event_jComboBoxRVoteLogicActionPerformed

    private void jTextFieldRVoteValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldRVoteValueActionPerformed
        // TODO add your handling code here:
        /*
        if(review_vote_value_str.length() != 0) {
            review_vote_value = Integer.valueOf(review_vote_value_str);
        }            
        */
    }//GEN-LAST:event_jTextFieldRVoteValueActionPerformed

    private void jTableBusinessSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableBusinessSearchMouseClicked
        // TODO add your handling code here:
        // int select_row = jTableBusinessSearch.getSelectedRow();
        String business_id = jTableBusinessSearch.getValueAt(jTableBusinessSearch.getSelectedRow(), 0).toString();
        displayReview(business_id);
        
    }//GEN-LAST:event_jTableBusinessSearchMouseClicked

    private void jComboBoxUCountLogicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxUCountLogicActionPerformed
        // TODO add your handling code here:
        user_rcount_logic  = (String)jComboBoxUCountLogic.getSelectedItem();
    }//GEN-LAST:event_jComboBoxUCountLogicActionPerformed

    private void jComboBoxUFriendLogicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxUFriendLogicActionPerformed
        // TODO add your handling code here:
        user_friends_logic  = (String)jComboBoxUFriendLogic.getSelectedItem();
    }//GEN-LAST:event_jComboBoxUFriendLogicActionPerformed

    private void jComboBoxUStarLogicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxUStarLogicActionPerformed
        // TODO add your handling code here:
        user_star_logic = (String)jComboBoxUStarLogic.getSelectedItem();
    }//GEN-LAST:event_jComboBoxUStarLogicActionPerformed

    private void jTextFieldUCountValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldUCountValueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldUCountValueActionPerformed

    /**
     * @param args the command line arguments
     */
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
            java.util.logging.Logger.getLogger(yelp_database.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(yelp_database.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(yelp_database.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(yelp_database.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new yelp_database().setVisible(true);
            }
        });
    }
    
    private JCheckBox  jCheckBoxMainCategory[];
    private JCheckBox  jCheckBoxSubCategory[];
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExecuteQuery;
    private javax.swing.JComboBox<String> jComboBoxCIFromDay;
    private javax.swing.JComboBox<String> jComboBoxCIFromHour;
    private javax.swing.JComboBox<String> jComboBoxCILogic;
    private javax.swing.JComboBox<String> jComboBoxCIToDay;
    private javax.swing.JComboBox<String> jComboBoxCIToHour;
    private javax.swing.JComboBox<String> jComboBoxRStarLogic;
    private javax.swing.JComboBox<String> jComboBoxRVoteLogic;
    private javax.swing.JComboBox<String> jComboBoxSelectLogic;
    private javax.swing.JComboBox<String> jComboBoxUCountLogic;
    private javax.swing.JComboBox<String> jComboBoxUFriendLogic;
    private javax.swing.JComboBox<String> jComboBoxUStarLogic;
    private com.toedter.calendar.JDateChooser jDateChooserReviewFrom;
    private com.toedter.calendar.JDateChooser jDateChooserReviewTo;
    private com.toedter.calendar.JDateChooser jDateChooserUserSince;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanelCat;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelSub;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTable jTableBusinessSearch;
    private javax.swing.JTable jTableReviewList;
    private javax.swing.JTextArea jTextAreaQueryCmd;
    private javax.swing.JTextField jTextFieldCIValue;
    private javax.swing.JTextField jTextFieldRStarValue;
    private javax.swing.JTextField jTextFieldRVoteValue;
    private javax.swing.JTextField jTextFieldUCountValue;
    private javax.swing.JTextField jTextFieldUFriendValue;
    private javax.swing.JTextField jTextFieldUStarValue;
    // End of variables declaration//GEN-END:variables
}
