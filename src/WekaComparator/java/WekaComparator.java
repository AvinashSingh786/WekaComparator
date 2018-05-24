
import com.sun.org.apache.xpath.internal.SourceTree;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


class WekaComparator extends java.awt.Component{
    private Frame mainFrame;
    private Label headerLabel;
    private Label statusLabel;
    private Panel controlPanel;

    private WekaComparator() {
//        makeGUI();
        start_process();
    }

    private void makeGUI() {
        mainFrame = new Frame("Weka Comparator");
        mainFrame.setSize(400,400);
        mainFrame.setLayout(new GridLayout(3, 1));
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        headerLabel = new Label();
        headerLabel.setAlignment(Label.CENTER);
        statusLabel = new Label();
        statusLabel.setAlignment(Label.CENTER);
        statusLabel.setSize(350,100);

        controlPanel = new Panel();
        controlPanel.setLayout(new FlowLayout());

        mainFrame.add(headerLabel);
        mainFrame.add(controlPanel);
        mainFrame.add(statusLabel);
        mainFrame.setVisible(true);
    }
    private void start_process() {
        Scanner scan = new Scanner(System.in);
        System.out.println("=============================================================================================");
        System.out.println("\n" +
                " _       __     __            ______                                       __            \n" +
                "| |     / /__  / /______ _   / ____/___  ____ ___  ____  ____ __________ _/ /_____  _____\n" +
                "| | /| / / _ \\/ //_/ __ `/  / /   / __ \\/ __ `__ \\/ __ \\/ __ `/ ___/ __ `/ __/ __ \\/ ___/\n" +
                "| |/ |/ /  __/ ,< / /_/ /  / /___/ /_/ / / / / / / /_/ / /_/ / /  / /_/ / /_/ /_/ / /    \n" +
                "|__/|__/\\___/_/|_|\\__,_/   \\____/\\____/_/ /_/ /_/ .___/\\__,_/_/   \\__,_/\\__/\\____/_/     \n" +
                "                                               /_/                                       \n" +
                "                                                                       -- By Avinash Singh\n");
        System.out.println("=============================================================================================");
        System.out.println("\nWelcome to the Weka Comparator\n");
        System.out.print("Please choose input file type (0 - tree, 1 - part): ");
        int type = Integer.parseInt(scan.nextLine());

        System.out.print("Please choose TREE or PART file (pop-up window): ");
        JFileChooser fileChooser = new JFileChooser();
        if (type == 0)
            fileChooser.setDialogTitle("Open TREE file");
        else
            fileChooser.setDialogTitle("Open PART file");
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        FileNameExtensionFilter ffilter = new FileNameExtensionFilter("Text files", "txt");
        fileChooser.setFileFilter(ffilter);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
            String file = selectedFile.getAbsolutePath();
            String dfile = file;
//            check
            if (type == 0) {
                dfile = selectedFile.getParent() + "\\TREE_" + selectedFile.getName();
                System.out.println("Converted to TREE format at: " +dfile);
                writePart(split(readFile(file)), dfile);
            }

            System.out.print("Save as (xlsx): ");
            ffilter = new FileNameExtensionFilter("Excel files", "xlsx");
            fileChooser.setFileFilter(ffilter);
            fileChooser.setDialogTitle("Save PART as");

            result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String dest = fileChooser.getSelectedFile().getAbsolutePath()+".xlsx";
                System.out.println(dest);
                writeTable(dfile, dest, type);
                System.out.print("Choose Test data file (xlsx) (pop-up window): ");
                fileChooser.setDialogTitle("Open Test data file (xlsx)");
                result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String filter = fileChooser.getSelectedFile().getAbsolutePath();
                    System.out.println(filter);

                    System.out.print("Enter value to filter (>=): ");
                    String value = scan.nextLine();
                    System.out.println("");
                    System.out.print("Enter percentage success to filter (>= %): ");
                    String percentage = scan.nextLine();

                    if (!value.equals("") && !percentage.equals(""))
                        filterTable(dest, filter, value, percentage);
                    else
                        System.out.println("ERROR: Value or percentage cannot be empty");
                }
            }
        }

        System.out.println("Press [ENTER] to exit" );
        scan.nextLine();
    }

    private void getFile(){
        headerLabel.setText("Select TREE or PART text file: ");
        Button showFileDialogButton = new Button("Open File");
        showFileDialogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 start_process();
            }
        });

        controlPanel.add(showFileDialogButton);
        mainFrame.setVisible(true);
    }

    private String split(String tree){

        String[] lines = tree.split("\n");
        List<List<String>> lists = new ArrayList<List<String>>();
        for(String line : lines){
            List<String> temp = new ArrayList<String>();
            while(line.indexOf("|") != -1){
                temp.add("|");
                line = line.replaceFirst("\\|", "");
            }
            temp.add(line.trim());
            lists.add(temp);
        }

        for(int i = 0; i < 3; i++){
            lists.remove(0);
        }
        for(int i = 0; i < 4; i++){
            lists.remove(lists.size()-1);
        }
        List<String> substitutes = new ArrayList<String>();

        for(List<String> list : lists){
            for(int i = 0; i < list.size(); i++){
                if(!list.get(i).contains(":") && !list.get(i).equals("|") && !substitutes.contains(list.get(i))){
                    substitutes.add(list.get(i));
                }
            }
        }
        for(List<String> list : lists){
            for(int i = 0; i < list.size(); i++){
                if(list.get(i).equals("|")){
                    list.set(i, substitutes.get(i));
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for(List<String> list : lists){
            String line = "";
            for(String s : list){
                line = line + "\n"+s ;
            }
            if(line.endsWith(")")){
                sb.append(line+"\n");
            }
        }
        return sb.toString();
    }

    private String readFile(String filePath)
    {
        String content = "";
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
//            e.printStackTrace();
            System.out.println("ERROR: Cannot read file, please ensure that the file exists.");
        }
        return content;
    }

    private void writePart(String input, String fname)
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            bw.write(input);
            System.out.println("Successfully saved");
        } catch (IOException e) {
            System.out.println("ERROR: Could not write file, please ensure that the file is not open.");
//            e.printStackTrace();
        }
    }

    private void writeTable(String fname, String dest, int type) {
        try {
            BufferedReader br = null;
            FileReader fr = null;
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Sheet 1");
            int i = 0, k = 0;

            Row row;
            row = sheet.createRow(i++);
            row.createCell(k++).setCellValue("#");
            row.createCell(k++).setCellValue("Rule");
            row.createCell(k++).setCellValue("Training Label");
            row.createCell(k++).setCellValue("+");
            row.createCell(k++).setCellValue("-");
            row.createCell(k).setCellValue("%");

            fr = new FileReader(fname);
            br = new BufferedReader(fr);
            boolean flag = true;
//            System.out.println("#\tRule\tTraining Label\t+\t-\t%");
            String sCurrentLine, plus = "0", minus = "0", lab = "";
            StringBuilder rule = new StringBuilder();

            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains(":"))
                {
                    if (type == 0)
                        rule.append(" AND ");
                    else rule.append(" ");
                    rule.append(sCurrentLine.substring(0, sCurrentLine.indexOf(":")));

                    String tmp = sCurrentLine.substring(sCurrentLine.indexOf(":")+1);
                    lab = tmp.substring(0, tmp.indexOf("(")-1);
                    if (tmp.contains("/")) {
                        plus = tmp.substring(tmp.indexOf("(")+1, tmp.indexOf("/"));
                        minus = tmp.substring(tmp.indexOf("/")+1, tmp.indexOf(")"));
                    }
                    else {
                        plus = tmp.substring(tmp.indexOf("(")+1, tmp.indexOf(")"));
                        minus = "0";
                    }
                }
                else {
                    if (!sCurrentLine.isEmpty()) {
                        if (flag)
                        {
                            rule.append(sCurrentLine);
                            flag = false;
                        }
                        else {
                            if (type == 0)
                                rule.append(" AND ");
                            else rule.append(" ");

                            rule.append(sCurrentLine);
                        }
                    }
                }

                if (sCurrentLine.isEmpty() && rule.length() > 0) {
//                    System.out.println(i + "\t" + rule + "\t" + lab + "\t" + plus + "\t" + minus + "\t" + (Float.parseFloat(plus)/(Float.parseFloat(plus)+Float.parseFloat(minus)))*100.0);

                    k = 0;
                    row = sheet.createRow(i++);
                    row.createCell(k++).setCellValue(i-1);
                    row.createCell(k++).setCellValue(rule.length() > 32767 ? rule.substring(0, 32767 - 3) + "..." : rule.toString());
                    row.createCell(k++).setCellValue(lab.trim());
                    row.createCell(k++).setCellValue(Float.parseFloat(plus));
                    row.createCell(k++).setCellValue( Float.parseFloat(minus));
                    row.createCell(k).setCellValue((Float.parseFloat(plus)/(Float.parseFloat(plus)+Float.parseFloat(minus)))*100.0);
                    rule = new StringBuilder();
                    flag = true;
                }
            }
//            System.out.println(i + "\t" + rule + "\t" + lab + "\t" + plus + "\t" + minus + "\t" + (Float.parseFloat(plus)/(Float.parseFloat(plus)+Float.parseFloat(minus)))*100.0);
            k = 0;
            row = sheet.createRow(i++);
            row.createCell(k++).setCellValue(i-1);
            row.createCell(k++).setCellValue(rule.length() > 32767 ? rule.substring(0, 32767 - 3) + "..." : rule.toString());
            row.createCell(k++).setCellValue(lab.trim());
            row.createCell(k++).setCellValue(Float.parseFloat(plus));
            row.createCell(k++).setCellValue( Float.parseFloat(minus));
            row.createCell(k).setCellValue((Float.parseFloat(plus)/(Float.parseFloat(plus)+Float.parseFloat(minus)))*100.0);

            File d = new File(dest);
            d.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(d,false);
            workbook.write(outputStream);
            workbook.close();

        }
        catch (Exception e)
        {
//            e.printStackTrace();
            System.out.println("ERROR: An error occurred, please ensure the file is not open");
        }
    }

    private void filterTable(String fname, String filter, String input, String percentage) {
        try {

            FileInputStream excelFile = new FileInputStream(new File(filter));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
            ArrayList<ArrayList<String>> table = new ArrayList<>();
            ArrayList<String> headings = new ArrayList<>();
            int i = 0, k;
            while (iterator.hasNext()) {

                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                k = 0;

                while (cellIterator.hasNext()) {

                    Cell currentCell = cellIterator.next();
                    if (i == 0){
                        headings.add(currentCell.getStringCellValue());
                        table.add(new ArrayList<String>());
                    }
                    else {
                        switch(currentCell.getCellType()) {
                            case Cell.CELL_TYPE_BOOLEAN:
                                table.get(k).add(String.valueOf(currentCell.getBooleanCellValue()));
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                table.get(k).add(String.valueOf(currentCell.getNumericCellValue()));
                                break;
                            case Cell.CELL_TYPE_STRING:
                                table.get(k).add(String.valueOf(currentCell.getStringCellValue()));
                                break;
                        }
                    }
                    k++;
                }
                i++;
            }
//            System.out.println();
//            System.out.println(headings);

            //////////////////////////////////////////////////////////////////////////////////////////////////////
            ///////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////////////

            FileInputStream exRule = new FileInputStream(new File(fname));
            workbook = new XSSFWorkbook(exRule);
            datatypeSheet = workbook.getSheetAt(0);
            iterator = datatypeSheet.iterator();
            ArrayList<ArrayList<String>> ruleTable = new ArrayList<>();
            ArrayList<String> ruleHeadings = new ArrayList<>();
            i = 0;
            k = 0;
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                k = 0;

                while (cellIterator.hasNext()) {

                    Cell currentCell = cellIterator.next();
                    if (i == 0){
                        ruleHeadings.add(currentCell.getStringCellValue());
                        ruleTable.add(new ArrayList<String>());
                    }
                    else {
                        switch(currentCell.getCellType()) {
                            case Cell.CELL_TYPE_BOOLEAN:
                                ruleTable.get(k).add(String.valueOf(currentCell.getBooleanCellValue()));
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                ruleTable.get(k).add(String.valueOf(currentCell.getNumericCellValue()));
                                break;
                            case Cell.CELL_TYPE_STRING:
                                ruleTable.get(k).add(String.valueOf(currentCell.getStringCellValue()));
                                break;
                        }
                    }
                    k++;
                }
                i++;
            }

//            System.out.println(ruleHeadings);
            ArrayList<Integer> successIndex = new ArrayList<>();
            ArrayList<String> testLabels = new ArrayList<>(Collections.nCopies(ruleTable.get(1).size(), ""));
            for (int c = 0; c < ruleTable.get(1).size(); c++)
            {
                boolean success = true;
                String[] rules = ruleTable.get(1).get(c).split("AND");
                for (String r : rules) {
                    if (success) {
                        if (r.contains(">=")) {

                            String h = r.substring(0, r.indexOf(">="));
                            Float val = Float.parseFloat(r.substring(r.indexOf(">=") + 2));
                            int index = headings.indexOf(h.trim());
                            int tt = 0;
                            for (String t : table.get(index)){
                                success = Float.parseFloat(t) >= val;
                                if (success)
                                {
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt));
                                    break;
                                }
                                tt++;
                            }
                        } else if (r.contains("<=")) {

                            String h = r.substring(0, r.indexOf("<="));
                            Float val = Float.parseFloat(r.substring(r.indexOf("<=") + 2));
                            int index = headings.indexOf(h.trim());
                            int tt = 0;
                            for (String t : table.get(index)){
                                success = Float.parseFloat(t) <= val;
                                if (success)
                                {
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt));
                                    break;
                                }
                                tt++;
                            }

                        } else if (r.contains("<")) {

                            String h = r.substring(0, r.indexOf("<"));
                            Float val = Float.parseFloat(r.substring(r.indexOf("<") + 1));
                            int index = headings.indexOf(h.trim());
                            int tt = 0;
                            for (String t : table.get(index)){
                                success = Float.parseFloat(t) < val;
                                if (success)
                                {
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt));

                                }
                                tt++;
                            }

                        } else if (r.contains(">")) {

                            String h = r.substring(0, r.indexOf(">"));
                            Float val = Float.parseFloat(r.substring(r.indexOf(">") + 1));
                            int index = headings.indexOf(h.trim());
                            int tt = 0;
                            for (String t : table.get(index)){
                                success = Float.parseFloat(t) > val;
                                if (success)
                                {
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt));

                                }
                                tt++;
                            }
                        } else if (r.contains("=")) {

                            String h = r.substring(0, r.indexOf("="));
                            Float val = Float.parseFloat(r.substring(r.indexOf("=") + 1));
                            int index = headings.indexOf(h.trim());
                            int tt = 0;
                            for (String t : table.get(index)){
                                success = Float.parseFloat(t) == val;
                                if (success)
                                {
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt));

                                }
                                tt++;
                            }
                        }
                    }
                }
                if (success) {
                    successIndex.add(c);
//                    System.out.println("SI : "+c);
                }

            }

            ////////////////////////////////////////////////////////////////////////////////
            ////
            ///////////////////////////////////////////////////////////////////////////////

            XSSFWorkbook out = new XSSFWorkbook();
            XSSFSheet successSheet = out.createSheet("Success");
            XSSFSheet failedSheet = out.createSheet("Failed");
            int si = 0, sk = 0, fi = 0, fk = 0;

            Row row;
            row = successSheet.createRow(si++);
            row.createCell(sk++).setCellValue("#");
            row.createCell(sk++).setCellValue("Rule");
            row.createCell(sk++).setCellValue("Training Label");
            row.createCell(sk++).setCellValue("+");
            row.createCell(sk++).setCellValue("-");
            row.createCell(sk++).setCellValue("%");
            row.createCell(sk).setCellValue("Test Label");

            row = failedSheet.createRow(fi++);
            row.createCell(fk++).setCellValue("#");
            row.createCell(fk++).setCellValue("Rule");
            row.createCell(fk++).setCellValue("Training Label");
            row.createCell(fk++).setCellValue("+");
            row.createCell(fk++).setCellValue("-");
            row.createCell(fk++).setCellValue("%");
            row.createCell(fk).setCellValue("Test Label");

            int count = 0;
            for (String x: ruleTable.get(0)) {
                fk = 0;
                sk = 0;

                if (successIndex.contains(count)) {
                    row = successSheet.createRow(si++);
                    row.createCell(sk++).setCellValue(si-1);
                    row.createCell(sk++).setCellValue(ruleTable.get(1).get(count));
                    row.createCell(sk++).setCellValue(ruleTable.get(2).get(count));
                    row.createCell(sk++).setCellValue(ruleTable.get(3).get(count));
                    row.createCell(sk++).setCellValue(ruleTable.get(4).get(count));
                    row.createCell(sk++).setCellValue(ruleTable.get(5).get(count));
                    if (testLabels.size() > 0) {
                        if (testLabels.get(count).length() > 0) {
                            List<String> items = Arrays.asList(testLabels.get(count).substring(1).split(","));
                            Map<String, Long> result =
                                    items.stream().collect(
                                            Collectors.groupingBy(
                                                    Function.identity(), Collectors.counting()
                                            )
                                    );
                            row.createCell(sk).setCellValue(result.toString());
                        }
                    }
                }
                else {
                    row = failedSheet.createRow(fi++);
                    row.createCell(fk++).setCellValue(fi-1);
                    row.createCell(fk++).setCellValue(ruleTable.get(1).get(count));
                    row.createCell(fk++).setCellValue(ruleTable.get(2).get(count));
                    row.createCell(fk++).setCellValue(ruleTable.get(3).get(count));
                    row.createCell(fk++).setCellValue(ruleTable.get(4).get(count));
                    row.createCell(fk++).setCellValue(ruleTable.get(5).get(count));
                    if (testLabels.size() > 0) {
                        if (testLabels.get(count).length() > 0) {
                            List<String> items = Arrays.asList(testLabels.get(count).split(","));
                            Map<String, Long> result =
                                    items.stream().collect(
                                            Collectors.groupingBy(
                                                    Function.identity(), Collectors.counting()
                                            )
                                    );
                            row.createCell(fk).setCellValue(result.toString());
                        }
                    }
                }
                count++;
            }
            FileOutputStream outputStream = new FileOutputStream(fname +"_Compared.xlsx");
            out.write(outputStream);
            out.close();

            //////////////////////////////////////////////////////////////////////////////////
            ////////////
            //////////////////////////////////////////////////////////////////////////////////

            XSSFWorkbook in = new XSSFWorkbook();
            successSheet = in.createSheet("Success");
            failedSheet = in.createSheet("Failed");
            si = 0;
            sk = 0;
            fi = 0;
            fk = 0;

            row = successSheet.createRow(si++);
            row.createCell(sk++).setCellValue("#");
            row.createCell(sk++).setCellValue("Rule");
            row.createCell(sk++).setCellValue("Training Label");
            row.createCell(sk++).setCellValue("+");
            row.createCell(sk++).setCellValue("-");
            row.createCell(sk++).setCellValue("%");
            row.createCell(sk++).setCellValue("Test Label");
            row.createCell(sk++).setCellValue("User input");
            row.createCell(sk).setCellValue("User Percentage");

            row = failedSheet.createRow(fi++);
            row.createCell(fk++).setCellValue("#");
            row.createCell(fk++).setCellValue("Rule");
            row.createCell(fk++).setCellValue("Training Label");
            row.createCell(fk++).setCellValue("+");
            row.createCell(fk++).setCellValue("-");
            row.createCell(fk++).setCellValue("%");
            row.createCell(fk++).setCellValue("Test Label");
            row.createCell(fk++).setCellValue("User input");
            row.createCell(fk).setCellValue("User Percentage");

            count = 0;
            for (String x: ruleTable.get(0)) {
                fk = 0;
                sk = 0;

                if (successIndex.contains(count)) {
                    if (Float.parseFloat(ruleTable.get(3).get(count)) >= Float.parseFloat(input)) {
                        if (Float.parseFloat(ruleTable.get(5).get(count)) >= Float.parseFloat(percentage)) {
                            row = successSheet.createRow(si++);
                            row.createCell(sk++).setCellValue(si - 1);
                            row.createCell(sk++).setCellValue(ruleTable.get(1).get(count));
                            row.createCell(sk++).setCellValue(ruleTable.get(2).get(count));
                            row.createCell(sk++).setCellValue(ruleTable.get(3).get(count));
                            row.createCell(sk++).setCellValue(ruleTable.get(4).get(count));
                            row.createCell(sk++).setCellValue(ruleTable.get(5).get(count));
                            if (testLabels.get(count).length() > 0) {
                                List<String> items = Arrays.asList(testLabels.get(count).substring(1).split(","));
                                Map<String, Long> result =
                                        items.stream().collect(
                                                Collectors.groupingBy(
                                                        Function.identity(), Collectors.counting()
                                                )
                                        );
                                row.createCell(sk++).setCellValue(result.toString());
                                row.createCell(sk++).setCellValue(Float.parseFloat(input));
                                row.createCell(sk).setCellValue(Float.parseFloat(percentage));
                            }
                        }
                    }
                }
                else {
                    if (Float.parseFloat(ruleTable.get(3).get(count)) >= Float.parseFloat(input)) {
                        if (Float.parseFloat(ruleTable.get(5).get(count)) >= Float.parseFloat(percentage)) {
                            row = failedSheet.createRow(fi++);
                            row.createCell(fk++).setCellValue(fi - 1);
                            row.createCell(fk++).setCellValue(ruleTable.get(1).get(count));
                            row.createCell(fk++).setCellValue(ruleTable.get(2).get(count));
                            row.createCell(fk++).setCellValue(ruleTable.get(3).get(count));
                            row.createCell(fk++).setCellValue(ruleTable.get(4).get(count));
                            row.createCell(fk++).setCellValue(ruleTable.get(5).get(count));
                            if (testLabels.get(count).length() > 0) {
                                List<String> items = Arrays.asList(testLabels.get(count).split(","));
                                Map<String, Long> result =
                                        items.stream().collect(
                                                Collectors.groupingBy(
                                                        Function.identity(), Collectors.counting()
                                                )
                                        );
                                row.createCell(fk++).setCellValue(result.toString());
                                row.createCell(fk++).setCellValue(Float.parseFloat(input));
                                row.createCell(fk).setCellValue(Float.parseFloat(percentage));
                            }
                        }
                    }
                }
                count++;
            }
            outputStream = new FileOutputStream(fname +"_Compared_"+input+"_"+percentage+"%_.xlsx");
            in.write(outputStream);
            in.close();
            System.out.println("Completed Successfully");


        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ERROR: Test Data columns does not match any rule");
//            e.printStackTrace();
        }
        catch (FileNotFoundException e) {
            System.out.println("ERROR: File does not exist or is currently open. Please close the file and try again");
//            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("ERROR: Could not write the file, please ensure that file is not open. Please close the file and try again");
//            e.printStackTrace();
        }
        catch (Exception e)
        {
            System.out.println("ERROR: An Error occurred.");
        }

    }


    public static void main(String[] args) {
        WekaComparator wc = new WekaComparator();
    }

}