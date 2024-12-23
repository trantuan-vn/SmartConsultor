package com.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ExcelToPostgres {

    public static void main(String[] args) {
        if (args.length < 8) {
            printUsage();
            System.exit(1);
        }

        // Đọc tham số từ dòng lệnh
        Map<String, String> params = parseArgs(args);

        String excelFilePath = params.get("path");
        String version = params.get("version");
        String dbUrl = params.get("dbUrl");
        String dbUser = params.get("dbUser");

        if (excelFilePath == null || version == null || dbUrl == null || dbUser == null) {
            System.err.println("Missing required arguments.");
            printUsage();
            System.exit(1);
        }

        // Nhập mật khẩu ở chế độ ẩn
        Console console = System.console();
        if (console == null) {
            System.err.println("No console available");
            System.exit(1);
        }

        char[] passwordChars = console.readPassword("Enter database password: ");
        String dbPassword = new String(passwordChars);

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {

            System.out.println("Processing Excel file version: " + version);

            // Lặp qua các sheet (bỏ qua sheet(0))
            for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                System.out.println("Processing sheet: " + sheet.getSheetName());

                // Lặp qua các dòng từ dòng 1 trở đi
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;

                    Cell cell = row.getCell(4); // Cột E là cột số 4 (index từ 0)
                    if (cell == null || cell.getCellType() != CellType.STRING) continue;

                    String sql = cell.getStringCellValue().trim();
                    if (!sql.isEmpty()) {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute(sql); // Thực thi câu SQL
                            System.out.println("Executed: " + sql);
                        } catch (Exception e) {
                            System.err.println("Error executing SQL: " + sql);
                            e.printStackTrace();
                        }
                    }
                }
            }
            System.out.println("Processing complete for version: " + version);

        } catch (IOException e) {
            System.err.println("Error reading Excel file.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Database connection error.");
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar demo-1.0-SNAPSHOT.jar --path [excelFilePath] --version [version] --dbUrl [dbUrl] --dbUser [dbUser]");
        System.out.println("Example: java -jar demo-1.0-SNAPSHOT.jar --path data.xlsx --version v1 --dbUrl jdbc:postgresql://localhost:5432/db --dbUser user");
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length && args[i].startsWith("--")) {
                params.put(args[i].substring(2), args[i + 1]);
            }
        }
        return params;
    }
}

