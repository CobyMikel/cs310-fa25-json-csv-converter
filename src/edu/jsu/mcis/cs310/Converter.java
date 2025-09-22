package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
public class Converter {
    
    /*
        
        Consider the following CSV data, a portion of a database of episodes of
        the classic "Star Trek" television series:
        
        "ProdNum","Title","Season","Episode","Stardate","OriginalAirdate","RemasteredAirdate"
        "6149-02","Where No Man Has Gone Before","1","01","1312.4 - 1313.8","9/22/1966","1/20/2007"
        "6149-03","The Corbomite Maneuver","1","02","1512.2 - 1514.1","11/10/1966","12/9/2006"
        
        (For brevity, only the header row plus the first two episodes are shown
        in this sample.)
    
        The corresponding JSON data would be similar to the following; tabs and
        other whitespace have been added for clarity.  Note the curly braces,
        square brackets, and double-quotes!  These indicate which values should
        be encoded as strings and which values should be encoded as integers, as
        well as the overall structure of the data:
        
        {
            "ProdNums": [
                "6149-02",
                "6149-03"
            ],
            "ColHeadings": [
                "ProdNum",
                "Title",
                "Season",
                "Episode",
                "Stardate",
                "OriginalAirdate",
                "RemasteredAirdate"
            ],
            "Data": [
                [
                    "Where No Man Has Gone Before",
                    1,
                    1,
                    "1312.4 - 1313.8",
                    "9/22/1966",
                    "1/20/2007"
                ],
                [
                    "The Corbomite Maneuver",
                    1,
                    2,
                    "1512.2 - 1514.1",
                    "11/10/1966",
                    "12/9/2006"
                ]
            ]
        }
        
        Your task for this program is to complete the two conversion methods in
        this class, "csvToJson()" and "jsonToCsv()", so that the CSV data shown
        above can be converted to JSON format, and vice-versa.  Both methods
        should return the converted data as strings, but the strings do not need
        to include the newlines and whitespace shown in the examples; again,
        this whitespace has been added only for clarity.
        
        NOTE: YOU SHOULD NOT WRITE ANY CODE WHICH MANUALLY COMPOSES THE OUTPUT
        STRINGS!!!  Leave ALL string conversion to the two data conversion
        libraries we have discussed, OpenCSV and json-simple.  See the "Data
        Exchange" lecture notes for more details, including examples.
        
    */
    
    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {
        
        String result = "{}"; // default return value; replace later!
        
        try {
            try (CSVReader reader = new CSVReaderBuilder(new StringReader(csvString)).build()) {
                List<String[]> rows = reader.readAll();
                if (rows.isEmpty()) {
                    JsonObject empty = new JsonObject();
                    empty.put("ProdNums", new JsonArray());
                    empty.put("ColHeadings", new JsonArray());
                    empty.put("Data", new JsonArray());
                    return Jsoner.serialize(empty);
                }
                
                String[] header = rows.get(0);
                
                JsonArray colHeadings = new JsonArray();
                for (String h : header) colHeadings.add(h);
                
                JsonArray prodNums = new JsonArray();
                JsonArray data = new JsonArray();
                
                for (int i = 1; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    if (row == null || row.length == 0) continue;
                    
                    prodNums.add(row[0]);
                    
                    JsonArray one = new JsonArray();
                    
                    one.add(row[1]);
                    one.add(Integer.parseInt(row[2].trim()));
                    one.add(Integer.parseInt(row[3].trim()));
                    one.add(row[4]);
                    one.add(row[5]);
                    one.add(row.length > 6 ? row[6] : "");
                    
                    data.add(one);
                }
                JsonObject root = new JsonObject();
                root.put("ProdNums", prodNums);
                root.put("ColHeadings", colHeadings);
                root.put("Data", data);
                
                result = Jsoner.serialize(root);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {
        
        String result = ""; // default return value; replace later!
        
        try {
            Object parsed = Jsoner.deserialize(jsonString);
            JsonObject root = (JsonObject) parsed;
            
            JsonArray colHeadings = (JsonArray) root.get("ColHeadings");
            JsonArray prodNums = (JsonArray) root.get("ProdNums");
            JsonArray data = (JsonArray) root.get("Data");
            
            StringWriter out = new StringWriter();
            
            try (CSVWriter writer = new CSVWriter(
                    out,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    "\n"
            )){
                
                String[] header = new String[colHeadings.size()];
                for (int i = 0; i < colHeadings.size(); i++) {
                    header[i] = String.valueOf(colHeadings.get(i));
                }
                writer.writeNext(header, true);
                
                for (int i = 0; i < data.size(); i++) {
                    JsonArray rowData = (JsonArray) data.get(i);
                    
                    String prodNum = String.valueOf(prodNums.get(i));
                    String title = String.valueOf(rowData.get(0));
                    int season = ((Number) rowData.get(1)).intValue();
                    int episode = ((Number) rowData.get(2)).intValue();
                    String stardate = String.valueOf(rowData.get(3));
                    String originalAirdate = String.valueOf(rowData.get(4));
                    String remasteredAirdate = String.valueOf(rowData.get(5));
                    
                    String episodeStr = String.format("%02d", episode);
                    
                    String[] csvRow = new String[] {
                        prodNum,
                        title,
                        String.valueOf(season),
                        episodeStr,
                        stardate,
                        originalAirdate,
                        remasteredAirdate
                    };
                    
                    writer.writeNext(csvRow, true);
                }
            }
            
            result = out.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
}
