import javalibs.FileUtils
import javalibs.TSL
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader

fun main(args: Array<String>) {
    val log = TSL.get()

    val csv = "csv-exercises/CS380W9.csv"
    val csvFile = File(csv)
    val parseFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader()
    val records: ArrayList<CSVRecord> = ArrayList()
    val countingMap: HashMap<String, Int> = HashMap()

    // Get a list of all CSV exercise file paths
    val allCSVFilesPaths = FileUtils.get().getAllFilePathsInDirWithPrefix("CS380", "csv-exercises")
    val csvFileObjects: ArrayList<File> = ArrayList()

    // Convert all file paths into File objects
    for(file in allCSVFilesPaths)
        csvFileObjects.add(File(file))

    // Parse each file, get the drexel ID from column "ID" and add it to the
    // hashmap. Each time an ID appears the counter will be incremented for
    // that student's ID

    for(exercise in csvFileObjects) {
        //log.info("Reading: ${exercise.absoluteFile}")
        try{
            val csvParser = CSVParser.parse(FileReader(exercise), parseFormat)
            for(record in csvParser) {
                val studentID = record.get("ID")
                // See if the student ID already exists, if so, increment the
                // counter
                if(countingMap.containsKey(studentID)) {
                    val existingCount = countingMap[studentID]
                    val newCount = existingCount!! + 1
                    countingMap[studentID] = newCount
                }
                else {
                    // Key doesn't exist yet, create it and init to value of 1
                    countingMap[studentID] = 1
                }
            }
        }
        catch(e: Exception) {
            log.die("Exception parsing CSV file")
        }
    }

    countingMap.forEach {entry ->
        println("${entry.key},${entry.value}")
    }



    log.shutDown()

}