import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


class main {
    private static String split(String tree){

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

    private static String readFile(String filePath)
    {
        String content = "";
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return content;
    }

    private static void writePart(String input, String fname)
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("PART_"+fname))) {
            bw.write(input);
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeTable(String fname) {
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
            row.createCell(k++).setCellValue("Label");
            row.createCell(k++).setCellValue("+");
            row.createCell(k++).setCellValue("-");
            row.createCell(k).setCellValue("%");

            fr = new FileReader(fname);
            br = new BufferedReader(fr);
            boolean flag = true;
            System.out.println("#\tRule\tLabel\t+\t-\t%");
            String sCurrentLine, plus = "0", minus = "0", lab = "";
            StringBuilder rule = new StringBuilder();

            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains(":"))
                {
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
                            rule.append(" AND ");
                            rule.append(sCurrentLine);
                        }
                    }
                }

                if (sCurrentLine.isEmpty() && rule.length() > 0) {
                    System.out.println(i + "\t" + rule + "\t" + lab + "\t" + plus + "\t" + minus + "\t" + (Float.parseFloat(plus)/(Float.parseFloat(plus)+Float.parseFloat(minus)))*100.0);

                    k = 0;
                    row = sheet.createRow(i++);
                    row.createCell(k++).setCellValue(i-1);
                    row.createCell(k++).setCellValue(rule.length() > 32767 ? rule.substring(0, 32767 - 3) + "..." : rule.toString());
                    row.createCell(k++).setCellValue(lab.trim());
                    row.createCell(k++).setCellValue(Float.parseFloat(plus));
                    row.createCell(k++).setCellValue( Float.parseFloat(minus));;
                    row.createCell(k).setCellValue((Float.parseFloat(plus)/(Float.parseFloat(plus)+Float.parseFloat(minus)))*100.0);
                    rule = new StringBuilder();
                    flag = true;
                }
            }
            System.out.println(i + "\t" + rule + "\t" + lab + "\t" + plus + "\t" + minus + "\t" + (Float.parseFloat(plus)/(Float.parseFloat(plus)+Float.parseFloat(minus)))*100.0);
            k = 0;
            row = sheet.createRow(i++);
            row.createCell(k++).setCellValue(i-1);
            row.createCell(k++).setCellValue(rule.length() > 32767 ? rule.substring(0, 32767 - 3) + "..." : rule.toString());
            row.createCell(k++).setCellValue(lab.trim());
            row.createCell(k++).setCellValue(Float.parseFloat(plus));
            row.createCell(k++).setCellValue( Float.parseFloat(minus));
            row.createCell(k).setCellValue((Float.parseFloat(plus)/(Float.parseFloat(plus)+Float.parseFloat(minus)))*100.0);

            FileOutputStream outputStream = new FileOutputStream(fname+"table.xlsx");
            workbook.write(outputStream);
            workbook.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void filterTable(String fname, String filter, String input) {
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
            System.out.println();
            System.out.println(headings);

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

            System.out.println(ruleHeadings);
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
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt) + "_" + tt+1);
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
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt) + "_" + tt+1);
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
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt) + "_" + tt+1);

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
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt) + "_" + tt+1);

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
                                    testLabels.set(c, testLabels.get(c) + ", " + table.get(table.size()-1).get(tt) + "_" + tt+1);

                                }
                                tt++;
                            }
                        }
                    }
                }
                if (success) {
                    successIndex.add(c);
                    System.out.println("SI : "+c);
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
            row.createCell(sk++).setCellValue("Label");
            row.createCell(sk++).setCellValue("Test Label");
            row.createCell(sk++).setCellValue("+");
            row.createCell(sk++).setCellValue("-");
            row.createCell(sk).setCellValue("%");

            row = failedSheet.createRow(fi++);
            row.createCell(fk++).setCellValue("#");
            row.createCell(fk++).setCellValue("Rule");
            row.createCell(fk++).setCellValue("Label");
            row.createCell(fk++).setCellValue("Test Label");
            row.createCell(fk++).setCellValue("+");
            row.createCell(fk++).setCellValue("-");
            row.createCell(fk).setCellValue("%");

            int count = 0;
            for (String x: ruleTable.get(0)) {
                fk = 0;
                sk = 0;

                if (successIndex.contains(count)) {
                    row = successSheet.createRow(si++);
                    row.createCell(sk++).setCellValue(si-1);
                    row.createCell(sk++).setCellValue(ruleTable.get(1).get(count));
                    row.createCell(sk++).setCellValue(ruleTable.get(2).get(count));
                    row.createCell(sk++).setCellValue(testLabels.get(count).substring(1));
                    row.createCell(sk++).setCellValue(ruleTable.get(3).get(count));
                    row.createCell(sk++).setCellValue(ruleTable.get(4).get(count));
                    row.createCell(sk).setCellValue(ruleTable.get(5).get(count));
                }
                else {
                    row = failedSheet.createRow(fi++);
                    row.createCell(fk++).setCellValue(fi-1);
                    row.createCell(fk++).setCellValue(ruleTable.get(1).get(count));
                    row.createCell(fk++).setCellValue(ruleTable.get(2).get(count));
                    row.createCell(fk++).setCellValue(testLabels.get(count).substring(1));
                    row.createCell(fk++).setCellValue(ruleTable.get(3).get(count));
                    row.createCell(fk++).setCellValue(ruleTable.get(4).get(count));
                    row.createCell(fk).setCellValue(ruleTable.get(5).get(count));
                }
                count++;
            }
            FileOutputStream outputStream = new FileOutputStream(fname+"_Compared.xlsx");
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
            row.createCell(sk++).setCellValue("Label");
            row.createCell(sk++).setCellValue("Test Label");
            row.createCell(sk++).setCellValue("+");
            row.createCell(sk++).setCellValue("-");
            row.createCell(sk++).setCellValue("%");
            row.createCell(sk).setCellValue("Input Value");

            row = failedSheet.createRow(fi++);
            row.createCell(fk++).setCellValue("#");
            row.createCell(fk++).setCellValue("Rule");
            row.createCell(fk++).setCellValue("Label");
            row.createCell(fk++).setCellValue("Test Label");
            row.createCell(fk++).setCellValue("+");
            row.createCell(fk++).setCellValue("-");
            row.createCell(fk++).setCellValue("%");
            row.createCell(fk).setCellValue("Input Value");

            count = 0;
            for (String x: ruleTable.get(0)) {
                fk = 0;
                sk = 0;

                if (successIndex.contains(count)) {
                    if (Float.parseFloat(ruleTable.get(3).get(count)) < Float.parseFloat(input)) {
                        row = successSheet.createRow(si++);
                        row.createCell(sk++).setCellValue(si - 1);
                        row.createCell(sk++).setCellValue(ruleTable.get(1).get(count));
                        row.createCell(sk++).setCellValue(ruleTable.get(2).get(count));
                        row.createCell(sk++).setCellValue(testLabels.get(count).substring(1));
                        row.createCell(sk++).setCellValue(ruleTable.get(3).get(count));
                        row.createCell(sk++).setCellValue(ruleTable.get(4).get(count));
                        row.createCell(sk).setCellValue(ruleTable.get(5).get(count));
                    }
                }
                else {
                    if (Float.parseFloat(ruleTable.get(3).get(count)) < Float.parseFloat(input)) {
                        row = failedSheet.createRow(fi++);
                        row.createCell(fk++).setCellValue(fi - 1);
                        row.createCell(fk++).setCellValue(ruleTable.get(1).get(count));
                        row.createCell(fk++).setCellValue(ruleTable.get(2).get(count));
                        row.createCell(fk++).setCellValue(testLabels.get(count).substring(1));
                        row.createCell(fk++).setCellValue(ruleTable.get(3).get(count));
                        row.createCell(fk++).setCellValue(ruleTable.get(4).get(count));
                        row.createCell(fk).setCellValue(ruleTable.get(5).get(count));
                    }
                }
                count++;
            }
            outputStream = new FileOutputStream(fname+"_Compared_"+input+"_.xlsx");
            in.write(outputStream);
            in.close();



        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ERROR: Test Data columns does not match any rule");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        System.out.print("Enter file name for the TREE or PART: ");
//        Scanner scanner = new Scanner(System.in);
//        String file = scanner.nextLine();
        String file = "Rule-Tree2.txt";
        System.out.print("Enter file name for test dataset: ");
//        String filter = scanner.nextLine();
        String filter = "Copy of Sample2-dataset.xlsx";
//        writePart(split(readFile(file)), file);
//        writeTable("PART_"+file);
        System.out.print("Enter the value to filter by (<): ");
//        String value = scanner.nextLine();
        String value = "3.0";
        filterTable("PART_"+file+"table.xlsx", filter, value);

    }

}
