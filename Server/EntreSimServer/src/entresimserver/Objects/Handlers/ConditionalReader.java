/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Handlers;

import entreObj.Decision;
import entresimserver.Objects.Data.ConditionalData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dylanleong
 */
public class ConditionalReader {

    ArrayList<ConditionalData> conditional_data;
    TreeSet<Decision> decisions;

    public ConditionalReader(ArrayList<ConditionalData> cd, TreeSet<Decision> d) {
        conditional_data = cd;
        decisions = d;
    }

    public void readConditionals(String path, String filename) {
        Path file = Paths.get(path + filename);
        try (InputStream in = Files.newInputStream(file)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line = "";
            int expect = 0; //0=ID, 1=Name, 2=Applicant type, 3=Applicant Change, 4=Recipient type, 5=Recipient change, 6=Both type, 7=Both change

            int id = 0;
            String name = "";
            String applicant_type = "";
            String recipient_type = "";
            String both_type = "";

            int[] applicant_indexes = new int[1];
            int[] recipient_indexes = new int[1];
            int[] both_indexes = new int[1];

            ArrayList<String> applicant_changes = new ArrayList<>();
            ArrayList<String> recipient_changes = new ArrayList<>();
            ArrayList<String> both_changes = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (line.trim().equals("")) {
                    continue; //empty line, ignore
                } else if (line.charAt(0) == '/') {
                    continue; //comment line, ignore
                }

                switch (expect) {
                    case 0: //ID
                        applicant_changes = new ArrayList<>();
                        recipient_changes = new ArrayList<>();
                        both_changes = new ArrayList<>(); //reset arraylists

                        System.out.println("id line is: " + line);
                        id = Integer.parseInt(line);
                        System.out.println("id parsed is: " + id);
                        expect = 1;
                        break;
                    case 1: //Name
                        name = line;
                        expect = 2;
                        break;
                    case 2: //Applicant Type
                        if (line.charAt(0) == 'R') { //if replace
                            StringTokenizer st = new StringTokenizer(line);
                            applicant_indexes = new int[st.countTokens() - 1];
                            applicant_type = st.nextToken();
                            for (int i = 0; i < applicant_indexes.length; i++) {
                                applicant_indexes[i] = Integer.parseInt(st.nextToken());
                            }
                        } else {
                            applicant_type = line;
                        }

                        expect = 3;
                        break;
                    case 3: //Applicant Changes
                        readChanges(line, applicant_changes, applicant_indexes, applicant_type);

                        Decision check_type = null;
                        if ((check_type = getDecision(id)) != null) {
                            System.out.println("Decision name: " + check_type.title);
                            if (check_type.type.equals("Indiv")) { //don't go further
                                expect = 0;
                                recipient_type = "N";
                                both_type = "N";
                                conditional_data.add(new ConditionalData(id, name, applicant_type, recipient_type, both_type, applicant_changes, recipient_changes, both_changes));
                            } else { //joint decision, go further
                                expect = 4;
                            }
                        } else { //couldn't find decision, change this to throw an error
                            System.out.println("HIT THE NULL CASE");
                            expect = 0;
                        }
                        break;
                    case 4: //Recipient Type
                        if (line.charAt(0) == 'R') { //if replace
                            StringTokenizer st = new StringTokenizer(line);
                            recipient_indexes = new int[st.countTokens() - 1];
                            recipient_type = st.nextToken();
                            for (int i = 0; i < recipient_indexes.length; i++) {
                                recipient_indexes[i] = Integer.parseInt(st.nextToken());
                            }
                        } else {
                            recipient_type = line;
                        }

                        expect = 5;
                        break;
                    case 5: //Recipient Changes
                        readChanges(line, recipient_changes, recipient_indexes, recipient_type);

                        expect = 6;
                        break;
                    case 6: //Both Type
                        if (line.charAt(0) == 'R') { //if replace
                            StringTokenizer st = new StringTokenizer(line);
                            both_indexes = new int[st.countTokens() - 1];
                            both_type = st.nextToken();
                            for (int i = 0; i < both_indexes.length; i++) {
                                both_indexes[i] = Integer.parseInt(st.nextToken());
                            }
                        } else {
                            both_type = line;
                        }

                        expect = 7;
                        break;
                    case 7: //Both Changes
                        readChanges(line, both_changes, both_indexes, both_type);
                        System.out.println(both_changes);
                        
                        expect = 0;
                        conditional_data.add(new ConditionalData(id, name, applicant_type, recipient_type, both_type, applicant_changes, recipient_changes, both_changes));
                        break;
                    default:
                        break;
                }

            }//endwhile

        } catch (IOException ex) {
            Logger.getLogger(ConditionalReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readChanges(String line, ArrayList<String> changes, int[] indexes, String type) {
        StringTokenizer st = new StringTokenizer(line);

        int count = 0;
        while (st.hasMoreTokens()) { //add effects to changes list
            String token = st.nextToken();

            if (token.charAt(0) == 'P') { //Check for probability case
                StringBuilder total_effect = new StringBuilder("P ");
                String probability = "";
                String effect_list = "";

                int read = 0; //0 = probability, 1 = effects, 2 = found a closing bracket ) and finished reading effects

                for (int i = 2; i < token.length(); i++) {
                    char c = token.charAt(i);
                    if (read == 0) {
                        if (c == '(') { //come accross opening bracket while reading probability
                            read = 1;
                        } else {
                            probability += c;
                        }
                    } else if (read == 1) {
                        if (c == ')') { //
                            read = 2;
                        } else {
                            effect_list += c;
                        }
                    }
                }

                if (read != 2) { //still have effects to read
                    while (true) {
                        token = st.nextToken();
                        effect_list += " ";
                        for (int i = 0; i < token.length(); i++) {
                            char c = token.charAt(i);
                            if (c != ')') {
                                effect_list += c;
                            }
                        }
                        if (token.contains(")")) {
                            break;
                        }
                    }
                }

                total_effect.append(probability);

                StringTokenizer effect_tokenizer = new StringTokenizer(effect_list);
                while (effect_tokenizer.hasMoreTokens()) {
                    total_effect.append(" " + extractEffect(effect_tokenizer.nextToken()));
                }

                if (type.equals("R")) {
                    changes.add(indexes[count] + " " + total_effect.toString());
                } else {
                    changes.add(total_effect.toString());
                }

            } //End Probability Case
            else {  //read normally
                if (token.equals("nothing")) { //nothing case
                    changes.add(token);
                } else {
                    String adding = extractEffect(token);
                    if (type.equals("R")) {
                        changes.add(indexes[count] + " " + adding);
                    } else {
                        changes.add(adding);
                    }
                }
            }
            
            count++;
        } //endwhile
    }

    private String extractEffect(String token) {
        String effect = "";
        String magnitude = "";
        String delay = "";
        boolean linked = false; //indicated by 2 equals signs

        int read = 0; //0=effect, 1=magnitude, 2=delay

        for (int i = 0; i < token.length(); i++) { //reading through the format EFFECT=(=)MAG:DELAY (double equals if linked)
            char c = token.charAt(i);
            if (read == 0) {
                if (c == '=') {
                    read = 1;
                } else {
                    effect += c;
                }
            } else if (read == 1) {
                if (c == '=') {
                    linked = true; //double equals case, where effect is linked
                } else if (c == ':') {
                    read = 2;
                } else {
                    magnitude += c;
                }
            } else if (read == 2) {
                delay += c;
            }
        } //endfor

        return (effect + " " + magnitude + " " + delay + " " + linked);
    }

    private Decision getDecision(int id) {
        for (Decision each_decision : decisions) {
            if (each_decision.id == id) {
                return each_decision;
            }
        }
        return null;
    }
}
