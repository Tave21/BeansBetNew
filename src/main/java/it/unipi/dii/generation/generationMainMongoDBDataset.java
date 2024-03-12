package it.unipi.dii.generation;
import java.io.*;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import it.unipi.dii.dao.mongo.CustomerMongoDBDAO;
import it.unipi.dii.dao.mongo.PollMongoDBDAO;
import it.unipi.dii.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static it.unipi.dii.utility.DateTimes.*;
import static it.unipi.dii.utility.MongoUtility.*;
import static it.unipi.dii.utility.PasswordGenerator.generateRandomPassword;
import static it.unipi.dii.utility.RandomNumber.*;
import static it.unipi.dii.utility.Security.calculateSHA256;
import static it.unipi.dii.utility.WriteJsonToFile.deleteFile;
import static it.unipi.dii.utility.WriteJsonToFile.writeToJsonFile;
import static it.unipi.dii.utility.randomGeneration.*;

public class generationMainMongoDBDataset {
    public static final String[] italianProvinces = {
            "Agrigento", "Alessandria", "Ancona", "Aosta", "Arezzo", "Ascoli Piceno", "Asti", "Avellino", "Bari",
            "Barletta-Andria-Trani", "Belluno", "Benevento", "Bergamo", "Biella", "Bologna", "Bolzano", "Brescia",
            "Brindisi", "Cagliari", "Caltanissetta", "Campobasso", "Carbonia-Iglesias", "Caserta", "Catania", "Catanzaro",
            "Chieti", "Como", "Cosenza", "Cremona", "Crotone", "Cuneo", "Enna", "Fermo", "Ferrara", "Firenze", "Foggia",
            "Forlì-Cesena", "Frosinone", "Genova", "Gorizia", "Grosseto", "Imperia", "Isernia", "La Spezia", "L'Aquila",
            "Latina", "Lecce", "Lecco", "Livorno", "Lodi", "Lucca", "Macerata", "Mantova", "Massa-Carrara", "Matera",
            "Messina", "Milano", "Modena", "Monza e della Brianza", "Napoli", "Novara", "Nuoro", "Ogliastra", "Olbia-Tempio",
            "Oristano", "Padova", "Palermo", "Parma", "Pavia", "Perugia", "Pesaro e Urbino", "Pescara", "Piacenza",
            "Pisa", "Pistoia", "Pordenone", "Potenza", "Prato", "Ragusa", "Ravenna", "Reggio Calabria", "Reggio Emilia",
            "Rieti", "Rimini", "Roma", "Rovigo", "Salerno", "Medio Campidano", "Sassari", "Savona", "Siena", "Siracusa",
            "Sondrio", "Taranto", "Teramo", "Terni", "Torino", "Ogliastra", "Trapani", "Trento", "Treviso", "Trieste",
            "Udine", "Varese", "Venezia", "Verbano-Cusio-Ossola", "Vercelli", "Verona", "Vibo Valentia", "Vicenza",
            "Viterbo"
    };

    public static final String[] livingQuarters = {
            "Via", "Piazza", "Via appendice", "Locale", "Rione" , "Monte"
    };

    public static final String[] eMails = {
            "gmail.com", "tin.it", "hotmail.com", "libero.it", "mail.com", "alice.it", "studenti.unipi.it"
    };

    public static void main(String[] args) throws IOException {
        deactivateMongoDBNotifications();

        CustomerMongoDBDAO cl = new CustomerMongoDBDAO();
        cl.openConnection();

        // Drop the collections.
        dropCollection(cl.mongoDB, "customers");
        dropCollection(cl.mongoDB, "slips");
        dropCollection(cl.mongoDB, "matches");
        dropCollection(cl.mongoDB, "polls");

        // Create again the collection
        createCollection(cl.mongoDB, "customers");
        createCollection(cl.mongoDB, "slips");
        createCollection(cl.mongoDB, "matches");
        createCollection(cl.mongoDB, "polls");

        JSONParser jsonParser = new JSONParser();

        List<Customer> users = new ArrayList<>();
        List<Match> matches = new ArrayList<>();
        System.out.println("Current Path: " + System.getProperty("user.dir"));

        System.out.println("Start the Customers generation.");

        try (FileReader reader = new FileReader("src/main/java/it/unipi/dii/generation/NomiCognomi.json") /*FileReader reader = new FileReader("NomiCognomi.json")*/) {

            Object obj = jsonParser.parse(reader); // Read the JSON file.
            JSONObject json_obj = (JSONObject) obj;
            JSONArray js_arr = (JSONArray) json_obj.get("people");
            final int number = Math.toIntExact(js_arr.size());
            String name , gender , surname;

            for (int i = 0; i < number; i++) {
                if (i == (int) (number * 0.25)) {
                    System.out.println("25%");
                }else if (i == (int) (number * 0.50)) {
                    System.out.println("50%");
                }else if (i == (int) (number * 0.75)) {
                    System.out.println("75%");
                }
                JSONObject js_i = (JSONObject) js_arr.get(i);
                name = (js_i.get("name")).toString();
                gender = (js_i.get("gender")).toString();

                for (int j = 0; j < number; j++) {
                    surname = js_i.get("surname").toString();
                    Customer us = new Customer();
                    us.setName(name);
                    us.setSurname(surname);
                    us.setGender(gender);
                    us.setCredit(generateSumOfMoney(2000)); // Generate the credit of the user with an exponential distribution.

                    us.setEmail(name + "." + surname + "." + i + j + "@" + eMails[(int) generateRandomNaturalNumber(0, eMails.length - 1)]);
                    us.setCellNumber(generatePhoneNumber("+39")); // Generate the phone number.
                    us.setPassword(calculateSHA256(generateRandomPassword(30))); // Compute the hash of the password.
                    us.setUsername(name + "_" + surname + "_" + i + j); // Generate the username.

                    LocalDate localDateTime = LocalDate.parse("1940-01-01");
                    localDateTime = localDateTime.plusYears(generateRandomNaturalNumber(1, 72));
                    localDateTime = localDateTime.plusMonths(generateRandomNaturalNumber(0, 12));
                    localDateTime = localDateTime.plusDays(generateRandomNaturalNumber(0, 365));
                    us.setBirthDate(localDateTime.toString());

                    Instant date = Instant.parse("2012-01-01T00:00:00Z").
                            plus(generateRandomNaturalNumber(0, 365 * 11), ChronoUnit.DAYS).
                            plus(generateRandomNaturalNumber(0, 23), ChronoUnit.HOURS).
                            plus(generateRandomNaturalNumber(0, 59), ChronoUnit.MINUTES).
                            plus(generateRandomNaturalNumber(0, 59), ChronoUnit.SECONDS);
                    us.setRegistrationDate(date.toString());

                    us.setAddress(
                            livingQuarters[(int) generateRandomNaturalNumber(0, livingQuarters.length - 1)] + " " + // type of living quarter.
                                    italianProvinces[(int) generateRandomNaturalNumber(0, italianProvinces.length - 1)] + " " + // name of the living quarter.
                                    ((int) generateRandomNaturalNumber(1, italianProvinces.length * 3)) // civic number.
                    );
                    long index = generateRandomNaturalNumber(0, italianProvinces.length - 1);
                    us.setCityOfResidence(italianProvinces[(int) index]);
                    us.setProvince(italianProvinces[(int) index]);
                    users.add(us);
                }
            }

            writeToJsonFile(users , "src/main/java/it/unipi/dii/generation/customers.json");

            System.out.println("Customers generation ended. [ " + users.size() + " ]");

        } catch (IOException | ParseException e) {
            System.out.println("Error in the customers generation.");
            return;
        }

        String csvFilePath = "src/main/java/it/unipi/dii/generation/Matches.csv";
        String jsonFilePath = "src/main/java/it/unipi/dii/generation/MatchesJSON.json";

        try {
            System.out.println("Start the Matches generation.");
            // Read CSV file.
            CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
            String[] headers = csvReader.readNext();
            // Create Jackson ObjectMapper.
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            // Create JSON array to store records.
            ArrayNode recordsArray = objectMapper.createArrayNode();

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                // Create JSON object for each record.
                ObjectNode recordNode = objectMapper.createObjectNode();
                // Populate JSON object with data from CSV.
                for (int i = 0; i < headers.length; i++) {
                    recordNode.put(headers[i], nextRecord[i]);
                }
                // Add the record to the array.
                recordsArray.add(recordNode);
            }

            // Create JSON object to hold the array of records.
            ObjectNode jsonRoot = objectMapper.createObjectNode();
            jsonRoot.set("records", recordsArray);

            // Write JSON to file.
            FileWriter jsonWriter = new FileWriter(jsonFilePath);
            objectMapper.writeValue(jsonWriter, jsonRoot);

            System.out.println("Conversion of 'matchesPage.csv' successfully executed, the JSON file '" + jsonFilePath + "'has been created.");

            // Close readers and writers.
            csvReader.close();
            jsonWriter.close();

        } catch (IOException | CsvValidationException e) {
            System.out.println("Error in the matches generation.");
            return;
        }

        jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("src/main/java/it/unipi/dii/generation/MatchesJSON.json")) {
            Object obj = jsonParser.parse(reader); // Read JSON file
            JSONObject json_obj = (JSONObject) obj;
            JSONArray js_arr = (JSONArray) json_obj.get("records");

            int number = Math.toIntExact(js_arr.size());

            String clubName;

            for (int i = 0; i < number; i++) {
                JSONObject js_i = (JSONObject) js_arr.get(i);
                String competition_id = js_i.get("competition_id").toString();

                if (
                        Objects.equals(competition_id, "IT1") ||
                                Objects.equals(competition_id, "GB1") ||
                                Objects.equals(competition_id, "ES1") ||
                                Objects.equals(competition_id, "FR1") ||
                                Objects.equals(competition_id, "L1")
                ) {
                    Match mat = new Match();

                    mat.setMatchID(i);
                    mat.setCompetition_id(competition_id);

                    clubName = (js_i.get("home_club_name")).toString();
                    if (Objects.equals(clubName, "null") || clubName == null) {
                        clubName = "Tavernone FC - "+competition_id;
                    }

                    mat.setTeam_home(clubName);

                    clubName = (js_i.get("away_club_name")).toString();
                    if (Objects.equals(clubName, "null") || clubName == null) {
                        clubName = "Scranagiani FC - "+competition_id;
                    }

                    mat.setTeam_away(clubName);
                    mat.setMatchDate(dateToTimestamp(js_i.get("date").toString(), (int) generateRandomNaturalNumber(15 , 21), 0, 0));
                    mat.setHome_goals(Integer.valueOf(js_i.get("home_club_goals").toString()));
                    mat.setAway_goals(Integer.valueOf(js_i.get("away_club_goals").toString()));
                    mat.setStatus("FINISHED");

                    mat.randomizeMultipliers();
                    matches.add(mat);
                }
            }

            writeToJsonFile(matches , "src/main/java/it/unipi/dii/generation/matches.json");
            deleteFile("src/main/java/it/unipi/dii/generation/MatchesJSON.json"); // Delete the MatchesJSON file.
            System.out.println("Matches generation ended. [ " + matches.size() + " ]");

        } catch (IOException | ParseException e) {
            System.out.println("Error in the matches generation.");
            return;
        }

        System.out.println("Start the Slips generation.");
        List<Slip> slips = new ArrayList<>();

        long how_many_bets, number_of_slips;
        int i, j, k, choosen_match, multIndex, slipCounter = 0;
        String rifDate, subDate;

        for (i = 0; i < users.size(); i++) {
            if (i == (int) (users.size() * 0.25)) {
                System.out.println("25%");
            }else if (i == (int) (users.size() * 0.50)) {
                System.out.println("50%");
            }else if (i == (int) (users.size() * 0.75)) {
                System.out.println("75%");
            }
            subDate = users.get(i).getRegistrationDate();
            number_of_slips = (int) generateExponentialDistributedNumber(40);

            if (number_of_slips == 0) {
                if (generateRandomDoubleNumber(0, 100) >= 10) {
                    number_of_slips = 5;
                }
            }

            for (j = 0; j < number_of_slips; j++) {
                how_many_bets = (int) generateExponentialDistributedNumber(10);
                if (how_many_bets < 1) {
                    how_many_bets = 1;
                }

                Slip sl_j = new Slip();
                sl_j.setSlipID(slipCounter);
                sl_j.setUsername(users.get(i).getUsername());

                sl_j.setBetAmount(generateSumOfMoney(15));
                choosen_match = (int) generateRandomNaturalNumber(0, matches.size() - 1);
                rifDate = matches.get(choosen_match).getMatchDate();
                while (differenceDays(subDate, rifDate) < 0) {
                    choosen_match = choosen_match + (int) ((matches.size() - 1) * 0.1);
                    if (choosen_match >= matches.size()) {
                        how_many_bets = 0;
                        break;
                    } else {
                        rifDate = matches.get(choosen_match).getMatchDate();
                    }
                }

                boolean slipWin = true;

                for (k = 0; k < how_many_bets; k++) {
                    multIndex = (int) generateRandomNaturalNumber(0, matches.get(choosen_match).getMultipliers().size() - 1);
                    Bet b = new Bet();
                    b.setMatchID(matches.get(choosen_match).getMatchID());
                    b.setChosenMultiplierValue(matches.get(choosen_match).pickMultiplierValue(multIndex));
                    b.setChosenMultiplierName(matches.get(choosen_match).pickMultiplierName(multIndex));
                    b.setWin(matches.get(choosen_match).checkMultiplierWin(b.getChosenMultiplierName()));

                    if(b.getWin() == 0){
                        slipWin = false;
                    }
                    b.setMatchDate(matches.get(choosen_match).getMatchDate());
                    b.setCompetition_id(matches.get(choosen_match).getCompetition_id());
                    b.setTeamHome(matches.get(choosen_match).getTeam_home());
                    b.setTeamAway(matches.get(choosen_match).getTeam_away());
                    choosen_match++;
                    sl_j.betsList.add(b);
                    if (choosen_match == matches.size()) {
                        break;
                    }
                }

                if(slipWin){
                    // The slip is win.
                    sl_j.computeTotal();
                    sl_j.setWin(1);
                }else{
                    // The slip has been lost.
                    sl_j.setAmount(0);
                    sl_j.setWin(0);
                }

                if (how_many_bets > 0) {
                    sl_j.setCreationDate(
                            String.valueOf(
                                    Instant.parse(rifDate)
                                            .minusSeconds(generateRandomNaturalNumber(3 * 24 * 60 * 60, 10 * 24 * 60 * 60))
                            )
                    );

                    sl_j.setConfirmationDate(
                            String.valueOf(
                                    Instant.parse(sl_j.getCreationDate())
                                            .plusSeconds(generateRandomNaturalNumber(0, 2 * 24 * 60 * 60))
                            )
                    );
                    slips.add(sl_j);
                }

                slipCounter++;
            }
        }
        PollMongoDBDAO pl = new PollMongoDBDAO();
        pl.openConnection();

        Poll p = new Poll();
        p.setPollType("Best Player");
        p.setPollName("Best players of this season");
        p.setCreationDate(getCurrentInstantString());
        p.setActivationDate(getCurrentInstant().plusSeconds(3600).toString());
        List<pollOption> options = new ArrayList<>();
        pollOption pollOpt = new pollOption("Haaland");
        pollOpt.multipleVoteOption(10);
        options.add(pollOpt);
        pollOpt = new pollOption("Verstappen");
        pollOpt.multipleVoteOption(10);
        options.add(pollOpt);
        pollOpt = new pollOption("Taverna");
        pollOpt.multipleVoteOption(10);
        options.add(pollOpt);
        p.setOptions(options);
        p.UpdateNumberOfVotes();
        pl.addPoll(p);

        p = new Poll();
        p.setPollType("Best Player");
        p.setPollName("Best players of this season");
        p.setCreationDate(Instant.parse("2023-02-01T12:00:00Z").toString());
        p.setActivationDate(Instant.parse("2023-02-01T12:00:00Z").plusSeconds(3600).toString());
        options = new ArrayList<>();
        pollOpt = new pollOption("Pianigiani");
        pollOpt.multipleVoteOption(0);
        options.add(pollOpt);
        pollOpt = new pollOption("Mosti");
        pollOpt.multipleVoteOption(10);
        options.add(pollOpt);
        pollOpt = new pollOption("Taverna");
        pollOpt.multipleVoteOption(10);
        options.add(pollOpt);
        p.setOptions(options);
        p.UpdateNumberOfVotes();
        pl.addPoll(p);

        p = new Poll();
        p.setPollType("Best Team");
        p.setPollName("Best Team of this season");
        p.setCreationDate("2024-01-20");
        p.setActivationDate("2024-01-26");
        options = new ArrayList<>();
        pollOpt = new pollOption("Udinese");
        pollOpt.multipleVoteOption(0);
        options.add(pollOpt);
        pollOpt = new pollOption("Napoli");
        pollOpt.multipleVoteOption(0);
        options.add(pollOpt);
        pollOpt = new pollOption("AC Milan");
        pollOpt.multipleVoteOption(0);
        options.add(pollOpt);
        p.setOptions(options);
        p.UpdateNumberOfVotes();
        pl.addPoll(p);

        p = new Poll();
        p.setPollType("Best Team");
        p.setPollName("Best Team of this season");
        p.setCreationDate("2024-01-20");
        p.setActivationDate("2024-01-26");
        options = new ArrayList<>();
        pollOpt = new pollOption("Inter");
        pollOpt.multipleVoteOption(3);
        options.add(pollOpt);
        pollOpt = new pollOption("AC Milan");
        pollOpt.multipleVoteOption(2);
        options.add(pollOpt);
        pollOpt = new pollOption("Juventus");
        pollOpt.multipleVoteOption(200);
        options.add(pollOpt);
        p.setOptions(options);
        p.UpdateNumberOfVotes();
        pl.addPoll(p);
        pl.closeConnection();

        System.out.println("Slips insertion start. [ " + slips.size() + " ]");

        writeToJsonFile(slips , "src/main/java/it/unipi/dii/generation/slips.json");

        System.out.println("Slips insertion ended.");
    }
}
