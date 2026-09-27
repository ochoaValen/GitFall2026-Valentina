import java.io.*;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Paths;

import java.util.*;

public class Main {

    /**

     * Input: User enters the name of a deck text file.

     * Output: Creates a PDF report containing the deck analysis.

     * Overview: Reads the deck file, validates each card, calculates

     * the total energy cost, creates a histogram, and generates a PDF.

     */

   private static final Set<String> VALID_CARDS =
        new HashSet<>(Arrays.asList(
                "Strike",
                "Defend",
                "Bash",
                "Clothesline",
                "Iron Wave",
                "Pommel Strike",
                "Shrug It Off",
                "All For One",
                "Genetic Algorithm",
                "Biased Cognition"
        ));


    public static void main(String[] args) {

        Scanner keyboard = new Scanner(System.in);

        System.out.print("Enter deck file name: ");

        String fileName = keyboard.nextLine();

        int deckId = generateDeckId();

        int totalCost = 0;

        int totalCards = 0;

        int invalidCards = 0;

        // Index represents card costs 0 through 6

        int[] histogram = new int[7];

        ArrayList<String> invalidCardList = new ArrayList<>();

        try {

            Scanner fileScanner = new Scanner(new File(fileName));

            while (fileScanner.hasNextLine()) {

                String line = fileScanner.nextLine();

                totalCards++;

                String[] parts = line.split(":");

                // Invalid format

                if (parts.length != 2) {

                    invalidCards++;

                    invalidCardList.add(line);

                    continue;

                }

                String cardName = parts[0].trim();

                String costText = parts[1].trim();

                // Invalid empty card name

                if (cardName.isEmpty()) {

                    invalidCards++;

                    invalidCardList.add(line);

                    continue;

                }
               if (!VALID_CARDS.contains(cardName.toLowerCase())) {

                     invalidCards++;

                     invalidCardList.add(line);

                     continue;

                }

                try {

                    int cost = Integer.parseInt(costText);

                    // Valid costs are 0 through 6

                    if (cost < 0 || cost > 6) {

                        invalidCards++;

                        invalidCardList.add(line);

                        continue;

                    }

                    totalCost += cost;

                    histogram[cost]++;

                } catch (NumberFormatException e) {

                    invalidCards++;

                    invalidCardList.add(line);

                }

            }

            fileScanner.close();

            boolean voidReport =

                    invalidCards > 10 ||

                    totalCards > 1000;

            String reportName;

            if (voidReport) {

                reportName =

                        "SpireDeck " +

                        deckId +

                        "(VOID).pdf";

            } else {

                reportName =

                        "SpireDeck " +

                        deckId +

                        ".pdf";

            }

            System.out.println();

            System.out.println("Deck ID: " + deckId);

            if (voidReport) {

                System.out.println("VOID");

            } else {

                System.out.println(

                        "Total Cost: " +

                        totalCost +

                        " energy"

                );

                System.out.println(

                        "Invalid Cards: " +

                        invalidCards

                );

                System.out.println();

                System.out.println("Energy Cost Histogram");

                for (int i = 0; i < histogram.length; i++) {

                    System.out.println(

                            i +

                            " energy: " +

                            histogram[i] +

                            " card(s)"

                    );

                }

                if (!invalidCardList.isEmpty()) {

                    System.out.println();

                    System.out.println("Invalid Cards:");

                    for (String card : invalidCardList) {

                        System.out.println(card);

                    }

                }

            }

            createPdfReport(

                    reportName,

                    deckId,

                    totalCost,

                    histogram,

                    invalidCardList,

                    voidReport

            );

            System.out.println();

            System.out.println(

                    "Report created: " +

                    reportName

            );

        } catch (FileNotFoundException e) {

            System.out.println(

                    "Error: File not found."

            );

        } catch (IOException e) {

            System.out.println(

                    "Error creating PDF report."

            );

        }

        keyboard.close();

    }

    /**

     * Input: None.

     * Output: Returns a random 9-digit integer.

     * Overview: Generates a deck identification number

     * between 100000000 and 999999999.

     */

    public static int generateDeckId() {

        Random random = new Random();

        return 100000000 +

                random.nextInt(900000000);

    }

    /**

     * Input: Report filename, deck ID, total cost,

     * histogram array, invalid-card list, and void status.

     * Output: Creates a PDF report file.

     * Overview: Builds the lines that will appear in the

     * report and sends them to the PDF writer.

     */

    public static void createPdfReport(

            String fileName,

            int deckId,

            int totalCost,

            int[] histogram,

            ArrayList<String> invalidCards,

            boolean voidReport

    ) throws IOException {

        ArrayList<String> lines = new ArrayList<>();

        lines.add("Slay the Spire Deck Report");

        lines.add("");

        if (voidReport) {

            lines.add("VOID");

        } else {

            lines.add("Deck ID: " + deckId);

            lines.add(

                    "Total Cost: " +

                    totalCost +

                    " energy"

            );

            lines.add("");

            lines.add("Energy Cost Histogram");

            for (int i = 0; i < histogram.length; i++) {

                lines.add(

                        i +

                        " energy: " +

                        histogram[i] +

                        " card(s) " +

                        makeBar(histogram[i])

                );

            }

            lines.add("");

            lines.add(

                    "Invalid Cards: " +

                    invalidCards.size()

            );

            if (!invalidCards.isEmpty()) {

                for (String card : invalidCards) {

                    if (card.trim().isEmpty()) {

                        lines.add("[blank line]");

                    } else {

                        lines.add(card);

                    }

                }

            }

        }

        writeSimplePdf(fileName, lines);

    }

    /**

     * Input: Number of cards for one energy value.

     * Output: Returns a text bar made from # characters.

     * Overview: Creates a simple visual histogram bar.

     */

    public static String makeBar(int amount) {

        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < amount; i++) {

            bar.append("#");

        }

        return bar.toString();

    }

    /**

     * Input: PDF filename and lines of text to display.

     * Output: Writes a valid PDF file to the computer.

     * Overview: Creates the PDF structure, writes the

     * report text, and saves the finished PDF.

     */

    public static void writeSimplePdf(

            String fileName,

            ArrayList<String> lines

    ) throws IOException {

        ByteArrayOutputStream output =

                new ByteArrayOutputStream();

        int[] offsets = new int[6];

        writeAscii(output, "%PDF-1.4\n");

        // Object 1 - Catalog

        offsets[1] = output.size();

        writeAscii(

                output,

                "1 0 obj\n" +

                "<< /Type /Catalog /Pages 2 0 R >>\n" +

                "endobj\n"

        );

        // Object 2 - Pages

        offsets[2] = output.size();

        writeAscii(

                output,

                "2 0 obj\n" +

                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +

                "endobj\n"

        );

        // Object 3 - Page

        offsets[3] = output.size();

        writeAscii(

                output,

                "3 0 obj\n" +

                "<< /Type /Page " +

                "/Parent 2 0 R " +

                "/MediaBox [0 0 612 792] " +

                "/Resources << /Font " +

                "<< /F1 4 0 R >> >> " +

                "/Contents 5 0 R >>\n" +

                "endobj\n"

        );

        // Object 4 - Font

        offsets[4] = output.size();

        writeAscii(

                output,

                "4 0 obj\n" +

                "<< /Type /Font " +

                "/Subtype /Type1 " +

                "/BaseFont /Helvetica >>\n" +

                "endobj\n"

        );

        // Build report text

        StringBuilder content = new StringBuilder();

        content.append("BT\n");

        content.append("/F1 12 Tf\n");

        content.append("50 740 Td\n");

        content.append("16 TL\n");

        for (String line : lines) {

            content.append("(");

            content.append(escapePdfText(line));

            content.append(") Tj\n");

            content.append("T*\n");

        }

        content.append("ET\n");

        byte[] contentBytes =

                content.toString().getBytes(

                        StandardCharsets.US_ASCII

                );

        // Object 5 - Page contents

        offsets[5] = output.size();

        writeAscii(

                output,

                "5 0 obj\n" +

                "<< /Length " +

                contentBytes.length +

                " >>\n" +

                "stream\n"

        );

        output.write(contentBytes);

        writeAscii(

                output,

                "endstream\n" +

                "endobj\n"

        );

        // Cross-reference table

        int xrefOffset = output.size();

        writeAscii(

                output,

                "xref\n" +

                "0 6\n"

        );

        writeAscii(

                output,

                "0000000000 65535 f \n"

        );

        for (int i = 1; i <= 5; i++) {

            writeAscii(

                    output,

                    String.format(

                            "%010d 00000 n \n",

                            offsets[i]

                    )

            );

        }

        // PDF trailer

        writeAscii(

                output,

                "trailer\n" +

                "<< /Size 6 /Root 1 0 R >>\n" +

                "startxref\n" +

                xrefOffset +

                "\n%%EOF"

        );

        Files.write(

                Paths.get(fileName),

                output.toByteArray()

        );

    }

    /**

     * Input: PDF text.

     * Output: Returns text safe for use inside a PDF.

     * Overview: Escapes characters that have special

     * meaning inside PDF text strings.

     */

    public static String escapePdfText(String text) {

        return text

                .replace("\\", "\\\\")

                .replace("(", "\\(")

                .replace(")", "\\)");

    }

    /**

     * Input: Output stream and ASCII text.

     * Output: Writes the text into the PDF byte stream.

     * Overview: Converts text to ASCII bytes and writes

     * those bytes to the PDF file.

     */

    public static void writeAscii(

            ByteArrayOutputStream output,

            String text

    ) throws IOException {

        output.write(

                text.getBytes(

                        StandardCharsets.US_ASCII

                )

        );

    }

}
