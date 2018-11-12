package be.mormont.iacf.boncom.export;

import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import be.mormont.iacf.boncom.data.OrderFormEntry;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;

/**
 * Date: 11-07-17
 * By  : Mormont Romain
 */
public class OrderFormXlsExporter implements Exporter<OrderForm> {
    private static int MAX_ENTRIES;
    private static int ROW_DATE, ROW_NAME, ROW_ADDRESS, ROW_CITY, ROW_PHONE1,
            ROW_PHONE2, ROW_CUSTOMER_NB, ROW_TARIF, ROW_TABLE_HEADERS,
            ROW_TABLE_FIRST_ENTRY, ROW_TOTAL, ROW_SIGN;

    private static int COL_REF = 0, COL_DESIGNATION = 1, COL_QUANTITY = 2, COL_UNIT_PRICE = 3, COL_TOTAL = 5;
    private static int[] ROWS, COLS;
    private static String EURO_CURRENCY_FORMAT = "#,##0.00\\ \"€\";\\-\\ #,##0.00\\ \"€\";\\-\\ \"€\"";

    static {
        MAX_ENTRIES = 29;
        ROW_DATE = 1;
        ROW_NAME = 3;
        ROW_ADDRESS = ROW_NAME + 1;
        ROW_CITY = ROW_ADDRESS + 1;
        ROW_PHONE1 = ROW_CITY + 1;
        ROW_PHONE2 = ROW_PHONE1 + 1;
        ROW_CUSTOMER_NB = ROW_PHONE2 + 1;
        ROW_TARIF = 9;
        ROW_TABLE_HEADERS = 11;
        ROW_TABLE_FIRST_ENTRY = ROW_TABLE_HEADERS + 1;
        ROW_TOTAL = ROW_TABLE_FIRST_ENTRY + MAX_ENTRIES + 1;
        ROW_SIGN = ROW_TOTAL + 2;
        ROWS = new int[] {
            ROW_DATE, ROW_NAME, ROW_ADDRESS, ROW_CITY, ROW_PHONE1,
            ROW_PHONE2, ROW_CUSTOMER_NB, ROW_TARIF, ROW_TABLE_HEADERS, ROW_TABLE_FIRST_ENTRY,
            ROW_TOTAL, ROW_SIGN
        };
        COL_REF = 0;
        COL_DESIGNATION = 1;
        COL_QUANTITY = 2;
        COL_UNIT_PRICE = 3;
        COL_TOTAL = 5;
        COLS = new int[] {
            COL_REF, COL_DESIGNATION, COL_QUANTITY, COL_UNIT_PRICE, COL_TOTAL
        };
    }

    private Map<Integer, Row> getRows(Sheet sheet) {
        Map<Integer, Row> map = new HashMap<>();
        for (int rowId : ROWS) {
            map.put(rowId, sheet.createRow(rowId));
        }
        return map;
    }

    public int baseColumnWidth(int colCount) {
        final int POINTS_IN_CHAR = 6;
        int emuPerChar = Units.toEMU(POINTS_IN_CHAR);
        double centimeterPerChar = emuPerChar / (float)Units.EMU_PER_CENTIMETER;
        double a4Width = 21.0;
        double charWidth = a4Width / centimeterPerChar;
        return (int)(charWidth / colCount);
    }

    @Override
    public void export(String filepath, OrderForm object) throws IOException {
        Workbook book = getWorkbook();
        Sheet sheet = book.createSheet();
        Map<Integer, Row> rows = getRows(sheet);

        // set column widths
        int baseWidth = baseColumnWidth(9) * 176;
        sheet.setColumnWidth(COL_REF, baseWidth);
        sheet.setColumnWidth(COL_DESIGNATION, baseWidth * 4);
        sheet.setColumnWidth(COL_QUANTITY, baseWidth);
        sheet.setColumnWidth(COL_UNIT_PRICE, (int)(baseWidth / 1.5));
        sheet.setColumnWidth(COL_UNIT_PRICE + 1, (int)(baseWidth / 1.5));
        sheet.setColumnWidth(COL_TOTAL, (int)(1.5 * baseWidth));

        // header
        createHeader(sheet, rows, object);

        // addresses
        sheet.addMergedRegion(new CellRangeAddress(ROW_NAME, ROW_NAME, COL_QUANTITY, COL_TOTAL));
        sheet.addMergedRegion(new CellRangeAddress(ROW_ADDRESS, ROW_ADDRESS, COL_QUANTITY, COL_TOTAL));
        sheet.addMergedRegion(new CellRangeAddress(ROW_CITY, ROW_CITY, COL_QUANTITY, COL_TOTAL));
        sheet.addMergedRegion(new CellRangeAddress(ROW_PHONE1, ROW_PHONE1, COL_QUANTITY, COL_TOTAL));
        sheet.addMergedRegion(new CellRangeAddress(ROW_PHONE2, ROW_PHONE2, COL_QUANTITY, COL_TOTAL));
        sheet.addMergedRegion(new CellRangeAddress(ROW_CUSTOMER_NB, ROW_CUSTOMER_NB, COL_QUANTITY, COL_TOTAL));
        int rowDeliveryDate = writeEntity(rows, object.getPurchaser(), "", COL_DESIGNATION);
        writeEntity(rows, object.getProvider(), object.getProvider().getCustomerNb(), COL_QUANTITY);
        sheet.addMergedRegion(new CellRangeAddress(ROW_TABLE_HEADERS, ROW_TABLE_HEADERS, COL_UNIT_PRICE,COL_UNIT_PRICE + 1));

        // Add delivery date
        Cell deliveryDateCell = rows.get(rowDeliveryDate).createCell(COL_DESIGNATION);
        if (object.hasDeliveryDate()) {
            deliveryDateCell.setCellValue("Livraison: " + object.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        rows.get(ROW_TABLE_HEADERS).createCell(COL_REF).setCellValue("REF");
        rows.get(ROW_TABLE_HEADERS).createCell(COL_DESIGNATION).setCellValue("DESIGNATION");
        rows.get(ROW_TABLE_HEADERS).createCell(COL_QUANTITY).setCellValue("Qté");
        rows.get(ROW_TABLE_HEADERS).createCell(COL_UNIT_PRICE).setCellValue("Prix Unit.");
        rows.get(ROW_TABLE_HEADERS).createCell(COL_TOTAL).setCellValue("Total");

        // merge all unit price cells
        for (int i = 0; i < MAX_ENTRIES; ++i) {
            int rowId = ROW_TABLE_FIRST_ENTRY + i;
            sheet.addMergedRegion(new CellRangeAddress(rowId, rowId, COL_UNIT_PRICE, COL_UNIT_PRICE + 1));
        }

        for(int i = 0; i < object.getEntries().size() && i < MAX_ENTRIES; ++i) {
            OrderFormEntry entry = object.getEntries().get(i);
            int rowId = ROW_TABLE_FIRST_ENTRY + i;
            Row row = sheet.createRow(rowId);
            if (entry.getReference() != null) {
                row.createCell(COL_REF).setCellValue(entry.getReference());
            }
            row.createCell(COL_DESIGNATION).setCellValue(entry.getDesignation());
            row.createCell(COL_QUANTITY).setCellValue(entry.getQuantity());
            row.createCell(COL_UNIT_PRICE).setCellValue(entry.getUnitPrice().doubleValue());
            CellAddress quantityAddr = new CellAddress(rowId, COL_QUANTITY),
                    unitPriceAddr = new CellAddress(rowId, COL_UNIT_PRICE);
            row.createCell(COL_TOTAL).setCellFormula(quantityAddr.formatAsString() + " * " + unitPriceAddr.formatAsString());
        }

        // total row
        rows.get(ROW_TOTAL).createCell(COL_DESIGNATION).setCellValue("TOTAL COMMANDE");
        CellRangeAddress qtyCellsRange = new CellRangeAddress(ROW_TABLE_FIRST_ENTRY, ROW_TABLE_FIRST_ENTRY + MAX_ENTRIES - 1, COL_QUANTITY, COL_QUANTITY);
        rows.get(ROW_TOTAL).createCell(COL_QUANTITY).setCellFormula("COUNT(" + qtyCellsRange.formatAsString() + ")");
        CellRangeAddress totalCellsRange = new CellRangeAddress(ROW_TABLE_FIRST_ENTRY, ROW_TABLE_FIRST_ENTRY + MAX_ENTRIES - 1, COL_TOTAL, COL_TOTAL);
        rows.get(ROW_TOTAL).createCell(COL_TOTAL).setCellFormula("SUM(" + totalCellsRange.formatAsString() + ")");

        // sign row
        rows.get(ROW_SIGN).createCell(COL_DESIGNATION).setCellValue("D. CORVERS, l'Ordonnateur");
        rows.get(ROW_SIGN).createCell(COL_QUANTITY).setCellValue("J-N MORMONT, le Comptable");

        // style
        styleSheet(book, sheet, rows, object.getEntries().size(), rowDeliveryDate);

        // save
        saveWorkfbook(book, filepath);
    }

    private void createHeader(Sheet sheet, Map<Integer, Row> rows, OrderForm orderForm) {
        // order form number
        rows.get(ROW_DATE).createCell(COL_DESIGNATION).setCellValue("Bon de commande");
        rows.get(ROW_DATE).createCell(COL_UNIT_PRICE).setCellValue("n°" + orderForm.getNumber());
        sheet.addMergedRegion(new CellRangeAddress(ROW_DATE, ROW_DATE, COL_UNIT_PRICE + 1, COL_UNIT_PRICE + 2));
        rows.get(ROW_DATE).createCell(COL_UNIT_PRICE + 1).setCellValue("Date: " + orderForm.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void saveWorkfbook(Workbook workbook, String filepath) throws IOException {
        try(FileOutputStream file = new FileOutputStream(filepath)) {
            workbook.write(file);
            file.flush();
        }
    }

    private static Workbook getWorkbook() {
        return new HSSFWorkbook();
    }

    /**
     *
     * @param rows
     * @param purchaser
     * @param customerNb
     * @param column
     * @return Row number after the last entity line
     */
    private int writeEntity(Map<Integer, Row> rows, Entity purchaser, String customerNb, int column) {
        String name = purchaser.getName().toUpperCase();
        rows.get(ROW_NAME).createCell(column).setCellValue(name);
        Address address = purchaser.getAddress();
        String addressStr = address.getStreet() + ", " + address.getNumber();
        if (address.getBox() != null) {
            addressStr += ", " + address.getBox();
        }
        rows.get(ROW_ADDRESS).createCell(column).setCellValue(addressStr);
        rows.get(ROW_CITY).createCell(column).setCellValue(address.getCity() + " " + address.getPostCode());
        String[] phones = purchaser.getPhoneNumbers();
        int curr = ROW_PHONE1;
        if (phones.length >= 1 && phones[0].trim().length() > 0) {
            rows.get(curr++).createCell(column).setCellValue(phones[0]);
        }
        if (phones.length >= 2 && phones[1].trim().length() > 0) {
            rows.get(curr++).createCell(column).setCellValue(phones[1]);
        }
        if (!customerNb.isEmpty()) {
            rows.get(curr++).createCell(column).setCellValue("Client: " + customerNb);
        }
        return curr;
    }

    private Font copyFont(Workbook book, Font toCopy) {
        Font font = book.createFont();
        font.setColor(toCopy.getColor());
        font.setCharSet(toCopy.getCharSet());
        font.setBold(toCopy.getBold());
        font.setFontHeight(toCopy.getFontHeight());
        font.setFontName(toCopy.getFontName());
        font.setItalic(toCopy.getItalic());
        font.setStrikeout(toCopy.getStrikeout());
        font.setTypeOffset(toCopy.getTypeOffset());
        font.setUnderline(toCopy.getUnderline());
        return font;
    }

    private void setBorders(CellStyle style, BorderStyle borderStyle) {
        style.setBorderBottom(borderStyle);
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
        style.setBorderTop(borderStyle);
    }

    private void styleSheet(Workbook workbook, Sheet sheet, Map<Integer, Row> rows, int nEntries, int rowDeliveryDate) {
        Font defaultFont = workbook.getFontAt(rows.get(ROW_TABLE_HEADERS).getCell(COL_REF).getCellStyle().getFontIndex());
        defaultFont.setFontHeightInPoints((short)12);
        Font boldFont = copyFont(workbook, defaultFont);
        boldFont.setBold(true);
        Font redBoldFont = copyFont(workbook, boldFont);
        redBoldFont.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        Font smallFont = copyFont(workbook, defaultFont);
        smallFont.setFontHeightInPoints((short)9);

        // create some styles
        CellStyle mediumBorderCenteredCellStyle = workbook.createCellStyle();
        mediumBorderCenteredCellStyle.setAlignment(HorizontalAlignment.CENTER);
        mediumBorderCenteredCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(mediumBorderCenteredCellStyle, BorderStyle.MEDIUM);

        CellStyle thinBorderCellStyle = workbook.createCellStyle();
        thinBorderCellStyle.setFont(smallFont);
        setBorders(thinBorderCellStyle, BorderStyle.THIN);

        CellStyle thinCenteredCellStyle = workbook.createCellStyle();
        thinCenteredCellStyle.setFont(smallFont);
        setBorders(thinCenteredCellStyle, BorderStyle.THIN);

        CellStyle tableHeaderCellStyle = workbook.createCellStyle();
        tableHeaderCellStyle.setAlignment(HorizontalAlignment.CENTER);
        tableHeaderCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(tableHeaderCellStyle, BorderStyle.MEDIUM);
        tableHeaderCellStyle.setFont(boldFont);

        short euroFormatIdx = createEuroFormat(workbook);
        CellStyle moneyMediumCellStyle = workbook.createCellStyle();
        moneyMediumCellStyle.setDataFormat(euroFormatIdx);
        setBorders(moneyMediumCellStyle, BorderStyle.MEDIUM);

        CellStyle moneyThinCellStyle = workbook.createCellStyle();
        moneyThinCellStyle.setDataFormat(euroFormatIdx);
        moneyThinCellStyle.setFont(smallFont);
        setBorders(moneyThinCellStyle, BorderStyle.THIN);

        CellStyle boldCellStyle = workbook.createCellStyle();
        boldCellStyle.setFont(boldFont);

        CellStyle moneyThinCenteredCellStyle = workbook.createCellStyle();
        moneyThinCenteredCellStyle.setDataFormat(euroFormatIdx);
        moneyThinCenteredCellStyle.setAlignment(HorizontalAlignment.CENTER);
        moneyThinCenteredCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        moneyThinCenteredCellStyle.setFont(smallFont);
        setBorders(moneyThinCenteredCellStyle, BorderStyle.THIN);

        CellStyle intThinCenteredCellStyle = workbook.createCellStyle();
        intThinCenteredCellStyle.setAlignment(HorizontalAlignment.CENTER);
        intThinCenteredCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        intThinCenteredCellStyle.setFont(smallFont);
        setBorders(intThinCenteredCellStyle, BorderStyle.THIN);

        CellStyle centeredThinCellStyle = workbook.createCellStyle();
        centeredThinCellStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredThinCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredThinCellStyle.setFont(smallFont);
        setBorders(centeredThinCellStyle, BorderStyle.THIN);

        CellStyle redBoldCellStyle = workbook.createCellStyle();
        redBoldCellStyle.setFont(redBoldFont);

        // apply styles
        rows.get(ROW_DATE).getCell(COL_DESIGNATION).setCellStyle(boldCellStyle);
        rows.get(ROW_DATE).getCell(COL_UNIT_PRICE).setCellStyle(boldCellStyle);
        rows.get(ROW_DATE).getCell(COL_UNIT_PRICE + 1).setCellStyle(boldCellStyle);
        rows.get(rowDeliveryDate).getCell(COL_DESIGNATION).setCellStyle(redBoldCellStyle);
        rows.get(ROW_NAME).getCell(COL_DESIGNATION).setCellStyle(boldCellStyle);
        rows.get(ROW_NAME).getCell(COL_QUANTITY).setCellStyle(boldCellStyle);

        Row headersRow = rows.get(ROW_TABLE_HEADERS);
        headersRow.setHeightInPoints(2 * headersRow.getHeightInPoints());
        for (int col : COLS) {
            headersRow.getCell(col).setCellStyle(tableHeaderCellStyle);
        }
        headersRow.createCell(COL_UNIT_PRICE + 1).setCellStyle(tableHeaderCellStyle);

        // set
        for (int rowId = ROW_TABLE_FIRST_ENTRY; rowId < ROW_TABLE_FIRST_ENTRY + MAX_ENTRIES; ++rowId) {
            Row row = sheet.getRow(rowId) == null ? sheet.createRow(rowId) : sheet.getRow(rowId);
            for (int col : COLS) {
                Cell cell = row.getCell(col) != null ? row.getCell(col) : row.createCell(col);
                if (col == COL_UNIT_PRICE) {
                    cell.setCellStyle(moneyThinCenteredCellStyle);
                } else if (col == COL_TOTAL) {
                    cell.setCellStyle(moneyThinCellStyle);
                } else if (col == COL_QUANTITY) {
                    cell.setCellStyle(intThinCenteredCellStyle);
                } else if (col == COL_REF) {
                    cell.setCellStyle(centeredThinCellStyle);
                } else {
                    cell.setCellStyle(thinBorderCellStyle);
                }
            }
            row.createCell(COL_UNIT_PRICE + 1).setCellStyle(thinBorderCellStyle);
        }

        rows.get(ROW_TOTAL).getCell(COL_QUANTITY).setCellStyle(mediumBorderCenteredCellStyle);
        rows.get(ROW_TOTAL).getCell(COL_TOTAL).setCellStyle(moneyMediumCellStyle);
    }

    private static short createEuroFormat(Workbook book) {
        return book.createDataFormat().getFormat(EURO_CURRENCY_FORMAT);
    }

    public static void main(String[] args) throws IOException {
        ArrayList<OrderFormEntry> entries = new ArrayList<>();
        entries.add(new OrderFormEntry("ref1", "desig1", 3, new BigDecimal(25.35)));
        entries.add(new OrderFormEntry("ref2", "desig2", 2, new BigDecimal(5.10)));
        Entity entity = new Entity("NAME", new Address("street", "number", "box", "postcode", "city"), new String[] {"04/225"}, "25");
        new OrderFormXlsExporter().export("a.xls", new OrderForm(25, entity, entity, LocalDate.now(), entries));
    }

}
