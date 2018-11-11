/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Handlers;

import entreObj.Decision;
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
public class DataReader {

    TreeSet<Decision> decisions;

    public DataReader(TreeSet<Decision> d) {
        decisions = d;
    }

    public void readData(String path, String filename) {
        Path file = Paths.get(path + filename);
        try (InputStream in = Files.newInputStream(file)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line = "";
            int expect = 0; //0=ID, 1=title, 2=description, 3=type, 4=prereqs, 5/6/7=outcomes

            int id = 0;
            String title = "";
            StringBuilder description = new StringBuilder("");
            String type = "";
            ArrayList<String> prereqs = new ArrayList<String>(3);
            ArrayList<String> applicant_effects = new ArrayList<String>(3);
            ArrayList<String> recipient_effects = new ArrayList<String>(3);
            ArrayList<String> both_effects = new ArrayList<String>(3);

            while ((line = br.readLine()) != null) {
                if (line.trim().equals("")) {
                    continue; //empty line, ignore
                } else if (line.charAt(0) == '/') {
                    continue; //comment line, ignore
                }

                //Please forgive me using a switch
                switch (expect) {
                    case 0: //ID
                        description = new StringBuilder("");
                        prereqs = new ArrayList<String>();
                        applicant_effects = new ArrayList<String>();
                        recipient_effects = new ArrayList<String>();
                        both_effects = new ArrayList<String>();
                        id = Integer.parseInt(line);
                        expect = 1;
                        break;
                    case 1: //Title
                        title = line;
                        expect = 2;
                        break;
                    case 2: //Description
                        if (readDescription(line, description)) {
                            expect = 3;
                        }
                        break;
                    case 3: //Type
                        type = line;
                        expect = 4;
                        break;
                    case 4: //Prerequisites
                        readPrereqs(line, prereqs);
                        expect = 5;
                        break;
                    case 5: //Applicant effects
                        readEffects(line, applicant_effects);

                        if (type.equals("Indiv")) {
                            expect = 0;
                            decisions.add(new Decision(id, title, description.toString(), type, prereqs, applicant_effects, recipient_effects, both_effects));
                            if (id == 14) System.out.println(applicant_effects);
                        } else {
                            expect = 6;
                        }
                        break;
                    case 6: //Recipient effects
                        readEffects(line, recipient_effects);
                        expect = 7;
                        break;
                    case 7: //Both effects
                        readEffects(line, both_effects);
                        expect = 0;
                        decisions.add(new Decision(id, title, description.toString(), type, prereqs, applicant_effects, recipient_effects, both_effects));
                        break;
                    default:
                        break;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, "Unable to read file.", ex);
        }

    }

    private boolean readDescription(String line, StringBuilder description) {
        boolean done = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && !description.toString().equals("")) {
                done = true;
            }
            description.append(c);
        }
        description.append("\n");
        return done;
    }

    private void readPrereqs(String line, ArrayList<String> prereqs) {
        StringTokenizer st = new StringTokenizer(line);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            String effect = "";
            String magnitude = "";

            boolean read_type = true;
            for (int i = 0; i < token.length(); i++) {
                char c = token.charAt(i);
                if (read_type) {
                    if (c == '=') {
                        read_type = false; //add characters to 'effect' until we find the = sign
                    } else {
                        effect += c;
                    }
                } else {
                    magnitude += c;
                }
            }//endfor
            prereqs.add(effect + " " + magnitude);
        }
    }

    private void readEffects(String line, ArrayList<String> effects) {
        StringTokenizer st = new StringTokenizer(line);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (token.equals("join") || token.equals("jointgrant")) { //Special Case Check (join or jointgrants)
                effects.add(token);
                continue;
            }

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
                        if (token.contains(")")) break;
                    } 
                }

                total_effect.append(probability);

                StringTokenizer effect_tokenizer = new StringTokenizer(effect_list);
                while (effect_tokenizer.hasMoreTokens()) {
                    total_effect.append(" " + extractEffect(effect_tokenizer.nextToken()));
                }
                
                effects.add(total_effect.toString());
            } //End Probability Case
            else {
                //read normally
                String adding = extractEffect(token);
                effects.add(adding);
            }

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
}
